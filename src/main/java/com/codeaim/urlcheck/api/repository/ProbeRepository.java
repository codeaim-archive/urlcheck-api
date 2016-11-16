package com.codeaim.urlcheck.api.repository;

import com.codeaim.urlcheck.api.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class ProbeRepository implements IProbeRepository
{
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final Pattern headerRegex;

    @Autowired
    public ProbeRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate)
    {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.headerRegex = Pattern.compile("\"(?<name>.+?)\": \"(?<value>.*?)\"");
    }

    public List<Check> getCandidates(Probe probe)
    {
        String sql = ""
                + "WITH headers AS "
                + "( "
                + "                SELECT DISTINCT \"check\".\"id\", "
                + "                                NULLIF(string_agg(concat(concat('\"', \"header\".\"name\", '\": '), concat('\"', \"value\", '\"')), ', '), '\"\": \"\"') AS headers "
                + "                FROM            \"check\" "
                + "                LEFT JOIN       \"header\" "
                + "                ON              \"check\".id = \"header\".check_id "
                + "                GROUP BY        \"check\".\"id\""
                + ") "
                + "UPDATE \"check\" "
                + "SET state = 'ELECTED'::state, locked = (NOW() + '1 minute') "
                + "FROM headers "
                + "WHERE \"check\".id = headers.id "
                + "AND \"check\".id IN ( "
                + "    SELECT id "
                + "    FROM ( "
                + "    	SELECT id "
                + "    	FROM \"check\" "
                + "    	WHERE disabled IS NULL AND ((state = 'WAITING'::state AND refresh <= NOW()) OR (state = 'ELECTED'::state AND locked <= NOW())) "
                + "        AND ((:isClustered = FALSE) OR (confirming = FALSE) OR (:isClustered = TRUE AND probe <> :probeName)) "
                + "        ORDER BY status = 'UNKNOWN' DESC, refresh ASC LIMIT :candidateLimit "
                + "	) AS electable "
                + ") "
                + "RETURNING \"check\".id, \"check\".\"name\", url, status, latest_result_id, confirming, \"headers\".\"headers\"";

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("probeName", probe.getName())
                .addValue("isClustered", probe.isClustered())
                .addValue("candidateLimit", probe.getCandidateLimit());

        return this.namedParameterJdbcTemplate
                .query(sql, parameters, mapCandidate());
    }

    @Transactional
    public void createResults(ArrayList<Result> results)
    {
        String insertSql = "INSERT INTO result(check_id, previous_result_id, status, probe, status_code, response_time, changed, confirmation, created) VALUES(:check_id, :previous_result_id, :status::status, :probe, :status_code, :response_time, :changed, :confirmation, :created)";

        SqlParameterSource[] insertParameters = results
                .stream()
                .map(result -> new MapSqlParameterSource()
                        .addValue("check_id", result.getCheckId())
                        .addValue("previous_result_id", result.getPreviousResultId())
                        .addValue("status", result.getStatus().toString())
                        .addValue("probe", result.getProbe())
                        .addValue("status_code", result.getStatusCode())
                        .addValue("response_time", result.getResponseTime() != 0 ? result.getResponseTime() : null)
                        .addValue("changed", result.isChanged())
                        .addValue("confirmation", result.isConfirmation())
                        .addValue("created", Timestamp.from(result.getCreated())))
                .toArray(SqlParameterSource[]::new);

        this.namedParameterJdbcTemplate.batchUpdate(insertSql, insertParameters);

        List<Long> checkIds = results.stream().map(Result::getCheckId).collect(Collectors.toList());

        String updateSql = ""
                + "UPDATE \"check\" "
                + "SET "
                + "	latest_result_id = check_latest.latest_result_id, "
                + "	\"state\" = 'WAITING'::\"state\", "
                + "	locked = NULL, "
                + "	probe = check_latest.probe, "
                + "	status = CASE WHEN check_latest.changed AND check_latest.confirmation THEN check_latest.status ELSE \"check\".status END, "
                + "	refresh = CASE WHEN check_latest.changed = check_latest.confirmation THEN (NOW() + (\"check\".\"interval\" * interval '1 minute')) "
                + "						WHEN check_latest.changed AND (check_latest.confirmation = FALSE) THEN (NOW() + (interval '30 seconds')) "
                + "						ELSE \"check\".refresh END, "
                + "	\"confirming\" = check_latest.changed AND (check_latest.confirmation = FALSE), "
                + "	modified = NOW(), "
                + "	\"version\" = \"check\".\"version\" + 1 "
                + "FROM ( "
                + "	SELECT \"result\".check_id, \"result\".id, status, probe, changed, confirmation "
                + "	FROM \"result\" "
                + "	INNER JOIN ( "
                + "		SELECT check_id, MAX(created) latest_result "
                + "		FROM \"result\" "
                + "		WHERE check_id IN (:checkIds) "
                + "		GROUP BY check_id "
                + "	) AS result_latest ON \"result\".check_id = result_latest.check_id AND \"result\".created = result_latest.latest_result "
                + ") AS check_latest (id, latest_result_id, status, probe, changed, confirmation) "
                + "WHERE \"check\".id = check_latest.id";

        SqlParameterSource updateParameters = new MapSqlParameterSource()
                .addValue("checkIds", checkIds);

        this.namedParameterJdbcTemplate
                .update(updateSql, updateParameters);
    }

    @Override
    public void expireResults(Expire expire)
    {
        String sql = ""
                + "DELETE FROM \"result\" "
                + "WHERE  \"result\".id IN (SELECT \"result\".id "
                + "                       FROM   \"result\" "
                + "                              INNER JOIN \"check\" "
                + "                                      ON \"check\".id = \"result\".check_id "
                + "                              INNER JOIN (SELECT \"check\".id "
                + "                                                 AS check_id "
                + "                                                 , "
                + "                              Max(\"role\".result_retention_duration) "
                + "                              AS "
                + "                              maximum_result_retention_duration "
                + "                                          FROM   \"check\" "
                + "                                                 INNER JOIN \"user\" "
                + "                                                         ON \"user\".id = "
                + "                                                            \"check\".user_id "
                + "                                                 INNER JOIN \"user_role\" "
                + "                                                         ON \"user_role\".user_id "
                + "                                                            = "
                + "                                                            \"user\".id "
                + "                                                 INNER JOIN \"role\" "
                + "                                                         ON \"role\".id = "
                + "                                                            \"user_role\".role_id "
                + "                                          GROUP  BY \"check\".id) AS limits "
                + "                                      ON limits.check_id = \"result\".check_id "
                + "                       WHERE  ( \"result\".changed = false "
                + "                                 OR \"result\".confirmation = false ) "
                + "                          AND \"result\".created < ( "
                + "                              Now() - limits.maximum_result_retention_duration ) "
                + "                          AND \"result\".id <> \"check\".latest_result_id "
                + "                       LIMIT :batchSize)";

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("batchSize", expire.getBatchSize());

        this.namedParameterJdbcTemplate.update(sql, parameters);
    }

    private RowMapper<Check> mapCandidate()
    {
        return (rs, rowNum) -> new Check()
                .setId(rs.getLong("id"))
                .setName(rs.getString("name"))
                .setUrl(rs.getString("url"))
                .setStatus(Status.valueOf(rs.getString("status")))
                .setLatestResultId(rs.getLong("latest_result_id") != 0 ? rs.getLong("latest_result_id") : null)
                .setConfirming(rs.getBoolean("confirming"))
                .setHeaders(getHeaders(rs.getString("headers")));
    }

    private Set<Header> getHeaders(String headersString)
    {
        if (headersString != null && !headersString.isEmpty())
        {
            Set<Header> headers = new HashSet<>();

            Matcher headerMatcher = Pattern
                    .compile("\"(?<name>.+?)\": \"(?<value>.*?)\"")
                    .matcher(headersString);

            while (headerMatcher.find())
            {
                headers.add(new Header()
                        .setName(headerMatcher.group("name"))
                        .setValue(headerMatcher.group("value")));
            }

            return headers;
        }

        return Collections.emptySet();
    }
}
