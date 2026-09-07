package market.commerce.repository;

import market.commerce.model.inventory.Warehouse;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface WarehouseRepository extends MongoRepository<Warehouse, String> {

    Optional<Warehouse> findByCode(String code);

    List<Warehouse> findByMerchantId(String merchantId);

}
