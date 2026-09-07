package market.commerce.exception;

/**
 * Localized message returned to the client as 400.
 *
 * @author Tergel
 */
public class MessageException extends RuntimeException {

    public MessageException(String message) {
        super(message);
    }
}
