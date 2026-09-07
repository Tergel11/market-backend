package market.commerce.service.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.exception.OutOfStockException;
import market.commerce.model.enums.InventoryMovementType;
import market.commerce.model.inventory.Inventory;
import market.commerce.model.inventory.InventoryMovement;
import market.commerce.repository.InventoryMovementRepository;
import market.commerce.repository.InventoryRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Stock reservation.
 *
 * <p>The flow is reserve on checkout, commit on payment, release on cancel or
 * timeout. Every mutation is a conditional atomic update: the filter itself
 * asserts there is enough stock, so a lost update is impossible without
 * needing a lock.
 *
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Holds stock for an unpaid order.
     *
     * @throws OutOfStockException when fewer than {@code quantity} units are available
     */
    public void reserve(String variantId, String warehouseId, int quantity, String orderId) {
        // the available >= quantity filter is the concurrency guard
        Query query = Query.query(Criteria
                .where(Inventory.Fields.variantId).is(variantId)
                .and(Inventory.Fields.warehouseId).is(warehouseId)
                .and(Inventory.Fields.available).gte(quantity));

        Update update = new Update()
                .inc(Inventory.Fields.reserved, quantity)
                .inc(Inventory.Fields.available, -quantity);

        Inventory updated = mongoTemplate.findAndModify(
                query, update, options(), Inventory.class);

        if (updated == null) {
            Integer available = inventoryRepository
                    .findByVariantIdAndWarehouseId(variantId, warehouseId)
                    .map(Inventory::getAvailable)
                    .orElse(0);
            throw new OutOfStockException("error.out-of-stock", variantId, available);
        }

        record(variantId, warehouseId, InventoryMovementType.RESERVE,
                -quantity, updated.getAvailable(), orderId);
    }

    /**
     * Payment succeeded: the reserved units physically leave the warehouse.
     */
    public void commit(String variantId, String warehouseId, int quantity, String orderId) {
        Query query = Query.query(Criteria
                .where(Inventory.Fields.variantId).is(variantId)
                .and(Inventory.Fields.warehouseId).is(warehouseId)
                .and(Inventory.Fields.reserved).gte(quantity));

        Update update = new Update()
                .inc(Inventory.Fields.reserved, -quantity)
                .inc(Inventory.Fields.quantity, -quantity);

        Inventory updated = mongoTemplate.findAndModify(query, update, options(), Inventory.class);
        if (updated == null) {
            log.error("Could not commit stock, variant={} order={}", variantId, orderId);
            return;
        }

        record(variantId, warehouseId, InventoryMovementType.SALE,
                -quantity, updated.getAvailable(), orderId);
    }

    /**
     * Order cancelled or payment timed out: reserved units go back on sale.
     */
    public void release(String variantId, String warehouseId, int quantity, String orderId) {
        Query query = Query.query(Criteria
                .where(Inventory.Fields.variantId).is(variantId)
                .and(Inventory.Fields.warehouseId).is(warehouseId)
                .and(Inventory.Fields.reserved).gte(quantity));

        Update update = new Update()
                .inc(Inventory.Fields.reserved, -quantity)
                .inc(Inventory.Fields.available, quantity);

        Inventory updated = mongoTemplate.findAndModify(query, update, options(), Inventory.class);
        if (updated == null) {
            log.warn("Nothing to release, variant={} order={}", variantId, orderId);
            return;
        }

        record(variantId, warehouseId, InventoryMovementType.RELEASE,
                quantity, updated.getAvailable(), orderId);
    }

    /**
     * Merchant added or corrected stock.
     */
    public void adjust(String variantId, String warehouseId, int quantity, String note) {
        Inventory inventory = inventoryRepository
                .findByVariantIdAndWarehouseId(variantId, warehouseId)
                .orElseGet(() -> Inventory.builder()
                        .variantId(variantId)
                        .warehouseId(warehouseId)
                        .quantity(0)
                        .reserved(0)
                        .available(0)
                        .trackInventory(true)
                        .build());

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setAvailable(inventory.getQuantity() - inventory.getReserved());
        inventoryRepository.save(inventory);

        record(variantId, warehouseId, InventoryMovementType.ADJUSTMENT,
                quantity, inventory.getAvailable(), note);
    }

    public List<Inventory> findByVariantIds(List<String> variantIds) {
        return inventoryRepository.findByVariantIdIn(variantIds);
    }

    private org.springframework.data.mongodb.core.FindAndModifyOptions options() {
        return org.springframework.data.mongodb.core.FindAndModifyOptions.options().returnNew(true);
    }

    private void record(
            String variantId,
            String warehouseId,
            InventoryMovementType type,
            int quantity,
            Integer quantityAfter,
            String referenceId) {

        inventoryMovementRepository.save(InventoryMovement.builder()
                .variantId(variantId)
                .warehouseId(warehouseId)
                .type(type)
                .quantity(quantity)
                .quantityAfter(quantityAfter)
                .referenceId(referenceId)
                .build());
    }
}
