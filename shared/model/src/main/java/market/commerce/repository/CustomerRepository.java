package market.commerce.repository;

import market.commerce.model.customer.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

}
