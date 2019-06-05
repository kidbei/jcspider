package com.jcspider.server.dao;

import com.jcspider.server.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
@Repository
public class TaskResultDao {
    @Autowired
    private JdbcTemplate    jdbcTemplate;

    private static final String COLUMNS = "id, project_id, task_id, result_text, created_at";

    public void insert(TaskResult taskResult) {
        final String sql = "insert into result(" + COLUMNS + ") values (?,?,?,?,?)";
        this.jdbcTemplate.update(sql, taskResult.getId(), taskResult.getProjectId(),
                taskResult.getTaskId(), taskResult.getResultText(), taskResult.getCreatedAt());
    }
}
