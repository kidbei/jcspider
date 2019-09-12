package com.jcspider.server.component.core;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.component.ifc.ResultExporter;
import com.jcspider.server.model.TaskResult;
import com.jcspider.server.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author zhuang.hu Date:2019-09-12 Time:17:24
 */
public class RedisResultExporter implements ResultExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisResultExporter.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void export(TaskResult result) {
        redisTemplate.opsForList().leftPush(Constant.TOPIC_RESULT_EXPORTER, JSON.toJSONString(result));
    }

    @Override
    public void delete(long projectId, String taskId) {
        LOGGER.warn("do not support delete in redis");
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String name() {
        return "redisResultExporter";
    }
}
