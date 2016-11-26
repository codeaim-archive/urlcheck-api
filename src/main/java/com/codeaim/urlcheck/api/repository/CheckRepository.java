package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.Check;
import com.codeaim.urlcheck.api.model.Event;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<Check> getChecksByUsername()
    {
        String sql = "SELECT id, name, url, status, interval, disabled, internal FROM \"check\" ORDER BY created DESC;";

        return this.namedParameterJdbcTemplate.query(sql, mapCheck());
    }

    @Override
    public List<Check> getChecksByUsername(String username)
    {
        String sql = "SELECT \"check\".id, name, url, status, interval, disabled, \"check\".created, internal FROM \"check\" INNER JOIN \"user\" ON \"check\".user_id = \"user\".id WHERE \"user\".username = :username ORDER BY \"check\".created DESC;";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("username", username);

        return this.namedParameterJdbcTemplate.query(
                sql,
                parameters,
                mapCheck()
        );
    }

    @Override
    public List<Check> getChecksWithEventsByUsername(String username)
    {
        List<Check> checks = getChecksByUsername(username);

        String sql = ""
                + "WITH event AS ( "
                + "	SELECT "
                + "		ROW_NUMBER() OVER (PARTITION BY \"result\".check_id ORDER BY \"result\".created DESC), "
                + "		\"result\".id, "
                + "		\"result\".check_id, "
                + "		\"result\".created, "
                + "		\"result\".status "
                + "	FROM \"result\" "
                + "	INNER JOIN \"check\" ON \"result\".check_id = \"check\".id "
                + "	INNER JOIN \"user\" ON \"check\".user_id = \"user\".id "
                + "	WHERE "
                + "		changed = TRUE "
                + "		AND confirmation = TRUE "
                + "		AND disabled IS NULL "
                + "		AND \"user\".username = :username"
                + ") "
                + "SELECT "
                + "	event.id AS \"id\", "
                + "	event.check_id, "
                + "	COALESCE(past.created, \"check\".created) AS \"start\", "
                + "	event.created AS \"end\", "
                + "	COALESCE(past.status, 'UNKNOWN'::status) AS \"start_status\", "
                + "	event.status AS \"end_status\", "
                + " COALESCE(EXTRACT(EPOCH FROM (event.created - past.created)), EXTRACT(EPOCH FROM (event.created - \"check\".created))) AS duration "
                + "FROM event "
                + "LEFT OUTER JOIN event AS past ON event.row_number = past.row_number - 1 AND event.check_id = past.check_id "
                + "LEFT JOIN \"check\" ON event.check_id = \"check\".id "
                + "ORDER BY event.check_id, event.created";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("username", username);

        this.namedParameterJdbcTemplate.query(
                sql,
                parameters,
                mapEvent()
        )
                .stream()
                .collect(Collectors.groupingBy(Event::getCheckId))
                .forEach((checkId, events) -> checks
                        .stream()
                        .filter(check -> check.getId() == checkId)
                        .findFirst()
                        .map(check -> check.setEvents(events))
                        .map(check -> check.setTotalMonitored(check
                                .getEvents()
                                .stream()
                                .mapToDouble(Event::getDuration)
                                .sum() +
                                ChronoUnit.SECONDS.between(
                                        Collections.max(
                                                check.getEvents(),
                                                Comparator.comparing(Event::getEnd)
                                        ).getEnd(),
                                        Instant.now()
                                )))
                        .map(check -> check.setTotalDowntime(check
                                .getEvents()
                                .stream()
                                .filter(x -> (x.getStartStatus() == Status.UNKNOWN && x.getEndStatus() == Status.DOWN) || x.getStartStatus() == Status.DOWN)
                                .mapToDouble(Event::getDuration).sum() +
                                (Collections.max(
                                        check.getEvents(),
                                        Comparator.comparing(Event::getEnd)
                                ).getEndStatus() == Status.UNKNOWN || Collections.max(
                                        check.getEvents(),
                                        Comparator.comparing(Event::getEnd)
                                ).getEndStatus() == Status.DOWN ? ChronoUnit.SECONDS.between(
                                        Collections.max(
                                                check.getEvents(),
                                                Comparator.comparing(Event::getEnd)
                                        ).getEnd(),
                                        Instant.now()
                                ) : 0)))
                        .map(check -> check.setTotalUptime(check
                                .getEvents()
                                .stream()
                                .filter(x -> (x.getStartStatus() == Status.UNKNOWN && x.getEndStatus() == Status.UP) || x.getStartStatus() == Status.UP)
                                .mapToDouble(Event::getDuration).sum() +
                                (Collections.max(
                                        check.getEvents(),
                                        Comparator.comparing(Event::getEnd)
                                ).getEndStatus() == Status.UNKNOWN || Collections.max(
                                        check.getEvents(),
                                        Comparator.comparing(Event::getEnd)
                                ).getEndStatus() == Status.UP ? ChronoUnit.SECONDS.between(
                                        Collections.max(
                                                check.getEvents(),
                                                Comparator.comparing(Event::getEnd)
                                        ).getEnd(),
                                        Instant.now()
                                ) : 0)))
                        .map(check -> check.setTotalUptimePrecentage(100 * (check.getTotalUptime() / check.getTotalMonitored())))
                        .map(check -> check.setTotalDowntimePrecentage(100 - check.getTotalUptimePrecentage())));

        return checks;
    }

    @Override
    public Optional<Check> getCheckById(Long id)
    {
        String sql = "SELECT \"check\".id, name, url, status, interval, disabled, internal FROM \"check\" WHERE \"check\".id = :id;";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);

        return this.namedParameterJdbcTemplate.query(
                sql,
                parameters,
                mapCheck()
        )
                .stream()
                .findFirst();
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
                .setInternal(rs.getBoolean("internal"))
                .setCreated(rs.getTimestamp("created").toInstant());
    }

    private RowMapper<Event> mapEvent()
    {
        return (rs, rowNum) -> new Event()
                .setId(rs.getLong("id"))
                .setCheckId(rs.getLong("check_id"))
                .setStart(rs.getTimestamp("start").toInstant())
                .setEnd(rs.getTimestamp("end").toInstant())
                .setStartStatus(Status.valueOf(rs.getString("start_status")))
                .setEndStatus(Status.valueOf(rs.getString("end_status")))
                .setDuration(rs.getLong("duration"));
    }
}
