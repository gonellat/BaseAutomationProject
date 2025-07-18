package step_definitions;

import org.apache.logging.log4j.Logger;
import utils.TestLoggerHolder;

public abstract class BaseStepsSupport {

    protected Logger log;

    protected void logStep(String message) {
        if (log == null) log = TestLoggerHolder.getLogger();
        log.info("📝 Step: " + message);
    }
}

