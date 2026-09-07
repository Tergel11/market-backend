package market.commerce.repository;

import market.commerce.model.user.Merchant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface MerchantRepository extends MongoRepository<Merchant, String> {

    Optional<Merchant> findBySlug(String slug);

    List<Merchant> findByStatus(Status status);

}
