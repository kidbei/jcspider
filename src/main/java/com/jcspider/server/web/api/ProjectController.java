package com.jcspider.server.web.api;

import com.jcspider.server.web.api.service.ProjectService;
import com.jcspider.server.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Page<Project> listUserProjects(Integer curPage, Integer pageSize) {

        return null;
    }

}
