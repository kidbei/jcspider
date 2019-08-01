package com.jcspider.server.component;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public interface JCLockTool {

    boolean getLock(String key);

    void releaseLock(String key);
}
