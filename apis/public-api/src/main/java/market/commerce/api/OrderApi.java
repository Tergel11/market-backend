package market.commerce.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import market.commerce.annotations.AuthUser;
import market.commerce.api.request.CheckoutRequest;
import market.commerce.dao.OrderDao;
import market.commerce.dto.AuthUserPrincipal;
import market.commerce.dto.PageRequestDto;
import market.commerce.dto.PaginationResponse;
import market.commerce.model.enums.OrderStatus;
import market.commerce.model.order.Order;
import market.commerce.service.cart.CartService;
import market.commerce.service.order.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tergel
 */
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderApi extends BaseController {

    private final OrderService orderService;
    private final CartService cartService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            @AuthUser AuthUserPrincipal user,
            @Valid @RequestBody CheckoutRequest request) {

        var cart = cartService.findOrCreate(user.getId(), null);
        cart.setCouponCode(request.getCouponCode());

        return ResponseEntity.ok(orderService.checkout(
                user.getId(),
                cart,
                request.getShippingAddress(),
                request.getDeliveryType(),
                request.getNote()));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<Order>> list(
            @AuthUser AuthUserPrincipal user,
            PageRequestDto pageRequest) {

        var filter = new OrderDao.OrderFilter(
                null, user.getId(), null, null, null, null, null, null);

        return ResponseEntity.ok(orderService.search(filter, pageRequest.toPageable()));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<?> detail(
            @AuthUser AuthUserPrincipal user,
            @PathVariable String orderNumber) {

        Order order = orderService.findByOrderNumber(orderNumber);

        // an order is only visible to the customer who placed it
        if (!user.getId().equals(order.getCustomerId()))
            return errorPermission();

        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancel(
            @AuthUser AuthUserPrincipal user,
            @PathVariable String orderId) {

        Order order = orderService.findById(orderId);
        if (!user.getId().equals(order.getCustomerId()))
            return errorPermission();

        return ResponseEntity.ok(orderService.changeStatus(
                orderId, OrderStatus.CANCELLED, "cancelled by customer", user.getId()));
    }
}
