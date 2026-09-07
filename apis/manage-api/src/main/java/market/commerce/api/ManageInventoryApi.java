package market.commerce.api;

import lombok.RequiredArgsConstructor;
import market.commerce.constants.ConstantValues;
import market.commerce.model.inventory.Inventory;
import market.commerce.service.catalog.ProductService;
import market.commerce.service.inventory.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Tergel
 */
@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
@Secured({"ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_MERCHANT"})
public class ManageInventoryApi extends BaseController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Inventory>> list(@RequestParam List<String> variantIds) {
        return ResponseEntity.ok(inventoryService.findByVariantIds(variantIds));
    }

    /**
     * Adds or corrects stock. Pass a negative quantity to write stock down.
     */
    @PostMapping("/adjust")
    public ResponseEntity<Void> adjust(
            @RequestParam String variantId,
            @RequestParam String productId,
            @RequestParam int quantity,
            @RequestParam(required = false, defaultValue = ConstantValues.DEFAULT_WAREHOUSE_CODE)
            String warehouseId,
            @RequestParam(required = false) String note) {

        inventoryService.adjust(variantId, warehouseId, quantity, note);
        productService.refreshAggregates(productId);
        return ResponseEntity.ok().build();
    }
}
