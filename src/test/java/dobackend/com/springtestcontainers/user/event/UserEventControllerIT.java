package dobackend.com.springtestcontainers.user.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import dobackend.com.springtestcontainers.AbstractContainerTests;
import dobackend.com.springtestcontainers.EventType;
import dobackend.com.springtestcontainers.user.User;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserEventControllerIT extends AbstractContainerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    @Autowired
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Autowired
    private UserEventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserEventRepository userEventRepository;

    @BeforeEach
    public void setUp() {
        DynamoDbTable<UserEntity> accountTable = dynamoDbEnhancedClient.table("user_events", TableSchema.fromBean(UserEntity.class));

        try {
            accountTable.createTable();
        } catch (ResourceInUseException e) {
            // swallow and ignore if table already exists
        }

        try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build()) {
            ResponseOrException<DescribeTableResponse> response = waiter.waitUntilTableExists(builder -> builder.tableName("user_events").build()).matched();
            DescribeTableResponse tableDescription = response.response().orElseThrow(() -> new RuntimeException("Table not found"));
        }
    }

    @Ignore
    public void runMeLocallyToDeleteTableWhenSchemaChanges() throws Exception {
        DeleteTableResponse deleteTableResponse = dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("user_events").build());
        assertThat(deleteTableResponse.tableDescription().tableStatus()).isEqualTo(TableStatus.DELETING);
    }

    @Test
    public void postEvent() throws Exception {

        UUID uuid = UUID.randomUUID();
        String name = "John Doe";

        eventRepository.deleteUser(uuid);

        Optional<UserEvent> userBeforeEvent = userEventRepository.getUser(uuid);

        assertThat(userBeforeEvent).isEmpty();

        String getUri = "/events/user";
        URI getLocation = restTemplate.postForLocation(getUri, new User(uuid, name));

        UserEvent userEvent = userEventRepository.getUser(uuid).orElseGet(null);

        assertThat(userEvent).isNotNull();

        assertThat(userEvent.getEventType()).isEqualTo(EventType.USER_CREATED_UPDATED);
        assertThat(userEvent.getUser()).isEqualTo(new User(uuid, name));
    }


}
