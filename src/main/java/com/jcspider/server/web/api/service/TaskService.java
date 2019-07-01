package com.jcspider.server.web.api.service;

import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.Task;
import com.jcspider.server.model.TaskQueryExp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * @Author: Gosin
 * @Date: 2019-06-13 22:36
 */
@Service
public class TaskService {
    @Autowired
    private TaskDao taskDao;


    public Page<Task> find(TaskQueryExp exp, Integer curPage, Integer pageSize) {
        PageRequest request = PageRequest.of(curPage == null ? 0 : curPage - 1, pageSize == null ? 10 : pageSize);
        return this.taskDao.findByExp(exp, request);
    }
}
