package market.commerce.social;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Initialises the Firebase Admin SDK.
 *
 * <p>Disabled by default so a local run needs no service account. When it is
 * off, {@code SocialAuthService} reports that social sign-in is unavailable
 * instead of the whole context failing to start.
 *
 * @author Tergel
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // reuse the app if something already initialised it (tests, hot reload)
        if (!FirebaseApp.getApps().isEmpty())
            return FirebaseApp.getInstance();

        FirebaseOptions.Builder builder = FirebaseOptions.builder()
                .setCredentials(credentials());

        if (!ObjectUtils.isEmpty(firebaseProperties.getProjectId()))
            builder.setProjectId(firebaseProperties.getProjectId());

        FirebaseApp app = FirebaseApp.initializeApp(builder.build());
        log.info("Initialised firebase app, projectId: {}", firebaseProperties.getProjectId());
        return app;
    }

    /**
     * Raw json wins over a path, so a container can inject the whole service
     * account as an environment variable without mounting a file.
     */
    private GoogleCredentials credentials() throws IOException {
        if (!ObjectUtils.isEmpty(firebaseProperties.getCredentialsJson())) {
            log.info("Loading firebase credentials from configured json");
            try (InputStream stream = new ByteArrayInputStream(
                    firebaseProperties.getCredentialsJson().getBytes(StandardCharsets.UTF_8))) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        if (!ObjectUtils.isEmpty(firebaseProperties.getCredentialsPath())) {
            log.info("Loading firebase credentials from {}", firebaseProperties.getCredentialsPath());
            try (InputStream stream = new DefaultResourceLoader()
                    .getResource(firebaseProperties.getCredentialsPath())
                    .getInputStream()) {
                return GoogleCredentials.fromStream(stream);
            }
        }

        // GOOGLE_APPLICATION_CREDENTIALS, or the metadata server on GCP
        log.info("Loading firebase application default credentials");
        return GoogleCredentials.getApplicationDefault();
    }
}
