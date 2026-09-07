package market.commerce.integration.payment.qpay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.exception.PaymentException;
import market.commerce.integration.payment.PaymentGateway;
import market.commerce.model.enums.PaymentMethod;
import market.commerce.model.enums.PaymentStatus;
import market.commerce.model.payment.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * QPay invoice + callback flow.
 *
 * <p>Skeleton only: the http calls to QPay still need to be implemented against
 * their v2 api, along with token caching.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QPayGateway implements PaymentGateway {

    private final QPayProperties qPayProperties;

    @Override
    public PaymentMethod method() {
        return PaymentMethod.QPAY;
    }

    @Override
    public Payment createInvoice(Payment payment) {
        log.info("Creating qpay invoice for order {} amount {}",
                payment.getOrderId(), payment.getAmount());

        // TODO POST {qpay.url}/invoice with invoiceCode, senderInvoiceNo = orderId,
        //      callbackUrl = qPayProperties.getCallbackUrl() + payment.getId()
        //      then set providerInvoiceId / qrText / qrImage / paymentUrl from the response.
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    @Override
    public boolean verify(Payment payment) {
        log.info("Verifying qpay payment {}", payment.getProviderInvoiceId());

        // TODO POST {qpay.url}/payment/check and return true when a paid row comes back.
        throw new PaymentException("QPay verify is not implemented yet", "NOT_IMPLEMENTED");
    }

    @Override
    public void refund(Payment payment, BigDecimal amount) {
        // TODO DELETE {qpay.url}/payment/refund/{providerTransactionId}
        throw new PaymentException("QPay refund is not implemented yet", "NOT_IMPLEMENTED");
    }

    @Override
    public String resolveInvoiceId(Map<String, Object> callbackBody) {
        Object invoiceId = callbackBody.get("object_id");
        return invoiceId == null ? null : invoiceId.toString();
    }
}
