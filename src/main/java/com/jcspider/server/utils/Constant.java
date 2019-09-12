package com.jcspider.server.utils;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Constant {


    public static final String  TOPIC_DISPATCHER_PROJECT_START = "dispatcher:project:start";
    public static final String  TOPIC_START_PROJECT = "start_project";
    public static final String  TOPIC_NEW_TASK = "new_task";
    public static final String  TOPIC_POP_TASK_REQ = "pop_task_req";
    public static final String  TOPIC_POP_TASK_RESP = "pop_task_resp";
    public static final String  TOPIC_STOP_PROJECT = "stop_project";
    public static final String  TOPIC_DEBUG_PROJECT_REQ = "debug_project_req";
    public static final String  TOPIC_DEBUG_PROJECT_RESP = "debug_project_resp";
    public static final String  TOPIC_NO_MORE_TASK = "no_more_task";
    public static final String  TOPIC_RECOVERY_PROJECT = "recovery_project";

    public static final String  TASK_STATUS_ERROR = "error";
    public static final String  TASK_STATUS_RUNNING = "running";
    public static final String  TASK_STATUS_NONE = "init";
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
    public static final String  UNIT_TYPE_HOURS = "hours";


    public static final String  DB_RESULT_EXPORTER = "dbResultExporter";
    public static final String  REDIS_RESULT_EXPORTER = "redisResultExporter";

    public static final String  MODEL_LOCAL = "local";
    public static final String  MODEL_CLUSTER = "cluster";

    public static final String  METHOD_START = "start";

    public static final String  SCRIPT_NASHORN = "nashorn";


    public static final String  PROJECT_ROLE_OWNER = "owner";
    public static final String  PROJECT_ROLE_ADMIN = "admin";
    public static final String  PROJECT_ROLE_GUEST = "guest";

    public static final String  USER_ROLE_SUPER = "super";
    public static final String  USER_ROLE_ADMIN = "admin";
    public static final String  USER_ROLE_NORMAL = "normal";


    public static final String  TOKEN_SALT = "UXsXsdaaXDsE";

    public static final String CORS_METHOD = "OPTIONS";

    public static final long    TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 15;

    public static final String  LEVEL_DEBUG = "debug";
    public static final String  LEVEL_INFO = "info";
    public static final String  LEVEL_ERROR = "error";


    public static final String  TOPIC_RESULT_EXPORTER = "jcspider_result_exporter";

}
