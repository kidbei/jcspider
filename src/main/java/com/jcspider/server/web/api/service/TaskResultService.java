package com.jcspider.server.web.api.service;

import com.jcspider.server.dao.TaskResultDao;
import com.jcspider.server.model.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * @Author: Gosin
 * @Date: 2019-07-25 09:32
 */
@Service
public class TaskResultService {
    @Autowired
    private TaskResultDao   taskResultDao;

    public Page<TaskResult> pageList(long projectId, Integer curPage, Integer pageSize) {
        PageRequest request = PageRequest.of(curPage == null ? 0 : curPage - 1, pageSize == null ? 10 : pageSize);
        return this.taskResultDao.findByProjectId(projectId, request);
    }


    public void delete(long projectId) {
        this.taskResultDao.deleteByProjectId(projectId);
    }

}
