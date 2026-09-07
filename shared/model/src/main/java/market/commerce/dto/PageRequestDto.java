package market.commerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

/**
 * Shared query params for every paged endpoint.
 *
 * @author Tergel
 */
@Getter
@Setter
public class PageRequestDto {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;

    private String sortBy;
    private Sort.Direction direction = Sort.Direction.DESC;

    public Pageable toPageable() {
        String field = ObjectUtils.isEmpty(sortBy) ? "createdAt" : sortBy;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}
