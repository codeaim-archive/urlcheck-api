package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.client.EmailClient;
import com.codeaim.urlcheck.api.model.Role;
import com.codeaim.urlcheck.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class UserRepository implements IUserRepository
{
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailClient emailClient;

    @Autowired
    public UserRepository(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            PasswordEncoder passwordEncoder,
            EmailClient emailClient
    )
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.emailClient = emailClient;
    }

    @Override
    public Optional<User> getUserByUsername(String username)
    {
        String getUserByUsernameSql = "SELECT \"user\".id, \"user\".username, \"user\".email, \"user\".\"password\", string_agg(\"role\".\"name\", ',') AS \"roles\" FROM \"user\" INNER JOIN \"user_role\" ON \"user\".id = \"user_role\".user_id INNER JOIN \"role\" ON \"role\".id = \"user_role\".role_id WHERE \"user\".username = :username OR \"user\".email = :username GROUP BY \"user\".id";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("username", username);

        return this.namedParameterJdbcTemplate
                .query(
                        getUserByUsernameSql,
                        parameters,
                        mapUser()
                )
                .stream()
                .findFirst();
    }

    @Override
    @Transactional
    public User createUser(User user)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String insertUserSql = ""
                + "INSERT INTO \"user\"(id,"
                + "                      username, "
                + "                      email, "
                + "                      reset_token, "
                + "                      access_token, "
                + "                      email_verification_token, "
                + "                      password, "
                + "                      email_verified, "
                + "                      created, "
                + "                      modified, "
                + "                      version) "
                + "VALUES      (default, "
                + "             :username, "
                + "             :email, "
                + "             :reset_token, "
                + "             :access_token, "
                + "             :email_verification_token, "
                + "             :password, "
                + "             FALSE, "
                + "             Now(), "
                + "             Now(), "
                + "             1);";

        String emailVerificationToken = UUID
                .randomUUID()
                .toString();

        MapSqlParameterSource insertUserParameters = new MapSqlParameterSource()
                .addValue("username", user.getUsername())
                .addValue("email", user.getEmail())
                .addValue("reset_token", UUID.randomUUID())
                .addValue("access_token", UUID.randomUUID())
                .addValue("email_verification_token", emailVerificationToken)
                .addValue("password", passwordEncoder.encode(user.getPassword()));

        this.namedParameterJdbcTemplate
                .update(
                        insertUserSql,
                        insertUserParameters,
                        keyHolder,
                        new String[]{"id"});

        user.setId(keyHolder
                .getKey()
                .longValue());

        String insertUserRoleSql = "INSERT INTO user_role(id, \"user_id\", \"role_id\") VALUES(default, :user_id, (SELECT id FROM \"role\" WHERE \"name\" = 'registered'))";

        MapSqlParameterSource insertUserRoleParameters = new MapSqlParameterSource()
                .addValue("user_id", user.getId());

        this.namedParameterJdbcTemplate
                .update(
                        insertUserRoleSql,
                        insertUserRoleParameters
                );

        user.setRoles(Collections.singletonList(new Role().setName("registered")));

        emailClient.sendVerifyEmail(
                user.getEmail(),
                user.getUsername(),
                emailVerificationToken);

        return user;
    }

    @Override
    public boolean verifyEmail(
            String username,
            String emailVerificationToken
    )
    {
        String sql = "UPDATE \"user\" SET email_verified = TRUE WHERE username = :username AND email_verification_token = :email_verification_token;";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("username", username)
                .addValue("email_verification_token", emailVerificationToken);

        return this.namedParameterJdbcTemplate
                .update(
                        sql,
                        parameters
                ) > 0;
    }


    private RowMapper<User> mapUser()
    {
        return (rs, rowNum) -> new User()
                .setId(rs.getLong("id"))
                .setUsername(rs.getString("username"))
                .setPassword(rs.getString("password"))
                .setRoles(Arrays.stream(rs.getString("roles")
                        .split(","))
                        .map(x -> new Role().setName(x))
                        .collect(Collectors.toList()));
    }
}
