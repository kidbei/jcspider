package com.jcspider.server.web.api.service;

import com.jcspider.server.component.core.OnEvent;
import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.utils.IDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DebugService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugService.class);

    @Autowired
    private             JCQueue jcQueue;

    private final Map<String, CompletableFuture<DebugResult>> pendingDebugMap = Collections.synchronizedMap(new HashMap<>());

    @PostConstruct
    public void init() {
        this.jcQueue.subscribe(Constant.TOPIC_DEBUG_PROJECT_RESP, new DebugTaskRespEvent());
    }


    class DebugTaskRespEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            DebugResult debugResult = (DebugResult) value;
            LOGGER.info("debug task success", debugResult);
            CompletableFuture<DebugResult> future = pendingDebugMap.remove(debugResult.getRequestId());
            if (future == null) {
                LOGGER.warn("future is already timeout ?, requestId:{}", debugResult.getRequestId());
            } else {
                future.complete(debugResult);
            }
        }
    }



    public DebugResult debug(DebugTask debugTask) {
        LOGGER.info("debug task", debugTask);
        final String requestId = IDUtils.genUUID();
        debugTask.setRequestId(requestId);
        CompletableFuture<DebugResult> future = new CompletableFuture<>();
        this.pendingDebugMap.put(requestId, future);
        this.jcQueue.publish(Constant.TOPIC_DEBUG_PROJECT_REQ, debugTask);
        try {
            return future.get(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            this.pendingDebugMap.remove(requestId);
            LOGGER.error("debug error", e);
            DebugResult debugResult = new DebugResult();
            debugResult.setSuccess(false);
            debugResult.setStack(e.toString());
            return debugResult;
        }
    }

}
