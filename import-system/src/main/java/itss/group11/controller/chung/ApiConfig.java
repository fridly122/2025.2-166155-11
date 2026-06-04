package itss.group11.controller.chung;

import io.github.cdimascio.dotenv.Dotenv;

public final class ApiConfig {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static final String DEFAULT_SERVER_PORT = "8080";
    private static final String API_PREFIX = "/api/v1";

    private ApiConfig() {
    }

    public static String baseUrl(String apiPath) {
        String normalizedPath = normalizeApiPath(apiPath);
        String configuredBaseUrl = configValue("API_BASE_URL");

        if (configuredBaseUrl != null) {
            String normalizedBaseUrl = stripTrailingSlash(configuredBaseUrl);

            if (normalizedBaseUrl.endsWith(normalizedPath)) {
                return normalizedBaseUrl;
            }

            if (normalizedBaseUrl.endsWith(API_PREFIX) && normalizedPath.startsWith(API_PREFIX + "/")) {
                return normalizedBaseUrl + normalizedPath.substring(API_PREFIX.length());
            }

            return normalizedBaseUrl + normalizedPath;
        }

        String serverPort = configValue("SERVER_PORT");
        if (serverPort == null) {
            serverPort = DEFAULT_SERVER_PORT;
        }

        return "http://localhost:" + serverPort + normalizedPath;
    }

    private static String normalizeApiPath(String apiPath) {
        if (apiPath == null || apiPath.isBlank()) {
            throw new IllegalArgumentException("API path must not be blank.");
        }

        String normalizedPath = apiPath.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }

        return normalizedPath;
    }

    private static String configValue(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = DOTENV.get(key);
        }

        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String stripTrailingSlash(String value) {
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}

