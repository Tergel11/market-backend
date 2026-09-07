package market.commerce.model.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import market.commerce.model.MultiLanguage;

import java.io.Serializable;
import java.util.List;

/**
 * Variant axis, e.g. name = "Size", values = [S, M, L].
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption implements Serializable {

    private String code;
    private MultiLanguage name;
    private List<String> values;
    private Integer sortOrder;
}
