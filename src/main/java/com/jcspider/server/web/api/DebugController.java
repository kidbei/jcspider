package com.jcspider.server.web.api;

import com.jcspider.server.web.api.service.DebugService;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.model.JSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/debug")
@RestController
public class DebugController {
    @Autowired
    private DebugService    debugService;


    @RequestMapping(value = "/task", method = RequestMethod.POST)
    public JSONResult<DebugResult> debug(@RequestBody DebugTask debugTask) {
        if (StringUtils.isBlank(debugTask.getScriptText())) {
            return JSONResult.error("scriptText must not be null");
        }
        if (debugTask.getSimpleTask() == null) {
            return JSONResult.error("simpleTask must not be null");
        }
        if (StringUtils.isBlank(debugTask.getSimpleTask().getMethod())) {
            return JSONResult.error("simpleTask.method must not be null");
        }

        DebugResult result = this.debugService.debug(debugTask);
        if (result.isSuccess()) {
            return JSONResult.success(result);
        } else {
            return JSONResult.error(result.getStack());
        }
    }


}
