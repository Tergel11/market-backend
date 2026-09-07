package market.commerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author Tergel
 */
@Configuration
@EnableMongoRepositories(
        basePackages = "market.commerce.repository",
        mongoTemplateRef = "mongoTemplate"
)
public class MongoConfig {
}
