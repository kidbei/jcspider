package com.jcspider.server.web.api.service;

import com.jcspider.server.dao.WebUserDao;
import com.jcspider.server.model.WebUser;
import com.jcspider.server.utils.Constant;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

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
            String safePass = DigestUtils.md5Hex(Constant.TOKEN_SALT + password);
            WebUser webUser = this.webUserDao.getByUidAndPassword(name, safePass);
            if (webUser == null) {
                LOGGER.info("create webUser, {}", nameAndPasses);
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

}
