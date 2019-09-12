package com.jcspider.server.component.ifc;

import com.jcspider.server.model.TaskResult;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public interface ResultExporter extends JCComponent{

    void export(TaskResult result);

    void delete(long projectId, String taskId);

}
