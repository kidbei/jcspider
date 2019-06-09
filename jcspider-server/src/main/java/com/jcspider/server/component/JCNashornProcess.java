package com.jcspider.server.component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.*;
import com.jcspider.server.utils.*;
import org.apache.commons.collections.CollectionUtils;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class JCNashornProcess implements JCComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCNashornProcess.class);

    private static final int INSERT_BATCH_SIZE = 100;

    @Value("${process.maxCodeCache:10000}")
    private int maxCodeCache;
    @Value("${process.threads:10}")
    private int processThreads;
    @Value("${process.result.exporter}")
    private String exportComponents;

    private List<ResultExporter> resultExporters = new ArrayList<>();

    private Map<Long, ProjectCache> projectCacheMap;
    @Autowired
    private ProjectDao              projectDao;
    @Autowired
    private TaskDao                 taskDao;
    @Autowired
    private JCQueue                 jcQueue;
    @Autowired
    private JCRegistry              jcRegistry;

    private String                  localIp;

    private ThreadPoolExecutor      threadPoolExecutor;

    private volatile boolean        isStop = false;

    private final Map<String, Fetcher> fetcherMap = new HashMap<>();

    @Autowired
    private ApplicationContext  applicationContext;

    @Override
    public void start() throws ComponentInitException {
        this.fetcherMap.put(Constant.FETCH_TYPE_HTML, new HttpFetcher());
        projectCacheMap = new ConcurrentHashMap<>(maxCodeCache);
        try {
            this.localIp = IPUtils.getLocalIP();
        } catch (UnknownHostException e) {
            throw new ComponentInitException(e, name());
        }

        List<String> exportComponentList = Arrays.asList(this.exportComponents.split(","));
        if (exportComponentList.contains(Constant.DB_RESULT_EXPORTER)) {
            this.resultExporters.add(applicationContext.getBean(Constant.DB_RESULT_EXPORTER, DbResultExporter.class));
        }

        this.jcRegistry.registerProcess(this.localIp);
        this.threadPoolExecutor = new ThreadPoolExecutor(processThreads, processThreads, Long.MAX_VALUE, TimeUnit.HOURS, new LinkedTransferQueue<>());

        this.threadPoolExecutor.execute(() -> {
            final String topic = Constant.TOPIC_PROCESS_TASK + this.localIp;
            LOGGER.info("subscript topic:{}", topic);
            while (!Thread.interrupted() && !isStop) {
                String taskId = this.jcQueue.blockingPopProcessTask(this.localIp);
                this.processTask(taskId);
            }
        });

        this.threadPoolExecutor.execute(() -> {
            final String topic = Constant.TOPIC_PROCESS_PROJECT_START + this.localIp;
            LOGGER.info("subscript topic:{}", topic);
            while (!Thread.interrupted() && !isStop) {
                Long projectId =  this.jcQueue.blockingPopProcessProjectStart(this.localIp);
                this.startProject(projectId);
            }
        });
    }



    private void processTask(String taskId) {
        LOGGER.info("start to process task:{}", taskId);
        Task task;
        try {
            task = this.taskDao.getById(taskId);
            if (task == null) {
                LOGGER.warn("task not found:{}", taskId);
                return;
            }
            if (task.getStatus().equals(Constant.TASK_STATUS_DONE)) {
                LOGGER.info("task {} is already done", taskId);
                return;
            }
            ProjectCache projectCache = this.getProject(task.getProjectId());
            this.runMethod(projectCache, task);
            this.taskDao.updateStatusAndStackById(taskId, "", Constant.TASK_STATUS_DONE);
        } catch (Exception e) {
            LOGGER.error("process task error:{}", taskId, e);
            this.taskDao.updateStatusAndStackById(taskId, e.toString(), Constant.TASK_STATUS_ERROR);
        }
    }


    private void startProject(Long projectId) {
        LOGGER.info("start project:{}", projectId);
        ProjectCache projectCache = this.getProject(projectId);
        String taskId = IDUtils.genTaskId(projectCache.project.getStartUrl(), "start");
        Task task = this.taskDao.getById(taskId);
        if (task == null) {
            task = new Task();
            task.setId(taskId);
            task.setMethod(Constant.METHOD_START);
            task.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            task.setScheduleType(projectCache.project.getScheduleType());
            task.setScheduleValue(projectCache.project.getScheduleValue());
            task.setSourceUrl(projectCache.project.getStartUrl());
            task.setStatus(Constant.TASK_STATUS_RUNNING);
            task.setProjectId(projectId);
            task.setFetchType(Constant.FETCH_TYPE_HTML);
            this.taskDao.insert(task);
        }
        try {
            this.runMethod(projectCache, task);
            this.taskDao.updateStatusAndStackById(taskId, "", Constant.TASK_STATUS_DONE);
            this.projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_START);
        } catch (RunMethodException e) {
            LOGGER.error("run task error,task:{}", task, e);
            this.taskDao.updateStatusAndStackById(taskId, e.toString(), Constant.TASK_STATUS_ERROR);
        }
    }


    private DebugResult debug(String requestId, String scriptText, SimpleTask simpleTask) {
        final DebugResult debugResult = new DebugResult();
        debugResult.setRequestId(requestId);

        ScriptEngine  scriptEngine = new ScriptEngineManager().getEngineByName(Constant.SCRIPT_NASHORN);
        try {
            scriptEngine.eval(scriptText);
        } catch (ScriptException e) {
            debugResult.setSuccess(false);
            debugResult.setStack(e.toString());
            return debugResult;
        }

        debugResult.setCurrentMethod(simpleTask.getMethod());

        Self self = new Self(0);
        Object result;
        if (Constant.METHOD_START.equals(simpleTask.getMethod())) {
            try {
                result = ((Invocable)scriptEngine).invokeFunction(simpleTask.getMethod(), self, simpleTask.getSourceUrl());
            } catch (Exception e) {
                debugResult.setSuccess(false);
                debugResult.setStack(e.toString());
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
                response = new Response(fetchResult.getHeaders(), fetchResult.getContent());
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
                return debugResult;
            }
            debugResult.setSuccess(true);
            debugResult.setResult(result);
            if (CollectionUtils.isNotEmpty(self.getNewTasks())) {
                debugResult.setSimpleTasks(self.getNewTasks().stream().map(t -> (SimpleTask)t).collect(Collectors.toList()));
            }
        }

        return debugResult;
    }




    private void runMethod(ProjectCache projectCache, SimpleTask task) throws RunMethodException {
        Fetcher fetcher = this.fetcherMap.get(task.getFetchType());
        if (fetcher == null) {
            throw new IllegalArgumentException("unknown fetch type:" + task.getFetchType());
        }
        Self self = new Self(projectCache.project.getId());
        Object result;

        if (Constant.METHOD_START.equals(task.getMethod())) {
            try {
                result = ((Invocable)projectCache.scriptEngine).invokeFunction(task.getMethod(), self, task.getSourceUrl());
            } catch (Exception e) {
                throw new RunMethodException(e, task.getMethod());
            }
        } else {
            FetchResult fetchResult;
            try {
                fetchResult = fetcher.fetch(task);
            } catch (IOException e) {
                throw new RunMethodException("fetch failed", e, task.getMethod());
            }
            if (!fetchResult.isSuccess()) {
                throw new RunMethodException("fetch failed,http status:" + fetchResult.getStatus(), task.getMethod());
            }
            Response response = new Response(fetchResult.getHeaders(), fetchResult.getContent());
            try {
                result = ((Invocable)projectCache.scriptEngine).invokeFunction(task.getMethod(), self, response);
            } catch (Exception e) {
                throw new RunMethodException(e, task.getMethod());
            }
        }
        this.removeRepeatTask(self.getNewTasks());
        if (CollectionUtils.isNotEmpty(self.getNewTasks())) {
            List<List<Task>> batchTaskList = Lists.partition(self.getNewTasks(), INSERT_BATCH_SIZE);
            batchTaskList.forEach(tasks -> this.taskDao.insertBatch(self.getNewTasks()));
        } else {
            LOGGER.info("tast {} has no new url found", task.getId());
        }
        if (result != null) {
            TaskResult taskResult = new TaskResult();
            taskResult.setTaskId(task.getId());
            taskResult.setProjectId(projectCache.project.getId());
            taskResult.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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


    private void removeRepeatTask(List<Task> newTasks) {
        if (CollectionUtils.isEmpty(newTasks)) {
            return;
        }
        List<Task> oldTask = this.taskDao.findByIds(newTasks.stream().map(t -> t.getId()).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(oldTask)) {
            newTasks.removeAll(oldTask);
        }
    }



    private ProjectCache getProject(long projectId) {
        ProjectCache projectCache = this.projectCacheMap.get(projectId);
        if (projectCache == null) {
            Project project = this.projectDao.getById(projectId);
            if (project == null) {
                return null;
            }
            ScriptEngine  scriptEngine = new ScriptEngineManager().getEngineByName(Constant.SCRIPT_NASHORN);
            try {
                scriptEngine.eval(project.getScriptText());
                LOGGER.info("init script engine for project:{}", projectId);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            projectCache = new ProjectCache(project, scriptEngine);
            this.projectCacheMap.put(projectId, projectCache);
        }
        return projectCache;
    }

    class ProjectCache {
        Project project;
        ScriptEngine    scriptEngine;

        public ProjectCache(Project project, ScriptEngine scriptEngine) {
            this.project = project;
            this.scriptEngine = scriptEngine;
        }
    }


    @Override
    public void shutdown() {
        this.isStop = true;
        this.threadPoolExecutor.shutdownNow();
    }

    @Override
    public String name() {
        return "jcNashornProcess";
    }
}
