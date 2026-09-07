package market.commerce.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.model.payment.Payment;
import market.commerce.service.payment.PaymentService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Catches payments the provider took but whose callback never arrived, by
 * re-checking every pending invoice that has passed its expiry.
 *
 * @author Tergel
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconcileJob {

    private final PaymentService paymentService;

    @Scheduled(cron = "${cron.payment-reconcile:0 */10 * * * *}")
    public void run() {
        List<Payment> pending = paymentService.findExpiredPending();
        if (pending.isEmpty())
            return;

        log.info("Reconciling {} pending payments", pending.size());
        for (Payment payment : pending) {
            try {
                // TODO ask the gateway whether this invoice was paid and settle
                //      the order if it was; otherwise mark the payment expired.
                log.info("Pending payment {} for order {}",
                        payment.getId(), payment.getOrderId());
            } catch (Exception ex) {
                log.error("Could not reconcile payment {}", payment.getId(), ex);
            }
        }
    }
}
