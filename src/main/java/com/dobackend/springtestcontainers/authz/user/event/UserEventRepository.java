package com.dobackend.springtestcontainers.authz.user.event;

import com.dobackend.springtestcontainers.authz.user.User;
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

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DynamoDbTemplate dynamoDbTemplate;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTableNameResolver tableNameResolver;
    private final UserEventUserEntityMapper userEventUserEntityMapper;

    public UserEventRepository(DynamoDbTemplate dynamoDbTemplate, DynamoDbEnhancedClient enhancedClient, DynamoDbTableNameResolver tableNameResolver, UserEventUserEntityMapper userEventUserEntityMapper) {
        this.dynamoDbTemplate = dynamoDbTemplate;
        this.enhancedClient = enhancedClient;
        this.tableNameResolver = tableNameResolver;
        this.userEventUserEntityMapper = userEventUserEntityMapper;
    }

    public Optional<UserEvent> getUser(UUID uuid) {
        logger.debug("Get UserEvent for UUID {}", uuid);
        UserEntity userEntity = dynamoDbTemplate.load(
                Key.builder().partitionValue(uuid.toString()).sortValue(UserEntity.SORT_PREFIX + uuid).build(),
                UserEntity.class
        );

        if (userEntity == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(userEventUserEntityMapper.mapFromDynamoEntity(userEntity));
    }

    public void saveUser(User user) {
        UserEvent userEvent = new UserCreatedEvent(user.userId(), user.name());
        logger.debug("Saving user {}", userEvent);
        persistEvent(userEvent);
    }

    public void deleteUser(UUID uuid) {
        UserEvent userEvent = getUser(uuid).orElseThrow(() -> new UserEventNotFoundException("User not found"));
        UserEvent userDeletedEvent = new UserDeletedEvent(userEvent.getUserId(), userEvent.getName());
        persistEvent(userDeletedEvent);
    }

    private void persistEvent(UserEvent userEvent) {
        logger.debug("Persisting user {}", userEvent);

        UserEntity entity = userEventUserEntityMapper.mapToDynamoEntity(userEvent);

        logger.debug("Created  user entity {}", entity);

        // DynamoDBTemplate doesn't have conditional updates, unfortunately.
        String tableName = tableNameResolver.resolve(UserEntity.class);
        DynamoDbTable<UserEntity> userTable = enhancedClient.table(tableName,
                TableSchema.fromBean(UserEntity.class));

        Map<String, AttributeValue> key = Map.of(":val", AttributeValue.fromN(entity.getSequenceNumber().toString()));

        //only update if the seq number is greater than or equal to existing seq number.
        try {
            userTable.updateItem(r -> r.item(entity)
                    .conditionExpression(Expression.builder().expression("attribute_not_exists(sequenceNumber) OR sequenceNumber < :val")
                            .expressionValues(key)
                            .build())
            );
            logger.debug("User {} updated sequence number {}", entity.getUserId(), entity.getSequenceNumber());
        } catch (ConditionalCheckFailedException e) {
            logger.debug("User {} already exists with sequence number {}", entity.getUserId(), entity.getSequenceNumber());
        }
    }
}
