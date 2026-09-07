package market.commerce.exception;

/**
 * Requested document does not exist.
 *
 * @author Tergel
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
