package com.jcspider;

import com.jcspider.server.component.JCQueue;
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

import java.io.IOException;

/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JCSpiderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class 商务部 {

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

        String scriptText = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("shangwubu.js"));
        Project project = new Project();
        project.setName("商务部部令公告");
        project.setStartUrl("http://www.mofcom.gov.cn/article/b/c/?");
        project.setRateNumber(1);
        project.setRateUnit(Constant.UNIT_TYPE_SECONDS);
        project.setScheduleType(Constant.SCHEDULE_TYPE_NONE);
        project.setScriptText(scriptText);
        project.setStatus(Constant.PROJECT_STATUS_STOP);
        project.setDispatcher(IPUtils.getLocalIP());
        projectDao.insert(project);
    }

    @Test
    public void test_start() {
        jcQueue.pub(Constant.TOPIC_DISPATCHER_PROJECT_START, 1l);
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void shutdown() {
        this.jdbcTemplate.update("truncate table project");
        this.jdbcTemplate.update("truncate table task");
        this.jdbcTemplate.update("truncate table result");
        this.jdbcTemplate.update("truncate table project_process_node");
    }

}
