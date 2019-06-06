package com.jcspider.server.dao;

import com.jcspider.server.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author zhuang.hu
 * @since 03 June 2019
 */
@Repository
public class ProjectDao {

    private String COLUMNS = "id, name, start_url, script_text, status, rate_unit, rate_number, dispatcher, created_at, updated_at, schedule_type, schedule_value";

    @Autowired
    private JdbcTemplate    jdbcTemplate;

    public void insert(Project project) {
        final String sql = "insert into project (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?,?)";
        this.jdbcTemplate.update(sql, project.getId(), project.getName(), project.getStartUrl(),
                project.getScriptText(), project.getStatus(), project.getRateUnit(),
                project.getRateNumber(), project.getDispatcher(), project.getCreatedAt(),
                project.getUpdatedAt(), project.getScheduleType(), project.getScheduleValue());
    }

    public Project getById(long id) {
        final String sql = "select " + COLUMNS + " from project where id = ?";
        return this.jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Project.class));
    }

    public void updateDispatcherById(long id, String dispatcher) {
        final String sql = "update project set dispatcher = ? where id = ?";
        this.jdbcTemplate.update(sql, dispatcher, id);
    }

    public void updateStatusById(long id, String status) {
        final String sql = "update project set status = ? where id = ?";
        this.jdbcTemplate.update(sql, status, id);
    }

}
