package market.commerce.repository;

import market.commerce.model.promotion.CouponUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Tergel
 */
@Repository
public interface CouponUsageRepository extends MongoRepository<CouponUsage, String> {

    long countByCouponIdAndCustomerId(String couponId, String customerId);

    List<CouponUsage> findByOrderId(String orderId);

}
