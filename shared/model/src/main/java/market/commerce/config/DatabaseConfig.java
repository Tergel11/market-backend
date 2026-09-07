package market.commerce.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.UuidRepresentation;
import org.bson.types.Decimal128;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.math.BigDecimal;
import java.util.List;

/**
 * Primary Mongo connection.
 *
 * <p>Money is stored as Decimal128, never as a double, so order totals stay exact.
 * A transaction manager is exposed because checkout writes order, inventory and
 * payment together.
 *
 * @author Tergel
 */
@Slf4j
@Configuration
@EnableMongoAuditing
public class DatabaseConfig extends AbstractMongoClientConfiguration {

    @Value("${primary.mongodb.uri}")
    private String primaryUri;

    @Value("${primary.mongodb.dbName}")
    private String dbName;

    @Override
    @NonNull
    protected String getDatabaseName() {
        return dbName;
    }

    @Primary
    @Bean
    @Override
    @NonNull
    public MongoClient mongoClient() {
        log.info("Configuring mongo client, database: {}", dbName);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(primaryUri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();

        return MongoClients.create(settings);
    }

    @Primary
    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, getDatabaseName());
    }

    /**
     * Checkout spans several collections, so it needs a real transaction.
     * Requires a replica set — a standalone mongod will reject this.
     */
    @Primary
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(
                dbFactory,
                TransactionOptions.builder()
                        .readConcern(ReadConcern.MAJORITY)
                        .writeConcern(WriteConcern.MAJORITY)
                        .build());
    }

    @Primary
    @Bean
    @Override
    @NonNull
    public MappingMongoConverter mappingMongoConverter(
            @NonNull MongoDatabaseFactory databaseFactory,
            @NonNull MongoCustomConversions customConversions,
            @NonNull MongoMappingContext mappingContext) {

        MappingMongoConverter converter =
                super.mappingMongoConverter(databaseFactory, customConversions, mappingContext);
        converter.setCustomConversions(mongoCustomConversions());
        converter.setMapKeyDotReplacement(".");
        return converter;
    }

    @Primary
    @Bean
    @NonNull
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new BigDecimalDecimal128Converter(),
                new Decimal128BigDecimalConverter()));
    }

    @WritingConverter
    private static class BigDecimalDecimal128Converter implements Converter<BigDecimal, Decimal128> {
        @Override
        public Decimal128 convert(@NonNull BigDecimal source) {
            return new Decimal128(source);
        }
    }

    @ReadingConverter
    private static class Decimal128BigDecimalConverter implements Converter<Decimal128, BigDecimal> {
        @Override
        public BigDecimal convert(@NonNull Decimal128 source) {
            return source.bigDecimalValue();
        }
    }
}
