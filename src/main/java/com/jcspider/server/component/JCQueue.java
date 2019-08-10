package com.jcspider.server.component;

import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;

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

    DebugTask blockingPopProcessDebugTask(String localIp);

    DebugResult blockingPopProcessDebugTaskReturn(String requestId);

    long blockingPopProcessProjectStart(String localIp);

    void blockingPushProcessTask(String processIp, String taskId);

    void blockingPushProcessProjectStart(String processIp, long projectId);

    void blockingPushProcessDebugTask(DebugTask debugTask);

    void blockingPushProcessDebugTaskReturn(DebugResult debugResult);

    void pubDispatcherStart(long projectId);

    void subDispatcherStart(QueueOnMessage queueOnMessage);

    void subDispatcherLoopUpdate(QueueOnMessage queueOnMessage);

    void pubDispatcherStop(long projectId);

    void pubDispatcherLoopUpdate(long projectId);

    void subDispatcherStop(QueueOnMessage queueOnMessage);

}
