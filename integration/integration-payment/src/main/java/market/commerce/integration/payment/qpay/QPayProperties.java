package market.commerce.integration.payment.qpay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Tergel
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.qpay")
public class QPayProperties {

    private String url;
    private String username;
    private String password;
    private String invoiceCode;

    /** Where QPay posts the payment notification. */
    private String callbackUrl;
}
