package market.commerce.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.enums.ShipmentStatus;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Delivery of an order, or of part of one when items ship from several merchants.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "shipment")
public class Shipment extends BaseDocument {

    @Indexed
    private String orderId;

    @Indexed(unique = true, sparse = true)
    private String trackingNumber;

    private String provider;
    private String merchantId;

    /** Variant ids covered by this shipment. */
    private List<String> variantIds;

    private ShipmentStatus status;
    private BigDecimal shippingCost;

    private Instant shippedAt;
    private Instant estimatedDeliveryAt;
    private Instant deliveredAt;

    private String receivedBy;
    private String failReason;
}
