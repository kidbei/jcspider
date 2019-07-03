package com.jcspider.server.dao;

import com.jcspider.server.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
@Repository
public class TaskResultDao {
    @Autowired
    private JdbcTemplate    jdbcTemplate;

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

}
