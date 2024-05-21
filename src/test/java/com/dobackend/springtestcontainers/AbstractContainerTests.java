package com.dobackend.springtestcontainers;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service;

@Testcontainers
public abstract class AbstractContainerTests {

    private static Network network = Network.newNetwork();

    @Container
    // This should negate the need for dynamic property source, but doesn't seem
    // to work if you have a datasource bean configured in your code. To fix Using a profile to exclude main datasource
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest").withNetwork(network);


    @Container
    protected static final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(Service.DYNAMODB, Service.SQS, Service.LAMBDA, Service.SNS)
            .withReuse(false)
            .withEnv(Map.of("DATA_DIR", "/tmp/localstack/data", "LAMBDA_DOCKER_NETWORK", ((Network.NetworkImpl)network).getName()))
            .withNetwork(network);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "aws.dynamodb.endpoint",
                () -> localStackContainer.getEndpointOverride(Service.DYNAMODB).toString()
        );
        registry.add(
                "aws.dynamodb.region",
                () -> localStackContainer.getEndpointOverride(localStackContainer::getRegion).toString()
        );
    }
    // @ServiceConnection has replaced the following:
//    @DynamicPropertySource
//    static void properties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
//        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
//    }


}
