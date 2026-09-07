package market.commerce.model.enums;

/**
 * @author Tergel
 */
public enum InventoryMovementType {

    /** Merchant added stock. */
    RECEIPT,
    /** Held while an order is unpaid. */
    RESERVE,
    RELEASE,
    /** Stock left the warehouse with an order. */
    SALE,
    RETURN,
    ADJUSTMENT
}
