package com.jcspider.server.web.api;

import com.jcspider.server.model.JSONResult;
import com.jcspider.server.model.ProjectQueryExp;
import com.jcspider.server.model.WebUser;
import com.jcspider.server.web.api.service.ProjectService;
import com.jcspider.server.model.Project;
import com.jcspider.server.web.filter.LoginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService  projectService;


    @RequestMapping(value = "query", method = RequestMethod.POST)
    public JSONResult<Page<Project>> listUserProjects(@RequestBody(required = false) ProjectQueryExp exp,
                                                      Integer curPage, Integer pageSize) {
        WebUser webUser = LoginInfo.getLoginInfo();
        Page<Project> projectPage = this.projectService.findUserProjects(webUser.getUid(), curPage, pageSize);
        return JSONResult.success(projectPage);
    }

}
