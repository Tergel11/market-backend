package market.commerce.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.model.inventory.Inventory;
import market.commerce.repository.InventoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Daily low-stock report for merchants.
 *
 * @author Tergel
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockAlertJob {

    private final InventoryRepository inventoryRepository;

    @Scheduled(cron = "${cron.low-stock:0 0 8 * * *}")
    public void run() {
        List<Inventory> lowStock = inventoryRepository.findAll().stream()
                .filter(inventory -> inventory.getLowStockThreshold() != null
                        && inventory.getAvailable() != null
                        && inventory.getAvailable() <= inventory.getLowStockThreshold())
                .toList();

        if (lowStock.isEmpty())
            return;

        // TODO notify each merchant instead of only logging
        log.warn("{} variants are at or below their low stock threshold", lowStock.size());
    }
}
