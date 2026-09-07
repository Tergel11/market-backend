package market.commerce.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import market.commerce.annotations.AuthUser;
import market.commerce.dao.ProductDao;
import market.commerce.dto.AuthUserPrincipal;
import market.commerce.dto.PageRequestDto;
import market.commerce.dto.PaginationResponse;
import market.commerce.model.catalog.Product;
import market.commerce.model.enums.ApplicationRole;
import market.commerce.model.enums.Status;
import market.commerce.service.catalog.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tergel
 */
@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
@Secured({"ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_MERCHANT"})
public class ManageProductApi extends BaseController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PaginationResponse<Product>> list(
            @AuthUser AuthUserPrincipal user,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Status status,
            PageRequestDto pageRequest) {

        // merchant staff only ever see their own catalog
        String merchantId = user.hasRole(ApplicationRole.ROLE_MERCHANT)
                ? user.getMerchantId() : null;

        var filter = new ProductDao.ProductFilter(
                search, categoryId, null, merchantId, null, null, null, null, null, status);

        return ResponseEntity.ok(productService.search(filter, pageRequest.toPageable()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> detail(@PathVariable String id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Product> create(
            @AuthUser AuthUserPrincipal user,
            @Valid @RequestBody Product product) {

        if (user.hasRole(ApplicationRole.ROLE_MERCHANT))
            product.setMerchantId(user.getMerchantId());

        product.setId(null);
        return ResponseEntity.ok(productService.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @AuthUser AuthUserPrincipal user,
            @PathVariable String id,
            @Valid @RequestBody Product product) {

        Product existing = productService.findById(id);

        if (user.hasRole(ApplicationRole.ROLE_MERCHANT)
                && !existing.getMerchantId().equals(user.getMerchantId()))
            return errorPermission();

        product.setId(id);
        product.setMerchantId(existing.getMerchantId());
        return ResponseEntity.ok(productService.save(product));
    }
}
