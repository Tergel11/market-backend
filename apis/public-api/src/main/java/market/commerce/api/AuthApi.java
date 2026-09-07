package market.commerce.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import market.commerce.dto.AuthResponse;
import market.commerce.dto.LoginRequest;
import market.commerce.service.AuthService;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authService.loginCustomer(request.getUsername(), request.getPassword()));
    }
}
