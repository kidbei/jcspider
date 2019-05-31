package com.jcspider.server.component;

import com.jcspider.server.utils.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class JCDispatcher implements JCComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCDispatcher.class);

    @Autowired
    private JCQueue jcQueue;
    @Autowired
    private JCRegistry  jcRegistry;
    @Value("${dispatcher.maxScheduleSize:100}")
    private int maxScheduleSize;

    @Override
    public void start() throws ComponentInitException{
        try {
            String ip = IPUtils.getLocalIP();
            this.jcRegistry.registerDispatcher(ip);
            DispatcherScheduleFactory.init(maxScheduleSize);
            LOGGER.info("reg dispatcher ip:{}, max schedule size:{}", ip, maxScheduleSize);
        } catch (Exception e) {
            throw new ComponentInitException(e, name());
        }
    }

    @Override
    public void shutdown() {
        LOGGER.info("{} is shutdown", name());
    }

    @Override
    public String name() {
        return "dispatcher";
    }
}
