package com.jcspider.server.api.service;

import com.jcspider.server.component.JCQueue;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.utils.IDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DebugService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugService.class);

    @Autowired
    private JCQueue jcQueue;

    public DebugResult debug(DebugTask debugTask) {
        final String requestId = IDUtils.genUUID();
        debugTask.setRequestId(requestId);
        this.jcQueue.blockingPushProcessDebugTask(debugTask);
        CompletableFuture<DebugResult> future = new CompletableFuture<>();

        new Thread(() -> {
            DebugResult debugResult = this.jcQueue.blockingPopProcessDebugTaskReturn(requestId);
            future.complete(debugResult);
        }).start();

        try {
            return future.get(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            DebugResult debugResult = new DebugResult();
            debugResult.setSuccess(false);
            debugResult.setStack(e.toString());
            return debugResult;
        }
    }

}
