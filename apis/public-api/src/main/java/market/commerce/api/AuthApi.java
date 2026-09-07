package market.commerce.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import market.commerce.dto.AuthResponse;
import market.commerce.api.request.SocialLoginRequest;
import market.commerce.dto.LoginRequest;
import market.commerce.service.AuthService;
import market.commerce.service.SocialAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tergel
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthApi extends BaseController {

    private final AuthService authService;
    private final SocialAuthService socialAuthService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authService.loginCustomer(request.getUsername(), request.getPassword()));
    }

    /**
     * Google, Apple and phone OTP sign-in.
     *
     * <p>One endpoint for all three: the client SDK completes the provider flow
     * and the sign-in method is a claim inside the Firebase token.
     */
    @PostMapping("/social")
    public ResponseEntity<AuthResponse> social(@Valid @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(socialAuthService.login(request.getIdToken()));
    }
}
