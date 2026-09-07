package market.commerce.integration.delivery;

import market.commerce.model.customer.Address;
import market.commerce.model.order.Order;
import market.commerce.model.order.Shipment;

import java.math.BigDecimal;

/**
 * Delivery partner integration.
 *
 * @author Tergel
 */
public interface DeliveryProvider {

    String code();

    /** Quote shown at checkout before the order exists. */
    BigDecimal calculateFee(Address address, Integer totalWeight);

    /** Registers the parcel with the partner and returns the tracking number. */
    Shipment createShipment(Order order, Shipment shipment);

    Shipment track(String trackingNumber);

    void cancel(String trackingNumber);
}
