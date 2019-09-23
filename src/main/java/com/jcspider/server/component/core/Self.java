package com.jcspider.server.component.core;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.model.SelfLog;
import com.jcspider.server.model.Task;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.IDUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public class Self implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Self.class);

    private static final long serialVersionUID = -224319223758375720L;
    private long projectId;
    private TreeMap<String, Task> newTasks = new TreeMap<>();
    private List<SelfLog>   logs;
    private String  fromTaskId;


    public Self(long projectId, String fromTaskId) {
        this.projectId = projectId;
        this.fromTaskId = fromTaskId;
    }

    public void crawl(String url, Map<String, Object> options) {
        this.checkOptions(options);
        String method = MapUtils.getString(options, "method");
        Task task = new Task();
        task.setId(IDUtils.genTaskId(projectId, url, method));
        task.setCreatedAt(System.currentTimeMillis());
        task.setProjectId(projectId);
        task.setSourceUrl(url);
        task.setMethod(method);
        task.setFromTaskId(this.fromTaskId);
        if (options.containsKey("headers")) {
            task.setHeaders(JSON.toJSONString(options.get("headers")));
        }
        if (options.containsKey("charset")) {
            task.setCharset(MapUtils.getString(options, "charset"));
        }
        if (options.containsKey("extra")) {
            task.setExtra(JSON.toJSONString(options.get("extra")));
        }
        if (options.containsKey("fetchType")) {
            task.setFetchType(MapUtils.getString(options, "fetchType"));
        } else {
            task.setFetchType(Constant.FETCH_TYPE_HTML);
        }
        if (options.containsKey("proxy")) {
            task.setProxy(MapUtils.getString(options, "proxy"));
        }
        task.setStatus(Constant.TASK_STATUS_NONE);
        if (options.containsKey("scheduleType")) {
            task.setScheduleType(MapUtils.getString(options, "scheduleType"));
        } else {
            task.setScheduleType(Constant.SCHEDULE_TYPE_NONE);
        }
        if (options.containsKey("scheduleValue")) {
            task.setScheduleValue(MapUtils.getLongValue(options, "scheduleValue"));
        } else {
            task.setScheduleValue(0L);
        }
        if (options.containsKey("expireValue")) {
            task.setExpireValue(MapUtils.getLong(options, "expireValue"));
        } else {
            task.setExpireValue(0L);
        }
        this.newTasks.putIfAbsent(task.getId(), task);
    }


    public void log(String logText) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }
        this.logs.add(new SelfLog(logText));
        LOGGER.info(logText);
    }

    public void log(String logText, boolean persistent) {
        this.logs.add(new SelfLog(logText, persistent));
        LOGGER.info(logText);
    }

    public void log(String level, String logText, boolean persistent) {
        SelfLog log = new SelfLog(logText, persistent);
        log.setLevel(level);
        this.logs.add(log);
        LOGGER.info(logText);
    }


    public long getProjectId() {
        return projectId;
    }

    private void checkOptions(Map<String,Object> options) {
        if (!options.containsKey("method")) {
            throw new IllegalArgumentException("no method found for task");
        }

    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public List<Task> getNewTasks() {
        return new ArrayList<>(newTasks.values());
    }

    public boolean hasNewTasks() {
        return !this.newTasks.isEmpty();
    }


    public List<SelfLog> getLogs() {
        return logs;
    }

    public boolean hasLog() {
        return this.logs != null;
    }
}
