package com.jcspider.server.dao;

import com.jcspider.server.model.SqlParam;
import com.jcspider.server.model.Task;
import com.jcspider.server.model.TaskQueryExp;
import com.jcspider.server.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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

    private static final String COLUMNS = "id,status,method,source_url,schedule_type," +
            "stack,project_id,schedule_value,headers,extra,fetch_type," +
            "proxy,created_at,updated_at, charset";


    public void insert(Task task) {
        if (task.getUpdatedAt() == null) {
            task.setUpdatedAt(System.currentTimeMillis());
        }
        final String sql = "insert into task (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        this.jdbcTemplate.update(sql, task.getId(), task.getStatus(), task.getMethod(), task.getSourceUrl(), task.getScheduleType(),
                task.getStack(), task.getProjectId(), task.getScheduleValue(), task.getHeaders(), task.getExtra(), task.getFetchType(),
                task.getProxy(), task.getCreatedAt(), task.getUpdatedAt(), task.getCharset());
    }


    public void insertBatch(List<Task> tasks) {
        tasks.forEach(t -> {
            if (t.getUpdatedAt() == null) {
                t.setUpdatedAt(System.currentTimeMillis());
            }
        });
        final String sql = "insert into task (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                ps.setLong(13, task.getCreatedAt());
                ps.setLong(14, task.getUpdatedAt());
                ps.setString(15, task.getCharset());
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


    public int updateStatusAndStackById(String taskId, String stack, String status) {
        final String sql = "update task set stack = ?, status = ?, updated_at = ? where id = ?";
        return this.jdbcTemplate.update(sql, stack, status, System.currentTimeMillis(), taskId);
    }

    public List<Task> findByIds(Collection<String> taskIds) {
        final String sql = "select " + COLUMNS + " from task where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", taskIds);
        return this.namedParameterJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Task.class));
    }

    public void updateStatusByIds(Collection<String> taskIds, String status) {
        final String sql = "update task set status = :status, updated_at = :updatedAt where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("status", status);
        parameters.addValue("ids", taskIds);
        parameters.addValue("updatedAt", System.currentTimeMillis());
        this.namedParameterJdbcTemplate.update(sql, parameters);
    }

    public List<Task> findByProjectIdAndStatus(long projectId, String status, int limit) {
        final String sql = "select " + COLUMNS + " from task where project_id = ? and status = ? limit ?";
        return this.jdbcTemplate.query(sql, new Object[]{projectId, status, limit}, new BeanPropertyRowMapper<>(Task.class));
    }

    public List<Task> findByOutOfNextRunTime(long projectId,long now, int limit) {
        final String sql = "select " + COLUMNS + " from task where project_id = ? and status not in(?,?) and next_run_time <= ? and next_run_time != 0 limit ?";
        return this.jdbcTemplate.query(sql,
                new Object[]{projectId, Constant.TASK_STATUS_DONE, Constant.TASK_STATUS_ERROR, now, limit},
                new BeanPropertyRowMapper<>(Task.class));
    }


    public void upgrade(Task task) {
        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder("update task set ");
        if (task.getStatus() != null) {
            sb.append("status = ?,");
            params.add(task.getStatus());
        }
        if (task.getStack() != null) {
            sb.append("stack = ?,");
            params.add(task.getStack());
        }
        if (task.getFetchType() != null) {
            sb.append("fetch_type = ?,");
            params.add(task.getFetchType());
        }
        if (task.getMethod() != null) {
            sb.append("method = ?,");
            params.add(task.getMethod());
        }
        if (task.getProxy() != null) {
            sb.append("proxy = ?,");
            params.add(task.getProxy());
        }
        if (task.getCreatedAt() != null) {
            sb.append("created_at = ?,");
            params.add(task.getCreatedAt());
        }
        if (task.getProjectId() != null) {
            sb.append("project_id = ?,");
            params.add(task.getProjectId());
        }
        if (task.getScheduleType() != null) {
            sb.append("schedule_type = ?,");
            params.add(task.getScheduleType());
        }
        if (task.getScheduleValue() != null) {
            sb.append("schedule_value = ?,");
            params.add(task.getScheduleValue());
        }
        if (task.getCharset() != null) {
            sb.append("charset = ?,");
            params.add(task.getCharset());
        }
        if (task.getExtra() != null) {
            sb.append("extra = ?,");
            params.add(task.getExtra());
        }
        if (task.getHeaders() != null) {
            sb.append("headers = ?,");
            params.add(task.getHeaders());
        }
        if (task.getSourceUrl() != null) {
            sb.append("source_url = ?,");
            params.add(task.getSourceUrl());
        }
        params.add(task.getId());
        String sql = sb.substring(0, sb.length() - 1) + " where id = ?";
        this.jdbcTemplate.update(sql, params.toArray());
    }


    public Page<Task> findByExp(TaskQueryExp exp, Pageable pageable) {
        SqlParam sqlParam = this.queryParams("select id," + COLUMNS + " from task where 1=1", exp);
        sqlParam.appendSql(" limit ? offset ?");
        sqlParam.addParam(pageable.getPageSize());
        sqlParam.addParam(pageable.getOffset());

        int count = this.countByExp(exp);

        List<Task> result =  this.jdbcTemplate.query(sqlParam.getSql(),
                sqlParam.toSqlParaqms(), new BeanPropertyRowMapper<>(Task.class));
        Page<Task> page = new PageImpl<>(result, pageable, count);
        return page;
    }



    private SqlParam queryParams(String selectPrefix, TaskQueryExp exp) {
        StringBuilder sb = new StringBuilder(selectPrefix).append(" ");
        List<Object> params = new ArrayList<>();
        if (exp.getProjectId() != null) {
            sb.append("and project_id = ?");
            params.add(exp.getProjectId());
        }
        if (exp.getStatus() != null) {
            sb.append("and status = ?");
            params.add(exp.getStatus());
        }
        return new SqlParam(sb.toString(), params);
    }



    public int countByExp(TaskQueryExp exp) {
        SqlParam sqlParam = this.queryParams("select count(id) from task where 1=1", exp);
        return this.jdbcTemplate.queryForObject(sqlParam.getSql(), sqlParam.toSqlParaqms(), int.class);
    }

    public void deleteByProjectId(long projectId) {
        final String sql = "delete from task where project_id = ?";
        this.jdbcTemplate.update(sql, projectId);
    }

    public void deleteByIds(Collection<String> taskIds) {
        final String sql = "delete from task where id in (:ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", taskIds);
        this.namedParameterJdbcTemplate.update(sql, parameters);
    }
}
