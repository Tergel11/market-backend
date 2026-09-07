package market.commerce.repository;

import market.commerce.model.catalog.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIdOrderBySortOrderAsc(String parentId);

    List<Category> findByPathContainingAndStatus(String categoryId, Status status);

}
