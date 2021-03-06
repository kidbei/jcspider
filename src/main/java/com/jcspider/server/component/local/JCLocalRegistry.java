package com.jcspider.server.component.local;

import com.jcspider.server.component.ifc.JCRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class JCLocalRegistry implements JCRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCLocalRegistry.class);

    private List<String> dispatcherNodes = new ArrayList<>();
    private List<String> processNodes = new ArrayList<>();

    @Override
    public void start() {
        LOGGER.info("start local registry");
    }

    @Override
    public void shutdown() {
        LOGGER.info("shutdown local registry");
        this.dispatcherNodes.clear();
    }

    @Override
    public String name() {
        return "localRegistry";
    }

    @Override
    public synchronized void registerDispatcher(String host) {
        LOGGER.info("register dispatcher:{}", host);
        if (!this.dispatcherNodes.contains(host)) {
            this.dispatcherNodes.add(host);
        }
    }

    @Override
    public synchronized void registerProcess(String host) {
        LOGGER.info("register process:{}", host);
        this.processNodes.add(host);
    }

    @Override
    public List<String> listDispatchers() {
        return this.dispatcherNodes;
    }

    @Override
    public List<String> listProcesses() {
        return this.processNodes;
    }
}
