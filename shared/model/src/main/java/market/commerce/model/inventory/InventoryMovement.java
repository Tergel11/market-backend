package market.commerce.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.enums.InventoryMovementType;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Append-only stock ledger. Replaying it must reproduce the current
 * {@link Inventory} counts.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "inventoryMovement")
public class InventoryMovement extends BaseDocument {

    @Indexed
    private String variantId;

    private String warehouseId;
    private InventoryMovementType type;

    /** Signed: negative for SALE and RESERVE. */
    private Integer quantity;

    private Integer quantityAfter;

    /** Order id, refund id, or manual adjustment reference. */
    @Indexed(sparse = true)
    private String referenceId;

    private String note;
}
