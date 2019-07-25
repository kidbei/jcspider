package com.jcspider.server.component;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.*;
import com.jcspider.server.utils.*;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu
 * @since 24 June 2019
 */
public abstract class JCProcess implements JCComponent{
    private static final Logger LOGGER = LoggerFactory.getLogger(JCProcess.class);


    @Value("${process.threads:10}")
    private int processThreads;
    @Value("${process.result.exporter}")
    private String exportComponents;

    protected List<ResultExporter> resultExporters = new ArrayList<>();

    @Autowired
    protected ProjectDao              projectDao;
    @Autowired
    protected TaskDao                 taskDao;
    @Autowired
    protected JCQueue                 jcQueue;
    @Autowired
    protected JCRegistry              jcRegistry;

    protected String                  localIp;

    protected ThreadPoolExecutor      threadPoolExecutor;

    protected volatile boolean        isStop = false;

    protected final Map<String, Fetcher> fetcherMap = new HashMap<>();

    @Autowired
    protected ApplicationContext    applicationContext;

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

        this.threadPoolExecutor.execute(() -> {
            final String topic = Constant.TOPIC_PROCESS_DEBUG + this.localIp;
            LOGGER.info("subscript topic:{}", topic);
            while (!Thread.interrupted() && !isStop) {
                DebugTask debugTask = this.jcQueue.blockingPopProcessDebugTask(this.localIp);
                try {
                    LOGGER.info("debug task:{}", debugTask);
                    DebugResult debugResult = this.debug(debugTask.getRequestId(),
                            debugTask.getScriptText(), debugTask.getSimpleTask());
                    this.jcQueue.blockingPushProcessDebugTaskReturn(debugResult);
                } catch (Exception e) {
                    LOGGER.error("debug error", e);
                }
            }
        });

        this.jcQueue.subDispatcherStop((topic, message) -> {
            LOGGER.info("stop project:{}", message);
            stopProject((Long) message);
        });

    }


    abstract void stopProject(long projectId);



    private void processTask(String taskId) {
        LOGGER.info("start to process task:{}", taskId);
        Task task;
        try {
            task = this.taskDao.getById(taskId);
            if (task == null) {
                LOGGER.warn("task not found:{}", taskId);
                return;
            }
            if (task.getScheduleType().equals(Constant.SCHEDULE_TYPE_ONCE)) {
                Long nextRunTime = task.getNextRunTime();
                //第一次运行
                if (nextRunTime == 0L) {
                    this.runMethod(task.getProjectId(), task);
                    Task update = new Task(taskId, Constant.TASK_STATUS_DONE, System.currentTimeMillis() + task.getScheduleValue());
                    this.taskDao.upgrade(update);
                } else if (nextRunTime.equals(Long.MAX_VALUE)){
                    //不运行
                } else {
                    //第2次运行
                    this.clearResult(task.getProjectId(), taskId);
                    this.runMethod(task.getProjectId(), task);
                    Task update = new Task(taskId, Constant.TASK_STATUS_DONE, Long.MAX_VALUE);
                    this.taskDao.upgrade(update);
                }
            } else if (task.getScheduleType().equals(Constant.SCHEDULE_TYPE_LOOP)) {
                if (task.getNextRunTime() == 0L) {
                    this.clearResult(task.getProjectId(), taskId);
                }
                this.runMethod(task.getProjectId(), task);
                Task update = new Task(taskId, Constant.TASK_STATUS_DONE, System.currentTimeMillis() + task.getScheduleValue());
                this.taskDao.upgrade(update);
            } else {
                this.runMethod(task.getProjectId(), task);
                Task update = new Task(taskId, Constant.TASK_STATUS_DONE, 0L);
                this.taskDao.upgrade(update);
            }
        } catch (Exception e) {
            LOGGER.error("process task error:{}", taskId, e);
            this.taskDao.updateStatusAndStackById(taskId, e.getMessage(), Constant.TASK_STATUS_ERROR);
        }
    }


    abstract void runMethod(long projectId, SimpleTask task) throws RunMethodException;


    private void clearResult(long projectId, String taskId) {
        this.resultExporters.forEach(resultExporter -> {
            try {
                resultExporter.delete(projectId, taskId);
            } catch (Exception e) {
                LOGGER.error("delete result error, projectId:{}. taskId:{}", projectId, taskId);
            }
        });
    }


    private void startProject(Long projectId) {
        LOGGER.info("start project:{}", projectId);
        Project project = this.getProject(projectId);
        String taskId = IDUtils.genTaskId(projectId, project.getStartUrl(), "start");
        Task task = this.taskDao.getById(taskId);
        if (task == null) {
            task = new Task();
            task.setId(taskId);
            task.setMethod(Constant.METHOD_START);
            task.setCreatedAt(new Timestamp(System.currentTimeMillis()));
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


    private DebugResult debug(String requestId, String scriptText, SimpleTask simpleTask) {
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

        Self self = new Self(0);
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
                response = new Response(fetchResult.getHeaders(), fetchResult.getContent());
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


    protected void removeRepeatTask(List<Task> newTasks) {
        if (CollectionUtils.isEmpty(newTasks)) {
            return;
        }
        List<Task> oldTask = this.taskDao.findByIds(newTasks.stream().map(t -> t.getId()).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(oldTask)) {
            newTasks.removeAll(oldTask);
        }
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
    }

}
