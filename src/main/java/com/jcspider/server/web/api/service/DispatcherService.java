package com.jcspider.server.web.api.service;

import com.jcspider.server.component.ifc.JCRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * @Author: Gosin
 * @Date: 2019-06-18 22:43
 */
@Service
public class DispatcherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherService.class);

    @Autowired
    private JCRegistry  jcRegistry;


    public String selectDispatcherNode() {
        List<String> dispatcherNodes = this.jcRegistry.listDispatchers();
        if (CollectionUtils.isEmpty(dispatcherNodes)) {
            LOGGER.error("no dispatcher node active");
            throw new RuntimeException("no dispatcher nodes found");
        }
        if (dispatcherNodes.size() == 1) {
            return dispatcherNodes.get(0);
        }
        return dispatcherNodes.get(new Random().nextInt(dispatcherNodes.size() - 1));
    }


    public List<String> listDispatcherNodes() {
        return this.jcRegistry.listDispatchers();
    }
}
