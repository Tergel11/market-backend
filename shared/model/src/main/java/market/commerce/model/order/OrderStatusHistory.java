package market.commerce.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import market.commerce.model.enums.OrderStatus;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author Tergel
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory implements Serializable {

    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String note;
    private String changedBy;
    private Instant changedAt;
}
