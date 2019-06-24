package com.jcspider.server.web.api.service;

import com.jcspider.server.component.JCQueue;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.UserProjectDao;
import com.jcspider.server.model.CreateProjectReq;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.ProjectQueryExp;
import com.jcspider.server.model.UserProject;
import com.jcspider.server.utils.Constant;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@Service
public class ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectDao      projectDao;
    @Autowired
    private UserProjectDao  userProjectDao;
    @Autowired
    private DispatcherService   dispatcherService;
    @Autowired
    private JCQueue         jcQueue;

    private String defaultScript;

    @PostConstruct
    public void init() throws IOException {
        this.defaultScript = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("default.js"));
    }


    @Transactional
    public Long createProject(CreateProjectReq createProjectReq) {
        if (StringUtils.isBlank(createProjectReq.getRateUnit())) {
            createProjectReq.setRateUnit(Constant.UNIT_TYPE_SECONDS);
        }
        if (createProjectReq.getRateNumber() <= 0) {
            createProjectReq.setRateNumber(1);
        }
        if (createProjectReq.getRateUnitMultiple() <= 0) {
            createProjectReq.setRateUnitMultiple(2);
        }
        if (StringUtils.isBlank(createProjectReq.getScheduleType())) {
            createProjectReq.setScheduleType(Constant.SCHEDULE_TYPE_NONE);
        }
        if (StringUtils.isBlank(createProjectReq.getDescription())) {
            createProjectReq.setDescription(createProjectReq.getName());
        }
        createProjectReq.setStatus(Constant.PROJECT_STATUS_STOP);
        createProjectReq.setScriptText(this.defaultScript);
        long projectId = this.projectDao.insert(createProjectReq);
        UserProject userProject = new UserProject(createProjectReq.getUid(), Constant.PROJECT_ROLE_OWNER, projectId);
        this.userProjectDao.insert(userProject);
        return projectId;
    }



    public Page<Project> query(ProjectQueryExp exp, Integer curPage, Integer pageSize) {
        PageRequest request = PageRequest.of(curPage == null ? 0 : curPage, pageSize == null ? 10 : pageSize);
        return this.projectDao.queryByExp(exp, request);
    }


    public UserProject get(String uid, long projectId) {
        return this.userProjectDao.getByUidAndProjectId(uid, projectId);
    }

    public Project get(long projectId) {
        return this.projectDao.getById(projectId);
    }


    @Transactional(rollbackFor = Exception.class)
    public void startProject(Project project) {
        long projectId = project.getId();
        if (project.getStatus().equals(Constant.PROJECT_STATUS_START)) {
            LOGGER.info("project {} is already started", projectId);
            return;
        }

        LOGGER.info("start project {}", projectId);

        Project updateExp = new Project(projectId);
        if (StringUtils.isBlank(project.getDispatcher()) ||
                !this.dispatcherService.listDispatcherNodes().contains(project.getDispatcher())) {
            String dispatcherNode = this.dispatcherService.selectDispatcherNode();
            updateExp.setDispatcher(dispatcherNode);
            project.setDispatcher(dispatcherNode);
            this.projectDao.updateByExp(updateExp);
            LOGGER.info("update dispatcher node for project {}, the new dispatcher node is {}", projectId, dispatcherNode);
        }

        this.jcQueue.pubDispatcherStart(projectId);
    }



    public void stopProject(long projectId) {
        Project project = this.projectDao.getById(projectId);
        if (project == null) {
            LOGGER.error("project not found:{}", projectId);
            return;
        }
        this.stopProject(project);
    }


    public void stopProject(Project project) {
        LOGGER.info("stop project {}", project.getId());
        this.jcQueue.pubDispatcherStop(project.getId());
    }

}
