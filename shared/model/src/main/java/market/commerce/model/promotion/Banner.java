package market.commerce.model.promotion;

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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Storefront promotional slot (home slider, category header, ...).
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "banner")
public class Banner extends BaseDocument {

    private MultiLanguage title;

    /** Placement key, e.g. HOME_SLIDER. */
    @Indexed
    private String position;

    /** Image per locale, so localized artwork can be uploaded. */
    private Map<String, FileData> images;

    private String linkUrl;
    private Integer sortOrder;

    private Instant startsAt;
    private Instant endsAt;

    @Indexed
    private Status status;
}
