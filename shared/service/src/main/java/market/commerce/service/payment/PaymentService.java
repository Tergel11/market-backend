package market.commerce.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.constants.ConstantValues;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.exception.PaymentException;
import market.commerce.integration.payment.PaymentGateway;
import market.commerce.model.enums.OrderStatus;
import market.commerce.model.enums.PaymentMethod;
import market.commerce.model.enums.PaymentStatus;
import market.commerce.model.order.Order;
import market.commerce.model.payment.Payment;
import market.commerce.repository.PaymentRepository;
import market.commerce.service.order.OrderService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Drives payments through whichever {@link PaymentGateway} matches the chosen
 * method.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final List<PaymentGateway> gateways;

    public Payment createInvoice(String orderId, PaymentMethod method) {
        Order order = orderService.findById(orderId);

        if (order.getStatus() != OrderStatus.PENDING)
            throw new MessageException("order.invalid-status");

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .method(method)
                .status(PaymentStatus.NEW)
                .amount(order.getTotalAmount())
                .currency(ConstantValues.DEFAULT_CURRENCY)
                .expiresAt(Instant.now().plus(
                        ConstantValues.ORDER_PAYMENT_TIMEOUT_MINUTES, ChronoUnit.MINUTES))
                .build();

        payment = paymentRepository.save(payment);
        payment = gateway(method).createInvoice(payment);
        return paymentRepository.save(payment);
    }

    /**
     * Handles a provider webhook.
     *
     * <p>The callback body is never trusted on its own — the gateway is asked to
     * confirm the payment before the order is marked paid.
     */
    public Payment handleCallback(PaymentMethod method, Map<String, Object> body) {
        PaymentGateway gateway = gateway(method);

        String invoiceId = gateway.resolveInvoiceId(body);
        Payment payment = paymentRepository.findByProviderInvoiceId(invoiceId)
                .orElseThrow(() -> new NotFoundException("data.not-found"));

        // a repeated callback must not pay the order twice
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Ignoring duplicate callback for payment {}", payment.getId());
            return payment;
        }

        if (!gateway.verify(payment)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProviderResponse(body);
            return paymentRepository.save(payment);
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        payment.setProviderResponse(body);
        payment = paymentRepository.save(payment);

        orderService.changeStatus(
                payment.getOrderId(), OrderStatus.PAID, "payment callback", "system");

        return payment;
    }

    public List<Payment> findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> findExpiredPending() {
        return paymentRepository.findByStatusAndExpiresAtBefore(
                PaymentStatus.PENDING, Instant.now());
    }

    private PaymentGateway gateway(PaymentMethod method) {
        return gateways.stream()
                .filter(gateway -> gateway.method() == method)
                .findFirst()
                .orElseThrow(() -> new PaymentException(
                        "No gateway registered for " + method, "NO_GATEWAY"));
    }
}
