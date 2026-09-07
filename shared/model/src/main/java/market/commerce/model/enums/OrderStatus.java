package market.commerce.model.enums;

import java.util.List;
import java.util.Set;

/**
 * Order lifecycle. Allowed transitions are declared on the enum itself so that
 * every service goes through the same rules.
 *
 * @author Tergel
 */
public enum OrderStatus {

    /** Created but not yet paid. */
    PENDING,
    /** Payment captured, waiting for the merchant to accept. */
    PAID,
    /** Merchant accepted, items being picked/packed. */
    CONFIRMED,
    /** Handed to the delivery provider. */
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REFUNDED;

    public Set<OrderStatus> next() {
        return switch (this) {
            case PENDING -> Set.of(PAID, CANCELLED);
            case PAID -> Set.of(CONFIRMED, CANCELLED, REFUNDED);
            case CONFIRMED -> Set.of(SHIPPED, CANCELLED, REFUNDED);
            case SHIPPED -> Set.of(DELIVERED, REFUNDED);
            case DELIVERED -> Set.of(COMPLETED, REFUNDED);
            case COMPLETED, CANCELLED, REFUNDED -> Set.of();
        };
    }

    public boolean canMoveTo(OrderStatus target) {
        return target != null && next().contains(target);
    }

    public static List<OrderStatus> openStatuses() {
        return List.of(PENDING, PAID, CONFIRMED, SHIPPED);
    }
}
