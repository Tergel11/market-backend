package market.commerce.repository;

import market.commerce.model.catalog.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface BrandRepository extends MongoRepository<Brand, String> {

    Optional<Brand> findBySlug(String slug);

    List<Brand> findByStatus(Status status);

}
