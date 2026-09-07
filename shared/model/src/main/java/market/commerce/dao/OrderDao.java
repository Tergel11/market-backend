package market.commerce.dao;

import lombok.RequiredArgsConstructor;
import market.commerce.model.enums.OrderStatus;
import market.commerce.model.enums.PaymentStatus;
import market.commerce.model.order.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.List;

/**
 * @author Tergel
 */
@Repository
@RequiredArgsConstructor
public class OrderDao {

    private final MongoTemplate mongoTemplate;

    public long count(OrderFilter filter) {
        return mongoTemplate.count(buildQuery(filter), Order.class);
    }

    public List<Order> list(OrderFilter filter, Pageable pageable) {
        Query query = buildQuery(filter);
        if (pageable != null)
            query = query.with(pageable);
        return mongoTemplate.find(query, Order.class);
    }

    private Query buildQuery(OrderFilter filter) {
        Query query = new Query();
        if (filter == null)
            return query;

        if (!ObjectUtils.isEmpty(filter.orderNumber()))
            query.addCriteria(Criteria.where(Order.Fields.orderNumber).regex(filter.orderNumber(), "i"));

        if (!ObjectUtils.isEmpty(filter.customerId()))
            query.addCriteria(Criteria.where(Order.Fields.customerId).is(filter.customerId()));

        if (!ObjectUtils.isEmpty(filter.customerPhone()))
            query.addCriteria(Criteria.where(Order.Fields.customerPhone).is(filter.customerPhone()));

        // items is an embedded array; dot notation matches any element
        if (!ObjectUtils.isEmpty(filter.merchantId()))
            query.addCriteria(Criteria.where("items.merchantId").is(filter.merchantId()));

        if (!ObjectUtils.isEmpty(filter.statuses()))
            query.addCriteria(Criteria.where(Order.Fields.status).in(filter.statuses()));

        if (filter.paymentStatus() != null)
            query.addCriteria(Criteria.where(Order.Fields.paymentStatus).is(filter.paymentStatus()));

        if (filter.startDate() != null && filter.endDate() != null)
            query.addCriteria(Criteria.where("createdAt").gte(filter.startDate()).lte(filter.endDate()));
        else if (filter.startDate() != null)
            query.addCriteria(Criteria.where("createdAt").gte(filter.startDate()));
        else if (filter.endDate() != null)
            query.addCriteria(Criteria.where("createdAt").lte(filter.endDate()));

        return query;
    }

    public record OrderFilter(
            String orderNumber,
            String customerId,
            String customerPhone,
            String merchantId,
            List<OrderStatus> statuses,
            PaymentStatus paymentStatus,
            Instant startDate,
            Instant endDate) {
    }
}
