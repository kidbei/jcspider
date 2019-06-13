package com.jcspider.server.web.filter;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.dao.WebUserDao;
import com.jcspider.server.model.JSONResult;
import com.jcspider.server.model.WebUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@WebFilter(urlPatterns = "/api/*", filterName = "loginFilter")
@Component
public class LoginFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    @Autowired
    private static WebUserDao   webUserDao;

    @Autowired
    public void setWebUserDao(WebUserDao webUserDao) {
        LoginFilter.webUserDao = webUserDao;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (setLoginInfo(request)) {
            filterChain.doFilter(request, response);
        } else {
            this.writeErrorResponse(response);
        }

    }


    private boolean setLoginInfo(HttpServletRequest request) {
        String uid = request.getHeader("uid");
        String token = request.getHeader("token");
        if (StringUtils.isBlank(uid)) {
            LOGGER.warn("request failed, uid is empty");
            return false;
        }
        if (StringUtils.isBlank(token)) {
            LOGGER.warn("request failed, token is empty");
            return false;
        }

        WebUser webUser = webUserDao.getByUidAndToken(uid, token);
        if (webUser == null) {
            LOGGER.warn("request failed, invalid login info");
            return false;
        }

        LoginInfo.setLoginInfo(webUser);

        return true;
    }


    private void writeErrorResponse(HttpServletResponse response) {
        try {

            JSONResult result = JSONResult.error("login required");
            response.setStatus(401);
            response.getWriter().write(JSON.toJSONString(result));
            response.getWriter().flush();
        } catch (Exception e) {
            LOGGER.error("write response error", e);
        }
    }



    @Override
    public void destroy() {

    }


}
