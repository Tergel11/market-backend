package market.commerce.exception;

/**
 * Generic business error rendered as 400.
 *
 * @author Tergel
 */
public class ErrorException extends RuntimeException {

    public ErrorException(String message) {
        super(message);
    }
}
