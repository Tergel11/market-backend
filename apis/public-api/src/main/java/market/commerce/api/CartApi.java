package market.commerce.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import market.commerce.annotations.AuthUser;
import market.commerce.api.request.CartItemRequest;
import market.commerce.dto.AuthUserPrincipal;
import market.commerce.model.order.Cart;
import market.commerce.service.cart.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cart works for guests too: an anonymous caller passes X-Session-Id instead of
 * a token, and the basket is merged on login.
 *
 * @author Tergel
 */
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartApi extends BaseController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> get(
            @AuthUser(required = false) AuthUserPrincipal user,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return ResponseEntity.ok(cartService.findOrCreate(userId(user), sessionId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(
            @AuthUser(required = false) AuthUserPrincipal user,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @Valid @RequestBody CartItemRequest request) {

        return ResponseEntity.ok(cartService.addItem(
                userId(user), sessionId, request.getVariantId(), request.getQuantity()));
    }

    @PutMapping("/items/{variantId}")
    public ResponseEntity<Cart> updateItem(
            @AuthUser(required = false) AuthUserPrincipal user,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable String variantId,
            @Valid @RequestBody CartItemRequest request) {

        return ResponseEntity.ok(cartService.updateQuantity(
                userId(user), sessionId, variantId, request.getQuantity()));
    }

    @DeleteMapping("/items/{variantId}")
    public ResponseEntity<Cart> removeItem(
            @AuthUser(required = false) AuthUserPrincipal user,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @PathVariable String variantId) {

        return ResponseEntity.ok(cartService.removeItem(userId(user), sessionId, variantId));
    }

    @PostMapping("/merge")
    public ResponseEntity<Cart> merge(
            @AuthUser AuthUserPrincipal user,
            @RequestHeader("X-Session-Id") String sessionId) {
        return ResponseEntity.ok(cartService.merge(user.getId(), sessionId));
    }

    private String userId(AuthUserPrincipal user) {
        return user == null ? null : user.getId();
    }
}
