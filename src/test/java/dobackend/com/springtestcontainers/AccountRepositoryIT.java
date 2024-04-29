package dobackend.com.springtestcontainers;

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
@Import(AccountRepository.class)
class AccountRepositoryIT extends AbstractContainerTests {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void create() {
        UUID accountId = UUID.randomUUID();
        createAccount(new Account(accountId, "John Doe"));

        Account account = getAccountId(accountId);
        assertThat(account).isNotNull();
        assertThat(account.accountId()).isEqualTo(accountId);

        List<Account> accounts = accountRepository.findAll();
        assertThat(accounts).hasSize(22);
    }

    @Test
    void delete() {
        UUID accountId = UUID.randomUUID();

        createAccount(new Account(accountId, "John Doe"));
        Account account = getAccountId(accountId);
        assertThat(account).isNotNull();

        accountRepository.delete(account.accountId());
        assertThrows(EmptyResultDataAccessException.class, () -> getAccountId(accountId));
    }

    @Test
    void find() {

        UUID id = getUuid();
        assertThat(id).isNotNull();

        Account account = getAccountId(id);
        assertThat(account).isNotNull();
        assertThat(account.accountId()).isEqualTo(id);
    }

    private UUID getUuid() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.getFirst().accountId();
    }

    @Test
    void update() {

        UUID id = getUuid();
        assertThat(id).isNotNull();

        Account account = getAccountId(id);
        assertThat(account.accountId()).isEqualTo(id);

        Account account1 = new Account(account.accountId(), "John Doe");
        accountRepository.update(account1);

        Account account2 = getAccountId(account1.accountId());
        assertThat(account2.name()).isEqualTo("John Doe");
    }

    @Test
    void connectionEstablished() {
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    private Account getAccountId(UUID accountId) {
        return accountRepository.findByAccountId(accountId);
    }

    private void createAccount(Account account) {
        accountRepository.create(account);
    }
}