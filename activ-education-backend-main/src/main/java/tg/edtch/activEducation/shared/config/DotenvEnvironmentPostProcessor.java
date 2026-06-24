package tg.edtch.activEducation.shared.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";
    private static final String ENV_FILE_PROPERTY = "springdotenv.filename";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String filename = environment.getProperty(ENV_FILE_PROPERTY, ".env");
        boolean ignoreIfMissing = environment.getProperty("springdotenv.ignore-if-missing", Boolean.class, true);
        try {
            Dotenv dotenv = Dotenv.configure()
                    .filename(filename)
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> properties = new HashMap<>();
            dotenv.entries().forEach(e -> properties.put(e.getKey(), e.getValue()));

            if (!properties.isEmpty()) {
                environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
            }
        } catch (Exception e) {
            if (!ignoreIfMissing) {
                throw new IllegalStateException("Failed to load " + filename, e);
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}