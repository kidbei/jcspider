package com.jcspider.server.component;

import com.jcspider.server.model.TaskResult;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public interface ResultExporter extends JCComponent{


    void export(TaskResult result);

}
