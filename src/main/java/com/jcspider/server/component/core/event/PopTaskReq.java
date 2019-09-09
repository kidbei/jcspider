package com.jcspider.server.component.core.event;

/**
 * @author zhuang.hu Date:2019-09-06 Time:19:57
 */
public class PopTaskReq {

    public long    projectId;
    public int     popSize;

    public PopTaskReq(long projectId, int popSize) {
        this.projectId = projectId;
        this.popSize = popSize;
    }
}
