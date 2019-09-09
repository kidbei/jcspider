package com.jcspider.server.component.core;

import com.jcspider.server.model.ComponentInitException;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.RunMethodException;
import com.jcspider.server.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class JSR223EngineProcess extends JCProcess {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSR223EngineProcess.class);

    private final Map<Long, ScriptEngine>     projectEngineCache = new ConcurrentHashMap<>();
    @Value("${process.maxCodeCache:10000}")
    private int maxCodeCache;



    @Override
    public void start() throws ComponentInitException {
        super.start();
    }


    @Override
    Object runMethod(long projectId, String method, Self self, Object params) throws RunMethodException{
        ScriptEngine scriptEngine = this.getProjectEngine(projectId);
        try {
            return ((Invocable)scriptEngine).invokeFunction(method, self, params);
        } catch (Exception e) {
            throw new RunMethodException(e, method);
        }
    }


    private synchronized ScriptEngine getProjectEngine(long projectId) {
        ScriptEngine scriptEngine = this.projectEngineCache.get(projectId);
        if (scriptEngine == null) {
            LOGGER.info("init nashorn script engine for project {}", projectId);
            scriptEngine = new ScriptEngineManager().getEngineByName(Constant.SCRIPT_NASHORN);
            Project project = this.getProject(projectId);
            try {
                scriptEngine.eval(project.getScriptText());
                LOGGER.info("init nashorn script engine complete for project:{}", projectId);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
            this.projectEngineCache.put(projectId, scriptEngine);
        }
        return scriptEngine;
    }


    @Override
    public String name() {
        return "jcNashornProcess";
    }
}
