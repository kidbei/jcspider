package com.jcspider.server.web.api;

import com.jcspider.server.model.JSONResult;
import com.jcspider.server.model.LoginReq;
import com.jcspider.server.model.WebUser;
import com.jcspider.server.web.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuang.hu
 * @since 25 June 2019
 */
@RestController
@RequestMapping(value = "/api")
public class LoginController {
    @Autowired
    private UserService     userService;


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public JSONResult<WebUser> login(@RequestBody LoginReq req) {
        WebUser webUser = this.userService.login(req.getUserName(), req.getPassword());
        if (webUser == null) {
            return JSONResult.error("login failed");
        }
        return JSONResult.success(webUser);
    }



}
