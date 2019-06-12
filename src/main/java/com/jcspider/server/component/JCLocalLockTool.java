package com.jcspider.server.component;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public class JCLocalLockTool implements JCLockTool {

    @Override
    public boolean getLock(String key) {
        return true;
    }



}
