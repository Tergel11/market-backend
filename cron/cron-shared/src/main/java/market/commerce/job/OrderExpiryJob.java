package market.commerce.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.model.order.Order;
import market.commerce.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cancels orders that were never paid and puts their reserved stock back on
 * sale. Without this, an abandoned checkout holds inventory forever.
 *
 * @author Tergel
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpiryJob {

    private final OrderService orderService;

    @Scheduled(cron = "${cron.order-expiry:0 */5 * * * *}")
    public void run() {
        List<Order> expired = orderService.findExpiredPending();
        if (expired.isEmpty())
            return;

        log.info("Expiring {} unpaid orders", expired.size());
        for (Order order : expired) {
            try {
                orderService.expire(order);
            } catch (Exception ex) {
                // one bad order must not stop the rest of the batch
                log.error("Could not expire order {}", order.getOrderNumber(), ex);
            }
        }
    }
}
