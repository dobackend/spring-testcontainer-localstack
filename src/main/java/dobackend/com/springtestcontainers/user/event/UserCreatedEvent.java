package dobackend.com.springtestcontainers.user.event;

import dobackend.com.springtestcontainers.EventType;
import dobackend.com.springtestcontainers.user.User;

import java.time.Instant;

public class UserCreatedEvent extends UserEvent {
    public UserCreatedEvent(User user) {
        super(EventType.USER_CREATED_UPDATED, user, Instant.now().toEpochMilli());
    }
}
