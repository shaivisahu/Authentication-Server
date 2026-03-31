package com.authforge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Getter @Setter
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private OAuth2 oauth2 = new OAuth2();

    @Getter @Setter
    public static class Jwt {
        private String accessSecret;
        private String refreshSecret;
        private long accessExpirationMs = 900_000L;      // 15 min
        private long refreshExpirationMs = 604_800_000L; // 7 days
    }

    @Getter @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }

    @Getter @Setter
    public static class OAuth2 {
        private List<String> authorizedRedirectUris = List.of("http://localhost:5173/oauth2/redirect");
    }
}
