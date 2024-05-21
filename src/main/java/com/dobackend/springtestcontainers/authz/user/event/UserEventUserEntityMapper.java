package com.dobackend.springtestcontainers.authz.user.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserEventUserEntityMapper {

    private final ObjectMapper objectMapper;

    public UserEventUserEntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserEntity mapToDynamoEntity(UserEvent userEvent) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(userEvent.getUserId());
        userEntity.setSortKey(UserEntity.SORT_PREFIX + userEvent.getUserId().toString());
        userEntity.setUserEvent(toJsonString(userEvent));
        userEntity.setEventType(userEvent.getEventType().name());
        userEntity.setSequenceNumber(userEvent.getSequenceNumber());
        userEntity.setTimeStamp(Instant.now());
        return userEntity;
    }

    public UserEvent mapFromDynamoEntity(UserEntity userEntity) {
        try {
            return objectMapper.readValue(userEntity.getUserEvent(), UserEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJsonString(UserEvent userEvent) {
        try {
            return objectMapper.writeValueAsString(userEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
