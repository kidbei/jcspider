package com.jcspider.server.api.service;

import com.jcspider.server.component.JCQueue;
import com.jcspider.server.component.JCRegistry;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.utils.IDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DebugService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugService.class);

    @Autowired
    private             JCQueue jcQueue;
    @Autowired
    private JCRegistry  jcRegistry;


    private String selectRandom() {
        List<String> nodes = this.jcRegistry.listProcesses();
        if (nodes == null) {
            throw new NullPointerException("no process node found");
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        int i = new Random().nextInt(nodes.size() - 1);
        return nodes.get(i);
    }

    public DebugResult debug(DebugTask debugTask) {
        final String requestId = IDUtils.genUUID();
        debugTask.setRequestId(requestId);
        debugTask.setProcessNode(this.selectRandom());
        CompletableFuture<DebugResult> future = new CompletableFuture<>();
        new Thread(() -> {
            DebugResult debugResult = this.jcQueue.blockingPopProcessDebugTaskReturn(requestId);
            future.complete(debugResult);
        }).start();
        this.jcQueue.blockingPushProcessDebugTask(debugTask);
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
