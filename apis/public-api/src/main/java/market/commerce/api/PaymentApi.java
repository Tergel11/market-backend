package market.commerce.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.annotations.AuthUser;
import market.commerce.dto.AuthUserPrincipal;
import market.commerce.model.enums.PaymentMethod;
import market.commerce.model.payment.Payment;
import market.commerce.service.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Tergel
 */
@Slf4j
@RestController
@RequestMapping("/v1/payment")
@RequiredArgsConstructor
public class PaymentApi extends BaseController {

    private final PaymentService paymentService;

    @PostMapping("/invoice")
    public ResponseEntity<Payment> createInvoice(
            @AuthUser AuthUserPrincipal user,
            @RequestParam String orderId,
            @RequestParam PaymentMethod method) {
        return ResponseEntity.ok(paymentService.createInvoice(orderId, method));
    }

    /**
     * Provider webhook. Unauthenticated by design — the payment is confirmed by
     * calling the provider back, never by trusting this body.
     */
    @PostMapping("/callback/{method}")
    public ResponseEntity<Void> callback(
            @PathVariable PaymentMethod method,
            @RequestBody Map<String, Object> body) {

        log.info("Payment callback {} : {}", method, body);
        paymentService.handleCallback(method, body);
        return ResponseEntity.ok().build();
    }
}
