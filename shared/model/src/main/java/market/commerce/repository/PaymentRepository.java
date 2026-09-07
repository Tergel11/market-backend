package market.commerce.repository;

import market.commerce.model.payment.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import market.commerce.model.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * @author Tergel
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByOrderId(String orderId);

    Optional<Payment> findByProviderInvoiceId(String providerInvoiceId);

    List<Payment> findByStatusAndExpiresAtBefore(PaymentStatus status, Instant before);

}
