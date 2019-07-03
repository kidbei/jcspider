package com.jcspider.server.component.local;

import com.jcspider.server.component.JCLockTool;

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
