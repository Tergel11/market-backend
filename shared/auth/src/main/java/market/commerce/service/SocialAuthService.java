package market.commerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.dto.AuthResponse;
import market.commerce.exception.MessageException;
import market.commerce.model.customer.Customer;
import market.commerce.model.customer.SocialIdentity;
import market.commerce.model.enums.Status;
import market.commerce.repository.CustomerRepository;
import market.commerce.social.SocialTokenVerifier;
import market.commerce.social.SocialUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Google, Apple and phone OTP sign-in.
 *
 * <p>The client SDK does the whole provider flow — including sending and
 * checking the OTP SMS — and hands us the resulting Firebase token. There is no
 * server-side OTP endpoint: we verify the token and issue our own JWT, so the
 * rest of the application sees an ordinary authenticated customer.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final CustomerRepository customerRepository;
    private final AuthService authService;

    /** Empty when firebase.enabled is false, so the app still starts locally. */
    private final Optional<SocialTokenVerifier> socialTokenVerifier;

    @Transactional
    public AuthResponse login(String idToken) {
        SocialUserInfo info = socialTokenVerifier
                .orElseThrow(() -> new MessageException("auth.social-disabled"))
                .verify(idToken);

        if (info.provider() == null)
            throw new MessageException("auth.provider-unsupported");

        Customer customer = findOrCreate(info);

        if (customer.getStatus() != Status.ACTIVE)
            throw new MessageException("error.permission");

        customer.setLastLoginAt(Instant.now());
        customerRepository.save(customer);

        log.info("Social login {} for customer {}", info.provider(), customer.getId());
        return authService.issueCustomerToken(customer);
    }

    /**
     * Resolves the token to a customer, linking to an existing account where
     * Firebase has already proven ownership of the email or phone number.
     */
    private Customer findOrCreate(SocialUserInfo info) {
        // the firebase uid is the only stable key: Firebase merges google,
        // apple and phone into one uid, while emails can change
        Optional<Customer> byUid = customerRepository.findByFirebaseUid(info.subject());
        if (byUid.isPresent())
            return recordIdentity(byUid.get(), info);

        // link only on a verified email. linking on an unverified one would let
        // someone pre-register a victim's address and capture their real login
        if (info.emailVerified() && !ObjectUtils.isEmpty(info.email())) {
            Optional<Customer> byEmail = customerRepository.findByEmail(info.email());
            if (byEmail.isPresent())
                return link(byEmail.get(), info);
        }

        // reaching phone sign-in at all means Firebase verified the number
        if (!ObjectUtils.isEmpty(info.phoneNumber())) {
            Optional<Customer> byPhone = customerRepository.findByPhone(info.phoneNumber());
            if (byPhone.isPresent())
                return link(byPhone.get(), info);
        }

        return create(info);
    }

    private Customer create(SocialUserInfo info) {
        Customer customer = Customer.builder()
                .firebaseUid(info.subject())
                .email(info.email())
                .phone(info.phoneNumber())
                .emailVerified(info.emailVerified())
                .phoneVerified(!ObjectUtils.isEmpty(info.phoneNumber()))
                .status(Status.ACTIVE)
                .orderCount(0L)
                .identities(new ArrayList<>())
                .build();

        applyName(customer, info.displayName());
        log.info("Created customer from {} sign-in", info.provider());
        return recordIdentity(customer, info);
    }

    /**
     * Attaches the firebase uid to an account that already existed, so the next
     * login resolves by uid directly.
     */
    private Customer link(Customer customer, SocialUserInfo info) {
        log.info("Linking {} sign-in to existing customer {}", info.provider(), customer.getId());
        customer.setFirebaseUid(info.subject());

        // the provider has just verified whichever one it gave us
        if (info.emailVerified() && !ObjectUtils.isEmpty(info.email()))
            customer.setEmailVerified(true);

        if (!ObjectUtils.isEmpty(info.phoneNumber())) {
            customer.setPhone(info.phoneNumber());
            customer.setPhoneVerified(true);
        }

        return recordIdentity(customer, info);
    }

    private Customer recordIdentity(Customer customer, SocialUserInfo info) {
        List<SocialIdentity> identities = customer.getIdentities() == null
                ? new ArrayList<>() : customer.getIdentities();

        boolean alreadyLinked = identities.stream()
                .anyMatch(identity -> identity.getProvider() == info.provider());

        if (!alreadyLinked) {
            identities.add(SocialIdentity.builder()
                    .provider(info.provider())
                    .identifier(ObjectUtils.isEmpty(info.email())
                            ? info.phoneNumber() : info.email())
                    .linkedAt(Instant.now())
                    .build());
            customer.setIdentities(identities);
        }

        return customerRepository.save(customer);
    }

    private void applyName(Customer customer, String displayName) {
        if (ObjectUtils.isEmpty(displayName))
            return;

        // Apple only ever sends the name on the very first authorization
        String[] parts = displayName.trim().split("\\s+", 2);
        customer.setFirstName(parts[0]);
        if (parts.length > 1)
            customer.setLastName(parts[1]);
    }
}
