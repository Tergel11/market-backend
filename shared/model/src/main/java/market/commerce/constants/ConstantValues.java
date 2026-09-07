package market.commerce.constants;

/**
 * @author Tergel
 */
public class ConstantValues {

    public static final String DEFAULT_CURRENCY = "MNT";
    public static final String ORDER_NUMBER_PREFIX = "MK";

    /** Unpaid orders are released back to stock after this many minutes. */
    public static final int ORDER_PAYMENT_TIMEOUT_MINUTES = 30;

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_CART_ITEM_QUANTITY = 99;

    public static final String DEFAULT_WAREHOUSE_CODE = "MAIN";

    private ConstantValues() {
    }
}
