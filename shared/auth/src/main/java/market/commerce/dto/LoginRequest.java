package market.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Tergel
 */
@Getter
@Setter
public class LoginRequest {

    /** Email, phone, or username depending on the api module. */
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
