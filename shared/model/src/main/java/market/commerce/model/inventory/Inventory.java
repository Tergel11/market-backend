package market.commerce.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Stock level per variant per warehouse.
 *
 * <p>{@code available = quantity - reserved}. Reserving on checkout and
 * releasing on cancel/expiry is what keeps two shoppers from buying the last
 * unit; every change is written through {@link InventoryMovement}.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "inventory")
@CompoundIndex(name = "variant_warehouse_idx", def = "{'variantId': 1, 'warehouseId': 1}", unique = true)
public class Inventory extends BaseDocument {

    @Indexed
    private String variantId;

    private String productId;
    private String merchantId;
    private String warehouseId;

    /** Physically on hand. */
    private Integer quantity;

    /** Held by unpaid orders. */
    private Integer reserved;

    /** Denormalized quantity - reserved, for fast "in stock" filtering. */
    private Integer available;

    /** Below this the cron raises a low-stock alert. */
    private Integer lowStockThreshold;

    private Boolean trackInventory;

    /** Whether the variant can still be ordered at zero stock. */
    private Boolean allowBackorder;
}
