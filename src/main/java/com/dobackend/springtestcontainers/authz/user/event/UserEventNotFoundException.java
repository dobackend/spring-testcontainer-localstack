package com.dobackend.springtestcontainers.authz.user.event;

public class UserEventNotFoundException extends RuntimeException {
    public UserEventNotFoundException(String message) {
        super(message);
    }
}
