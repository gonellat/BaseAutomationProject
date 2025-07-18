package step_definitions;

import org.apache.logging.log4j.Logger;
import utils.TestLoggerHolder;

/**
 * Base class for Cucumber step definitions to provide common logging
 * functionality.
 */
public abstract class BaseStepsSupport {

   protected Logger log;

   /**
    * Default constructor.
    */
   public BaseStepsSupport() {
      // no-op
   }

   protected void logStep(String message) {
      if (log == null)
         log = TestLoggerHolder.getLogger();
      log.info("📝 Step: " + message);
   }
}
