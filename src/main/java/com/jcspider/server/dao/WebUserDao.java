package com.jcspider.server.dao;

import com.jcspider.server.model.SqlParam;
import com.jcspider.server.model.UserQueryExp;
import com.jcspider.server.model.WebUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@Repository
public class WebUserDao {
    @Autowired
    private JdbcTemplate    jdbcTemplate;

    private static final String COLUMNS = "uid,cn_name,role,token,password,created_at,updated_at,invite_uid, token_created_at";


    public void insert(WebUser webUser) {
        if (webUser.getCreatedAt() == null) {
            webUser.setCreatedAt(System.currentTimeMillis());
        }
        if (webUser.getUpdatedAt() == null) {
            webUser.setUpdatedAt(webUser.getCreatedAt());
        }
        final String sql = "insert into web_user (" + COLUMNS + ") values (?,?,?,?,?,?,?,?,?)";
        this.jdbcTemplate.update(sql, webUser.getUid(), webUser.getCnName(), webUser.getRole(),
                webUser.getToken(), webUser.getPassword(), webUser.getCreatedAt(), webUser.getUpdatedAt(),
                webUser.getInviteUid(), webUser.getTokenCreatedAt());
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

    public void updateTokenById(long id, String token) {
        final String sql = "update web_user set token = ? where id = ?";
        this.jdbcTemplate.update(sql, token, id);
    }


    public WebUser getByUid(String uid) {
        final String sql = "select id, " + COLUMNS + " from web_user where uid = ? limit 1";
        try {
            return this.jdbcTemplate.queryForObject(sql, new Object[]{uid},
                    new BeanPropertyRowMapper<>(WebUser.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void deleteById(long id) {
        final String sql = "delete from web_user where id = ?";
        this.jdbcTemplate.update(sql, id);
    }


    private SqlParam queryParams(String selectPrefix, UserQueryExp exp) {
        StringBuilder sb = new StringBuilder(selectPrefix).append(" ");
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotBlank(exp.getCnName())) {
            sb.append("and cn_name like ? ");
            params.add("%" + exp.getCnName() + "%");
        }
        if (StringUtils.isNotBlank(exp.getUid())) {
            sb.append("and uid = ? ");
            params.add(exp.getUid());
        }
        if (StringUtils.isNotBlank(exp.getRole())) {
            sb.append("and role = ? ");
            params.add(exp.getRole());
        }
        if (StringUtils.isNotBlank(exp.getInviteUid())) {
            sb.append("and invite_uid = ? ");
            params.add(exp.getInviteUid());
        }
        return new SqlParam(sb.toString(), params);
    }

    int count(UserQueryExp exp) {
        SqlParam sqlParam = this.queryParams("select count(id) from web_user where 1 = 1", exp);
        return this.jdbcTemplate.queryForObject(sqlParam.getSql(), sqlParam.toSqlParaqms(), int.class);
    }

    public Page<WebUser> queryByExp(UserQueryExp exp, Pageable pageable) {
        SqlParam sqlParam = this.queryParams("select " + COLUMNS + " from web_user where 1 = 1", exp);
        sqlParam.appendSql(" order by id asc limit ? offset ?");
        sqlParam.addParam(pageable.getPageSize());
        sqlParam.addParam(pageable.getOffset());

        int count = this.count(exp);
        List<WebUser> result = this.jdbcTemplate.query(sqlParam.getSql(), sqlParam.toSqlParaqms(), new BeanPropertyRowMapper<>(WebUser.class));
        return new PageImpl<>(result, pageable, count);
    }


    public void updateByExp(WebUser user) {
        if (user.getId() == null) {
            throw new NullPointerException("id must not be null");
        }
        user.setUpdatedAt(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder("update web_user set ");
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotBlank(user.getRole())) {
            sb.append("role = ?,");
            params.add(user.getRole());
        }
        if (StringUtils.isNotBlank(user.getCnName())) {
            sb.append("cn_name = ?,");
            params.add(user.getCnName());
        }
        if (StringUtils.isNotBlank(user.getPassword())) {
            sb.append("password = ?,");
            params.add(user.getPassword());
        }
        if (StringUtils.isNotBlank(user.getToken())) {
            sb.append("token = ?,");
            params.add(user.getToken());
        }
        if (user.getUpdatedAt() != null) {
            sb.append("updated_at = ?,");
            params.add(user.getUpdatedAt());
        }
        if (user.getTokenCreatedAt() != null) {
            sb.append("token_created_at = ?,");
            params.add(user.getTokenCreatedAt());
        }
        String sql = sb.substring(0, sb.length() - 1) + " where id = ?";
        params.add(user.getId());
        this.jdbcTemplate.update(sql, params.toArray());
    }

}
