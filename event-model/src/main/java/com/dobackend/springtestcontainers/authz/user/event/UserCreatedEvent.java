package com.dobackend.springtestcontainers.authz.user.event;

import com.dobackend.springtestcontainers.EventType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public final class UserCreatedEvent extends UserEvent {

    @JsonCreator
    public UserCreatedEvent(
            @JsonProperty("userId") UUID userId,
            @JsonProperty("name") String name) {
        super(EventType.USER_CREATED_UPDATED, userId, name, Instant.now().toEpochMilli());
    }
}
