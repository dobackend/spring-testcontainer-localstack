package dobackend.com.springtestcontainers.user.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dobackend.com.springtestcontainers.user.User;
import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserEventRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserEventRepository.class);
    private final DynamoDbTemplate dynamoDbTemplate;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTableNameResolver tableNameResolver;

    private final ObjectMapper objectMapper;


    public UserEventRepository(DynamoDbTemplate dynamoDbTemplate, DynamoDbEnhancedClient enhancedClient, DynamoDbTableNameResolver tableNameResolver, ObjectMapper objectMapper) {
        this.dynamoDbTemplate = dynamoDbTemplate;
        this.enhancedClient = enhancedClient;
        this.tableNameResolver = tableNameResolver;
        this.objectMapper = objectMapper;
    }

    public void saveUser(User user) {
        // DynamoDBTemplate doesn't have conditional updates, unfortunately.
        String tableName = tableNameResolver.resolve(UserEntity.class);
        DynamoDbTable<UserEntity> userTable = enhancedClient.table(tableName,
                TableSchema.fromBean(UserEntity.class));

        UserEntity entity = new UserCreatedEvent(user).mapToDynamoEntity(objectMapper);
        Map<String, AttributeValue> key = Map.of(":val", AttributeValue.fromN(entity.getSequenceNumber().toString()));

        //only upsert if the seq number is greater than or equal to existing seq number.
        try {
            userTable.updateItem(r -> r.item(entity)
                    .conditionExpression(Expression.builder().expression("attribute_not_exists(sequenceNumber) OR sequenceNumber < :val")
                            .expressionValues(key)
                            .build())
            );
        } catch (ConditionalCheckFailedException e) {
            logger.info("User {} already exists with sequence number {}", entity.getUserId(), entity.getSequenceNumber());
        }
    }

    public Optional<UserEvent> getUser(UUID uuid) {
        UserEntity userEntity = dynamoDbTemplate.load(Key.builder().partitionValue(uuid.toString())
                .sortValue(UserEntity.SORT_PREFIX + uuid).build(), UserEntity.class);

        if (userEntity == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(userEntity.getUserEvent(), UserEvent.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public void deleteUser(UUID uuid) {
        dynamoDbTemplate.delete(Key.builder().partitionValue(uuid.toString())
                .sortValue(UserEntity.SORT_PREFIX + uuid).build(), UserEntity.class);
    }
}
