package market.commerce.repository;

import market.commerce.model.catalog.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    List<Product> findByMerchantIdAndStatus(String merchantId, Status status);

    boolean existsBySkuAndIdNot(String sku, String id);

}
