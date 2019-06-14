package com.jcspider.server.dao;

import com.jcspider.server.model.Project;
import com.jcspider.server.model.ProjectQueryExp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 03 June 2019
 */
@Repository
public class ProjectDao {

    private String COLUMNS = "name, start_url, script_text, status, rate_unit, rate_number, dispatcher, created_at, updated_at," +
            " schedule_type, schedule_value, rate_unit_multiple, description";

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
        final String sql = "insert into project (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id";
        return this.jdbcTemplate.queryForObject(sql, new Object[]{project.getName(), project.getStartUrl(),
                project.getScriptText(), project.getStatus(), project.getRateUnit(),
                project.getRateNumber(), project.getDispatcher(), project.getCreatedAt(),
                project.getUpdatedAt(), project.getScheduleType(), project.getScheduleValue(),
                project.getRateUnitMultiple(), project.getDescription()}, Long.class);
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


    public Page<Project> queryByExp(ProjectQueryExp exp, Pageable pageable) {
        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder("select id,").append(COLUMNS).append(" from project where 1=1");
        StringBuilder sqlBuilder = new StringBuilder();
        if (exp.getProjectId() != null) {
            sqlBuilder.append("and project_id = ? ");
            params.add(exp.getProjectId());
        }
        if (exp.getStatus() != null) {
            sqlBuilder.append("and status = ? ");
            params.add(exp.getStatus());
        }
        if (exp.getName() != null) {
            sqlBuilder.append("and name like ?");
            params.add("%" + exp.getName() + "%");
        }
        if (exp.getDescription() != null) {
            sqlBuilder.append("and description like ?");
            params.add("%" + exp.getDescription() + "%");
        }
        if (exp.getUid() != null) {
            sqlBuilder.append("and id in (select project_id from user_project where uid = ?) ");
            params.add(exp.getUid());
        }
        String countSql = "select count(1) from project where 1=1 " + sqlBuilder.toString();
        int count = this.jdbcTemplate.queryForObject(countSql, params.toArray(), int.class);
        sb.append(sqlBuilder).append("limit ? offset ? order by id asc");
        params.add(pageable.getPageSize(), pageable.getOffset());

        List<Project> result = this.jdbcTemplate.query(sb.toString(), params.toArray(),
                new BeanPropertyRowMapper<>(Project.class));
        return new PageImpl<>(result, pageable, count);
    }

}
