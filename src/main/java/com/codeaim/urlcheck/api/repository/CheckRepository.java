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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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
        String sql = "SELECT id, name, url, status, interval, disabled, internal FROM \"check\" ORDER BY created DESC;";

        return this.namedParameterJdbcTemplate.query(sql, mapCheck());
    }

    @Override
    public List<Check> getChecks(String username)
    {
        String sql = "SELECT \"check\".id, name, url, status, interval, disabled, internal FROM \"check\" INNER JOIN \"user\" ON \"check\".user_id = \"user\".id WHERE \"user\".username = :username ORDER BY \"check\".created DESC;";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("username", username);

        return this.namedParameterJdbcTemplate.query(
                sql,
                parameters,
                mapCheck()
        );
    }

    @Override
    @Transactional
    public Check createCheck(Check check)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String insertCheckSql = ""
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
                + "             1,"
                + "             NULL,"
                + "             :internal);";

        SqlParameterSource insertCheckParameters = new MapSqlParameterSource()
                .addValue("user_id", check.getUserId())
                .addValue("name", check.getName())
                .addValue("url", check.getUrl())
                .addValue("interval", check.getInterval())
                .addValue("internal", check.isInternal());

        this.namedParameterJdbcTemplate.update(
                insertCheckSql,
                insertCheckParameters,
                keyHolder,
                new String[]{"id"}
        );

        check.setId(keyHolder
                .getKey()
                .longValue());

        if (check.getHeaders() != null && check.getHeaders().size() > 0)
        {
            String insertHeaderSql = "INSERT INTO header(check_id, \"name\", \"value\") VALUES(:check_id, :name, :value)";

            SqlParameterSource[] insertHeaderParameters = check
                    .getHeaders()
                    .stream()
                    .map(header -> new MapSqlParameterSource()
                            .addValue("check_id", check.getId())
                            .addValue("name", header.getName())
                            .addValue("value", header.getValue()))
                    .toArray(SqlParameterSource[]::new);

            this.namedParameterJdbcTemplate
                    .batchUpdate(
                            insertHeaderSql,
                            insertHeaderParameters
                    );
        }

        return check;
    }

    @Override
    public void deleteCheck(long id)
    {
        String sql = "DELETE FROM \"check\" WHERE id = :id;";

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        this.namedParameterJdbcTemplate.update(
                sql,
                parameters
        );
    }

    @Override
    public Check updateCheck(Check check)
    {
        String sql = "UPDATE \"check\" SET \"name\" = :name, url = :url, \"interval\" = :interval, disabled = :disabled, internal = :internal, version = (version + 1), modified = NOW() WHERE id = :id;";

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", check.getId())
                .addValue("name", check.getName())
                .addValue("url", check.getUrl())
                .addValue("interval", check.getInterval())
                .addValue("disabled", check.getDisabled() != null ? Timestamp.from(check.getDisabled()) : null)
                .addValue("internal", check.isInternal());

        this.namedParameterJdbcTemplate.update(
                sql,
                parameters
        );

        return check;
    }

    @Override
    public boolean checkExists(long id)
    {
        String sql = "SELECT count(*) FROM \"check\" WHERE id = :id;";

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        Integer count = this.namedParameterJdbcTemplate
                .queryForObject(sql, parameters, Integer.class);

        return count != null && count > 0;
    }

    private RowMapper<Check> mapCheck()
    {
        return (rs, rowNum) -> new Check()
                .setId(rs.getLong("id"))
                .setName(rs.getString("name"))
                .setUrl(rs.getString("url"))
                .setStatus(Status.valueOf(rs.getString("status")))
                .setInterval(rs.getInt("interval"))
                .setDisabled(rs.getTimestamp("disabled") != null ? rs.getTimestamp("disabled").toInstant() : null)
                .setInternal(rs.getBoolean("internal"));
    }
}
