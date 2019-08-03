package com.jcspider.server.web.api.service;

import com.jcspider.server.dao.SelfLogDao;
import com.jcspider.server.model.LogQueryExp;
import com.jcspider.server.model.SelfLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * @Author: Gosin
 * @Date: 2019-08-03 22:26
 */
@Service
public class SelfLogService {
    @Autowired
    private SelfLogDao  selfLogDao;


    public Page<SelfLog> query(Integer curPage, Integer pageSize, LogQueryExp exp) {
        PageRequest request = PageRequest.of(curPage == null ? 0 : curPage - 1, pageSize == null ? 10 : pageSize);
        return this.selfLogDao.queryByExp(exp, request);
    }

    public void addLog(Long projectId, String level, String text) {
        SelfLog selfLog = new SelfLog(text, true);
        selfLog.setProjectId(projectId);
        selfLog.setLevel(level);
        this.selfLogDao.insert(selfLog);
    }

}
