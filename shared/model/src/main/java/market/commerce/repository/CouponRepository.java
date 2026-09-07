package market.commerce.repository;

import market.commerce.model.promotion.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByStatus(Status status);

}
