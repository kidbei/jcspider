package com.jcspider.server.component.redis;

import com.alibaba.fastjson.JSON;
import com.jcspider.server.component.JCQueue;
import com.jcspider.server.component.QueueOnMessage;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * @author zhuang.hu Date:2019-07-04 Time:14:38
 */
public class RedisQueue implements JCQueue {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void sub(String topic, QueueOnMessage onMessage) {
        this.redisTemplate.getConnectionFactory().getConnection().subscribe((message, bytes) ->
                    onMessage.onMessage(topic, new String(message.getBody())),
                topic.getBytes());
    }

    @Override
    public void pub(String topic, Object message) {
        this.redisTemplate.getConnectionFactory().getConnection().publish(topic.getBytes(), message.toString().getBytes());
    }

    @Override
    public void bPub(String topic, Object message) {
        this.redisTemplate.opsForList().leftPush(topic, message.toString());
    }

    @Override
    public Object bPop(String topic) {
        try {
            List<byte[]> result = this.redisTemplate.getConnectionFactory().getConnection().bLPop(Short.MAX_VALUE, topic.getBytes());
            return new String(result.get(1));
        } catch (Exception e) {
            return bPop(topic);
        }
    }

    @Override
    public String blockingPopProcessTask(String localIp) {
        return this.bPop(Constant.TOPIC_PROCESS_TASK + localIp).toString();
    }

    @Override
    public DebugTask blockingPopProcessDebugTask(String localIp) {
        return JSON.parseObject((String) this.bPop(Constant.TOPIC_PROCESS_DEBUG + localIp), DebugTask.class);
    }

    @Override
    public DebugResult blockingPopProcessDebugTaskReturn(String requestId) {
        return JSON.parseObject((String)this.bPop(Constant.TOPIC_PROCESS_DEBUG_TASK_RETURN + requestId), DebugResult.class);
    }

    @Override
    public long blockingPopProcessProjectStart(String localIp) {
        return Long.valueOf((String)this.bPop(Constant.TOPIC_PROCESS_PROJECT_START + localIp));
    }

    @Override
    public void blockingPushProcessTask(String processIp, String taskId) {
        this.bPub(Constant.TOPIC_PROCESS_TASK + processIp, taskId);
    }

    @Override
    public void blockingPushProcessProjectStart(String processIp, long projectId) {
        this.bPub(Constant.TOPIC_PROCESS_PROJECT_START + processIp, projectId+"");
    }

    @Override
    public void blockingPushProcessDebugTask(DebugTask debugTask) {
        this.bPub(Constant.TOPIC_PROCESS_DEBUG + debugTask.getProcessNode(), JSON.toJSONString(debugTask));
    }

    @Override
    public void blockingPushProcessDebugTaskReturn(DebugResult debugResult) {
        this.bPub(Constant.TOPIC_PROCESS_DEBUG_TASK_RETURN + debugResult.getRequestId(), JSON.toJSONString(debugResult));
    }

    @Override
    public void pubDispatcherStart(long projectId) {
        this.pub(Constant.TOPIC_DISPATCHER_PROJECT_START, projectId + "");
    }

    @Override
    public void subDispatcherStart(QueueOnMessage queueOnMessage) {
        this.sub(Constant.TOPIC_DISPATCHER_PROJECT_START, (topic,value) -> queueOnMessage.onMessage(topic, Long.valueOf((String) value)));
    }

    @Override
    public void subDispatcherLoopUpdate(QueueOnMessage queueOnMessage) {

    }

    @Override
    public void pubDispatcherStop(long projectId) {
        this.pub(Constant.TOPIC_DISPATCHER_PROJECT_STOP, projectId);
    }

    @Override
    public void pubDispatcherLoopUpdate(long projectId) {

    }

    @Override
    public void subDispatcherStop(QueueOnMessage queueOnMessage) {
        this.sub(Constant.TOPIC_DISPATCHER_PROJECT_STOP, (topic,value) -> queueOnMessage.onMessage(topic, Long.valueOf((String) value)));
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String name() {
        return "redisQueue";
    }
}
