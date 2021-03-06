package com.jcspider;

import java.io.IOException;

import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.model.Project;
import com.jcspider.server.starter.JCSpiderApplication;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.IPUtils;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JCSpiderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppTest {

    @Autowired
    private JdbcTemplate    jdbcTemplate;
    @Autowired
    private ProjectDao  projectDao;
    @Autowired
    private JCQueue jcQueue;

    @Before
    public void init() throws IOException {
        this.jdbcTemplate.update("truncate table project");
        this.jdbcTemplate.update("truncate table task");
        this.jdbcTemplate.update("truncate table result");
        this.jdbcTemplate.update("truncate table project_process_node");

        String scriptText = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("src/main/resources/script.js"));
        Project project = new Project();
        project.setName("开源中国");
        project.setStartUrl("https://www.oschina.net/news/project");
        project.setScheduleType(Constant.SCHEDULE_TYPE_NONE);
        project.setScriptText(scriptText);
        project.setStatus(Constant.PROJECT_STATUS_STOP);
        project.setDispatcher(IPUtils.getLocalIP());
        projectDao.insert(project);
    }


    @After
    public void shutdown() {
        this.jdbcTemplate.update("truncate table project");
        this.jdbcTemplate.update("truncate table task");
        this.jdbcTemplate.update("truncate table result");
        this.jdbcTemplate.update("truncate table project_process_node");
    }

}
