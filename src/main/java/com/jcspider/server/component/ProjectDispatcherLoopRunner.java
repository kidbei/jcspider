package com.jcspider.server.component;

import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.ProjectProcessNodeDao;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.ProjectProcessNode;
import com.jcspider.server.utils.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu
 * @since 10 June 2019
 */
@Component
public class ProjectDispatcherLoopRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDispatcherLoopRunner.class);

    private long    projectId;

    private static ProjectDao projectDao;

    private static ProjectProcessNodeDao    projectProcessNodeDao;

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        ProjectDispatcherLoopRunner.projectDao = projectDao;
    }

    @Autowired
    public void setProjectProcessNodeDao(ProjectProcessNodeDao projectProcessNodeDao) {
        ProjectDispatcherLoopRunner.projectProcessNodeDao = projectProcessNodeDao;
    }

    public ProjectDispatcherLoopRunner() {
    }

    public ProjectDispatcherLoopRunner(long projectId) {
        this.projectId = projectId;
    }

    @Override
    public void run() {
        Project project = projectDao.getById(this.projectId);
        if (project == null) {
            LOGGER.warn("project not exist:{}", this.projectId);
            return;
        }
        List<ProjectProcessNode> projectProcessNodes = projectProcessNodeDao.findByProjectId(this.projectId);
        if (CollectionUtils.isEmpty(projectProcessNodes)) {
            LOGGER.warn("project has no process node:{}", this.projectId);
            return;
        }
        LOGGER.info("run project loop {}", this.projectId);
        projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_START);
        List<String> nodes = projectProcessNodes.stream().map(p -> p.getProcessNode()).collect(Collectors.toList());;
        ProjectDispatcherRunner dispatcherRunner = new ProjectDispatcherRunner(this.projectId, project.getRateNumber(), nodes);
        DispatcherScheduleFactory.setProjectRunner(this.projectId, dispatcherRunner, project.getRateUnit(), project.getRateUnitMultiple());
    }
}
