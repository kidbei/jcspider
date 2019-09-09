package com.jcspider.server.component.core;

/**
 * @author zhuang.hu Date:2019-09-09 Time:14:18
 */
@FunctionalInterface
public interface OnEvent {

    void event(String topic, Object value);

}
