package market.commerce.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * All money arithmetic goes through here so rounding is consistent across
 * cart, order and payment.
 *
 * @author Tergel
 */
public class MoneyUtil {

    /** MNT has no minor unit in practice, so totals are whole tugrik. */
    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private MoneyUtil() {
    }

    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static BigDecimal scale(BigDecimal value) {
        return zeroIfNull(value).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal multiply(BigDecimal price, Integer quantity) {
        return scale(zeroIfNull(price).multiply(BigDecimal.valueOf(quantity == null ? 0 : quantity)));
    }

    public static BigDecimal sum(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal value : values)
            total = total.add(zeroIfNull(value));
        return scale(total);
    }

    public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return scale(zeroIfNull(amount).multiply(zeroIfNull(percent)).divide(HUNDRED, SCALE, ROUNDING));
    }

    /** Never lets a total go below zero after discounts. */
    public static BigDecimal subtractFloorZero(BigDecimal amount, BigDecimal subtract) {
        BigDecimal result = zeroIfNull(amount).subtract(zeroIfNull(subtract));
        return result.signum() < 0 ? BigDecimal.ZERO : scale(result);
    }
}
