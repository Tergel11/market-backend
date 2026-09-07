package market.commerce.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Tergel
 */
@Getter
@Setter
public class SocialLoginRequest {

    /**
     * Firebase ID token from the client SDK. One field for every method —
     * Google, Apple and phone OTP all produce the same token, and the sign-in
     * provider is a claim inside it.
     */
    @NotBlank
    private String idToken;
}
