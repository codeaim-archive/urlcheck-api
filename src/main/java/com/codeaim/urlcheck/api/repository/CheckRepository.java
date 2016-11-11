package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;

@Configuration
public class CheckRepository implements ICheckRepository
{
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public CheckRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate)
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<Check> getChecks()
    {
        String getChecksSql = "SELECT id, name, url, status FROM \"check\"";

        return this.namedParameterJdbcTemplate.query(getChecksSql, mapCheck());
    }

    @Override
    public List<Check> getChecks(String username)
    {
        String getChecksByUsernameSql = "SELECT \"check\".id, name, url, status FROM \"check\" INNER JOIN \"user\" ON \"check\".user_id = \"user\".id WHERE \"user\".username = :username";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("username", username);

        return this.namedParameterJdbcTemplate.query(
                getChecksByUsernameSql,
                parameters,
                mapCheck());
    }

    @Override
    public Check createCheck(Check check)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String sql = ""
                + "INSERT INTO \"check\" "
                + "VALUES      (default, "
                + "             :user_id, "
                + "             NULL, "
                + "             :name, "
                + "             :url, "
                + "             NULL, "
                + "             'UNKNOWN' :: status, "
                + "             'WAITING' :: state, "
                + "             Now(), "
                + "             Now(), "
                + "             Now(), "
                + "             NULL, "
                + "             :interval, "
                + "             FALSE, "
                + "             1);";

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("user_id", check.getUserId())
                .addValue("name", check.getName())
                .addValue("url", check.getUrl())
                .addValue("interval", check.getInterval());

        this.namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        return check.setId(keyHolder.getKey().longValue());
    }

    private RowMapper<Check> mapCheck()
    {
        return (rs, rowNum) -> new Check()
                .setId(rs.getLong("id"))
                .setName(rs.getString("name"))
                .setUrl(rs.getString("url"))
                .setStatus(Status.valueOf(rs.getString("status")));
    }
}
