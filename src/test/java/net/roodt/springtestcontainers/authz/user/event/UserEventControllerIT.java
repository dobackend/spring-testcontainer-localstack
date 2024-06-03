package net.roodt.springtestcontainers.authz.user.event;

import net.roodt.springtestcontainers.AbstractContainerTests;
import net.roodt.springtestcontainers.EventType;
import net.roodt.springtestcontainers.authz.user.User;
import net.roodt.springtestcontainers.authz.user.event.UserEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableResponse;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserEventControllerIT extends AbstractContainerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserEventRepository userEventRepository;

    @BeforeEach
    public void setUp() throws Exception{
        createDynamoDBAndLambda();
        createSNS();
    }

//    @Test
    public void runMeLocallyToDeleteTableWhenSchemaChanges() throws Exception {
        DeleteTableResponse deleteTableResponse = dynamoDbClient.deleteTable(DeleteTableRequest.builder().tableName("user_events").build());
        assertThat(deleteTableResponse.tableDescription().tableStatus()).isEqualTo(TableStatus.DELETING);
    }

    @Test
    public void postEvent() throws Exception {

        UUID uuid = UUID.randomUUID();
        String name = "John Doe";

        Optional<UserEvent> userBeforeEvent = userEventRepository.getUser(uuid);

        Assertions.assertThat(userBeforeEvent).isEmpty();

        String getUri = "/events/user";
        restTemplate.postForLocation(getUri, new User(uuid, name));

        Optional<UserEvent> userEvent = userEventRepository.getUser(uuid);

        assertThat(userEvent).isNotEmpty();

        assertThat(userEvent.get().getEventType()).isEqualTo(EventType.USER_CREATED_UPDATED);
        assertThat(userEvent.get().getName()).isEqualTo(name);
        assertThat(userEvent.get().getUserId()).isEqualTo(uuid);
    }


}
