package market.commerce.service.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.constants.ConstantValues;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.model.catalog.ProductVariant;
import market.commerce.model.order.Cart;
import market.commerce.model.order.CartItem;
import market.commerce.repository.CartRepository;
import market.commerce.repository.ProductVariantRepository;
import market.commerce.util.MoneyUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private static final int GUEST_CART_TTL_DAYS = 30;

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    public Cart findOrCreate(String customerId, String sessionId) {
        return (ObjectUtils.isEmpty(customerId)
                ? cartRepository.findBySessionId(sessionId)
                : cartRepository.findByCustomerId(customerId))
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .customerId(customerId)
                        .sessionId(sessionId)
                        .items(new ArrayList<>())
                        .expiresAt(Instant.now().plus(GUEST_CART_TTL_DAYS, ChronoUnit.DAYS))
                        .build()));
    }

    public Cart addItem(String customerId, String sessionId, String variantId, int quantity) {
        if (quantity < 1 || quantity > ConstantValues.MAX_CART_ITEM_QUANTITY)
            throw new MessageException("error.invalid-request");

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("data.not-found"));

        Cart cart = findOrCreate(customerId, sessionId);
        List<CartItem> items = cart.getItems() == null ? new ArrayList<>() : cart.getItems();

        items.stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> items.add(CartItem.builder()
                                .productId(variant.getProductId())
                                .variantId(variantId)
                                .quantity(quantity)
                                .unitPrice(variant.getPrice())
                                .addedAt(Instant.now())
                                .build()));

        cart.setItems(items);
        return recalculate(cart);
    }

    public Cart updateQuantity(String customerId, String sessionId, String variantId, int quantity) {
        Cart cart = findOrCreate(customerId, sessionId);
        if (cart.getItems() == null)
            return cart;

        if (quantity <= 0)
            cart.getItems().removeIf(item -> item.getVariantId().equals(variantId));
        else
            cart.getItems().stream()
                    .filter(item -> item.getVariantId().equals(variantId))
                    .forEach(item -> item.setQuantity(quantity));

        return recalculate(cart);
    }

    public Cart removeItem(String customerId, String sessionId, String variantId) {
        return updateQuantity(customerId, sessionId, variantId, 0);
    }

    public void clear(String customerId) {
        cartRepository.deleteByCustomerId(customerId);
    }

    /**
     * On login the guest basket is folded into the customer's own cart.
     */
    public Cart merge(String customerId, String sessionId) {
        var guestCart = cartRepository.findBySessionId(sessionId);
        if (guestCart.isEmpty())
            return findOrCreate(customerId, null);

        Cart target = findOrCreate(customerId, null);
        List<CartItem> items = target.getItems() == null ? new ArrayList<>() : target.getItems();

        for (CartItem guestItem : guestCart.get().getItems()) {
            items.stream()
                    .filter(item -> item.getVariantId().equals(guestItem.getVariantId()))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> item.setQuantity(item.getQuantity() + guestItem.getQuantity()),
                            () -> items.add(guestItem));
        }

        target.setItems(items);
        cartRepository.delete(guestCart.get());
        return recalculate(target);
    }

    /**
     * Re-prices every line against the current variant price — the price stored
     * on the item is only a display cache.
     */
    public Cart recalculate(Cart cart) {
        BigDecimal subTotal = BigDecimal.ZERO;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                productVariantRepository.findById(item.getVariantId())
                        .ifPresent(variant -> item.setUnitPrice(variant.getPrice()));
                subTotal = subTotal.add(MoneyUtil.multiply(item.getUnitPrice(), item.getQuantity()));
            }
        }

        cart.setSubTotal(MoneyUtil.scale(subTotal));
        cart.setTotal(MoneyUtil.subtractFloorZero(cart.getSubTotal(), cart.getDiscountAmount()));
        return cartRepository.save(cart);
    }
}
