package market.commerce.api;

import lombok.RequiredArgsConstructor;
import market.commerce.annotations.AuthUser;
import market.commerce.dao.OrderDao;
import market.commerce.dto.AuthUserPrincipal;
import market.commerce.dto.PageRequestDto;
import market.commerce.dto.PaginationResponse;
import market.commerce.model.enums.ApplicationRole;
import market.commerce.model.enums.OrderStatus;
import market.commerce.model.enums.PaymentStatus;
import market.commerce.model.order.Order;
import market.commerce.service.order.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * @author Tergel
 */
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Secured({"ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_MERCHANT"})
public class ManageOrderApi extends BaseController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PaginationResponse<Order>> list(
            @AuthUser AuthUserPrincipal user,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) List<OrderStatus> statuses,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            PageRequestDto pageRequest) {

        String merchantId = user.hasRole(ApplicationRole.ROLE_MERCHANT)
                ? user.getMerchantId() : null;

        var filter = new OrderDao.OrderFilter(
                orderNumber, null, customerPhone, merchantId,
                statuses, paymentStatus, startDate, endDate);

        return ResponseEntity.ok(orderService.search(filter, pageRequest.toPageable()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> detail(@PathVariable String id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> changeStatus(
            @AuthUser AuthUserPrincipal user,
            @PathVariable String id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String note) {

        return ResponseEntity.ok(orderService.changeStatus(id, status, note, user.getId()));
    }
}
