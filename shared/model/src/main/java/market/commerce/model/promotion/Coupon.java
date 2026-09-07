package market.commerce.model.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.MultiLanguage;
import market.commerce.model.enums.DiscountType;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "coupon")
public class Coupon extends BaseDocument {

    @Indexed(unique = true)
    private String code;

    private MultiLanguage name;
    private MultiLanguage description;

    private DiscountType discountType;

    /** Percent (0-100) or absolute amount, per {@link #discountType}. */
    private BigDecimal discountValue;

    /** Caps the discount when the type is PERCENT. */
    private BigDecimal maxDiscountAmount;

    private BigDecimal minOrderAmount;

    /** Empty means the coupon applies to the whole catalog. */
    private List<String> categoryIds;
    private List<String> productIds;
    private List<String> merchantIds;

    private Integer usageLimit;
    private Integer usageLimitPerCustomer;
    private Integer usedCount;

    private Instant startsAt;
    private Instant endsAt;

    @Indexed
    private Status status;
}
