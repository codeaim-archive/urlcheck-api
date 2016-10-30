package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.Role;
import com.codeaim.urlcheck.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class UserRepository implements IUserRepository
{
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate)
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
