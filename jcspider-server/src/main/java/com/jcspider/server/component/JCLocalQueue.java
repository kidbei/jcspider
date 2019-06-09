package com.jcspider.server.component;

import com.jcspider.server.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zhuang.hu
 * @since 30 May 2019
 */
public class JCLocalQueue implements JCQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCLocalQueue.class);

    private final Map<String, QueueOnMessage> onMessageMap = new HashMap<>();
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
                    QueueOnMessage queueOnMessage = onMessageMap.get(subData.topic);
                    if (queueOnMessage == null) {
                        LOGGER.warn("no consumer for sub data:{}", subData);
                        return;
                    }
                    subData.queueOnMessage.onMessage(subData.topic, subData.data);
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
        if (onMessageMap.containsKey(topic)) {
            throw new IllegalStateException("topic is already had subscription:" + topic);
        }
        this.onMessageMap.put(topic, onMessage);
        LOGGER.info("new subscription for topic:{}", topic);
    }

    @Override
    public void pub(String topic, Object message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("pub message, topic:{}, message:{}", topic, message);
        }
        QueueOnMessage queueOnMessage = this.onMessageMap.get(topic);
        if (queueOnMessage == null) {
            LOGGER.warn("no consumer for topic:{}", topic);
            return;
        }
        try {
            this.queue.put(new SubData(queueOnMessage, topic, message));
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
        QueueOnMessage queueOnMessage;
        String topic;
        Object data;

        public SubData(QueueOnMessage queueOnMessage, String topic, Object data) {
            this.queueOnMessage = queueOnMessage;
            this.topic = topic;
            this.data = data;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SubData{");
            sb.append("queueOnMessage=").append(queueOnMessage);
            sb.append(", topic='").append(topic).append('\'');
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }
    }
}
