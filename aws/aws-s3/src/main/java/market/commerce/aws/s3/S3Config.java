package market.commerce.aws.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * @author Tergel
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("!'${aws.access-key:}'.isEmpty()")
public class S3Config {

    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
