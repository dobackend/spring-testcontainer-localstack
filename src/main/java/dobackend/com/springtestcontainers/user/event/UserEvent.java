package dobackend.com.springtestcontainers.user.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dobackend.com.springtestcontainers.EventType;
import dobackend.com.springtestcontainers.user.User;

import java.time.Instant;

public class UserEvent {

    private final User user;
    private final EventType eventType;
    private final Long sequenceNumber;

    public UserEvent(EventType eventType, User user, Long sequenceNumber) {
        this.user = user;
        this.eventType = eventType;
        this.sequenceNumber = sequenceNumber;
    }

    public UserEntity mapToDynamoEntity(ObjectMapper objectMapper) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(this.user.userId());
        userEntity.setSortKey(UserEntity.SORT_PREFIX + this.user.userId());
        userEntity.setUserEvent(toJsonString(objectMapper));
        userEntity.setEventType(this.eventType.name());
        userEntity.setSequenceNumber(this.sequenceNumber);
        userEntity.setTimeStamp(Instant.now());
        return userEntity;
    }

    private String toJsonString(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUser() {
        return user;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }
}

