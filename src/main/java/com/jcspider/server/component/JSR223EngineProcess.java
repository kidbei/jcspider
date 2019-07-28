package com.jcspider.server.component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jcspider.server.model.*;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.Fetcher;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class JSR223EngineProcess extends JCProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSR223EngineProcess.class);

    private static final int INSERT_BATCH_SIZE = 100;

    private Map<Long, ScriptEngine> projectEngineCache = new ConcurrentHashMap<>();
    @Value("${process.maxCodeCache:10000}")
    private int maxCodeCache;


    @Override
    public void start() throws ComponentInitException {
        super.start();
    }

    @Override
    void stopProject(long projectId) {
        this.projectEngineCache.remove(projectId);
    }


    @Override
    public void runMethod(long projectId, SimpleTask task) throws RunMethodException {
        ScriptEngine scriptEngine = this.getProjectEngine(projectId);
        Fetcher fetcher = this.fetcherMap.get(task.getFetchType());
        if (fetcher == null) {
            throw new IllegalArgumentException("unknown fetch type:" + task.getFetchType());
        }
        Self self = new Self(projectId);
        Object result;

        if (Constant.METHOD_START.equals(task.getMethod())) {
            try {
                result = ((Invocable)scriptEngine).invokeFunction(task.getMethod(), self, task.getSourceUrl());
            } catch (Exception e) {
                throw new RunMethodException(e, task.getMethod());
            }
        } else {
            FetchResult fetchResult;
            try {
                fetchResult = fetcher.fetch(task);
            } catch (IOException e) {
                throw new RunMethodException("fetch failed, method:" + task.getMethod() + ".reason" + e.getMessage());
            }
            if (!fetchResult.isSuccess()) {
                throw new RunMethodException("fetch failed,http status:" + fetchResult.getStatus(), task.getMethod());
            }
            Response response = new Response(fetchResult.getHeaders(), fetchResult.getContent(), task.getSourceUrl());
            if (StringUtils.isNotBlank(task.getExtra())) {
                response.setExtras(JSON.parseObject(task.getExtra()));
            }
            try {
                result = ((Invocable)scriptEngine).invokeFunction(task.getMethod(), self, response);
            } catch (Exception e) {
                throw new RunMethodException(e, task.getMethod());
            }
        }
        if (CollectionUtils.isNotEmpty(self.getNewTasks())) {
            List<Task> newTasks = this.removeRepeatTask(self.getNewTasks());
            if (CollectionUtils.isNotEmpty(newTasks)) {
                List<List<Task>> batchTaskList = Lists.partition(newTasks, INSERT_BATCH_SIZE);
                batchTaskList.forEach(tasks -> this.taskDao.insertBatch(newTasks));
            }
        } else {
            LOGGER.info("task {} has no new url found", task.getId());
        }
        if (result != null) {
            TaskResult taskResult = new TaskResult();
            taskResult.setTaskId(task.getId());
            taskResult.setProjectId(projectId);
            taskResult.setCreatedAt(System.currentTimeMillis());
            taskResult.setResultText(JSON.toJSONString(result));
            this.resultExporters.forEach(resultExporter -> {
                try {
                    resultExporter.export(taskResult);
                } catch (Exception e) {
                    LOGGER.error("export result error, exporter:{}", resultExporter, e);
                }
            });
        }
    }


    private ScriptEngine getProjectEngine(long projectId) {
        ScriptEngine scriptEngine = this.projectEngineCache.get(projectId);
        if (scriptEngine == null) {
            LOGGER.info("init nashorn script engine for project {}", projectId);
            scriptEngine = new ScriptEngineManager().getEngineByName(Constant.SCRIPT_NASHORN);
            Project project = this.getProject(projectId);
            try {
                scriptEngine.eval(project.getScriptText());
                LOGGER.info("init nashorn script engine complete for project:{}", projectId);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            this.projectEngineCache.put(projectId, scriptEngine);
        }
        return scriptEngine;
    }



    @Override
    public String name() {
        return "jcNashornProcess";
    }
}
