package net.roodt.springtestcontainers.authz;

import net.roodt.springtestcontainers.DatasourceProperties;
import net.roodt.springtestcontainers.authz.user.event.UserEventTableName;
import com.zaxxer.hikari.HikariDataSource;
import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver;
import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementOrdering;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties({DatasourceProperties.class})
@Import(SqsBootstrapConfiguration.class)
public class AuthzConfiguration {

    private final DatasourceProperties datasourceProperties;

    public AuthzConfiguration(DatasourceProperties postgresConfig) {
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

    @Bean
    SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory
                .builder()
                .configure(options -> options
                        .acknowledgementMode(AcknowledgementMode.MANUAL)
                        .acknowledgementInterval(Duration.ofSeconds(3))
                        .acknowledgementThreshold(5)
                        .acknowledgementOrdering(AcknowledgementOrdering.ORDERED)
                )
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
}
