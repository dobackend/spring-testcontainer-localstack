package com.dobackend.springtestcontainers;

import com.dobackend.springtestcontainers.authz.user.event.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.StreamViewType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service;

@Testcontainers
public abstract class AbstractContainerTests {

    @Autowired
    protected DynamoDbClient dynamoDbClient;

    @Autowired
    protected DynamoDbEnhancedClient dynamoDbEnhancedClient;

    private static Network network = Network.newNetwork();

    @Container
    // This should negate the need for dynamic property source, but doesn't seem
    // to work if you have a datasource bean configured in your code. To fix Using a profile to exclude main datasource
    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest").withNetwork(network);

    @Container
    protected static final LocalStackContainer localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(Service.DYNAMODB, Service.SQS, Service.LAMBDA, Service.SNS, Service.S3)
            .withEnv("LAMBDA_DOCKER_NETWORK", ((Network.NetworkImpl) Network.SHARED).getName())
            .withReuse(false);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.dynamodb.endpoint", localStackContainer::getEndpoint);
        registry.add("aws.dynamodb.region", localStackContainer::getRegion);
        registry.add("aws.sns.endpoint", localStackContainer::getEndpoint);
        registry.add("aws.s3.endpoint", localStackContainer::getEndpoint);
        registry.add("aws.sqs.endpoint", localStackContainer::getEndpoint);
        registry.add("aws.credentials.secret-key", localStackContainer::getSecretKey);
        registry.add("aws.credentials.access-key", localStackContainer::getAccessKey);
        registry.add("aws.region", localStackContainer::getRegion);
    }

    protected String createDynamoDB() {
        DynamoDbTable<UserEntity> accountTable = dynamoDbEnhancedClient.table("user_events", TableSchema.fromBean(UserEntity.class));

        try {
            accountTable.createTable(builder ->
                builder.streamSpecification(sBuilder -> sBuilder.streamEnabled(true).streamViewType(StreamViewType.NEW_IMAGE))
            );
        } catch (ResourceInUseException e) {
            // swallow and ignore if table already exists
        }

        try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build()) {
            ResponseOrException<DescribeTableResponse> response = waiter.waitUntilTableExists(builder -> builder.tableName("user_events").build()).matched();
            DescribeTableResponse tableDescription = response.response().orElseThrow(() -> new RuntimeException("Table not found"));
            System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + tableDescription.table().latestStreamArn());
            return tableDescription.table().latestStreamArn();
        }

    }

    protected void createDynamoDBAndLambda() throws IOException {

        String dynamoDBStreamArn = createDynamoDB();

        try (LambdaClient lambdaClient = LambdaClient.builder().endpointOverride(localStackContainer.getEndpointOverride(Service.LAMBDA)).region(Region.of(localStackContainer.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .build()) {

            String lambdaPath = "lambda/build/libs/lambda-0.0.1-SNAPSHOT-all.jar";
            byte[] lambdaBytes = Files.readAllBytes(Paths.get(lambdaPath));

            CreateFunctionRequest createFunctionRequest = CreateFunctionRequest.builder()
                    .functionName("user-events-function")
                    .runtime(Runtime.JAVA21)
                    .handler("com.dobackend.Handler::handleRequest")
                    .code(FunctionCode.builder().zipFile(SdkBytes.fromByteArray(lambdaBytes)).build())
                    .role("arn:aws:iam::000000000000:role/user-events-function-role")
                    .timeout(60)
                    .memorySize(512)
                    .build();

            lambdaClient.createFunction(createFunctionRequest);
            CreateEventSourceMappingRequest createEventSourceMappingRequest = CreateEventSourceMappingRequest.builder()
                    .functionName("user-events-function")
                    .eventSourceArn(dynamoDBStreamArn)
                    .batchSize(1)
                    .startingPosition(EventSourcePosition.TRIM_HORIZON)
                    .build();

            lambdaClient.createEventSourceMapping(createEventSourceMappingRequest);
        }
    }

    protected void createSNS() {
        CreateTopicResponse result;
        try (SnsClient snsClient = SnsClient.builder()
                .region(Region.of(localStackContainer.getRegion()))
                .endpointOverride(localStackContainer.getEndpointOverride(Service.SNS))
                .build()) {

            CreateTopicRequest request = CreateTopicRequest.builder()
                    .name("user-events-topic")
                    .attributes(Map.of("DisplayName", "UserEventsTopic"))
                    .build();

            result = snsClient.createTopic(request);
        }


        System.out.println("********************************** " + result.topicArn());
    }
    // @ServiceConnection has replaced the following:
//    @DynamicPropertySource
//    static void properties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
//        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
//    }


}
