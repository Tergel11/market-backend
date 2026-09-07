package market.commerce.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.customer.Address;
import market.commerce.model.enums.DeliveryType;
import market.commerce.model.enums.OrderStatus;
import market.commerce.model.enums.PaymentStatus;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Immutable record of a purchase. Product name, price and the delivery address
 * are copied onto the order so later catalog edits cannot change history.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "order")
@CompoundIndex(name = "customer_created_idx", def = "{'customerId': 1, 'createdAt': -1}")
@CompoundIndex(name = "status_created_idx", def = "{'status': 1, 'createdAt': -1}")
public class Order extends BaseDocument {

    /** Human readable number shown to the customer, e.g. MK24000123. */
    @Indexed(unique = true)
    private String orderNumber;

    @Indexed
    private String customerId;

    private String customerPhone;
    private String customerEmail;

    private List<OrderItem> items;

    @Indexed
    private OrderStatus status;

    private PaymentStatus paymentStatus;
    private List<OrderStatusHistory> statusHistory;

    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private String couponCode;

    private DeliveryType deliveryType;

    /** Snapshot of the address at checkout time. */
    private Address shippingAddress;

    private String note;

    private Instant paidAt;
    private Instant shippedAt;
    private Instant deliveredAt;
    private Instant cancelledAt;
    private String cancelReason;
}
