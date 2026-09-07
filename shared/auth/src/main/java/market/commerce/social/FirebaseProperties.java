package market.commerce.social;

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
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    private boolean enabled;

    private String projectId;

    /** Path to the service account json. Supports {@code classpath:} prefixes. */
    private String credentialsPath;

    /** Raw service account json, for deployments that inject it as an env var. */
    private String credentialsJson;
}
