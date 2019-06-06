package com.jcspider.server.component;

import com.jcspider.server.dao.TaskResultDao;
import com.jcspider.server.model.ComponentInitException;
import com.jcspider.server.model.TaskResult;
import com.jcspider.server.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public class DbResultExporter implements ResultExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbResultExporter.class);

    @Autowired
    private TaskResultDao   taskResultDao;


    @Override
    public void export(TaskResult result) {
        this.taskResultDao.insert(result);
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
