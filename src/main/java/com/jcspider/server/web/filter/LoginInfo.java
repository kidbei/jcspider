package com.jcspider.server.web.filter;

import com.jcspider.server.model.WebUser;

public class LoginInfo {

    private static final ThreadLocal<WebUser> LOCAL = new ThreadLocal<>();


    public static void setLoginInfo(WebUser webUser) {
        LOCAL.set(webUser);
    }

    public static WebUser getLoginInfo() {
        return LOCAL.get();
    }


    public static void release() {
        LOCAL.remove();
    }

}
