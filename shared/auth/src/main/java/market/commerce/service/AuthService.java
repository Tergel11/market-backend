package market.commerce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.dto.AuthResponse;
import market.commerce.exception.MessageException;
import market.commerce.model.customer.Customer;
import market.commerce.model.enums.ApplicationRole;
import market.commerce.model.enums.Status;
import market.commerce.model.user.User;
import market.commerce.repository.CustomerRepository;
import market.commerce.repository.UserRepository;
import market.commerce.util.JwtTokenUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Issues tokens for both storefront customers and back-office users.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Storefront login by email or phone.
     */
    public AuthResponse loginCustomer(String identifier, String rawPassword) {
        Customer customer = (identifier != null && identifier.contains("@")
                ? customerRepository.findByEmail(identifier)
                : customerRepository.findByPhone(identifier))
                .orElseThrow(() -> new MessageException("auth.invalid-credentials"));

        if (!passwordEncoder.matches(rawPassword, customer.getPassword()))
            throw new MessageException("auth.invalid-credentials");

        if (customer.getStatus() != Status.ACTIVE)
            throw new MessageException("error.permission");

        customer.setLastLoginAt(Instant.now());
        customerRepository.save(customer);

        String token = jwtTokenUtil.generateToken(
                customer.getId(), List.of(ApplicationRole.ROLE_CUSTOMER), null);

        return buildResponse(token, customer.getId(),
                customer.getEmail(), List.of(ApplicationRole.ROLE_CUSTOMER));
    }

    /**
     * Back-office login. Merchant staff carry their merchantId in the token so
     * every admin query can be scoped to their own store.
     */
    public AuthResponse loginUser(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new MessageException("auth.invalid-credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            throw new MessageException("auth.invalid-credentials");

        if (user.getStatus() != Status.ACTIVE)
            throw new MessageException("error.permission");

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        if (user.getMerchantId() != null)
            claims.put(JwtTokenUtil.CLAIM_MERCHANT_ID, user.getMerchantId());

        String token = jwtTokenUtil.generateToken(user.getId(), user.getRoles(), claims);
        return buildResponse(token, user.getId(), user.getEmail(), user.getRoles());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private AuthResponse buildResponse(
            String token, String id, String email, List<ApplicationRole> roles) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenUtil.getExpirationSeconds())
                .id(id)
                .email(email)
                .roles(roles)
                .build();
    }
}
