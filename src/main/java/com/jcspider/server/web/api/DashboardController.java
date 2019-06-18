package com.jcspider.server.web.api;

import com.jcspider.server.model.DashboardData;
import com.jcspider.server.model.JSONResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Gosin
 * @Date: 2019-06-14 23:21
 */
@RestController
@RequestMapping(value = "/api/dashboard")
public class DashboardController {


    @RequestMapping(value = "", method = RequestMethod.GET)
    public JSONResult<DashboardData> index() {

        return JSONResult.success(new DashboardData());
    }

}
