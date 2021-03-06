package com.jcspider.server.dao;

import com.jcspider.server.model.Project;
import com.jcspider.server.model.ProjectQueryExp;
import com.jcspider.server.model.SqlParam;
import org.apache.commons.lang3.StringUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 03 June 2019
 */
@Repository
public class ProjectDao {

    private String COLUMNS = "name, start_url, script_text, status, dispatcher, created_at, updated_at," +
            " schedule_type, schedule_value, description,qps";

    @Autowired
    private JdbcTemplate    jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate  namedParameterJdbcTemplate;

    public long insert(Project project) {
        if (project.getUpdatedAt() == null) {
            project.setUpdatedAt(System.currentTimeMillis());
        }
        if (project.getCreatedAt() == null) {
            project.setCreatedAt(project.getUpdatedAt());
        }
        final String sql = "insert into project (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?,?,?) RETURNING id";
        return this.jdbcTemplate.queryForObject(sql, new Object[]{project.getName(), project.getStartUrl(),
                project.getScriptText(), project.getStatus(), project.getDispatcher(), project.getCreatedAt(),
                project.getUpdatedAt(), project.getScheduleType(), project.getScheduleValue(), project.getDescription(), project.getQps()}, Long.class);
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


    public void deleteById(long projectId) {
        final String sql = "delete from project where id = ?";
        this.jdbcTemplate.update(sql, projectId);
    }


    public void updateByExp(Project project) {
        final StringBuilder sb = new StringBuilder("update project set ");
        final List<Object> params = new ArrayList<>();
        if (project.getStatus() != null) {
            sb.append("status = ?,");
            params.add(project.getStatus());
        }
        project.setUpdatedAt(System.currentTimeMillis());
        if (project.getDescription() != null) {
            sb.append("description = ?,");
            params.add(project.getDescription());
        }

        if (project.getDispatcher() != null) {
            sb.append("dispatcher = ?,");
            params.add(project.getDispatcher());
        }
        if (project.getScheduleType() != null) {
            sb.append("schedule_type = ?,");
            params.add(project.getScheduleType());
        }
        if (project.getScheduleValue() != null) {
            sb.append("schedule_value = ?,");
            params.add(project.getScheduleValue());
        }
        if (project.getScriptText() != null) {
            sb.append("script_text = ?,");
            params.add(project.getScriptText());
        }
        if (project.getStartUrl() != null) {
            sb.append("start_url = ?,");
            params.add(project.getStartUrl());
        }
        if (project.getName() != null) {
            sb.append("name = ?,");
            params.add(project.getName());
        }
        if (project.getUpdatedAt() != null) {
            sb.append("updated_at = ?,");
            params.add(project.getUpdatedAt());
        }
        if (project.getQps() != null) {
            sb.append("qps = ?,");
            params.add(project.getQps());
        }
        final String sql = sb.substring(0, sb.length() - 1) + " where id = ?";
        params.add(project.getId());
        this.jdbcTemplate.update(sql, params.toArray());
    }


    public int countByExp(ProjectQueryExp exp) {
        SqlParam sqlParam = queryParams("select count(id) from project where 1=1", exp);
        return this.jdbcTemplate.queryForObject(sqlParam.getSql(), sqlParam.toSqlParaqms(), int.class);
    }


    private SqlParam queryParams(String selectPrefix, ProjectQueryExp exp) {
        StringBuilder sqlBuilder = new StringBuilder(selectPrefix).append(" ");
        List<Object> params = new ArrayList<>();
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
        if (StringUtils.isNotBlank(exp.getCreatedAtStart())) {
            sqlBuilder.append("and created_at >= ? ");
            try {
                params.add(this.parseStandardTime(exp.getCreatedAtStart()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (StringUtils.isNotBlank(exp.getCreatedAtEnd())) {
            sqlBuilder.append("and created_at <= ? ");
            try {
                params.add(this.parseStandardTime(exp.getCreatedAtEnd()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (StringUtils.isNotBlank(exp.getUpdatedAtStart())) {
            sqlBuilder.append("and updated_at >= ? ");
            try {
                params.add(this.parseStandardTime(exp.getUpdatedAtStart()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (StringUtils.isNotBlank(exp.getUpdatedAtStart())) {
            sqlBuilder.append("and updated_at <= ? ");
            try {
                params.add(this.parseStandardTime(exp.getUpdatedAtEnd()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return new SqlParam(sqlBuilder.toString(), params);
    }


    private Long parseStandardTime(String timeStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(timeStr);
        return date.getTime();
    }


    public Page<Project> queryByExp(ProjectQueryExp exp, Pageable pageable) {
        String selectPrefix = "select id," + COLUMNS + " from project where 1=1";
        SqlParam sqlParam = this.queryParams(selectPrefix, exp);

        sqlParam.addParam(pageable.getPageSize());
        sqlParam.addParam(pageable.getOffset());

        String sql = sqlParam.appendSql(" order by id desc limit ? offset ?");

        int count = this.countByExp(exp);

        List<Project> result = this.jdbcTemplate.query(sql, sqlParam.toSqlParaqms(), new BeanPropertyRowMapper<>(Project.class));
        return new PageImpl<>(result, pageable, count);
    }

    public List<Project> findByNameList(String name, int limit) {
        final String sql = "select id, " + COLUMNS + " from project where name like ? limit ?";
        return this.jdbcTemplate.query(sql, new Object[]{"%" + name + "%", limit}, new BeanPropertyRowMapper<>(Project.class));
    }


    public List<Project> findByDispatcher(String dispatcher) {
        final String sql = "select id," + COLUMNS + " from project where dispatcher = ?";
        return this.jdbcTemplate.query(sql, new Object[]{dispatcher}, new BeanPropertyRowMapper<>(Project.class));
    }

    public List<Project> findByStatus(String status) {
        final String sql = "select id," + COLUMNS + " from project where status = ?";
        return this.jdbcTemplate.query(sql, new Object[]{status}, new BeanPropertyRowMapper<>(Project.class));
    }

    public List<Project> findAll() {
        final String sql = "select id," + COLUMNS + " from project";
        return this.jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Project.class));
    }

}
