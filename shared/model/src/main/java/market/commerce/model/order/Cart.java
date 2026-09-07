package market.commerce.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Active basket. Guests get a cart keyed by {@code sessionId}; it is merged into
 * the customer's cart on login.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "cart")
public class Cart extends BaseDocument {

    @Indexed(sparse = true)
    private String customerId;

    @Indexed(sparse = true)
    private String sessionId;

    private List<CartItem> items;

    private String couponCode;

    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal total;

    /** TTL index drops abandoned guest carts; see DatabaseConfig. */
    @Indexed(expireAfterSeconds = 2592000)
    private Instant expiresAt;
}
