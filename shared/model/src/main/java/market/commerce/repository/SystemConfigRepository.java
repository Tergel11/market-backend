package market.commerce.repository;

import market.commerce.model.SystemConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {

    Optional<SystemConfig> findByCode(String code);
}
