package dobackend.com.springtestcontainers;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties({DatasourceConfig.class})
public class AccountConfiguration {

    private final DatasourceConfig datasourceConfig;

    public AccountConfiguration(DatasourceConfig postgresConfig) {
        this.datasourceConfig = postgresConfig;
    }

    // When testing we want the TestContainers datasource to be auto-injected with @ServiceConnection
    @Profile("!test")
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(datasourceConfig.url());
        dataSource.setUsername(datasourceConfig.username());
        dataSource.setPassword(datasourceConfig.password());
        dataSource.setConnectionTimeout(datasourceConfig.hikari().connectionTimeout());
        dataSource.setMaximumPoolSize(datasourceConfig.hikari().maximumPoolSize());

        return dataSource;
    }
}
