package market.commerce.model.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Embedded in {@link Customer} and copied onto an order at checkout, so later
 * edits to the customer's address book never rewrite past orders.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address implements Serializable {

    private String id;
    private String label;

    private String receiverName;
    private String receiverPhone;

    private String country;
    private String province;
    private String district;
    private String khoroo;
    private String addressDetail;
    private String zipCode;

    private Double latitude;
    private Double longitude;

    private Boolean isDefault;
}
