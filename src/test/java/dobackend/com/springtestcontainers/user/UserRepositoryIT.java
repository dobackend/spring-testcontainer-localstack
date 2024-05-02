package dobackend.com.springtestcontainers.user;

import dobackend.com.springtestcontainers.AbstractContainerTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest(properties = {"spring.flyway.locations=classpath:/db/migration,classpath:/db/test_migration"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserRepository.class)
class UserRepositoryIT extends AbstractContainerTests {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create() {
        UUID userId = UUID.randomUUID();
        createUser(new User(userId, "John Doe"));

        User user = getUserId(userId);
        assertThat(user).isNotNull();
        assertThat(user.userId()).isEqualTo(userId);

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(22);
    }

    @Test
    void delete() {
        UUID userId = UUID.randomUUID();

        createUser(new User(userId, "John Doe"));
        User user = getUserId(userId);
        assertThat(user).isNotNull();

        userRepository.delete(user.userId());
        assertThrows(EmptyResultDataAccessException.class, () -> getUserId(userId));
    }

    @Test
    void find() {

        UUID id = getUuid();
        assertThat(id).isNotNull();

        User user = getUserId(id);
        assertThat(user).isNotNull();
        assertThat(user.userId()).isEqualTo(id);
    }

    private UUID getUuid() {
        List<User> users = userRepository.findAll();
        return users.getFirst().userId();
    }

    @Test
    void update() {

        UUID id = getUuid();
        assertThat(id).isNotNull();

        User user = getUserId(id);
        assertThat(user.userId()).isEqualTo(id);

        User user1 = new User(user.userId(), "John Doe");
        userRepository.update(user1);

        User user2 = getUserId(user1.userId());
        assertThat(user2.name()).isEqualTo("John Doe");
    }

    @Test
    void connectionEstablished() {
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    private User getUserId(UUID userId) {
        return userRepository.findByUserId(userId);
    }

    private void createUser(User user) {
        userRepository.create(user);
    }
}