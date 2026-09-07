package market.commerce.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.enums.ApplicationRole;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Back-office operator or merchant staff. Storefront shoppers are
 * {@code Customer}, not User.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "user")
public class User extends BaseDocument {

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true, sparse = true)
    private String email;

    private String phone;
    private String password;

    private String firstName;
    private String lastName;

    private List<ApplicationRole> roles;

    /** Set for merchant staff; null for platform operators. */
    @Indexed
    private String merchantId;

    private Instant lastLoginAt;
    private Instant passwordChangedAt;

    @Indexed
    private Status status;
}
