package market.commerce.model.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import market.commerce.model.BaseDocument;
import market.commerce.model.FileData;
import market.commerce.model.enums.Status;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "review")
@CompoundIndex(name = "product_status_idx", def = "{'productId': 1, 'status': 1}")
public class Review extends BaseDocument {

    @Indexed
    private String productId;

    @Indexed
    private String customerId;

    /** Set when the review comes from a delivered order. */
    private String orderId;

    private Integer rating;
    private String comment;
    private List<FileData> images;

    private Boolean verifiedPurchase;
    private Long helpfulCount;

    private Status status;
}
