package net.roodt.springtestcontainers.authz.user.event;

import net.roodt.springtestcontainers.EventType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public final class UserDeletedEvent extends UserEvent{
    @JsonCreator
    public UserDeletedEvent(
            @JsonProperty("userId") UUID userId,
            @JsonProperty("name") String name) {
        super(EventType.USER_DELETED, userId, name, Instant.now().toEpochMilli());
    }
}
