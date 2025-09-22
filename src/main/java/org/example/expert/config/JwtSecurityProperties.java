package org.example.expert.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JwtSecurityProperties {

    private Secret secret;

    @Getter
    @AllArgsConstructor
    public static class Secret {
        private String key;
        private List<String> whiteList;
    }
}
