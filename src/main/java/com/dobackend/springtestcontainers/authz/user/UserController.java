package com.dobackend.springtestcontainers.authz.user;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> user(@PathVariable("id") UUID userId) {
        // This is a straight forward use of optional to inline an if/else type block
        return Optional.ofNullable(userRepository.findByUserId(userId))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> users() {
        // trying to be less verbose, potentially at the expense of readability
        // could achieve the same with an if/else based on whether the list has elements or not.
        return Optional.ofNullable(userRepository.findAll())
                .filter((list) -> !list.isEmpty())
                .map(ResponseEntity::ok)
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        userRepository.create(new User(user.userId(), user.name()));
        // Do some HATEOAS here. The repository is throwing a ResponseStatus aware exception if a conflict exists
        // Conflict exception handled by dobackend.com.springtestcontainers.user.UserController.handleAlreadyExists
        return ResponseEntity.created(URI.create("/users/" + user.userId().toString())).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody User user) {
//        return userRepository.update(new User(user.userId(), user.name())) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        return Optional.of(userRepository.update(new User(user.userId(), user.name())))
                .filter(success -> success)
                .map(success -> ResponseEntity.noContent().<Void>build())
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID userId) {
        return Optional.ofNullable(userRepository.delete(userId))
                .filter(success -> success)
                .map(success -> ResponseEntity.noContent().<Void>build())
                .orElseGet(ResponseEntity.notFound()::build);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({IllegalArgumentException.class, EmptyResultDataAccessException.class, InvalidFormatException.class})
    public void handleNotFound(Exception ex) {
        logger.error("Data not found", ex);
        // return empty 404
    }

    /**
     * Maps DataIntegrityViolationException to a 409 Conflict HTTP status code.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({DataIntegrityViolationException.class})
    public void handleAlreadyExists(Exception ex) {
        logger.error("Data already exists", ex);
        // return empty 409
    }

}
