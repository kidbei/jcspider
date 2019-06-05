package com.jcspider.server.dao;

import com.jcspider.server.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
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


    public Project getById(long id) {
        final String sql = "select " + COLUMNS + " from project where id = ?";
        return this.jdbcTemplate.queryForObject(sql, Project.class);
    }

    public void updateDispatcherById(long id, String dispatcher) {
        final String sql = "update project set dispatcher = ? where id = ?";
        this.jdbcTemplate.update(sql, dispatcher, id);
    }

}
