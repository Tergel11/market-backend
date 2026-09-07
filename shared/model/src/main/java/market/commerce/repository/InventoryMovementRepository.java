package market.commerce.repository;

import market.commerce.model.inventory.InventoryMovement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Tergel
 */
@Repository
public interface InventoryMovementRepository extends MongoRepository<InventoryMovement, String> {

    List<InventoryMovement> findByVariantIdOrderByCreatedAtDesc(String variantId);

    List<InventoryMovement> findByReferenceId(String referenceId);

}
