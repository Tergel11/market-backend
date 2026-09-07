package market.commerce.exception;

/**
 * Caller is authenticated but not allowed.
 *
 * @author Tergel
 */
public class PermissionException extends RuntimeException {

    public PermissionException(String message) {
        super(message);
    }
}
