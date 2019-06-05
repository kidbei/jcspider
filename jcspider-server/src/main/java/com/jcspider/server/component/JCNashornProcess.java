package com.jcspider.server.component;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.dao.TaskResultDao;
import com.jcspider.server.model.*;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.Fetcher;
import com.jcspider.server.utils.HttpFetcher;
import com.jcspider.server.utils.IPUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.jboss.netty.util.internal.LinkedTransferQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private Map<Long, ProjectCache> projectCacheMap;
    @Autowired
    private ProjectDao              projectDao;
    @Autowired
    private TaskDao                 taskDao;
    @Autowired
    private TaskResultDao           taskResultDao;
    @Autowired
    private JCQueue                 jcQueue;
    @Autowired
    private JCRegistry              jcRegistry;

    private String                  localIp;

    private ThreadPoolExecutor      threadPoolExecutor;

    private volatile boolean        isStop = false;

    private final Map<String, Fetcher> fetcherMap = new HashMap<>();



    @Override
    public void start() throws ComponentInitException {
        this.fetcherMap.put(Constant.FETCH_TYPE_HTML, new HttpFetcher());
        projectCacheMap = new ConcurrentHashMap<>(maxCodeCache);
        try {
            this.localIp = IPUtils.getLocalIP();
        } catch (UnknownHostException e) {
            throw new ComponentInitException(e, name());
        }
        this.jcRegistry.registerProcess(this.localIp);
        this.threadPoolExecutor = new ThreadPoolExecutor(1, processThreads, 1L, TimeUnit.HOURS, new LinkedTransferQueue<>());

        this.threadPoolExecutor.execute(() -> {
            while (!Thread.interrupted() && !isStop) {
                String taskId = (String) this.jcQueue.bPop(Constant.TOPIC_PROCESS_TASK_SUB + this.localIp);
                this.processTask(taskId);
            }
        });

        this.threadPoolExecutor.execute(() -> {
            while (!Thread.interrupted() && !isStop) {
                Long projectId =  Long.valueOf(this.jcQueue.bPop(Constant.TOPIC_PROCESS_PROJECT_START + this.localIp).toString());
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
            ProjectCache projectCache = this.getProject(task.getProjectId());
            this.runMethod(projectCache, task);
        } catch (Exception e) {
            LOGGER.error("process task error:{}", taskId, e);
            this.taskDao.updateStatusAndStackById(taskId, e.toString(), Constant.TASK_STATUS_ERROR);
        }
    }


    private void startProject(Long projectId) {
        LOGGER.info("start project:{}", projectId);
        ProjectCache projectCache = this.getProject(projectId);
        String taskId = this.genTaskId(projectCache.project.getStartUrl(), "start");
        Task task = this.taskDao.getById(taskId);
        if (task == null) {
            task = new Task();
            task.setId(taskId);
            task.setMethod("start");
            task.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            task.setScheduleType(projectCache.project.getScheduleType());
            task.setScheduleValue(projectCache.project.getScheduleValue());
            task.setSourceUrl(projectCache.project.getStartUrl());
            task.setStatus(Constant.TASK_STATUS_RUNNING);
            task.setProjectId(projectId);
            this.taskDao.insert(task);
        }
        try {
            this.runMethod(projectCache, task);
            this.taskDao.updateStatusAndStackById(taskId, "", Constant.TASK_STATUS_DONE);
        } catch (RunMethodException e) {
            LOGGER.error("run task error,task:{}", task, e);
            this.taskDao.updateStatusAndStackById(taskId, e.toString(), Constant.TASK_STATUS_ERROR);
        }
    }



    private void runMethod(ProjectCache projectCache, Task task) throws RunMethodException {
        Fetcher fetcher = this.fetcherMap.get(task.getFetchType());
        if (fetcher == null) {
            throw new IllegalArgumentException("unknown fetch type:" + task.getFetchType());
        }
        Self self = new Self(task.getProjectId());
        FetchResult fetchResult;
        Object result;
        try {
            fetchResult = fetcher.fetch(task);
        } catch (IOException e) {
            throw new RunMethodException("fetch failed", e, task.getMethod());
        }
        if (!fetchResult.isSuccess()) {
            throw new RunMethodException("fetch failed,http status:" + fetchResult.getStatus(), task.getMethod());
        }
        Response response = new Response(fetchResult.getHeaders(), fetchResult.getContent());
        if ("start".equals(task.getMethod())) {
            try {
                result = ((Invocable)projectCache.scriptEngine).invokeFunction(task.getMethod(), self);
            } catch (Exception e) {
                throw new RunMethodException(e, task.getMethod());
            }
        } else {
            try {
                result = ((Invocable)projectCache.scriptEngine).invokeFunction(task.getMethod(), self, response);
            } catch (Exception e) {
                throw new RunMethodException(e, task.getMethod());
            }
        }
        this.removeRepeatTask(self.newTasks);
        if (CollectionUtils.isNotEmpty(self.newTasks)) {
            List<List<Task>> batchTaskList = Lists.partition(self.newTasks, INSERT_BATCH_SIZE);
            batchTaskList.forEach(tasks -> this.taskDao.insertBatch(self.newTasks));
        }
        if (result != null) {
            TaskResult taskResult = new TaskResult();
            taskResult.setTaskId(task.getId());
            taskResult.setProjectId(projectCache.project.getId());
            taskResult.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            taskResult.setResultText(JSON.toJSONString(result));
            this.taskResultDao.insert(taskResult);
        }
    }


    private void removeRepeatTask(List<Task> newTasks) {
        List<Task> oldTask = this.taskDao.findByIds(newTasks.stream().map(t -> t.getId()).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(oldTask)) {
            newTasks.removeAll(oldTask);
        }
    }


    public class Self {
        private long        projectId;
        private List<Task>  newTasks = new ArrayList<>();

        public Self(long projectId) {
            this.projectId = projectId;
        }

        public void crawl(String url, Map<String, Object> options) {
            this.checkOptions(options);
            String method = MapUtils.getString(options, "method");
            Task task = new Task();
            task.setId(genTaskId(url, method));
            task.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            task.setProjectId(projectId);
            task.setSourceUrl(url);
            task.setMethod(method);
            if (options.containsKey("headers")) {
                task.setHeaders(JSON.toJSONString(options.get("headers")));
            }
            if (options.containsKey("charset")) {
                task.setCharset(MapUtils.getString(options, "charset"));
            }
            if (options.containsKey("extra")) {
                task.setExtra(JSON.toJSONString(options.get("extra")));
            }
            if (options.containsKey("fetchType")) {
                task.setFetchType(MapUtils.getString(options, "fetchType"));
            } else {
                task.setFetchType(Constant.FETCH_TYPE_HTML);
            }
            if (options.containsKey("proxy")) {
                task.setProxy(MapUtils.getString(options, "proxy"));
            }
            task.setStatus(Constant.TASK_STATUS_NONE);
            if (options.containsKey("scheduleType")) {
                task.setScheduleType(MapUtils.getString(options, "scheduleType"));
            } else {
                task.setScheduleType(Constant.SCHEDULE_TYPE_NONE);
            }
            if (options.containsKey("scheduleValue")) {
                task.setScheduleValue(MapUtils.getLongValue(options, "scheduleValue"));
            } else {
                task.setScheduleValue(0L);
            }
            this.newTasks.add(task);
        }

        public long getProjectId() {
            return projectId;
        }

        private void checkOptions(Map<String,Object> options) {
            if (!options.containsKey("method")) {
                throw new IllegalArgumentException("no method found for task");
            }

        }
    }


    private String genTaskId(String sourceUrl, String method) {
        return DigestUtils.md5Hex(sourceUrl + "|" + method);
    }




    private ProjectCache getProject(long projectId) {
        ProjectCache projectCache = this.projectCacheMap.get(projectId);
        if (projectCache == null) {
            Project project = this.projectDao.getById(projectId);
            if (project == null) {
                return null;
            }
            ScriptEngine  scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
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
