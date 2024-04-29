package dobackend.com.springtestcontainers;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "spring.datasource")
public record DatasourceConfig(String url, String username, String password, Hikari hikari) {
    public record Hikari(long connectionTimeout, int maximumPoolSize) {}
}

