package dobackend.com.springtestcontainers.user;

import com.zaxxer.hikari.HikariDataSource;
import dobackend.com.springtestcontainers.DatasourceProperties;
import dobackend.com.springtestcontainers.user.event.UserEventTableName;
import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties({DatasourceProperties.class})
public class UserConfiguration {

    private final DatasourceProperties datasourceProperties;

    public UserConfiguration(DatasourceProperties postgresConfig) {
        this.datasourceProperties = postgresConfig;
    }

    // When testing we want the TestContainers datasource to be auto-injected with @ServiceConnection
    @Profile("!test")
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(datasourceProperties.url());
        dataSource.setUsername(datasourceProperties.username());
        dataSource.setPassword(datasourceProperties.password());
        dataSource.setConnectionTimeout(datasourceProperties.hikari().connectionTimeout());
        dataSource.setMaximumPoolSize(datasourceProperties.hikari().maximumPoolSize());

        return dataSource;
    }


    @Bean
    public DynamoDbTableNameResolver dynamoDbTableNameResolver() {
        return new UserEventTableName();
    }
}
