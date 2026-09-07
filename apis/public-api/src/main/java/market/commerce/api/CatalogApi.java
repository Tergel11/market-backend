package market.commerce.api;

import lombok.RequiredArgsConstructor;
import market.commerce.dao.ProductDao;
import market.commerce.dto.PageRequestDto;
import market.commerce.dto.PaginationResponse;
import market.commerce.model.catalog.Category;
import market.commerce.model.catalog.Product;
import market.commerce.model.catalog.ProductVariant;
import market.commerce.model.enums.Status;
import market.commerce.service.catalog.CategoryService;
import market.commerce.service.catalog.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public catalog browsing. No authentication required.
 *
 * @author Tergel
 */
@RestController
@RequestMapping("/v1/catalog")
@RequiredArgsConstructor
public class CatalogApi extends BaseController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> categories(
            @RequestParam(required = false) String parentId) {
        return ResponseEntity.ok(categoryService.children(parentId));
    }

    @GetMapping("/products")
    public ResponseEntity<PaginationResponse<Product>> products(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStockOnly,
            PageRequestDto pageRequest) {

        // storefront only ever sees ACTIVE products
        var filter = new ProductDao.ProductFilter(
                search, categoryId, brandId, null, null,
                minPrice, maxPrice, inStockOnly, null, Status.ACTIVE);

        return ResponseEntity.ok(productService.search(filter, pageRequest.toPageable()));
    }

    @GetMapping("/products/{slug}")
    public ResponseEntity<Product> product(@PathVariable String slug) {
        return ResponseEntity.ok(productService.findBySlug(slug));
    }

    @GetMapping("/products/{productId}/variants")
    public ResponseEntity<List<ProductVariant>> variants(@PathVariable String productId) {
        return ResponseEntity.ok(productService.variants(productId));
    }
}
