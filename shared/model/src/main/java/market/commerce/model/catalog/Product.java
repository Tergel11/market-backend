package market.commerce.model.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.FileData;
import market.commerce.model.MultiLanguage;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A sellable product. Purchasable stock lives on {@link ProductVariant}; the
 * price range here is denormalized from the variants for listing queries.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "product")
@CompoundIndex(name = "status_category_idx", def = "{'status': 1, 'categoryId': 1}")
@CompoundIndex(name = "merchant_status_idx", def = "{'merchantId': 1, 'status': 1}")
public class Product extends BaseDocument {

    @Indexed(unique = true)
    private String slug;

    @Indexed(unique = true)
    private String sku;

    @TextIndexed
    private MultiLanguage name;

    private MultiLanguage shortDescription;
    private MultiLanguage description;

    @Indexed
    private String categoryId;

    /** Ancestor category ids, so a subtree listing needs one query. */
    private List<String> categoryPath;

    @Indexed
    private String brandId;

    @Indexed
    private String merchantId;

    private List<FileData> images;

    /** Option names offered by this product, e.g. Size, Color. */
    private List<ProductOption> options;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    /** Sum of the variants' available quantity, refreshed on stock change. */
    private Integer totalQuantity;

    private List<String> tags;

    @Indexed
    private Status status;

    private Boolean featured;
    private Instant publishedAt;

    private Double ratingAverage;
    private Long ratingCount;
    private Long soldCount;
    private Long viewCount;
}
