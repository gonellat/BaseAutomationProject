package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for dynamically obtaining a Log4j2 logger
 * <p> 
 * This eliminates the need to manually define a logger in each page class
 * It automatically detects the calling class and provides an instance
 * of {@link Logger} for that class </p>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 * private static final Logger TestLoggerHolder.getLogger() = LoggerUtil.getLogger();
 * </pre>
 * 
 * <p> This ensures that logs are correctly attributed to the calling class. </p>
 */
public class LoggerUtil {
   
   /**
    * Constructor required for Sonar
    */
   private LoggerUtil() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * Returns a Log4j2 {@link Logger} instance for the calling class.
    * <p>
    * This method dynamically determines the class that called it, 
    * so there is no need to manually pass the class name.
    * </p>
    * 
    * @return A {link Logger} instance for the calling class
    */
   public static Logger getLogger() {
      return LogManager.getLogger(getCallingClass());
   }
   
   /**
    * Retrieves the calling class dynamically
    * 
    * @return Calling class name
    */
   private static Class<?> getCallingClass() {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      try {
         return Class.forName(stackTrace[3].getClassName()); // Dynamically gets caller class
      }
      catch (ClassNotFoundException e) {
         throw new CustomExceptions.LoggerUtilException("LoggerUtil: Unable to determine calling class", e);
      }
   }
}
