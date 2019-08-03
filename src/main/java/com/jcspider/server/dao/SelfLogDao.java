package com.jcspider.server.dao;

import com.jcspider.server.model.LogQueryExp;
import com.jcspider.server.model.SelfLog;
import com.jcspider.server.model.SqlParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Gosin
 * @Date: 2019-08-03 22:03
 */
@Repository
public class SelfLogDao {

    private static final String COLUMNS = "level,project_id,task_id,log_time,log_text";

    @Autowired
    private JdbcTemplate    jdbcTemplate;

    public void insert(SelfLog selfLog) {
        final String sql = "insert into self_log(" + COLUMNS + ") values(?,?,?,?,?)";
        this.jdbcTemplate.update(sql,
                selfLog.getLevel(),
                selfLog.getProjectId(),
                selfLog.getTaskId(),
                selfLog.getLogTime(),
                selfLog.getLogText());
    }

    public void insert(List<SelfLog> selfLogs) {
        final String sql = "insert into self_log(" + COLUMNS + ") values(?,?,?,?,?)";
        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SelfLog selfLog = selfLogs.get(i);
                ps.setString(1, selfLog.getLevel());
                ps.setLong(2, selfLog.getProjectId());
                ps.setString(3, selfLog.getTaskId());
                ps.setLong(4, selfLog.getLogTime());
                ps.setString(5, selfLog.getLogText());
            }

            @Override
            public int getBatchSize() {
                return selfLogs.size();
            }
        });
    }


    private SqlParam queryParams(String selectPrefix, LogQueryExp exp) {
        StringBuilder sb = new StringBuilder(selectPrefix).append(" ");
        List<Object> params = new ArrayList<>();
        if (exp.getLevel() != null) {
            sb.append("and level = ? ");
            params.add(exp.getLevel());
        }
        if (exp.getLogTimeStart() != null) {
            sb.append("and log_time >= ? ");
            try {
                params.add(this.parseStandardTime(exp.getLogTimeStart()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (exp.getLogTimeEnd() != null) {
            sb.append("and log_time <= ? ");
            try {
                params.add(this.parseStandardTime(exp.getLogTimeEnd()));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if (exp.getProjectId() != null) {
            sb.append("and project_id = ? ");
            params.add(exp.getProjectId());
        }
        if (exp.getTaskId() != null) {
            sb.append("and task_id = ? ");
            params.add(exp.getTaskId());
        }
        return new SqlParam(sb.toString(), params);
    }

    public int countByExp(LogQueryExp exp) {
        SqlParam sqlParam = this.queryParams("select count(id) from self_log where 1=1", exp);
        return this.jdbcTemplate.queryForObject(sqlParam.getSql(), sqlParam.toSqlParaqms(), int.class);
    }

    public Page<SelfLog> queryByExp(LogQueryExp exp, Pageable pageable) {
        final String selectPrefix = "select id," + COLUMNS + " from self_log where 1=1";
        SqlParam sqlParam = this.queryParams(selectPrefix, exp);
        sqlParam.addParam(pageable.getPageSize());
        sqlParam.addParam(pageable.getOffset());

        String sql = sqlParam.appendSql(" order by id desc limit ? offset ?");
        int count = this.countByExp(exp);

        List<SelfLog> result = this.jdbcTemplate.query(sql, sqlParam.toSqlParaqms(), new BeanPropertyRowMapper<>(SelfLog.class));
        return new PageImpl<>(result, pageable, count);
    }


    private Long parseStandardTime(String timeStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(timeStr);
        return date.getTime();
    }
}
