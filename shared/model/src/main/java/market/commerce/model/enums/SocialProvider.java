package market.commerce.model.enums;

import java.util.Arrays;

/**
 * Sign-in method behind a Firebase account.
 *
 * <p>The values map to Firebase's {@code firebase.sign_in_provider} claim.
 *
 * @author Tergel
 */
public enum SocialProvider {

    GOOGLE("google.com"),
    APPLE("apple.com"),
    PHONE("phone"),
    PASSWORD("password");

    private final String firebaseValue;

    SocialProvider(String firebaseValue) {
        this.firebaseValue = firebaseValue;
    }

    public String getFirebaseValue() {
        return firebaseValue;
    }

    /**
     * @return the matching provider, or null when Firebase reports one we do
     *         not support (a console toggle can enable others at any time)
     */
    public static SocialProvider fromFirebase(String value) {
        return Arrays.stream(values())
                .filter(provider -> provider.firebaseValue.equals(value))
                .findFirst()
                .orElse(null);
    }
}
