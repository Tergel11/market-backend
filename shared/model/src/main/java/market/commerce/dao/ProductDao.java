package market.commerce.dao;

import lombok.RequiredArgsConstructor;
import market.commerce.model.catalog.Product;
import market.commerce.model.enums.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Catalog search. Built as a query builder rather than derived repository
 * methods because the storefront filters are all optional.
 *
 * @author Tergel
 */
@Repository
@RequiredArgsConstructor
public class ProductDao {

    private final MongoTemplate mongoTemplate;

    public long count(ProductFilter filter) {
        return mongoTemplate.count(buildQuery(filter), Product.class);
    }

    public List<Product> list(ProductFilter filter, Pageable pageable) {
        Query query = buildQuery(filter);
        if (pageable != null)
            query = query.with(pageable);
        return mongoTemplate.find(query, Product.class);
    }

    private Query buildQuery(ProductFilter filter) {
        Query query = new Query();
        if (filter == null)
            return query;

        if (!ObjectUtils.isEmpty(filter.search()))
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name.values.MN").regex(filter.search(), "i"),
                    Criteria.where("name.values.EN").regex(filter.search(), "i"),
                    Criteria.where(Product.Fields.sku).regex(filter.search(), "i")));

        // matches the category itself and everything under it
        if (!ObjectUtils.isEmpty(filter.categoryId()))
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where(Product.Fields.categoryId).is(filter.categoryId()),
                    Criteria.where(Product.Fields.categoryPath).is(filter.categoryId())));

        if (!ObjectUtils.isEmpty(filter.brandId()))
            query.addCriteria(Criteria.where(Product.Fields.brandId).is(filter.brandId()));

        if (!ObjectUtils.isEmpty(filter.merchantId()))
            query.addCriteria(Criteria.where(Product.Fields.merchantId).is(filter.merchantId()));

        if (!ObjectUtils.isEmpty(filter.tags()))
            query.addCriteria(Criteria.where(Product.Fields.tags).in(filter.tags()));

        if (filter.status() != null)
            query.addCriteria(Criteria.where(Product.Fields.status).is(filter.status()));

        if (filter.featured() != null)
            query.addCriteria(Criteria.where(Product.Fields.featured).is(filter.featured()));

        if (Boolean.TRUE.equals(filter.inStockOnly()))
            query.addCriteria(Criteria.where(Product.Fields.totalQuantity).gt(0));

        // a product matches when any of its variants falls inside the range
        if (filter.minPrice() != null)
            query.addCriteria(Criteria.where(Product.Fields.maxPrice).gte(filter.minPrice()));

        if (filter.maxPrice() != null)
            query.addCriteria(Criteria.where(Product.Fields.minPrice).lte(filter.maxPrice()));

        return query;
    }

    /**
     * Optional storefront/back-office filters; null fields are ignored.
     */
    public record ProductFilter(
            String search,
            String categoryId,
            String brandId,
            String merchantId,
            List<String> tags,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStockOnly,
            Boolean featured,
            Status status) {
    }
}
