package market.commerce.repository;

import market.commerce.model.payment.Refund;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Tergel
 */
@Repository
public interface RefundRepository extends MongoRepository<Refund, String> {

    List<Refund> findByOrderId(String orderId);

}
