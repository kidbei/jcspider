package com.jcspider.server.web.api.service;

import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.UserProjectDao;
import com.jcspider.server.model.CreateProjectReq;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.ProjectQueryExp;
import com.jcspider.server.model.UserProject;
import com.jcspider.server.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@Service
public class ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectDao  projectDao;
    @Autowired
    private UserProjectDao  userProjectDao;


    @Transactional
    public Long createProject(CreateProjectReq createProjectReq) {
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

}
