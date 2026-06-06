package itss.group11;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication(scanBasePackages = "itss.group11")
public class ImportSystemBackendApplication {

    public static void main(String[] args) {
        run(args);
    }

    public static ConfigurableApplicationContext run(String... args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        return new SpringApplicationBuilder(ImportSystemBackendApplication.class)
                .headless(false)
                .properties(buildSpringProperties(dotenv))
                .run(args);
    }

    private static Map<String, Object> buildSpringProperties(Dotenv dotenv) {
        Map<String, Object> properties = new HashMap<>();

        properties.put("server.port", optionalValue(dotenv, "SERVER_PORT", "8080"));
        properties.put("spring.datasource.hikari.maximum-pool-size", optionalValue(dotenv, "DB_MAX_POOL_SIZE", "3"));
        properties.put("spring.datasource.hikari.minimum-idle", optionalValue(dotenv, "DB_MIN_IDLE", "0"));
        properties.put("spring.datasource.hikari.connection-timeout",
                optionalValue(dotenv, "DB_CONNECTION_TIMEOUT_MS", "5000"));
        properties.put("spring.jpa.open-in-view", "true");
        properties.put("spring.jpa.show-sql", "false");

        String dbUrl = optionalValue(dotenv, "DB_URL", null);
        if (dbUrl == null || dbUrl.isBlank()) {
            configureLocalDatabase(properties);
        } else {
            configurePostgresDatabase(dotenv, properties, dbUrl);
        }

        return properties;
    }

    private static void configurePostgresDatabase(Dotenv dotenv, Map<String, Object> properties, String dbUrl) {
        properties.put("spring.datasource.url", dbUrl);
        properties.put("spring.datasource.username", requiredValue(dotenv, "DB_USER"));
        properties.put("spring.datasource.password", requiredValue(dotenv, "DB_PASSWORD"));
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        properties.put("spring.jpa.hibernate.ddl-auto", optionalValue(dotenv, "DB_DDL_AUTO", "none"));
    }

    private static void configureLocalDatabase(Map<String, Object> properties) {
        properties.put("spring.datasource.url",
                "jdbc:h2:file:./data/import-system-local;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE");
        properties.put("spring.datasource.username", "sa");
        properties.put("spring.datasource.password", "");
        properties.put("spring.datasource.driver-class-name", "org.h2.Driver");
        properties.put("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
        properties.put("spring.jpa.hibernate.ddl-auto", "update");
    }

    private static String requiredValue(Dotenv dotenv, String key) {
        String value = optionalValue(dotenv, key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration: " + key);
        }
        return value;
    }

    private static String optionalValue(Dotenv dotenv, String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
