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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Self-referencing category tree. {@code path} holds the ancestor ids so a whole
 * subtree can be queried with a single {@code path: categoryId} match.
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "category")
public class Category extends BaseDocument {

    @Indexed(unique = true)
    private String slug;

    private MultiLanguage name;
    private MultiLanguage description;

    @Indexed
    private String parentId;

    /** Ancestor ids from root to direct parent. */
    private List<String> path;

    private Integer level;
    private Integer sortOrder;
    private FileData image;
    private FileData icon;

    @Indexed
    private Status status;

    private Long productCount;
}
