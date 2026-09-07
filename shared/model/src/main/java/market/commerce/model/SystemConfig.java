package market.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Runtime-editable settings (shipping fee, tax rate, order expiry, ...).
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "systemConfig")
public class SystemConfig extends BaseDocument {

    @Indexed(unique = true)
    private String code;

    private String value;
    private String description;
    private Boolean editable;
}
