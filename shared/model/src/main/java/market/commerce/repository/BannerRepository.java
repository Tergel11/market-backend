package market.commerce.repository;

import market.commerce.model.promotion.Banner;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.Status;

import java.util.List;

/**
 * @author Tergel
 */
@Repository
public interface BannerRepository extends MongoRepository<Banner, String> {

    List<Banner> findByPositionAndStatusOrderBySortOrderAsc(String position, Status status);

}
