package market.commerce.repository;

import market.commerce.model.inventory.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {

    Optional<Inventory> findByVariantIdAndWarehouseId(String variantId, String warehouseId);

    List<Inventory> findByVariantIdIn(Collection<String> variantIds);

    List<Inventory> findByProductId(String productId);

}
