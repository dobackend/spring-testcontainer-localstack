package dobackend.com.springtestcontainers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Spinning up postgres to test full e2e flow from api to db
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerIT extends AbstractContainerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    // We just want one happy path test for e2e integration. The WebMvc Slice test
    // takes care of the rest of the test cases.
    @Test
    public void addGetAndDeleteAccount() {

        String addUri = "/accounts";

        UUID uuid = UUID.randomUUID();
        String name = "John Doe";

        URI getLocation = restTemplate.postForLocation(addUri, new Account(uuid, name));

        assertThat(getLocation).isNotNull();

        Account account = restTemplate.getForObject(getLocation, Account.class);

        assertThat(account).isNotNull();
        assertThat(account.name()).isEqualTo(name);
        assertThat(account.accountId()).isEqualTo(uuid);

        restTemplate.delete(getLocation);

        account = restTemplate.getForObject(getLocation, Account.class);

        assertThat(account).isNull();

    }
}
