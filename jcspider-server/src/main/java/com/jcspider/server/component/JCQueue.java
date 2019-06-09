package com.jcspider.server.component;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public interface JCQueue extends JCComponent {

    void sub(String topic, QueueOnMessage onMessage);

    void pub(String topic, Object message);

    void bPub(String topic, Object message);

    Object bPop(String topic);

    String blockingPopProcessTask(String localIp);

    long blockingPopProcessProjectStart(String localIp);

    void blockingPushProcessTask(String processIp, String taskId);

    void blockingPushProcessProjectStart(String processIp, long projectId);

}
