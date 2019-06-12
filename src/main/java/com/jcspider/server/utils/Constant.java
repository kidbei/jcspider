package com.jcspider.server.utils;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Constant {


    public static final String COMPONENT_NAME_DISPATCHER = "dispatcher";


    public static final String  TOPIC_PROCESS_TASK = "process:task:consumer:";
    public static final String  TOPIC_PROCESS_PROJECT_START = "process:project:start:";
    public static final String  TOPIC_DISPATCHER_PROJECT_START = "dispatcher:project:start";
    public static final String  TOPIC_PROCESS_DEBUG = "process:task:debug:consumer:";
    public static final String  TOPIC_PROCESS_DEBUG_TASK_RETURN = "process:task:debug:return:";

    public static final String  TASK_STATUS_ERROR = "error";
    public static final String  TASK_STATUS_RUNNING = "running";
    public static final String  TASK_STATUS_NONE = "none";
    public static final String  TASK_STATUS_DONE = "done";

    public static final String  PROJECT_STATUS_START = "start";
    public static final String  PROJECT_STATUS_STOP = "stop";


    public static final String  FETCH_TYPE_HTML = "html";
    public static final String  FETCH_TYPE_JS = "js";

    public static final String  SCHEDULE_TYPE_NONE = "none";
    public static final String  SCHEDULE_TYPE_ONCE = "once";
    public static final String  SCHEDULE_TYPE_LOOP = "loop";

    public static final String  UNIT_TYPE_SECONDS = "seconds";
    public static final String  UNIT_TYPE_MINUTES = "minutes";


    public static final String  DB_RESULT_EXPORTER = "dbResultExporter";

    public static final String  MODEL_LOCAL = "local";
    public static final String  MODEL_CLUSTER = "cluster";

    public static final String  METHOD_START = "start";

    public static final String  SCRIPT_NASHORN = "nashorn";

}
