package market.commerce.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
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
public class CartItem implements Serializable {

    private String productId;
    private String variantId;
    private String merchantId;

    private Integer quantity;

    /** Price when added; re-validated against the variant at checkout. */
    private BigDecimal unitPrice;

    private Instant addedAt;
}
