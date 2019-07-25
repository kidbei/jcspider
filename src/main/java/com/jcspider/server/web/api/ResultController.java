package com.jcspider.server.web.api;

import com.jcspider.server.model.JSONResult;
import com.jcspider.server.model.TaskResult;
import com.jcspider.server.web.api.service.TaskResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Gosin
 * @Date: 2019-07-25 09:30
 */
@RestController
@RequestMapping(value = "/api/projects/")
public class ResultController {
    @Autowired
    private TaskResultService   taskResultService;

    @RequestMapping(value = "/{projectId}/results", method = RequestMethod.GET)
    public JSONResult<Page<TaskResult>> list(@PathVariable long projectId,
                                             Integer curPage, Integer pageSize) {
        Page<TaskResult> result = this.taskResultService.pageList(projectId, curPage, pageSize);
        return JSONResult.success(result);
    }


    @RequestMapping(value = "/{projectId}/results/clear", method = RequestMethod.DELETE)
    public JSONResult<String> clear(@PathVariable long projectId) {
        this.taskResultService.delete(projectId);
        return JSONResult.success("ok");
    }

}
