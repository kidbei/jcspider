package com.jcspider.server.dao;

import com.jcspider.server.model.WebUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@Repository
public class WebUserDao {
    @Autowired
    private JdbcTemplate    jdbcTemplate;

    private static final String COLUMNS = "uid,cn_name,role,token,password,created_at,updated_at";


    public void insert(WebUser webUser) {
        if (webUser.getCreatedAt() == null) {
            webUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        if (webUser.getUpdatedAt() == null) {
            webUser.setUpdatedAt(webUser.getCreatedAt());
        }
        final String sql = "insert into web_user (" + COLUMNS + ") values (?,?,?,?,?,?,?)";
        this.jdbcTemplate.update(sql, webUser.getUid(), webUser.getCnName(), webUser.getRole(),
                webUser.getToken(), webUser.getPassword(), webUser.getCreatedAt(), webUser.getUpdatedAt());
    }


    public WebUser getByUidAndPassword(String uid, String password) {
        final String sql = "select id, " + COLUMNS + " from web_user where uid = ? and password = ? limit 1";
        try {
            return this.jdbcTemplate.queryForObject(sql, new Object[]{uid, password},
                    new BeanPropertyRowMapper<>(WebUser.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public WebUser getByUidAndToken(String uid, String token) {
        final String sql = "select id, " + COLUMNS + " from web_user where uid = ? and token = ? limit 1";
        try {
            return this.jdbcTemplate.queryForObject(sql, new Object[]{uid, token},
                    new BeanPropertyRowMapper<>(WebUser.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}