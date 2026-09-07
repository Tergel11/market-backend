package market.commerce.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Tergel
 */
@Getter
@Setter
public class CartItemRequest {

    @NotBlank
    private String variantId;

    @Min(0)
    @Max(99)
    private int quantity = 1;
}
