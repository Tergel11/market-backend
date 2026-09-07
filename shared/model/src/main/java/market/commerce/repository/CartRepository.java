package market.commerce.repository;

import market.commerce.model.order.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

    Optional<Cart> findByCustomerId(String customerId);

    Optional<Cart> findBySessionId(String sessionId);

    void deleteByCustomerId(String customerId);

}
