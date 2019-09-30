package com.jcspider.server.web.filter;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.dao.WebUserDao;
import com.jcspider.server.model.JSONResult;
import com.jcspider.server.model.WebUser;

import com.jcspider.server.utils.Constant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@WebFilter(urlPatterns = "/api/*", filterName = "loginFilter")
@Component
public class LoginFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    private static final HashSet<String> EXCLUDES = new HashSet<String>(){
        private static final long serialVersionUID = -8048317463493087332L;

        {
            this.add("/api/login");
            this.add("/api/projects/results/export");
        }
    };

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
        String method = request.getMethod();
        if (method.equals(Constant.CORS_METHOD) || EXCLUDES.contains(request.getRequestURI()) || setLoginInfo(request)) {
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

            JSONResult<String> result = JSONResult.error("login required");
            response.setStatus(401);
            response.setHeader("Content-Type", "application/json;charset=utf-8");
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
