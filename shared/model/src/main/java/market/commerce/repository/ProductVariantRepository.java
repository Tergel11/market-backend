package market.commerce.repository;

import market.commerce.model.catalog.ProductVariant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {

    List<ProductVariant> findByProductId(String productId);

    List<ProductVariant> findByIdIn(Collection<String> ids);

    Optional<ProductVariant> findBySku(String sku);

}
