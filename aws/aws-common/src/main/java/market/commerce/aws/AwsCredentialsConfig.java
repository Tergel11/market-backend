package market.commerce.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

/**
 * Only active when credentials are actually configured, so a local run without
 * AWS keys still starts.
 *
 * @author Tergel
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("!'${aws.access-key:}'.isEmpty()")
public class AwsCredentialsConfig {

    private final AwsProperties awsProperties;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        awsProperties.getAccessKey(),
                        awsProperties.getSecretKey()));
    }

    @Bean
    public Region awsRegion() {
        return Region.of(awsProperties.getRegion());
    }
}
