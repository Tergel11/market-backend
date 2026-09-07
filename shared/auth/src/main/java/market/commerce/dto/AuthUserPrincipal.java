package market.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import market.commerce.model.enums.ApplicationRole;

import java.io.Serializable;
import java.util.List;

/**
 * Resolved caller, injected into controllers with {@code @AuthUser}.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserPrincipal implements Serializable {

    private String id;
    private String username;
    private String email;
    private String phone;
    private List<ApplicationRole> roles;

    /** Set for merchant staff only. */
    private String merchantId;

    public boolean hasRole(ApplicationRole role) {
        return roles != null && roles.contains(role);
    }

    public boolean isCustomer() {
        return hasRole(ApplicationRole.ROLE_CUSTOMER);
    }
}
