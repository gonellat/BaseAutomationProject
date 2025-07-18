package utils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * TestLoggerHolder is a utility class for storing and retrieving a per-thread logger
 * and the log file path used by that test.
 * <p>
 * This class uses ThreadLocal to ensure thread-safe storage of logger instances
 * and their corresponding log file paths. It is useful in parallel test environments.
 * </p>
 */
public class TestLoggerHolder {

   /**
    * Constructor required for Sonar
    */
   private TestLoggerHolder() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * A ThreadLocal variable to hold the logger for each thread.
    */
   private static final ThreadLocal<Logger> threadLocalLogger = new ThreadLocal<>();

   /**
    * A ThreadLocal variable to hold the log file path for each thread.
    */
   private static final ThreadLocal<String> logFilePath = new ThreadLocal<>();

   /**
    * Sets the Logger for the current thread.
    *
    * @param logger The logger for the current thread
    */
   public static void setLogger(Logger logger) {
      threadLocalLogger.set(logger);
   }

   /**
    * Returns the Logger for the current thread.
    * If no Logger is set, returns a fallback global logger.
    *
    * @return the current thread's logger instance
    */
   public static Logger getLogger() {
      Logger logger = threadLocalLogger.get();
      return (logger != null) ? logger : LogManager.getLogger("GlobalTestLogger");
   }

   /**
    * Clears the logger and log file path from the current thread's local storage.
    */
   public static void clear() {
      threadLocalLogger.remove();
      logFilePath.remove();
   }

   /**
    * Sets the log file path for the current thread.
    *
    * @param path the log file path
    */
   public static void setLogFilePath(String path) {
      logFilePath.set(path);
   }

   /**
    * Gets the log file path for the current thread.
    *
    * @return the log file path
    */
   public static String getLogFilePath() {
      return logFilePath.get();
   }
}
