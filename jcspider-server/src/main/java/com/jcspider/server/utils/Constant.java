package com.jcspider.server.utils;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Constant {


    public static final String COMPONENT_NAME_DISPATCHER = "dispatcher";


    public static final String  TOPIC_PROCESS_TASK_SUB = "process:task:consumer:";

    public static final String  TOPIC_PROCESS_PROJECT_START = "process:project:start:";


    public static final String  TASK_STATUS_ERROR = "error";
    public static final String  TASK_STATUS_RUNNING = "running";
    public static final String  TASK_STATUS_NONE = "none";
    public static final String  TASK_STATUS_DONE = "done";


    public static final String  FETCH_TYPE_HTML = "html";
    public static final String  FETCH_TYPE_JS = "js";

    public static final String  SCHEDULE_TYPE_NONE = "none";
    public static final String  SCHEDULE_TYPE_ONCE = "once";
    public static final String  SCHEDULE_TYPE_LOOP = "loop";

}
