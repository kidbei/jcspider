package com.jcspider.server.component;

import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.ProjectProcessNodeDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.ComponentInitException;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.ProjectProcessNode;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.IDUtils;
import com.jcspider.server.utils.IPUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class JCDispatcher implements JCComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCDispatcher.class);

    @Autowired
    private JCQueue     jcQueue;
    @Autowired
    private JCRegistry  jcRegistry;
    @Value("${dispatcher.maxScheduleSize:100}")
    private int         maxScheduleSize;
    @Autowired
    private ProjectProcessNodeDao   projectProcessNodeDao;
    @Autowired
    private JCLockTool  jcLockTool;
    @Autowired
    private ProjectDao  projectDao;
    @Autowired
    private TaskDao     taskDao;

    private String      localIp;


    @Override
    public void start() throws ComponentInitException {
        try {
            this.localIp = IPUtils.getLocalIP();
            this.jcRegistry.registerDispatcher(this.localIp);
            DispatcherScheduleFactory.init(maxScheduleSize);
            this.subProjectStart();
            this.subProjectStop();
            LOGGER.info("reg dispatcher ip:{}, max schedule size:{}", this.localIp, maxScheduleSize);
            this.recoveryLocalProject();
        } catch (Exception e) {
            throw new ComponentInitException(e, name());
        }
    }


    private void subProjectStart() {
        this.jcQueue.subDispatcherStart((topic, projectId) -> this.toStartProject((Long) projectId));
    }

    private void subProjectStop() {
        this.jcQueue.subDispatcherStop((topic, projectId) -> this.toStopProject((Long) projectId));
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized void toStopProject(long projectId) {
        Project project = this.projectDao.getById(projectId);
        if (project == null) {
            LOGGER.warn("project not found:{}", projectId);
            return;
        }
        if (!project.getStatus().equals(Constant.PROJECT_STATUS_START)) {
            LOGGER.warn("project {} is already stoped", projectId);
            return;
        }
        this.projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_STOP);
        DispatcherScheduleFactory.stopProjectRunner(projectId);
    }

    /**
     * 恢复本节点下的项目调度
     */
    private void recoveryLocalProject() {
        List<Project> localProjects = this.projectDao.findByDispatcher(this.localIp);
        if (CollectionUtils.isEmpty(localProjects)) {
            return;
        }
        for (Project localProject : localProjects) {
            if (localProject.getStatus().equals(Constant.PROJECT_STATUS_STOP)) {
                continue;
            }
            if (!DispatcherScheduleFactory.isDispatcherStarted(localProject.getId())) {
                LOGGER.info("recovery project:{}", localProject);
                this.startAtLocal(localProject);
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public synchronized void toStartProject(long projectId) {
        Project project = this.projectDao.getById(projectId);
        if (project == null) {
            throw new NullPointerException("project not found:" + projectId);
        }
        if (project.getStatus().equals(Constant.PROJECT_STATUS_START)
                && DispatcherScheduleFactory.isDispatcherStarted(projectId)) {
            LOGGER.info("project is already started");
            return;
        }
        List<String> dispatcherNodes = this.jcRegistry.listDispatchers();
        if (StringUtils.isBlank(project.getDispatcher())) {
            project.setDispatcher(this.localIp);
            this.projectDao.updateDispatcherById(projectId, this.localIp);
        }
        if (project.getDispatcher().equals(this.localIp)) {

            this.startAtLocal(project);

        } else {
            if (dispatcherNodes.contains(project.getDispatcher())) {
                LOGGER.info("project {} on dispatcher node:{}, skip to start", projectId, project.getDispatcher());
            } else {
                if (this.jcLockTool.getLock("start_lock:" + projectId)) {
                    LOGGER.info("project {} dispatcher node {} is offline, start at local", projectId, project.getDispatcher());
                    this.startAtLocal(project);
                }
            }
        }
    }



    private void startAtLocal(Project project) {
        String startTaskId = IDUtils.genTaskId(project.getId(), project.getStartUrl(), Constant.METHOD_START);
        this.taskDao.updateStatusAndStackById(startTaskId, "", Constant.TASK_STATUS_NONE);
        long projectId = project.getId();
        if (DispatcherScheduleFactory.isDispatcherStarted(projectId)) {
            LOGGER.info("project {} is already started at this node", projectId);
        } else {
            this.projectProcessNodeDao.deleteByProjectId(projectId);
            List<String> processNodes = this.jcRegistry.listProcesses();
            ProjectDispatcherRunner runner = new ProjectDispatcherRunner(projectId, project.getRateNumber(), processNodes);
            DispatcherScheduleFactory.setProjectRunner(projectId, runner, project.getRateUnit(), project.getRateUnitMultiple());
            Long now = System.currentTimeMillis();
            List<ProjectProcessNode> projectProcessNodes = processNodes.stream()
                    .map(p -> new ProjectProcessNode(projectId, p, now))
                    .collect(Collectors.toList());
            this.projectProcessNodeDao.insertBatch(projectProcessNodes);
            this.jcQueue.blockingPushProcessProjectStart(processNodes.get(0), projectId);
            DispatcherScheduleFactory.setProjectDispatcherLoopRunner(projectId, project.getScheduleType(), project.getScheduleValue());
            this.projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_START);
        }
    }


    @Override
    public void shutdown() {
        LOGGER.info("{} is shutdown", name());
    }

    @Override
    public String name() {
        return "dispatcher";
    }
}
