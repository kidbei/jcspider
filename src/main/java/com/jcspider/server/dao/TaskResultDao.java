package com.jcspider.server.dao;

import com.jcspider.server.model.ProjectResultCount;
import com.jcspider.server.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
            taskResult.setCreatedAt(System.currentTimeMillis());
        }
        final String sql = "insert into result(" + COLUMNS + ") values (?,?,?,?)";
        this.jdbcTemplate.update(sql, taskResult.getProjectId(),
                taskResult.getTaskId(), taskResult.getResultText(), taskResult.getCreatedAt());
    }


    public void upsert(TaskResult taskResult) {
        if (taskResult.getCreatedAt() == null) {
            taskResult.setCreatedAt(System.currentTimeMillis());
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


    public int countByProjectId(long projectId) {
        final String sql = "select count(id) from result where project_id = ?";
        return this.jdbcTemplate.queryForObject(sql, new Object[]{projectId}, int.class);
    }

    public Page<TaskResult> findByProjectId(long projectId, PageRequest request) {
        final String sql = "select id, " + COLUMNS + " from result where project_id = ? order by id desc limit ? offset ?";

        int count = this.countByProjectId(projectId);

        List<TaskResult> taskResults = this.jdbcTemplate.query(sql,
                new Object[]{projectId, request.getPageSize(), request.getOffset()}, new BeanPropertyRowMapper<>(TaskResult.class));

        return new PageImpl<>(taskResults, request, count);
    }


    public List<TaskResult> findByGtId(long id, int limit) {
        final String sql = "select id," + COLUMNS + " from result where id > ? order by id asc limit ?";
        return this.jdbcTemplate.query(sql, new Object[]{id, limit}, new BeanPropertyRowMapper<>(TaskResult.class));
    }

}
