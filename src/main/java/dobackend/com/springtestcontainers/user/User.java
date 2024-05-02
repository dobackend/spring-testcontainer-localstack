package dobackend.com.springtestcontainers.user;

import java.util.UUID;

public record User(UUID userId, String name) {
}
