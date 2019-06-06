package com.jcspider.server.dao;

import com.jcspider.server.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 04 June 2019
 */
@Repository
public class TaskDao {
    @Autowired
    private JdbcTemplate    jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

    private static final String COLUMNS = "id,`status`,method,source_url,schedule_type," +
            "stack,project_id,schedule_value,headers,extra,fetch_type," +
            "`proxy`,created_at,updated_at";


    public void insert(Task task) {
        final String sql = "insert into task (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        this.jdbcTemplate.update(sql, task.getId(), task.getStatus(), task.getMethod(), task.getSourceUrl(), task.getScheduleType(),
                task.getStack(), task.getProjectId(), task.getScheduleValue(), task.getHeaders(), task.getExtra(), task.getFetchType(),
                task.getProxy(), task.getCreatedAt(), task.getUpdatedAt());
    }


    public void insertBatch(List<Task> tasks) {
        final String sql = "insert into task (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Task task = tasks.get(i);
                ps.setString(1, task.getId());
                ps.setString(2, task.getStatus());
                ps.setString(3, task.getMethod());
                ps.setString(4, task.getSourceUrl());
                ps.setString(5, task.getScheduleType());
                ps.setString(6, task.getStack());
                ps.setLong(7, task.getProjectId());
                ps.setLong(8, task.getScheduleValue());
                ps.setString(9, task.getHeaders());
                ps.setString(10, task.getExtra());
                ps.setString(11, task.getFetchType());
                ps.setString(12, task.getProxy());
                ps.setTimestamp(13, task.getCreatedAt());
                ps.setTimestamp(14, task.getUpdatedAt());
            }

            @Override
            public int getBatchSize() {
                return tasks.size();
            }
        });
    }

    public Task getById(String taskId) {
        final String sql = "select " + COLUMNS + " from task where id = ?";
        try {
            return this.jdbcTemplate.queryForObject(sql, new Object[]{taskId}, new BeanPropertyRowMapper<>(Task.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public void updateStatusAndStackById(String taskId, String stack, String status) {
        final String sql = "update task set stack = ?, status = ? where id = ?";
        this.jdbcTemplate.update(sql, stack, status, taskId);
    }

    public List<Task> findByIds(Collection<String> taskIds) {
        final String sql = "select " + COLUMNS + " from task where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", taskIds);
        return this.namedParameterJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Task.class));
    }

    public void updateStatusByIds(Collection<String> taskIds, String status) {
        final String sql = "update task set status = :status where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", status);
        parameters.addValue("ids", taskIds);
        this.namedParameterJdbcTemplate.update(sql, parameters);
    }

    public List<Task> findByProjectIdAndStatus(long projectId, String status, int limit) {
        final String sql = "select " + COLUMNS + " from task where project_id = ? and status = ? limit ?";
        return this.jdbcTemplate.query(sql, new Object[]{projectId, status, limit}, new BeanPropertyRowMapper<>(Task.class));
    }

}
