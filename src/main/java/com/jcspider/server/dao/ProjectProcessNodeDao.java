package com.jcspider.server.dao;

import com.jcspider.server.model.ProjectProcessNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
@Repository
public class ProjectProcessNodeDao {

    private static final String COLUMNS = "project_id,process_node,created_at";

    @Autowired
    private JdbcTemplate    jdbcTemplate;


    public void insert(ProjectProcessNode projectProcessNode) {
        if (projectProcessNode.getCreatedAt() == null) {
            projectProcessNode.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        final String sql = "insert into project_process_node (" + COLUMNS + ") values (?,?,?)";
        this.jdbcTemplate.update(sql,
                projectProcessNode.getProjectId(), projectProcessNode.getProcessNode(),
                projectProcessNode.getCreatedAt());
    }


    public void insertBatch(List<ProjectProcessNode> projectProcessNodes) {
        final String sql = "insert into project_process_node (" + COLUMNS + ") values (?,?,?)";
        this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                ProjectProcessNode projectProcessNode = projectProcessNodes.get(i);
                preparedStatement.setLong(1, projectProcessNode.getProjectId());
                preparedStatement.setString(2, projectProcessNode.getProcessNode());
                preparedStatement.setTimestamp(3, projectProcessNode.getCreatedAt());
            }

            @Override
            public int getBatchSize() {
                return projectProcessNodes.size();
            }
        });
    }

    public List<ProjectProcessNode> findByProjectId(long projectId) {
        final String sql = "select id, " + COLUMNS + " from project_process_node where project_id = ?";
        return this.jdbcTemplate.query(sql, new Object[]{projectId}, new BeanPropertyRowMapper<>(ProjectProcessNode.class));
    }


    public void deleteByProjectId(long projectId) {
        final String sql = "delete from project_process_node where project_id = ?";
        this.jdbcTemplate.update(sql, projectId);
    }

}
