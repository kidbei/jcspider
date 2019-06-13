package com.jcspider.server.dao;

import com.jcspider.server.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 03 June 2019
 */
@Repository
public class ProjectDao {

    private String COLUMNS = "name, start_url, script_text, status, rate_unit, rate_number, dispatcher, created_at, updated_at," +
            " schedule_type, schedule_value, rate_unit_multiple";

    @Autowired
    private JdbcTemplate    jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

    public long insert(Project project) {
        if (project.getUpdatedAt() == null) {
            project.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        }
        if (project.getCreatedAt() == null) {
            project.setCreatedAt(project.getUpdatedAt());
        }
        final String sql = "insert into project (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id";
        return this.jdbcTemplate.queryForObject(sql, new Object[]{project.getName(), project.getStartUrl(),
                project.getScriptText(), project.getStatus(), project.getRateUnit(),
                project.getRateNumber(), project.getDispatcher(), project.getCreatedAt(),
                project.getUpdatedAt(), project.getScheduleType(), project.getScheduleValue(),
                project.getRateUnitMultiple()}, Long.class);
    }

    public Project getById(long id) {
        final String sql = "select id, " + COLUMNS + " from project where id = ?";
        try {
            return this.jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Project.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void updateDispatcherById(long id, String dispatcher) {
        final String sql = "update project set dispatcher = ? where id = ?";
        this.jdbcTemplate.update(sql, dispatcher, id);
    }

    public void updateStatusById(long id, String status) {
        final String sql = "update project set status = ? where id = ?";
        this.jdbcTemplate.update(sql, status, id);
    }

    public List<Project> findByIds(List<Long> ids) {
        final String sql = "select id, " + COLUMNS + " from project where id in (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", ids);
        return this.namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Project.class));
    }

}
