package market.commerce.repository;

import market.commerce.model.order.Shipment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.ShipmentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface ShipmentRepository extends MongoRepository<Shipment, String> {

    List<Shipment> findByOrderId(String orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatusIn(Collection<ShipmentStatus> statuses);

}
