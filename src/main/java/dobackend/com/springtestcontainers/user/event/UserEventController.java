package dobackend.com.springtestcontainers.user.event;

import dobackend.com.springtestcontainers.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.net.URI;


@RestController
@RequestMapping("/events")
public class UserEventController {

    private final Logger logger = LoggerFactory.getLogger(UserEventController.class);

    private final UserEventRepository userEventRepository;

    public UserEventController(UserEventRepository userEventRepository) {
        this.userEventRepository = userEventRepository;
    }

    @PostMapping("/user")
    public ResponseEntity<Void> createUserEvent(@RequestBody User user) {
        userEventRepository.saveUser(user);
        // Not sure about this return type
        return ResponseEntity.created(URI.create("/users/" + user.userId().toString())).build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({DynamoDbException.class})
    public void handleDynamoDbException(Exception e) {
        logger.error("An error occurred while writing the user event to DynamoDB", e);
    }
}
