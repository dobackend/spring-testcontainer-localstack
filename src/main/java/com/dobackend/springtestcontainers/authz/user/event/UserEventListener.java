package com.dobackend.springtestcontainers.authz.user.event;

import com.dobackend.springtestcontainers.authz.user.User;
import com.dobackend.springtestcontainers.authz.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;

    public UserEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @SqsListener(queueNames = {"user-events-queue"})
    public void onUserEvent(UserEvent event, Acknowledgement acknowledgement) throws JsonProcessingException {

        switch (event) {
            case UserCreatedEvent userCreatedEvent -> {
                logger.debug("User created/updated: " + event);
                userRepository.upsert(new User(userCreatedEvent.getUserId(), userCreatedEvent.getName()), userCreatedEvent.getSequenceNumber());
            }
            case UserDeletedEvent userDeletedEvent -> {
                logger.debug("User removed: " + event);
                userRepository.delete(userDeletedEvent.getUserId());
            }
        }
        acknowledgement.acknowledge();
    }
}
