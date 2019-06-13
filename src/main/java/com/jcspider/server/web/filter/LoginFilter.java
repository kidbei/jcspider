package com.jcspider.server.web.filter;

import com.jcspider.server.dao.WebUserDao;
import org.apache.commons.lang3.StringUtils;
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
    }


    private boolean setLoginInfo(HttpServletRequest request) {
        String uid = request.getHeader("uid");
        String token = request.getHeader("token");
        if (StringUtils.isBlank(uid)) {

        }

        return true;
    }


    @Override
    public void destroy() {

    }


}
