package market.commerce.model.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.enums.PaymentStatus;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "refund")
public class Refund extends BaseDocument {

    @Indexed
    private String orderId;

    @Indexed
    private String paymentId;

    private BigDecimal amount;
    private String reason;
    private PaymentStatus status;

    private String approvedBy;
    private Instant approvedAt;
    private Instant refundedAt;
}
