package com.jcspider.server.dao;

import com.jcspider.server.model.ProjectResultCount;
import com.jcspider.server.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
@Repository
public class TaskResultDao {
    @Autowired
    private JdbcTemplate    jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String COLUMNS = "project_id, task_id, result_text, created_at";

    public void insert(TaskResult taskResult) {
        if (taskResult.getCreatedAt() == null) {
            taskResult.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        final String sql = "insert into result(" + COLUMNS + ") values (?,?,?,?)";
        this.jdbcTemplate.update(sql, taskResult.getProjectId(),
                taskResult.getTaskId(), taskResult.getResultText(), taskResult.getCreatedAt());
    }


    public void upsert(TaskResult taskResult) {
        if (taskResult.getCreatedAt() == null) {
            taskResult.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        final String sql = "insert into result(" + COLUMNS + ") values (?,?,?,?) on conflict (task_id) " +
                "do update set result_text = excluded.result_text, created_at = excluded.created_at";
        this.jdbcTemplate.update(sql, taskResult.getProjectId(),
                taskResult.getTaskId(), taskResult.getResultText(), taskResult.getCreatedAt());
    }


    public void deleteByProjectIdAndTaskId(long projectId, String taskId) {
        final String sql = "delete from result where project_id = ? and task_id = ?";
        this.jdbcTemplate.update(sql, projectId, taskId);
    }

    public void deleteByProjectId(long projectId) {
        final String sql = "delete from result where project_id = ?";
        this.jdbcTemplate.update(sql, projectId);
    }

    public List<ProjectResultCount> findByProjectIds(Collection<Long> projectIds) {
        final String sql = "select project_id, count(id) as result_count from result where project_id in (:projectIds) group by project_id";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("projectIds", projectIds);
        return this.namedParameterJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(ProjectResultCount.class));
    }

}
