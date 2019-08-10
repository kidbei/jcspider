package com.jcspider.server.component.local;

import com.google.common.collect.ArrayListMultimap;
import com.jcspider.server.component.JCQueue;
import com.jcspider.server.component.QueueOnMessage;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.utils.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zhuang.hu
 * @since 30 May 2019
 */
public class JCLocalQueue implements JCQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCLocalQueue.class);

    private final ArrayListMultimap<String, QueueOnMessage> onMessageMap = ArrayListMultimap.create();
    private final LinkedBlockingQueue<SubData> queue = new LinkedBlockingQueue<>();
    private final Map<String, LinkedBlockingQueue<Object>> bDataQueue = new HashMap<>();
    private Thread subThread;
    private volatile boolean isStop = false;

    @Override
    public void start() {
        this.subThread = new Thread(()->{
            while (!isStop && !Thread.interrupted()) {
                try {
                    SubData subData = queue.take();
                    List<QueueOnMessage> queueOnMessages = onMessageMap.get(subData.topic);
                    if (CollectionUtils.isEmpty(queueOnMessages)) {
                        LOGGER.warn("no consumer for sub data:{}", subData);
                        return;
                    }
                    queueOnMessages.forEach(queueOnMessage -> {
                        try {
                            queueOnMessage.onMessage(subData.topic, subData.data);
                        } catch (Exception e) {
                            LOGGER.error("sub message error, topic:{}, message:{}", subData.topic, subData.data, e);
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("sub error", e);
                }
            }
        });
        this.subThread.start();
    }

    @Override
    public void shutdown() {
        this.isStop = true;
        this.subThread.interrupt();
    }

    @Override
    public String name() {
        return "localQueue";
    }

    @Override
    public synchronized void sub(String topic, QueueOnMessage onMessage) {
        this.onMessageMap.put(topic, onMessage);
        LOGGER.info("new subscription for topic:{}", topic);
    }

    @Override
    public void pub(String topic, Object message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("pub message, topic:{}, message:{}", topic, message);
        }
        try {
            this.queue.put(new SubData(topic, message));
        } catch (InterruptedException e) {
            LOGGER.error("in queue error,topic:{}, message:{}", topic, message);
        }
    }

    @Override
    public void bPub(String topic, Object message) {
        LinkedBlockingQueue<Object> queue = this.checkOrCreateQueue(topic);
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            LOGGER.error("put bdata error, data:{}", message, e);
        }
    }

    @Override
    public Object bPop(String topic) {
        LinkedBlockingQueue<Object> queue = this.checkOrCreateQueue(topic);
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String blockingPopProcessTask(String localIp) {
        return this.bPop(Constant.TOPIC_PROCESS_TASK + localIp).toString();
    }

    @Override
    public DebugTask blockingPopProcessDebugTask(String localIp) {
        return (DebugTask) this.bPop(Constant.TOPIC_PROCESS_DEBUG + localIp);
    }

    @Override
    public DebugResult blockingPopProcessDebugTaskReturn(String requestId) {
        return (DebugResult) this.bPop(Constant.TOPIC_PROCESS_DEBUG_TASK_RETURN + requestId);
    }

    @Override
    public long blockingPopProcessProjectStart(String localIp) {
        return (long) this.bPop(Constant.TOPIC_PROCESS_PROJECT_START + localIp);
    }

    @Override
    public void blockingPushProcessTask(String processIp, String taskId) {
        this.bPub(Constant.TOPIC_PROCESS_TASK + processIp, taskId);
    }

    @Override
    public void blockingPushProcessProjectStart(String processIp, long projectId) {
        this.bPub(Constant.TOPIC_PROCESS_PROJECT_START + processIp, projectId);
    }

    @Override
    public void blockingPushProcessDebugTask(DebugTask debugTask) {
        this.bPub(Constant.TOPIC_PROCESS_DEBUG + debugTask.getProcessNode(), debugTask);
    }

    @Override
    public void blockingPushProcessDebugTaskReturn(DebugResult debugResult) {
        this.bPub(Constant.TOPIC_PROCESS_DEBUG_TASK_RETURN + debugResult.getRequestId(), debugResult);
    }

    @Override
    public void pubDispatcherStart(long projectId) {
        this.pub(Constant.TOPIC_DISPATCHER_PROJECT_START, projectId);
    }

    @Override
    public void subDispatcherStart(QueueOnMessage queueOnMessage) {
        this.sub(Constant.TOPIC_DISPATCHER_PROJECT_START, queueOnMessage);
    }

    @Override
    public void subDispatcherLoopUpdate(QueueOnMessage queueOnMessage) {
        this.sub(Constant.TOPIC_DISPATCHER_LOOP_UPDATE, queueOnMessage);
    }

    @Override
    public void pubDispatcherStop(long projectId) {
        this.pub(Constant.TOPIC_DISPATCHER_PROJECT_STOP, projectId);
    }

    @Override
    public void pubDispatcherLoopUpdate(long projectId) {
        this.pub(Constant.TOPIC_DISPATCHER_LOOP_UPDATE, projectId);
    }

    @Override
    public void subDispatcherStop(QueueOnMessage queueOnMessage) {
        this.sub(Constant.TOPIC_DISPATCHER_PROJECT_STOP, queueOnMessage);
    }


    private LinkedBlockingQueue<Object> checkOrCreateQueue(String topic) {
        LinkedBlockingQueue<Object> queue =this.bDataQueue.get(topic);
        if (queue == null) {
            synchronized (this.bDataQueue) {
                queue = this.bDataQueue.get(topic);
                if (queue == null) {
                    queue = new LinkedBlockingQueue<>();
                    this.bDataQueue.put(topic, queue);
                }
            }
        }
        return queue;
    }


    class SubData {
        String topic;
        Object data;

        public SubData(String topic, Object data) {
            this.topic = topic;
            this.data = data;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SubData{");
            sb.append(", topic='").append(topic).append('\'');
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }
    }
}
