package market.commerce.social;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.exception.MessageException;
import market.commerce.model.enums.SocialProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Verifies Firebase ID tokens.
 *
 * <p>Google, Apple and phone OTP all arrive here as the same Firebase token —
 * the client SDK does the provider dance and the sign-in method is reported in
 * the {@code firebase.sign_in_provider} claim. Verification is local: the
 * Admin SDK caches Google's public keys, so this is not a network call per
 * login.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseTokenVerifier implements SocialTokenVerifier {

    private final FirebaseApp firebaseApp;

    @Override
    public SocialUserInfo verify(String idToken) {
        FirebaseToken token;
        try {
            token = FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken);
        } catch (FirebaseAuthException ex) {
            log.warn("Rejected firebase id token : {}", ex.getMessage());
            throw new MessageException("auth.invalid-token");
        }

        Map<String, Object> claims = token.getClaims();

        return new SocialUserInfo(
                token.getUid(),
                resolveProvider(claims),
                token.getEmail(),
                token.isEmailVerified(),
                asString(claims.get("phone_number")),
                token.getName(),
                token.getPicture());
    }

    /**
     * Reads {@code firebase.sign_in_provider} out of the nested firebase claim.
     */
    @SuppressWarnings("unchecked")
    private SocialProvider resolveProvider(Map<String, Object> claims) {
        Object firebase = claims.get("firebase");
        if (!(firebase instanceof Map))
            return null;

        String signInProvider = asString(((Map<String, Object>) firebase).get("sign_in_provider"));
        SocialProvider provider = SocialProvider.fromFirebase(signInProvider);

        if (provider == null)
            log.warn("Unsupported firebase sign_in_provider : {}", signInProvider);

        return provider;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
