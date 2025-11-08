package com.achobeta.themis.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "meilisearch", ignoreInvalidFields = true)
public class MeiliSearchConfigProperties {
    private String host;
    private String apiKey;
}
