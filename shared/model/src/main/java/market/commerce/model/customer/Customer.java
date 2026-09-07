package market.commerce.model.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.FileData;
import market.commerce.model.enums.Locale;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Storefront account.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "customer")
public class Customer extends BaseDocument {

    @Indexed(unique = true, sparse = true)
    private String email;

    @Indexed(unique = true, sparse = true)
    private String phone;

    private String password;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private FileData avatar;
    private Locale preferredLocale;

    private Boolean emailVerified;
    private Boolean phoneVerified;

    private List<Address> addresses;

    /** Points to an entry in {@link #addresses}. */
    private String defaultAddressId;

    private Long orderCount;
    private Instant lastLoginAt;

    @Indexed
    private Status status;
}
