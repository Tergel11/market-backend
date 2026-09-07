package market.commerce.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.customer.Address;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "warehouse")
public class Warehouse extends BaseDocument {

    @Indexed(unique = true)
    private String code;

    private String name;

    @Indexed
    private String merchantId;

    private Address address;

    /** Whether customers can collect orders here. */
    private Boolean pickupEnabled;

    private Status status;
}
