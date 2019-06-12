package com.jcspider.server.component;

import com.jcspider.server.dao.TaskResultDao;
import com.jcspider.server.model.TaskResult;
import com.jcspider.server.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public class DbResultExporter implements ResultExporter {

    @Autowired
    private TaskResultDao   taskResultDao;


    @Override
    public void export(TaskResult result) {
        this.taskResultDao.insert(result);
    }

    @Override
    public void delete(long projectId, String taskId) {
        this.taskResultDao.deleteByProjectIdAndTaskId(projectId, taskId);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String name() {
        return Constant.DB_RESULT_EXPORTER;
    }
}
