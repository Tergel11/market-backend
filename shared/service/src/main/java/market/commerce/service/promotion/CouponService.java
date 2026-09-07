package market.commerce.service.promotion;

import lombok.RequiredArgsConstructor;
import market.commerce.exception.MessageException;
import market.commerce.model.enums.DiscountType;
import market.commerce.model.enums.Status;
import market.commerce.model.order.OrderItem;
import market.commerce.model.promotion.Coupon;
import market.commerce.repository.CouponRepository;
import market.commerce.repository.CouponUsageRepository;
import market.commerce.util.MoneyUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * @author Tergel
 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    /**
     * Validates the code and returns the discount for this basket, or throws
     * with a localized reason.
     */
    public BigDecimal calculateDiscount(
            String code, String customerId, List<OrderItem> items, BigDecimal subTotal) {

        if (ObjectUtils.isEmpty(code))
            return BigDecimal.ZERO;

        Coupon coupon = validate(code, customerId, subTotal);
        BigDecimal eligibleAmount = eligibleAmount(coupon, items, subTotal);

        BigDecimal discount = switch (coupon.getDiscountType()) {
            case PERCENT -> MoneyUtil.percentOf(eligibleAmount, coupon.getDiscountValue());
            case AMOUNT -> coupon.getDiscountValue();
            case FREE_SHIPPING -> BigDecimal.ZERO; // applied against the shipping line instead
        };

        if (coupon.getDiscountType() == DiscountType.PERCENT
                && coupon.getMaxDiscountAmount() != null
                && discount.compareTo(coupon.getMaxDiscountAmount()) > 0)
            discount = coupon.getMaxDiscountAmount();

        // never discount more than the basket is worth
        return discount.compareTo(eligibleAmount) > 0
                ? MoneyUtil.scale(eligibleAmount)
                : MoneyUtil.scale(discount);
    }

    public Coupon validate(String code, String customerId, BigDecimal subTotal) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new MessageException("coupon.invalid"));

        if (coupon.getStatus() != Status.ACTIVE)
            throw new MessageException("coupon.invalid");

        Instant now = Instant.now();
        if ((coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt()))
                || (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())))
            throw new MessageException("coupon.expired");

        if (coupon.getUsageLimit() != null
                && coupon.getUsedCount() != null
                && coupon.getUsedCount() >= coupon.getUsageLimit())
            throw new MessageException("coupon.usage-limit");

        if (coupon.getUsageLimitPerCustomer() != null && !ObjectUtils.isEmpty(customerId)) {
            long used = couponUsageRepository.countByCouponIdAndCustomerId(coupon.getId(), customerId);
            if (used >= coupon.getUsageLimitPerCustomer())
                throw new MessageException("coupon.usage-limit");
        }

        if (coupon.getMinOrderAmount() != null
                && subTotal.compareTo(coupon.getMinOrderAmount()) < 0)
            throw new MessageException("coupon.invalid");

        return coupon;
    }

    /**
     * A coupon restricted to categories, products or merchants only discounts
     * the matching lines.
     */
    private BigDecimal eligibleAmount(Coupon coupon, List<OrderItem> items, BigDecimal subTotal) {
        boolean unrestricted = ObjectUtils.isEmpty(coupon.getProductIds())
                && ObjectUtils.isEmpty(coupon.getMerchantIds());

        if (unrestricted || items == null)
            return subTotal;

        return items.stream()
                .filter(item ->
                        (!ObjectUtils.isEmpty(coupon.getProductIds())
                                && coupon.getProductIds().contains(item.getProductId()))
                        || (!ObjectUtils.isEmpty(coupon.getMerchantIds())
                                && coupon.getMerchantIds().contains(item.getMerchantId())))
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
