package com.jcspider;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.component.JCQueue;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.model.*;
import com.jcspider.server.starter.JCSpiderApplication;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.IPUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Unit test for simple App.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JCSpiderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiTest {

    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        String url = String.format("http://localhost:%d/api/debug/task", port);
        System.out.println(String.format("port is : [%d]", port));
        this.base = new URL(url);
    }

    @Test
    public void test_request() throws IOException {
        String scriptText = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("shangwubu.js"));
        DebugTask debugTask = new DebugTask();
        SimpleTask simpleTask = new SimpleTask();
        simpleTask.setMethod("start");
        debugTask.setScriptText(scriptText);
        debugTask.setSimpleTask(simpleTask);
        System.out.println(JSON.toJSONString(debugTask));
    }

    @Test
    public void test_debug() throws IOException, URISyntaxException {
        String scriptText = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("shangwubu.js"));
        DebugTask debugTask = new DebugTask();
        SimpleTask simpleTask = new SimpleTask();
        simpleTask.setMethod("start");
        debugTask.setScriptText(scriptText);
        debugTask.setSimpleTask(simpleTask);
        ResponseEntity<JSONResult> responseEntity =
                this.restTemplate.postForEntity(base.toURI(), debugTask, JSONResult.class);
        Assert.assertEquals(responseEntity.getStatusCode().value(), 200);
        Assert.assertTrue(responseEntity.getBody().isSuccess());
    }

}
