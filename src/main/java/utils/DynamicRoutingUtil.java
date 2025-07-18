package utils;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Creates a new logger with a dedicated RollingFileAppender for the given test
 * case
 */
public class DynamicRoutingUtil {

   /**
    * Constructor required for Sonar
    */
   private DynamicRoutingUtil() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * Method for creating a RollingFileAppender for the given test case
    * 
    * @param testName    The name of the test to use as the name of the log
    * @param featureName The name of the feature to use as the path to the log
    * @return Logger for the test case
    */
   @SuppressWarnings("java:S4792")
   public static org.apache.logging.log4j.Logger createLoggerForTest(String testName, String featureName) {

      // 1) Get the feature name or set to defaultFeature if not provided
      String decodedFeatureName = "";
      try {
         decodedFeatureName = URLDecoder.decode(featureName, StandardCharsets.UTF_8.name());
      } catch (Exception e) {
         decodedFeatureName = featureName;
      }

      String finalFeatureName = (decodedFeatureName == null || decodedFeatureName.isEmpty()) ? "defaultFeature"
            : escapeFileName(decodedFeatureName);

      // 2) Clean up the testName to avoid invalid filename characters (and create a
      // unique log for multiple examples
      String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
      String safeTestName = escapeFileName(testName) + "-" + timestamp;

      // 3) Build the path to logs folder : i.e. target/logs/SomeFeature
      String logsDir;

      if ("GlobalRun".equalsIgnoreCase(safeTestName) && "global".equalsIgnoreCase(finalFeatureName)) {
         logsDir = "target/logs";
      } else {
         logsDir = "target/logs/" + finalFeatureName;
      }
      File directory = new File(logsDir);
      if (!directory.exists()) {
         directory.mkdir();
      }

      // 4) Append a static timestamp
      String logFileName = logsDir + File.separator + safeTestName + "-" + timestamp + ".log";
      String logFilePattern = logsDir + File.separator + safeTestName + "-" + timestamp + "%d{yyyy-MM-dd}-%i.log.gz";

      TestLoggerHolder.setLogFilePath(logFileName);

      // 5) Create or retrieve the loggerContext
      LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
      Configuration config = ctx.getConfiguration();

      // 6) Define the layout pattern
      PatternLayout layout = PatternLayout.newBuilder()
            .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] [%t] %c{1} - %msg%n").build();

      // 7) Composite triggering policy
      CompositeTriggeringPolicy compositePolicy = CompositeTriggeringPolicy.createPolicy(
            SizeBasedTriggeringPolicy.createPolicy("10MB"),
            TimeBasedTriggeringPolicy.newBuilder().withInterval(1).build());

      // 8) Create a RollingFileAppender builder
      RollingFileAppender.Builder<?> rollingBuilder = RollingFileAppender.newBuilder().setConfiguration(config)
            .setName("RollingFileAppender_" + safeTestName).withFileName(logFileName)
            .withFilePattern(logFilePattern).withAppend(true).withPolicy(compositePolicy)
            .withStrategy(DefaultRolloverStrategy.newBuilder().withMax("20").build()).setLayout(layout);

      Appender fileAppender = rollingBuilder.build();
      fileAppender.start();
      config.addAppender(fileAppender);

      // 9) Build the console appender
      ConsoleAppender.Builder<?> consoleBuilder = ConsoleAppender.newBuilder().setConfiguration(config)
            .setName("ConsoleAppender_" + safeTestName).setLayout(layout)
            .setTarget(ConsoleAppender.Target.SYSTEM_OUT);

      Appender consoleAppender = consoleBuilder.build();
      consoleAppender.start();
      config.addAppender(consoleAppender);

      // 10) Create a new Logger Config with a unique name
      String loggerName = "DynamicLogger_" + safeTestName;
      LoggerConfig existingConfig = config.getLoggerConfig(loggerName);
      LoggerConfig loggerConfig;
      if (!existingConfig.getName().equals(loggerName) || existingConfig == null) {
         loggerConfig = LoggerConfig.newBuilder().withConfig(config).withLoggerName(loggerName).withLevel(Level.INFO)
               .withAdditivity(false).build();
      } else {
         loggerConfig = existingConfig;
      }

      // 11) Add the appenders
      loggerConfig.addAppender(fileAppender, Level.INFO, null);
      loggerConfig.addAppender(consoleAppender, Level.INFO, null);

      // 12) Add or replace the logger in the configuration
      config.addLogger(loggerName, loggerConfig);

      // 12a) Add a logger for io.netty to suppress warnings below ERROR
      LoggerConfig nettyLoggerConfig = new LoggerConfig("io.netty", Level.ERROR, false);
      nettyLoggerConfig.addAppender(fileAppender, Level.ERROR, null);
      nettyLoggerConfig.addAppender(consoleAppender, Level.ERROR, null);
      config.addLogger("io.netty", nettyLoggerConfig);

      // 13) Update the context to apply changes
      ctx.updateLoggers();

      Logger logger = LogManager.getLogger(loggerName);
      logger.info("Dynamic logger initialised for test:" + testName);

      // 14) Return the logger
      return logger;
   }

   /**
    * Replaces invalid filename characters with an underscore
    * 
    * @param fileName The original filename
    * @return The sanitised filename
    */
   private static String escapeFileName(String fileName) {
      return fileName == null ? "null" : fileName.replaceAll("[^A-Za-z0-9._-]", "_");
   }
}
