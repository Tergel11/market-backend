package market.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PaginationResponse<T> of(List<T> content, long total, int page, int size) {
        return PaginationResponse.<T>builder()
                .content(content)
                .totalElements(total)
                .totalPages(size == 0 ? 0 : (int) Math.ceil((double) total / size))
                .page(page)
                .size(size)
                .build();
    }
}
