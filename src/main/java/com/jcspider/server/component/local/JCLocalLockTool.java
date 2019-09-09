package com.jcspider.server.component.local;

import com.jcspider.server.component.ifc.JCLockTool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public class JCLocalLockTool implements JCLockTool {

    private final Set<String> lockSet = Collections.synchronizedSet(new HashSet<>());


    @Override
    public boolean getLock(String key) {
        return !lockSet.contains(key);
    }

    @Override
    public void releaseLock(String key) {
        this.lockSet.remove(key);
    }


}
