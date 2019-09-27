package com.jcspider.server.web.api;

import com.jcspider.server.model.*;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.web.api.service.ProjectService;
import com.jcspider.server.web.filter.LoginInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService  projectService;


    @RequestMapping(value = "", method = RequestMethod.POST)
    public JSONResult<Project> create(@RequestBody CreateProjectReq createProjectReq) {
        createProjectReq.setScheduleValue(createProjectReq.getScheduleValue() * 1000 * 60);
        JSONResult checkResult = this.checkProject(createProjectReq);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        WebUser webUser = LoginInfo.getLoginInfo();
        createProjectReq.setUid(webUser.getUid());
        long projectId = this.projectService.createProject(createProjectReq);
        createProjectReq.setId(projectId);
        return JSONResult.success(createProjectReq);
    }


    @RequestMapping(value = "query", method = RequestMethod.POST)
    public JSONResult<Page<Project>> listUserProjects(@RequestBody(required = false) ProjectQueryExp exp,
                                                      Integer curPage, Integer pageSize) {
        if (exp == null) {
            exp = new ProjectQueryExp();
        }
        WebUser webUser = LoginInfo.getLoginInfo();
        if (webUser.getRole().equals(Constant.USER_ROLE_NORMAL)) {
            if (StringUtils.isBlank(exp.getUid())) {
                exp.setUid(webUser.getUid());
            }
        }
        Page<Project> page = this.projectService.query(exp, curPage, pageSize);
        if (CollectionUtils.isNotEmpty(page.getContent())) {
            page.getContent().forEach(project -> project.setScheduleValue(project.getScheduleValue() / (1000 * 60)));
        }
        return JSONResult.success(page);
    }


    @RequestMapping(value = "/suggest", method = RequestMethod.POST)
    public JSONResult<List<Project>> suggest(@RequestBody ProjectQueryExp exp) {
        return JSONResult.success(this.projectService.suggest(exp.getName()));
    }


    @RequestMapping(value = "/{projectId}", method = RequestMethod.DELETE)
    public JSONResult<String> delete(@PathVariable long projectId) {
        WebUser webUser = LoginInfo.getLoginInfo();
        if (webUser.getRole().equals(Constant.USER_ROLE_NORMAL)) {
            UserProject userProject = this.projectService.get(webUser.getUid(), projectId);
            if (userProject == null) {
                return JSONResult.error("permission required");
            }
        }

        this.projectService.deleteProject(projectId);
        return JSONResult.success("ok");
    }



    @RequestMapping(value = "/start/{projectId}", method = RequestMethod.GET)
    public JSONResult<String> start(@PathVariable long projectId) {
        Project project = this.projectService.get(projectId);
        JSONResult<String> result = this.checkPermission(project);
        if (!result.isSuccess()) {
            return result;
        }
        this.projectService.startProject(project);
        return JSONResult.success("ok");
    }



    @RequestMapping(value = "/start/all", method = RequestMethod.GET)
    public JSONResult<String> startAll() {
        this.projectService.startAllProject();
        return JSONResult.success("ok");
    }


    private JSONResult<String> checkPermission(Project project) {
        if (project == null) {
            return JSONResult.error("project is not found:" + project.getId());
        }
        WebUser webUser = LoginInfo.getLoginInfo();

        if (webUser.getRole().equals(Constant.USER_ROLE_NORMAL)) {
            UserProject userProject = this.projectService.get(webUser.getUid(), project.getId());
            if (userProject == null) {
                return JSONResult.error("permission required");
            }
        }
        return JSONResult.success("ok");
    }


    @RequestMapping(value = "/stop/{projectId}", method = RequestMethod.GET)
    public JSONResult<String> stop(@PathVariable long projectId) {
        Project project = this.projectService.get(projectId);
        JSONResult<String> result = this.checkPermission(project);
        if (!result.isSuccess()) {
            return result;
        }

        this.projectService.stopProject(project);
        return JSONResult.success("ok");
    }

    @RequestMapping(value = "/{projectId}", method = RequestMethod.GET)
    public JSONResult<Project> get(@PathVariable long projectId) {
        Project project = this.projectService.get(projectId);
        if (project == null) {
            return JSONResult.error("not found:" + projectId);
        }
        WebUser webUser = LoginInfo.getLoginInfo();
        if (webUser.getRole().equals(Constant.USER_ROLE_NORMAL)) {
            UserProject userProject = this.projectService.get(webUser.getUid(), projectId);
            if (userProject == null) {
                return JSONResult.error("permission required");
            }
        }
        project.setScheduleValue(project.getScheduleValue() / (1000 * 60));
        return JSONResult.success(project);
    }


    private JSONResult<Object> checkProject(Project project) {
        if (StringUtils.isBlank(project.getName())) {
            return JSONResult.error("name is empty");
        }
        if (StringUtils.isBlank(project.getStartUrl())) {
            return JSONResult.error("startUrl is empty");
        }
        if (project.getQps() == null) {
            project.setQps(1.);
        }
        return JSONResult.success(null);
    }


    @RequestMapping(value = "", method = RequestMethod.PUT)
    public JSONResult<Project> update(@RequestBody Project project) {
        project.setScheduleValue(project.getScheduleValue() * 1000 * 60);
        JSONResult checkResult = this.checkProject(project);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }
        this.projectService.update(project);
        return JSONResult.success(project);
    }

}
