package dobackend.com.springtestcontainers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component
public class AccountRepository {

    private static final String CREATE_QUERY = "INSERT INTO ACCOUNT (NAME, ACCOUNT_ID) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM ACCOUNT WHERE ACCOUNT_ID = ?";
    private static final String FIND_QUERY = "SELECT ID, ACCOUNT_ID, NAME FROM ACCOUNT WHERE ACCOUNT_ID = ?";
    private static final String FIND_ALL_QUERY = "SELECT ID, ACCOUNT_ID, NAME FROM ACCOUNT ORDER BY ID";
    private static final String UPDATE_QUERY = "UPDATE ACCOUNT SET ACCOUNT_ID=?, NAME=? WHERE ACCOUNT_ID=?";

    private final JdbcTemplate jdbcTemplate;

    // Staying with plain JdbcTemplate instead of Hibernate. Makes the migration to R2JdbcTemplate easier.
    public AccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Account account) {
        jdbcTemplate.update(
                CREATE_QUERY,
                account.name(),
                account.accountId()
        );
    }

    public Boolean delete(UUID accountId) {
        return jdbcTemplate.update(
                DELETE_QUERY,
                accountId
        ) > 0;
    }

    public Account findByAccountId(UUID accountId) {
        return jdbcTemplate.queryForObject(
                FIND_QUERY,
                (rs, rowNum) -> new Account(rs.getObject("ACCOUNT_ID", UUID.class), rs.getString("name")),
                accountId
        );
    }

    public List<Account> findAll() {
//        return jdbcTemplate.query(FIND_ALL_QUERY, new AccountMapper());
        return jdbcTemplate.query(
                FIND_ALL_QUERY,
                (rs, rowNum) -> new Account(rs.getObject("ACCOUNT_ID", UUID.class), rs.getString("name"))
        );
    }

    public Boolean update(Account account) {
        return jdbcTemplate.update(UPDATE_QUERY,
                account.accountId(),
                account.name(),
                account.accountId()
        ) > 0;
    }

    // Using inline lambda instead, left this here to demo how it is done
    private static class AccountMapper implements RowMapper<Account> {
        @Override
        public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Account(rs.getObject("account_number", UUID.class), rs.getString("name"));
        }
    }
}
