package com.jcspider.server.web.api;

import com.jcspider.server.model.JSONResult;
import com.jcspider.server.model.LogQueryExp;
import com.jcspider.server.model.SelfLog;
import com.jcspider.server.web.api.service.SelfLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Gosin
 * @Date: 2019-08-03 22:30
 */
@RestController
@RequestMapping(value = "/api/logs")
public class SelfLogController {
    @Autowired
    private SelfLogService  selfLogService;

    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public JSONResult<Page<SelfLog>> query(Integer curPage, Integer pageSize,
                                           @RequestBody LogQueryExp exp) {
        return JSONResult.success(this.selfLogService.query(curPage, pageSize, exp));
    }
}
