package market.commerce.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import market.commerce.model.customer.Address;
import market.commerce.model.enums.DeliveryType;

/**
 * @author Tergel
 */
@Getter
@Setter
public class CheckoutRequest {

    @NotNull
    private DeliveryType deliveryType;

    /** Required unless the delivery type is a pickup. */
    private Address shippingAddress;

    private String couponCode;
    private String note;
}
