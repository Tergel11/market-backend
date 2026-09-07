package market.commerce.model.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.FileData;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The actually purchasable unit: one combination of the product's options with
 * its own SKU, price and barcode.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "productVariant")
public class ProductVariant extends BaseDocument {

    @Indexed
    private String productId;

    @Indexed(unique = true)
    private String sku;

    @Indexed
    private String barcode;

    /** Selected option values, e.g. {"size": "M", "color": "black"}. */
    private Map<String, String> optionValues;

    private BigDecimal price;

    /** Original price, shown struck through when a discount is active. */
    private BigDecimal comparePrice;

    /** What the merchant paid; never exposed through public-api. */
    private BigDecimal costPrice;

    private FileData image;

    /** Grams, used by the delivery integration. */
    private Integer weight;

    private Status status;
}
