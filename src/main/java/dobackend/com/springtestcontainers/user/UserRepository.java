package dobackend.com.springtestcontainers.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Component
public class UserRepository {

    private static final String CREATE_QUERY = "INSERT INTO USERS (NAME, USER_ID) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM USERS WHERE USER_ID = ?";
    private static final String FIND_QUERY = "SELECT ID, USER_ID, NAME FROM USERS WHERE USER_ID = ?";
    private static final String FIND_ALL_QUERY = "SELECT ID, USER_ID, NAME FROM USERS ORDER BY ID";
    private static final String UPDATE_QUERY = "UPDATE USERS SET USER_ID=?, NAME=? WHERE USER_ID=?";

    private final JdbcTemplate jdbcTemplate;

    // Staying with plain JdbcTemplate instead of Hibernate. Makes the migration to R2JdbcTemplate easier.
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(User user) {
        jdbcTemplate.update(
                CREATE_QUERY,
                user.name(),
                user.userId()
        );
    }

    public Boolean delete(UUID userId) {
        return jdbcTemplate.update(
                DELETE_QUERY,
                userId
        ) > 0;
    }

    public User findByUserId(UUID userId) {
        return jdbcTemplate.queryForObject(
                FIND_QUERY,
                (rs, rowNum) -> new User(rs.getObject("USER_ID", UUID.class), rs.getString("name")),
                userId
        );
    }

    public List<User> findAll() {
//        return jdbcTemplate.query(FIND_ALL_QUERY, new UserMapper());
        return jdbcTemplate.query(
                FIND_ALL_QUERY,
                (rs, rowNum) -> new User(rs.getObject("USER_ID", UUID.class), rs.getString("name"))
        );
    }

    public Boolean update(User user) {
        return jdbcTemplate.update(UPDATE_QUERY,
                user.userId(),
                user.name(),
                user.userId()
        ) > 0;
    }

    // Using inline lambda instead, left this here to demo how it is done
    private static class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new User(rs.getObject("user_id", UUID.class), rs.getString("name"));
        }
    }
}
