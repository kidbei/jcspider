package com.jcspider.server.component;

import com.jcspider.server.model.FindTask;
import com.jcspider.server.model.MethodResult;
import com.jcspider.server.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class JCNashornProcess implements JCProcessComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(JCNashornProcess.class);


    @Override
    public List<FindTask> runStart(long projectId, String url) {
        return null;
    }

    @Override
    public MethodResult runMethod(long projectId, Response response) {
        return null;
    }

    @Override
    public void start() throws ComponentInitException {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String name() {
        return "jcNashornProcess";
    }
}
