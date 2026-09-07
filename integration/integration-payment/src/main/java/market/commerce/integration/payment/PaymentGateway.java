package market.commerce.integration.payment;

import market.commerce.model.enums.PaymentMethod;
import market.commerce.model.payment.Payment;

import java.util.Map;

/**
 * One implementation per provider. Adding a provider means adding a bean, not
 * touching the checkout service.
 *
 * @author Tergel
 */
public interface PaymentGateway {

    PaymentMethod method();

    /**
     * Creates the provider-side invoice and fills the qr / url fields on the
     * given payment.
     */
    Payment createInvoice(Payment payment);

    /**
     * Asks the provider whether the invoice was actually paid. Always call this
     * before marking an order paid — a callback alone is not proof.
     */
    boolean verify(Payment payment);

    void refund(Payment payment, java.math.BigDecimal amount);

    /**
     * Parses a provider webhook body into the provider invoice id.
     */
    String resolveInvoiceId(Map<String, Object> callbackBody);
}
