package market.commerce.exception;

import lombok.Getter;

/**
 * Payment provider rejected or failed to process a request.
 *
 * @author Tergel
 */
@Getter
public class PaymentException extends RuntimeException {

    private final String providerCode;

    public PaymentException(String message) {
        this(message, null);
    }

    public PaymentException(String message, String providerCode) {
        super(message);
        this.providerCode = providerCode;
    }
}
