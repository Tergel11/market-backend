package market.commerce.service.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import market.commerce.LocalStringUtil;
import market.commerce.dao.ProductDao;
import market.commerce.dto.PaginationResponse;
import market.commerce.exception.MessageException;
import market.commerce.exception.NotFoundException;
import market.commerce.model.catalog.Category;
import market.commerce.model.catalog.Product;
import market.commerce.model.catalog.ProductVariant;
import market.commerce.model.enums.Status;
import market.commerce.model.inventory.Inventory;
import market.commerce.repository.ProductRepository;
import market.commerce.repository.ProductVariantRepository;
import market.commerce.service.inventory.InventoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * @author Tergel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductDao productDao;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;

    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("data.not-found"));
    }

    public Product findBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("data.not-found"));
    }

    public List<ProductVariant> variants(String productId) {
        return productVariantRepository.findByProductId(productId);
    }

    public PaginationResponse<Product> search(ProductDao.ProductFilter filter, Pageable pageable) {
        long total = productDao.count(filter);
        List<Product> content = productDao.list(filter, pageable);
        return PaginationResponse.of(
                content, total, pageable.getPageNumber(), pageable.getPageSize());
    }

    public Product save(Product product) {
        if (!ObjectUtils.isEmpty(product.getSku())
                && productRepository.existsBySkuAndIdNot(
                        product.getSku(), product.getId() == null ? "" : product.getId()))
            throw new MessageException("data.exists");

        if (LocalStringUtil.isEmpty(product.getSlug()))
            product.setSlug(LocalStringUtil.toSlug(product.getName().get(
                    market.commerce.model.enums.Locale.EN)));

        // denormalize the category ancestors so listing needs one query
        if (!LocalStringUtil.isEmpty(product.getCategoryId())) {
            Category category = categoryService.findById(product.getCategoryId());
            product.setCategoryPath(category.getPath());
        }

        Product saved = productRepository.save(product);
        refreshAggregates(saved.getId());
        return saved;
    }

    /**
     * Recomputes the price range and stock total kept on the product for fast
     * listing. Call after any variant or stock change.
     */
    public void refreshAggregates(String productId) {
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        if (variants.isEmpty())
            return;

        List<BigDecimal> prices = variants.stream()
                .filter(variant -> variant.getStatus() != Status.ARCHIVED)
                .map(ProductVariant::getPrice)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .toList();

        int totalQuantity = inventoryService
                .findByVariantIds(variants.stream().map(ProductVariant::getId).toList())
                .stream()
                .map(Inventory::getAvailable)
                .filter(java.util.Objects::nonNull)
                .reduce(0, Integer::sum);

        Product product = findById(productId);
        if (!prices.isEmpty()) {
            product.setMinPrice(prices.get(0));
            product.setMaxPrice(prices.get(prices.size() - 1));
        }
        product.setTotalQuantity(totalQuantity);
        productRepository.save(product);
    }
}
