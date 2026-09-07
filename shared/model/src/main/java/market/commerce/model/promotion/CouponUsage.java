package market.commerce.model.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Enforces per-customer usage limits.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "couponUsage")
@CompoundIndex(name = "coupon_customer_idx", def = "{'couponId': 1, 'customerId': 1}")
public class CouponUsage extends BaseDocument {

    private String couponId;
    private String couponCode;
    private String customerId;
    private String orderId;
    private BigDecimal discountAmount;
}
