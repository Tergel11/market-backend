package market.commerce.exception;

import lombok.Getter;

/**
 * Raised when a checkout asks for more units than are available.
 *
 * @author Tergel
 */
@Getter
public class OutOfStockException extends RuntimeException {

    private final String variantId;
    private final Integer available;

    public OutOfStockException(String message, String variantId, Integer available) {
        super(message);
        this.variantId = variantId;
        this.available = available;
    }
}
