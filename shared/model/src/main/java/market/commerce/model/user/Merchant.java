package market.commerce.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.FileData;
import market.commerce.model.MultiLanguage;
import market.commerce.model.customer.Address;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Seller / store on the marketplace.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "merchant")
public class Merchant extends BaseDocument {

    @Indexed(unique = true)
    private String slug;

    private String name;
    private MultiLanguage description;

    private String registerNumber;
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private FileData logo;
    private FileData cover;
    private Address address;

    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    /** Platform commission percentage taken from each order. */
    private BigDecimal commissionRate;

    private Double ratingAverage;
    private Long productCount;

    @Indexed
    private Status status;
}
