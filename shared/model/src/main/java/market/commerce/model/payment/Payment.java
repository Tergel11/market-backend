package market.commerce.model.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.enums.PaymentMethod;
import market.commerce.model.enums.PaymentStatus;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * One payment attempt against an order. An order can have several (retry after
 * a failure, or a partial refund).
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "payment")
public class Payment extends BaseDocument {

    @Indexed
    private String orderId;

    @Indexed
    private String customerId;

    private PaymentMethod method;
    private PaymentStatus status;

    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private String currency;

    /** Provider-side id, e.g. the QPay invoice id. */
    @Indexed(sparse = true)
    private String providerInvoiceId;

    private String providerTransactionId;

    /** Raw provider callback payload, kept for reconciliation. */
    private Map<String, Object> providerResponse;

    private String qrText;
    private String qrImage;
    private String paymentUrl;

    private Instant paidAt;
    private Instant expiresAt;
    private String failReason;
}
