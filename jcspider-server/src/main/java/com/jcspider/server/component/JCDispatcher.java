package com.jcspider.server.component;

import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.ProjectProcessNodeDao;
import com.jcspider.server.model.ComponentInitException;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.ProjectProcessNode;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.IPUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private String      localIp;

    private final Map<Long,List<ProjectProcessNode>> projectProcessNodeMap = new HashMap<>();

    @Override
    public void start() throws ComponentInitException {
        try {
            this.localIp = IPUtils.getLocalIP();
            this.jcRegistry.registerDispatcher(this.localIp);
            DispatcherScheduleFactory.init(maxScheduleSize);
            this.subProjectStart();
            LOGGER.info("reg dispatcher ip:{}, max schedule size:{}", this.localIp, maxScheduleSize);
        } catch (Exception e) {
            throw new ComponentInitException(e, name());
        }
    }


    private void subProjectStart() {
        this.jcQueue.sub(Constant.TOPIC_DISPATCHER_PROJECT_START, (topic, message) -> {
            long projectId = (message.getClass() == long.class || message instanceof Long) ? (long) message : Long.valueOf(message.toString());
            this.toStartProject(projectId);
        });
    }


    @Transactional(rollbackFor = Exception.class)
    public synchronized void toStartProject(long projectId) {
        Project project = this.projectDao.getById(projectId);
        if (project == null) {
            throw new NullPointerException("project not found:" + projectId);
        }
        List<String> dispatcherNodes = this.jcRegistry.listDispatchers();
        if (StringUtils.isBlank(project.getDispatcher())) {
            project.setDispatcher(this.localIp);
            this.projectDao.updateDispatcherById(projectId, this.localIp);
        }
        if (project.getDispatcher().equals(this.localIp)) {

            startAtLocal(projectId, project);

        } else {
            if (dispatcherNodes.contains(project.getDispatcher())) {
                LOGGER.info("project {} on dispatcher node:{}, skip to start", projectId, project.getDispatcher());
            } else {
                if (this.jcLockTool.getLock("start_lock:" + projectId)) {
                    LOGGER.info("project {} dispatcher node {} is offline, start at local", projectId, project.getDispatcher());
                    this.startAtLocal(projectId, project);
                }
            }
        }
    }

    private void startAtLocal(long projectId, Project project) {
        if (DispatcherScheduleFactory.isDispatcherStarted(projectId)) {
            LOGGER.info("project {} is already started at this node", projectId);
        } else {
            this.projectProcessNodeDao.deleteByProjectId(projectId);
            List<String> processNodes = this.jcRegistry.listProcesses();
            ProjectDispatcherRunner runner = new ProjectDispatcherRunner(project.getRateNumber(), processNodes);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            List<ProjectProcessNode> projectProcessNodes = processNodes.stream()
                    .map(p -> new ProjectProcessNode(projectId, p, now))
                    .collect(Collectors.toList());
            this.projectProcessNodeDao.insertBatch(projectProcessNodes);
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
