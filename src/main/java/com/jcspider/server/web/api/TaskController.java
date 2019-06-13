package com.jcspider.server.web.api;

import com.jcspider.server.model.*;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.web.api.service.ProjectService;
import com.jcspider.server.web.api.service.TaskService;
import com.jcspider.server.web.filter.LoginInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Gosin
 * @Date: 2019-06-13 22:35
 */
@RestController
@RequestMapping(value = "/api/tasks")
public class TaskController {
    @Autowired
    private ProjectService  projectService;
    @Autowired
    private TaskService     taskService;


    @RequestMapping(value = "", method = RequestMethod.POST)
    public JSONResult<Page<Task>> query(Integer curPage, Integer pageSize,
                                        @RequestBody(required = false) TaskQueryExp exp) {
        if (exp == null) {
            exp = new TaskQueryExp();
        }
        WebUser webUser = LoginInfo.getLoginInfo();
        if (webUser.getRole().equals(Constant.USER_ROLE_NORMAL)) {
            if (exp.getProjectId() == null) {
                return JSONResult.error("projectId required");
            }
            UserProject userProject = this.projectService.get(webUser.getUid(), exp.getProjectId());
            if (userProject == null) {
                return JSONResult.error("permission required");
            }
            return JSONResult.success(this.taskService.find(exp, curPage, pageSize));
        } else {
            return JSONResult.success(this.taskService.find(exp, curPage, pageSize));
        }
    }

}
