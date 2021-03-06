package com.jcspider.server.web.api.service;

import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.dao.TaskResultDao;
import com.jcspider.server.dao.UserProjectDao;
import com.jcspider.server.model.*;
import com.jcspider.server.utils.Constant;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@Service
public class ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectDao          projectDao;
    @Autowired
    private UserProjectDao      userProjectDao;
    @Autowired
    private JCQueue             jcQueue;
    @Autowired
    private TaskDao             taskDao;
    @Autowired
    private TaskResultDao       taskResultDao;


    private String defaultScript;

    @PostConstruct
    public void init() throws IOException {
        this.defaultScript = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("default.js"));
    }


    @Transactional
    public Long createProject(CreateProjectReq createProjectReq) {
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



    public void update(Project project) {
        this.projectDao.updateByExp(project);
        this.jcQueue.publish(Constant.TOPIC_STOP_PROJECT, project.getId());
    }


    public Page<Project> query(ProjectQueryExp exp, Integer curPage, Integer pageSize) {
        PageRequest request = PageRequest.of(curPage == null ? 0 : curPage - 1, pageSize == null ? 10 : pageSize);
        Page<Project> page = this.projectDao.queryByExp(exp, request);
        if (page.hasContent()) {
            List<ProjectResultCount> projectResultCountList = this.taskResultDao.findByProjectIds(page.getContent().stream().map(p -> p.getId()).collect(Collectors.toList()));
            Map<Long, ProjectResultCount> countMap = projectResultCountList.stream().collect(Collectors.toMap(ProjectResultCount::getProjectId, Function.identity()));
            page.getContent().forEach(project -> {
                if (countMap.containsKey(project.getId())) {
                    project.setResultCount(countMap.get(project.getId()).getResultCount());
                }
            });
        }
        return page;
    }


    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(long projectId) {
        this.projectDao.deleteById(projectId);
        this.userProjectDao.deleteByProjectId(projectId);
        this.taskDao.deleteByProjectId(projectId);
        this.taskResultDao.deleteByProjectId(projectId);
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
        this.jcQueue.publish(Constant.TOPIC_START_PROJECT, project.getId());
    }


    public void startAllProject() {
        List<Project> projectList = this.projectDao.findAll();
        projectList.forEach(project -> startProject(project));
    }


    public List<Project> suggest(String name) {
        return this.projectDao.findByNameList(name, 10);
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
        this.jcQueue.publish(Constant.TOPIC_STOP_PROJECT, project.getId());
    }

}
