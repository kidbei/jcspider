package com.jcspider.server.web.api;

import com.jcspider.server.model.JSONResult;
import com.jcspider.server.model.WebUser;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.web.api.service.UserService;
import com.jcspider.server.web.filter.LoginInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuang.hu
 * @since 14 June 2019
 */
@RestController
@RequestMapping(value = "/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public JSONResult<String> deleteUser(String uid) {

        WebUser webUser = LoginInfo.getLoginInfo();
        if (webUser.getRole().equals(Constant.USER_ROLE_NORMAL)) {
            return JSONResult.error("permission required");
        }

        WebUser deleteUser = this.userService.get(uid);
        if (deleteUser.getRole().equals(Constant.USER_ROLE_SUPER)) {
            return JSONResult.error("can not delete super user");
        }
        if (webUser.getRole().equals(Constant.USER_ROLE_ADMIN)) {
            if (deleteUser.getRole().equals(Constant.USER_ROLE_NORMAL)) {
                this.userService.deleteById(deleteUser.getId());
            }  else {
                return JSONResult.error("permission required");
            }
        } else {
            this.userService.deleteById(deleteUser.getId());
        }

        return JSONResult.success("ok");
    }


}
