package com.jcspider.server.component.ifc;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public interface JCLockTool {

    boolean getLock(String key);

    void releaseLock(String key);
}
