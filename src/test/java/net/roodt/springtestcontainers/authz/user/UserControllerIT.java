package net.roodt.springtestcontainers.authz.user;

import net.roodt.springtestcontainers.AbstractContainerTests;
import net.roodt.springtestcontainers.authz.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Spinning up postgres to test full e2e flow from api to db
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT extends AbstractContainerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    // We just want one happy path test for e2e integration. The WebMvc Slice test
    // takes care of the rest of the test cases.
    @Test
    public void addGetAndDeleteUser() throws Exception {
        UUID uuid = UUID.randomUUID();
        String name = "John Doe";

        String addUri = "/users";

        URI getLocation = restTemplate.postForLocation(addUri, new User(uuid, name));

        assertThat(getLocation).isNotNull();

        User user = restTemplate.getForObject(getLocation, User.class);

        assertThat(user).isNotNull();
        assertThat(user.name()).isEqualTo(name);
        assertThat(user.userId()).isEqualTo(uuid);

        restTemplate.delete(getLocation);

        user = restTemplate.getForObject(getLocation, User.class);

        assertThat(user).isNull();

    }
}
