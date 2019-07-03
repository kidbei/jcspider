package com.jcspider.server.web.api.service;

import com.jcspider.server.dao.WebUserDao;
import com.jcspider.server.model.UserQueryExp;
import com.jcspider.server.model.WebUser;
import com.jcspider.server.utils.Constant;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private WebUserDao webUserDao;

    @Value("${admins}")
    private String  admins;

    @PostConstruct
    public void init() {
        String[] nameAndPasses = admins.split(",");
        for (String nameAndPass : nameAndPasses) {
            String name = nameAndPass.split(":")[0];
            String password = nameAndPass.split(":")[1];
            String safePass = this.getSecretPassword(password);
            WebUser webUser = this.webUserDao.getByUidAndPassword(name, safePass);
            if (webUser == null) {
                LOGGER.info("create webUser, {}", nameAndPass);
                webUser = new WebUser();
                webUser.setCnName(name);
                webUser.setUid(name);
                webUser.setPassword(safePass);
                webUser.setInviteUid(name);
                webUser.setRole(Constant.USER_ROLE_SUPER);
                this.webUserDao.insert(webUser);
            }
        }
    }


    public WebUser get(String uid) {
        return this.webUserDao.getByUid(uid);
    }


    public void deleteById(long id) {
        this.webUserDao.deleteById(id);
    }


    private String getSecretPassword(String password) {
        return DigestUtils.md5Hex(Constant.TOKEN_SALT + password);
    }

    public WebUser login(String userName, String password) {
        String secretPassword = this.getSecretPassword(password);
        WebUser webUser = this.webUserDao.getByUidAndPassword(userName, secretPassword);
        if (webUser == null) {
            LOGGER.info("invalid user name or password, {}:{}", userName, secretPassword);
            return null;
        }
        if (StringUtils.isNotBlank(webUser.getToken())) {
            long now = System.currentTimeMillis();
            if (webUser != null && webUser.getTokenCreatedAt() != null && now - webUser.getTokenCreatedAt().getTime() < Constant.TOKEN_EXPIRE_TIME ) {
                return webUser;
            }
        }
        String token = DigestUtils.md5Hex(userName + System.currentTimeMillis() + secretPassword);

        WebUser update = new WebUser();
        update.setId(webUser.getId());
        update.setToken(token);
        update.setTokenCreatedAt(new Timestamp(System.currentTimeMillis()));
        this.webUserDao.updateByExp(update);
        return webUser;
    }


    public Page<WebUser> query(UserQueryExp exp, Integer curPage, Integer pageSize) {
        PageRequest request = PageRequest.of(curPage == null ? 0 : curPage - 1, pageSize == null ? 10 : pageSize);
        return this.webUserDao.queryByExp(exp, request);
    }


}
