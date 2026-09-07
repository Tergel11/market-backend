package market.commerce.model.enums;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author Tergel
 */
public enum ApplicationRole implements GrantedAuthority {

    ROLE_CUSTOMER,
    ROLE_MERCHANT,
    ROLE_OPERATOR,
    ROLE_ADMIN,
    ROLE_SYSTEM;

    @Override
    public String getAuthority() {
        return name();
    }
}
