package market.commerce.integration.delivery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.constants.SystemConfigCode;
import market.commerce.model.customer.Address;
import market.commerce.model.enums.ShipmentStatus;
import market.commerce.model.order.Order;
import market.commerce.model.order.Shipment;
import market.commerce.repository.SystemConfigRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * In-house courier: one flat fee read from systemConfig, tracking numbers
 * generated locally. Used until a real partner is wired up.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlatRateDeliveryProvider implements DeliveryProvider {

    private static final BigDecimal DEFAULT_FEE = BigDecimal.valueOf(5000);

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public String code() {
        return "SELF";
    }

    @Override
    public BigDecimal calculateFee(Address address, Integer totalWeight) {
        return systemConfigRepository.findByCode(SystemConfigCode.SHIPPING_FLAT_FEE)
                .map(config -> new BigDecimal(config.getValue()))
                .orElse(DEFAULT_FEE);
    }

    @Override
    public Shipment createShipment(Order order, Shipment shipment) {
        shipment.setProvider(code());
        shipment.setTrackingNumber("SELF" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 10).toUpperCase());
        shipment.setStatus(ShipmentStatus.CREATED);
        log.info("Created self shipment {} for order {}",
                shipment.getTrackingNumber(), order.getOrderNumber());
        return shipment;
    }

    @Override
    public Shipment track(String trackingNumber) {
        // status is driven by the courier app, so nothing to pull
        return null;
    }

    @Override
    public void cancel(String trackingNumber) {
        log.info("Cancelled self shipment {}", trackingNumber);
    }
}
