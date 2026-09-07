package market.commerce.repository;

import market.commerce.model.customer.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {

    Optional<Wishlist> findByCustomerId(String customerId);

}
