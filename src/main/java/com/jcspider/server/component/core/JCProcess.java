package com.jcspider.server.component.core;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.component.core.event.NewTask;
import com.jcspider.server.component.ifc.JCComponent;
import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.component.ifc.JCRegistry;
import com.jcspider.server.component.ifc.ResultExporter;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.*;
import com.jcspider.server.utils.*;
import com.jcspider.server.web.api.service.SelfLogService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.util.internal.LinkedTransferQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu
 * @since 24 June 2019
 */
public abstract class JCProcess implements JCComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCProcess.class);

    @Value("${process.threads:10}")
    private int                     processThreads;
    @Autowired
    protected ProjectDao              projectDao;
    @Autowired
    protected TaskDao                 taskDao;
    @Autowired
    protected JCQueue                 jcQueue;
    @Autowired
    protected JCRegistry                jcRegistry;

    @Autowired
    protected ApplicationContext    applicationContext;
    @Autowired
    private SelfLogService          selfLogService;

    protected String                  localIp;

    protected ThreadPoolExecutor      threadPoolExecutor;

    protected volatile boolean        isStop = false;

    protected final Map<String, Fetcher> fetcherMap = new HashMap<>();



    private final ConcurrentHashMap<Long, Project>  projectMap = new ConcurrentHashMap<>();


    @Override
    public void start() throws ComponentInitException {
        this.fetcherMap.put(Constant.FETCH_TYPE_HTML, new HttpFetcher());
        this.fetcherMap.put(Constant.FETCH_TYPE_JS, new AjaxFetcher());
        try {
            this.localIp = IPUtils.getLocalIP();
        } catch (UnknownHostException e) {
            throw new ComponentInitException(e, name());
        }
        this.jcRegistry.registerProcess(this.localIp);
        this.threadPoolExecutor = new ThreadPoolExecutor(processThreads, processThreads, Long.MAX_VALUE, TimeUnit.HOURS, new LinkedTransferQueue<>());

    }



    public void processTask(String taskId) {
        LOGGER.info("start to process task:{}", taskId);
        Task task;
        try {
            task = this.taskDao.getById(taskId);
            if (task == null) {
                LOGGER.warn("task not found:{}", taskId);
                return;
            }
            this.runMethod(task.getProjectId(), task);
            Task update = new Task(taskId, Constant.TASK_STATUS_DONE);
            this.taskDao.upgrade(update);
        } catch (Exception e) {
            LOGGER.error("process task error:{}", taskId, e);
            this.taskDao.updateStatusAndStackById(taskId, e.getMessage(), Constant.TASK_STATUS_ERROR);
        }
    }


    private void runMethod(long projectId, SimpleTask task) throws RunMethodException{
        LOGGER.info("run method,projectId:{},method:{}", projectId, task.getMethod());
        Fetcher fetcher = this.fetcherMap.get(task.getFetchType());
        if (fetcher == null) {
            throw new IllegalArgumentException("unknown fetch type:" + task.getFetchType());
        }
        Self self = new Self(projectId, task.getId());
        Object result;

        if (Constant.METHOD_START.equals(task.getMethod())) {
            try {
                result = this.runMethod(projectId, task.getMethod(), self, task.getSourceUrl());
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
                result = this.runMethod(projectId, task.getMethod(), self, response);
            } catch (Exception e) {
                this.selfLogService.addLog(projectId, Constant.LEVEL_ERROR,
                        "函数执行失败,project:" + projectId + ",url:" + task.getSourceUrl() + ",method:" + task.getMethod() + "error:" + e.getMessage());
                throw new RunMethodException(e, task.getMethod());
            }
        }
        if (self.hasNewTasks()) {
            self.getNewTasks().forEach(newTask -> this.jcQueue.publish(Constant.TOPIC_NEW_TASK, new NewTask(newTask)));
        } else {
            LOGGER.info("task {} has no new url found", task.getId());
            if (result == null) {
                this.selfLogService.addLog(projectId, Constant.LEVEL_ERROR,
                        "没有嗅探到新的URL,项目:" + projectId + ",url:" + task.getSourceUrl() + ",method:" + task.getMethod());
            }
        }
        if (result != null) {
            TaskResult taskResult = new TaskResult();
            taskResult.setTaskId(task.getId());
            taskResult.setProjectId(projectId);
            taskResult.setCreatedAt(System.currentTimeMillis());
            taskResult.setResultText(JSON.toJSONString(result));

            jcQueue.publish(Constant.TOPC_EXPORT_RESULT, taskResult);

        }
    }


    abstract Object runMethod(long projectId, String method, Self self, Object params) throws RunMethodException;

    public void startProject(Long projectId) {
        LOGGER.info("start project:{}", projectId);
        Project project = this.getProject(projectId);
        String taskId = IDUtils.genTaskId(projectId, project.getStartUrl(), "start");
        Task task = this.taskDao.getById(taskId);
        if (task == null) {
            task = new Task();
            task.setId(taskId);
            task.setMethod(Constant.METHOD_START);
            task.setCreatedAt(System.currentTimeMillis());
            task.setScheduleType(project.getScheduleType());
            task.setScheduleValue(project.getScheduleValue());
            task.setSourceUrl(project.getStartUrl());
            task.setStatus(Constant.TASK_STATUS_RUNNING);
            task.setProjectId(projectId);
            task.setFetchType(Constant.FETCH_TYPE_HTML);
            this.taskDao.insert(task);
        }
        try {
            this.runMethod(projectId, task);
            this.taskDao.updateStatusAndStackById(taskId, "", Constant.TASK_STATUS_DONE);
            this.projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_START);
        } catch (RunMethodException e) {
            LOGGER.error("run task error,task:{}", task, e);
            this.taskDao.updateStatusAndStackById(taskId, e.getMessage(), Constant.TASK_STATUS_ERROR);
        }
    }


    public DebugResult debug(String requestId, String scriptText, SimpleTask simpleTask) {
        final DebugResult debugResult = new DebugResult();
        debugResult.setRequestId(requestId);

        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(Constant.SCRIPT_NASHORN);
        try {
            scriptEngine.eval(scriptText);
        } catch (ScriptException e) {
            debugResult.setSuccess(false);
            debugResult.setStack(e.toString());
            return debugResult;
        }

        debugResult.setCurrentMethod(simpleTask.getMethod());

        Self self = new Self(0, simpleTask.getId());
        Object result;
        if (Constant.METHOD_START.equals(simpleTask.getMethod())) {
            try {
                result = ((Invocable)scriptEngine).invokeFunction(simpleTask.getMethod(), self, simpleTask.getSourceUrl());
                debugResult.setResult(result);
                debugResult.setSuccess(true);
                debugResult.setSimpleTasks(self.getNewTasks());
                debugResult.setLogs(self.getLogs());
            } catch (Exception e) {
                debugResult.setSuccess(false);
                debugResult.setStack(e.toString());
                debugResult.setLogs(self.getLogs());
                return debugResult;
            }
        } else {
            Fetcher fetcher = this.fetcherMap.get(simpleTask.getFetchType());
            if (fetcher == null) {
                debugResult.setSuccess(false);
                debugResult.setStack("unknown fetchType:" + simpleTask.getFetchType());
                return debugResult;
            }
            Response response;
            try {
                FetchResult fetchResult = fetcher.fetch(simpleTask);
                response = new Response(fetchResult.getHeaders(), fetchResult.getContent(), simpleTask.getSourceUrl());
                if (StringUtils.isNotBlank(simpleTask.getExtra())) {
                    response.setExtras(JSON.parseObject(simpleTask.getExtra()));
                }
                if (fetchResult.getStatus() >= 400) {
                    debugResult.setSuccess(false);
                    debugResult.setStack("remote response http status:" + fetchResult.getStatus());
                    debugResult.setLogs(self.getLogs());
                    return debugResult;
                }
            } catch (IOException e) {
                debugResult.setSuccess(false);
                debugResult.setStack(e.toString());
                return debugResult;
            }
            try {
                result = ((Invocable)scriptEngine).invokeFunction(simpleTask.getMethod(), self, response);
            } catch (Exception e) {
                debugResult.setSuccess(false);
                debugResult.setStack(e.toString());
                debugResult.setLogs(self.getLogs());
                return debugResult;
            }
            debugResult.setSuccess(true);
            debugResult.setResult(result);
            debugResult.setSimpleTasks(self.getNewTasks());
            debugResult.setLogs(self.getLogs());
            if (CollectionUtils.isNotEmpty(self.getNewTasks())) {
                debugResult.setSimpleTasks(self.getNewTasks().stream().map(t -> (SimpleTask)t).collect(Collectors.toList()));
            }
        }

        return debugResult;
    }



    protected Project getProject(long projectId) {
        Project project = this.projectMap.get(projectId);
        if (project == null) {
            project = this.projectDao.getById(projectId);
            if (project != null) {
                this.projectMap.put(projectId, project);
            }
        }
        return project;
    }



    @Override
    public void shutdown() {
        this.isStop = true;
        this.threadPoolExecutor.shutdownNow();
        this.fetcherMap.values().forEach(fetcher -> fetcher.shutdown());
    }



}
