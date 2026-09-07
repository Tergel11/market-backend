package market.commerce.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.constants.ConstantValues;
import market.commerce.constants.SystemConfigCode;
import market.commerce.dao.OrderDao;
import market.commerce.dto.PaginationResponse;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.model.catalog.Product;
import market.commerce.model.catalog.ProductVariant;
import market.commerce.model.customer.Address;
import market.commerce.model.enums.DeliveryType;
import market.commerce.model.enums.OrderStatus;
import market.commerce.model.enums.PaymentStatus;
import market.commerce.model.order.Cart;
import market.commerce.model.order.CartItem;
import market.commerce.model.order.Order;
import market.commerce.model.order.OrderItem;
import market.commerce.model.order.OrderStatusHistory;
import market.commerce.repository.OrderRepository;
import market.commerce.repository.ProductRepository;
import market.commerce.repository.ProductVariantRepository;
import market.commerce.service.cart.CartService;
import market.commerce.service.inventory.InventoryService;
import market.commerce.service.promotion.CouponService;
import market.commerce.service.system.SequenceService;
import market.commerce.service.system.SystemConfigService;
import market.commerce.util.LocalizationUtil;
import market.commerce.util.MoneyUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Checkout and order lifecycle.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDao orderDao;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartService cartService;
    private final CouponService couponService;
    private final InventoryService inventoryService;
    private final SequenceService sequenceService;
    private final SystemConfigService systemConfigService;

    public Order findById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("data.not-found"));
    }

    public Order findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("data.not-found"));
    }

    public PaginationResponse<Order> search(OrderDao.OrderFilter filter, Pageable pageable) {
        long total = orderDao.count(filter);
        return PaginationResponse.of(
                orderDao.list(filter, pageable), total,
                pageable.getPageNumber(), pageable.getPageSize());
    }

    /**
     * Turns a cart into a PENDING order and reserves the stock.
     *
     * <p>Runs in a transaction so a failure part-way cannot leave stock held for
     * an order that was never written. Requires a replica set.
     */
    @Transactional
    public Order checkout(
            String customerId,
            Cart cart,
            Address shippingAddress,
            DeliveryType deliveryType,
            String note) {

        if (cart == null || ObjectUtils.isEmpty(cart.getItems()))
            throw new MessageException("cart.empty");

        List<OrderItem> items = buildItems(cart);
        BigDecimal subTotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = couponService.calculateDiscount(
                cart.getCouponCode(), customerId, items, subTotal);
        BigDecimal shipping = shippingFee(deliveryType, subTotal);
        BigDecimal tax = MoneyUtil.percentOf(
                MoneyUtil.subtractFloorZero(subTotal, discount),
                systemConfigService.getDecimal(SystemConfigCode.TAX_RATE, BigDecimal.ZERO));

        Order order = Order.builder()
                .orderNumber(sequenceService.nextOrderNumber())
                .customerId(customerId)
                .items(items)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.NEW)
                .statusHistory(new ArrayList<>(List.of(OrderStatusHistory.builder()
                        .toStatus(OrderStatus.PENDING)
                        .changedBy(customerId)
                        .changedAt(Instant.now())
                        .build())))
                .subTotal(MoneyUtil.scale(subTotal))
                .discountAmount(discount)
                .shippingAmount(shipping)
                .taxAmount(tax)
                .totalAmount(MoneyUtil.sum(
                        MoneyUtil.subtractFloorZero(subTotal, discount), shipping, tax))
                .couponCode(cart.getCouponCode())
                .deliveryType(deliveryType)
                .shippingAddress(shippingAddress)
                .note(note)
                .build();

        // hold the stock before the order is visible to anyone else
        for (OrderItem item : items)
            inventoryService.reserve(
                    item.getVariantId(),
                    ConstantValues.DEFAULT_WAREHOUSE_CODE,
                    item.getQuantity(),
                    order.getOrderNumber());

        Order saved = orderRepository.save(order);
        cartService.clear(customerId);

        log.info("Created order {} total {}", saved.getOrderNumber(), saved.getTotalAmount());
        return saved;
    }

    /**
     * Moves an order along, rejecting transitions the status machine forbids.
     */
    public Order changeStatus(String orderId, OrderStatus target, String note, String changedBy) {
        Order order = findById(orderId);

        if (!order.getStatus().canMoveTo(target))
            throw new MessageException("order.invalid-status");

        List<OrderStatusHistory> history = order.getStatusHistory() == null
                ? new ArrayList<>() : order.getStatusHistory();

        history.add(OrderStatusHistory.builder()
                .fromStatus(order.getStatus())
                .toStatus(target)
                .note(note)
                .changedBy(changedBy)
                .changedAt(Instant.now())
                .build());

        order.setStatusHistory(history);
        order.setStatus(target);

        switch (target) {
            case PAID -> {
                order.setPaidAt(Instant.now());
                order.setPaymentStatus(PaymentStatus.PAID);
                // reserved units now actually leave the warehouse
                order.getItems().forEach(item -> inventoryService.commit(
                        item.getVariantId(), ConstantValues.DEFAULT_WAREHOUSE_CODE,
                        item.getQuantity(), order.getOrderNumber()));
            }
            case SHIPPED -> order.setShippedAt(Instant.now());
            case DELIVERED -> order.setDeliveredAt(Instant.now());
            case CANCELLED -> {
                order.setCancelledAt(Instant.now());
                order.setCancelReason(note);
                releaseStock(order);
            }
            case REFUNDED -> order.setPaymentStatus(PaymentStatus.REFUNDED);
            default -> { }
        }

        return orderRepository.save(order);
    }

    /**
     * Called by the cron for orders that were never paid.
     */
    public void expire(Order order) {
        log.info("Expiring unpaid order {}", order.getOrderNumber());
        changeStatus(order.getId(), OrderStatus.CANCELLED, "payment timeout", "system");
    }

    public List<Order> findExpiredPending() {
        int timeout = systemConfigService.getInt(
                SystemConfigCode.ORDER_PAYMENT_TIMEOUT, ConstantValues.ORDER_PAYMENT_TIMEOUT_MINUTES);

        return orderRepository.findByStatusAndCreatedAtBefore(
                OrderStatus.PENDING, Instant.now().minus(timeout, ChronoUnit.MINUTES));
    }

    /**
     * Copies name, price and options onto the order so later catalog edits do
     * not rewrite history, and re-reads the price from the variant rather than
     * trusting the cart.
     */
    private List<OrderItem> buildItems(Cart cart) {
        List<OrderItem> items = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = productVariantRepository.findById(cartItem.getVariantId())
                    .orElseThrow(() -> new NotFoundException("data.not-found"));

            Product product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new NotFoundException("data.not-found"));

            items.add(OrderItem.builder()
                    .productId(product.getId())
                    .variantId(variant.getId())
                    .merchantId(product.getMerchantId())
                    .productName(LocalizationUtil.translate(product.getName()))
                    .sku(variant.getSku())
                    .imageUrl(variant.getImage() != null ? variant.getImage().getUrl()
                            : (ObjectUtils.isEmpty(product.getImages()) ? null
                                    : product.getImages().get(0).getUrl()))
                    .optionValues(variant.getOptionValues())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(variant.getPrice())
                    .totalPrice(MoneyUtil.multiply(variant.getPrice(), cartItem.getQuantity()))
                    .build());
        }

        return items;
    }

    private BigDecimal shippingFee(DeliveryType deliveryType, BigDecimal subTotal) {
        if (deliveryType != DeliveryType.HOME_DELIVERY)
            return BigDecimal.ZERO;

        BigDecimal threshold = systemConfigService.getDecimal(
                SystemConfigCode.FREE_SHIPPING_THRESHOLD, null);

        if (threshold != null && subTotal.compareTo(threshold) >= 0)
            return BigDecimal.ZERO;

        return systemConfigService.getDecimal(
                SystemConfigCode.SHIPPING_FLAT_FEE, BigDecimal.ZERO);
    }

    private void releaseStock(Order order) {
        if (order.getItems() == null)
            return;

        order.getItems().forEach(item -> inventoryService.release(
                item.getVariantId(), ConstantValues.DEFAULT_WAREHOUSE_CODE,
                item.getQuantity(), order.getOrderNumber()));
    }
}
