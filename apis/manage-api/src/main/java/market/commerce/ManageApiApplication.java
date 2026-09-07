package market.commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ManageApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageApiApplication.class, args);
    }
}
