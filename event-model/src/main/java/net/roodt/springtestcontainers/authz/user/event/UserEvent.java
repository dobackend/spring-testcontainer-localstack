package net.roodt.springtestcontainers.authz.user.event;

import net.roodt.springtestcontainers.EventType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.StringJoiner;
import java.util.UUID;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserCreatedEvent.class, name = "USER_CREATED_UPDATED"),
        @JsonSubTypes.Type(value = UserDeletedEvent.class, name = "USER_DELETED")
})
public sealed abstract class UserEvent permits UserCreatedEvent, UserDeletedEvent {

    private final EventType eventType;
    private final Long sequenceNumber;
    private final UUID userId;
    private final String name;


    public UserEvent(
            EventType eventType,
            UUID userId,
            String name,
            Long sequenceNumber
    ) {
        this.eventType = eventType;
        this.sequenceNumber = sequenceNumber;
        this.name = name;
        this.userId = userId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public UUID getUserId() { return userId; }

    public String getName() { return name; }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserEvent.class.getSimpleName() + "[", "]")
                .add("eventType=" + eventType)
                .add("sequenceNumber=" + sequenceNumber)
                .add("userId=" + userId)
                .add("name='" + name + "'")
                .toString();
    }
}

