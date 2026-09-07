package market.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Uploaded file reference (S3 key + CDN url).
 *
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileData implements Serializable {

    private String key;
    private String url;
    private String name;
    private String contentType;
    private Long size;
    private Integer sortOrder;
}
