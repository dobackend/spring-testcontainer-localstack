package com.dobackend.springtestcontainers.authz.user.event;

import com.dobackend.springtestcontainers.authz.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping("/events/user")
public class UserEventController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserEventRepository userEventRepository;

    public UserEventController(UserEventRepository userEventRepository) {
        this.userEventRepository = userEventRepository;
    }

    @PostMapping
    public ResponseEntity<Void> createUserEvent(@RequestBody User user) {
        logger.info("Creating user event {}", user);
        userEventRepository.saveUser(user);
        // Not sure about this return type, since the creation is eventually consistent
        return ResponseEntity.created(URI.create("/users/" + user.userId().toString())).build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserEvent(@PathVariable("userId") UUID userId) {
        userEventRepository.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({DynamoDbException.class})
    public void handleDynamoDbException(Exception e) {
        logger.error("An error occurred while writing the user event to DynamoDB", e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserEventNotFoundException.class})
    public void handleUserEventNotFoundException(UserEventNotFoundException e) {
        logger.error("User not found in dynamoDB", e);
    }
}
