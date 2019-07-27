package com.jcspider.server.dao;

import com.jcspider.server.model.UserProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@Repository
public class UserProjectDao {
    @Autowired
    private JdbcTemplate    jdbcTemplate;

    private static final String COLUMNS = "uid, role, project_id, created_at, updated_at";

    public void insert(UserProject userProject) {
        if (userProject.getCreatedAt() == null) {
            userProject.setCreatedAt(System.currentTimeMillis());
        }
        if (userProject.getUpdatedAt() == null) {
            userProject.setUpdatedAt(userProject.getCreatedAt());
        }

        final String sql = "insert into user_project (" + COLUMNS + ") values (?,?,?,?,?)";
        this.jdbcTemplate.update(sql, userProject.getUid(),
                userProject.getRole(),
                userProject.getProjectId(),
                userProject.getCreatedAt(),
                userProject.getUpdatedAt());
    }


    public List<UserProject> findByUid(String uid) {
        final String sql = "select id, " + COLUMNS + " from user_project where uid = ?";
        return this.jdbcTemplate.query(sql, new Object[]{uid}, new BeanPropertyRowMapper<>(UserProject.class));
    }

    public UserProject getByUidAndProjectId(String uid, long projectId) {
        final String sql = "select id, " + COLUMNS + " from user_project where uid = ? and project_id = ?";
        try {
            return this.jdbcTemplate.queryForObject(sql, new Object[]{uid, projectId},
                    new BeanPropertyRowMapper<>(UserProject.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public void deleteByProjectId(long projectId) {
        final String sql = "delete from user_project where project_id = ?";
        this.jdbcTemplate.update(sql, projectId);
    }


    public Page<UserProject> findByUid(String uid, Pageable pageable) {
        final String sql = "select id, " + COLUMNS + " from user_project where uid = ?  limit ? offset ?";
        final String countSql = "select count(1) from user_project where uid = ?";

        int count = this.jdbcTemplate.queryForObject(countSql, int.class, uid);
        List<UserProject> result =  this.jdbcTemplate.query(sql, new Object[]{uid, pageable.getPageSize(), pageable.getOffset()},
                new BeanPropertyRowMapper<>(UserProject.class));

        return new PageImpl<>(result, pageable, count);
    }




}
