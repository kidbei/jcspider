package com.jcspider.server.web.api.service;

import com.jcspider.server.component.core.DbResultExporter;
import com.jcspider.server.component.ifc.ResultExporter;
import com.jcspider.server.dao.TaskResultDao;
import com.jcspider.server.model.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Gosin
 * @Date: 2019-07-25 09:32
 */
@Service
public class TaskResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskResultService.class);

    @Autowired
    private TaskResultDao   taskResultDao;
    @Autowired
    private ApplicationContext applicationContext;

    private List<ResultExporter> resultExporters = new ArrayList<>();
    @Autowired
    private DbResultExporter    dbResultExporter;

    @PostConstruct
    public void postConstruct() {
        this.resultExporters.addAll(this.applicationContext.getBeansOfType(ResultExporter.class).values());
        this.resultExporters.remove(dbResultExporter);
    }


    public Page<TaskResult> pageList(long projectId, Integer curPage, Integer pageSize) {
        PageRequest request = PageRequest.of(curPage == null ? 0 : curPage - 1, pageSize == null ? 10 : pageSize);
        return this.taskResultDao.findByProjectId(projectId, request);
    }


    public void delete(long projectId) {
        this.taskResultDao.deleteByProjectId(projectId);
    }


    public void syncToExporter() {
        List<TaskResult> resultList;
        long preId = 0;
        while (true) {
            resultList = this.taskResultDao.findByGtId(preId, 100);
            if (resultList != null && resultList.size() > 0) {
                preId = resultList.get(resultList.size() - 1).getId();
                for (TaskResult result: resultList) {
                    try {
                        resultExporters.forEach(resultExporter -> resultExporter.export(result));
                        LOGGER.info("export result:{}", result);
                    } catch (Exception e) {
                        LOGGER.error("export result error", e);
                    }
                }
            } else {
                break;
            }
        }
    }
}
