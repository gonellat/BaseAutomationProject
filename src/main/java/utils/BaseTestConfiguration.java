package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import constants.DateTimeFormatConstants;
import constants.IConstants;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * This class is used at the start of a test run to store variables supplied by
 * the property files.
 */
public class BaseTestConfiguration {

   private static String env;

   private static String runDateTime;
   private static String baseUrl;
   private static String headless;
   private static String tagValue;
   private static String browser;
   private static String downloadsPath;
   private static int maxLocalInstances;
   private static BaseObjectManager pageObjectManager;

   /** the Products API page **/
   public static String getProductsAPI;

   // ssh
   private static String reportUsername;
   private static String reportUserPassword;

   /**
    * Constructs the BaseTestConfiguration used to initialize shared test config
    * variables.
    */
   public BaseTestConfiguration() {
      // no-op
   }

   /**
    * Injects a page object manager instance.
    *
    * @param manager the page object manager to set
    */
   public static void setPageObjectManager(BaseObjectManager manager) {
      pageObjectManager = manager;
   }

   /**
    * Retrieves the current page object manager.
    *
    * @return the page object manager instance
    */
   public static BaseObjectManager getPageObjectManager() {
      if (pageObjectManager == null) {
         throw new IllegalStateException(
               "PageObjectManager not set. Did you forget to call setPageObjectManager()?");
      }
      return pageObjectManager;
   }

   /**
    * Reads a `.properties` file from the given path and loads it into a
    * {@link Properties} object.
    *
    * @param file the path to the properties file
    * @return the loaded properties
    * @throws IOException if the file cannot be read
    */
   public static Properties readProperties(Path file) throws IOException {
      try (FileInputStream fileInputStream = new FileInputStream(file.toFile())) {
         Properties properties = new Properties();
         properties.load(fileInputStream);
         return properties;
      }
   }

   /**
    * This method reads the supplied properties file (including the Cucumber
    * Runner). It also handles properties being supplied via a Maven Run
    * Configuration or via an Azure pipeline.
    * 
    * @param localFile This is the local property file path
    * @throws IOException general IO exception if the file cannot be read or
    *                     written to
    */
   public static void readRunProperties(Path localFile) throws IOException {
      Properties properties = readProperties(localFile);

      TestLoggerHolder.getLogger()
            .info("================================================================================");
      TestLoggerHolder.getLogger()
            .info("********************************************************************************");
      TestLoggerHolder.getLogger().info("* Starting Test Execution Run");
      TestLoggerHolder.getLogger()
            .info("********************************************************************************");
      TestLoggerHolder.getLogger()
            .info("================================================================================");

      getEnvironmentFromPropertyFileMaven(properties);

      getTagValueFromMavenRunner(properties);

      getBrowserFromPropertyFileMaven(properties);

      getHeadlessFromPropertyFileMaven(properties);

      getMaxLocalInstancesFromPropertyFileMaven(properties);

      // Logging the environment variables used in the current test run
      TestLoggerHolder.getLogger().info(
            String.format("Environment (env)                                     Variable Setting: %s", getEnv()));
      TestLoggerHolder.getLogger().info(String
            .format("Headless                                              Variable Setting: %s", getHeadless()));
      TestLoggerHolder.getLogger().info(String
            .format("Browser                                               Variable Setting: %s", getBrowser()));
      TestLoggerHolder.getLogger().info(String
            .format("Tags                                                  Variable Setting: %s", getTagValue()));
   }

   /**
    * This method gets the browser to use from Maven or the config file
    * 
    * @param properties
    */
   private static void getBrowserFromPropertyFileMaven(Properties properties) {
      // Check if the browser property has been supplied via Maven, or in the
      // configuration file
      if (System.getProperty(IConstants.BROWSER) == null) {
         browser = properties.getProperty(IConstants.BROWSER);
         // If we haven't supplied it in the configuration file or Maven then use the
         // default of 'Chrome'
         if (browser.equals(IConstants.NOT_SET_BROWSER_VALUE)) {
            browser = IConstants.CHROME;
         }
      } else {
         // If yes: read it from the system
         browser = System.getProperty(IConstants.BROWSER);
      }
   }

   /**
    * This method gets the tag value to use from Maven or the config file
    * 
    * @param properties
    */
   private static void getTagValueFromMavenRunner(Properties properties) {
      String mavenTagValue = System.getProperty(IConstants.CUCUMBER_FILTER_TAGS);

      TestLoggerHolder.getLogger().info("{} {}", "Checking Cucumber filter tags from Maven: ", mavenTagValue);

      if (mavenTagValue != null) {
         setTagValueAndLog(mavenTagValue, "Tag Value Supplied via Maven: ");
         return;
      }

      String propertyTagValue = properties.getProperty(IConstants.TAG_VALUE_LOWER_CASE);
      if (isValidTag(propertyTagValue)) {
         setTagValueAndLog(propertyTagValue, "Tag Value Supplied via run.properties: ");
         return;
      }

      detectAndSetTagFromRunner();
   }

   /**
    * Attempts to detect and set the tag from the Cucumber Runner class using
    * reflection
    */
   private static void detectAndSetTagFromRunner() {
      // Try to retrieve the tags from the Cucumber runner annotation using reflection
      String runnerClassName = System.getProperty("runnerClass");

      if (runnerClassName == null || runnerClassName.trim().isEmpty()) {
         runnerClassName = detectTestNGRunnerClass();
      }

      if (runnerClassName == null) {
         setTagValueAndLog("See XML File", "Tag Value not suppliede by Maven or run.properties, defaulting to: ");
         return;
      }

      try {
         // Dynamically Load the runner class
         Class<?> runnerClazz = Class.forName(runnerClassName);

         // Attempt to read the @CucumberOptions annotation
         CucumberOptions options = runnerClazz.getAnnotation(CucumberOptions.class);
         if (options != null && !options.tags().isEmpty()) {
            // if that runner has tags set them
            setTagValueAndLog(String.join(" ", options.tags()), "Tag Value retrieved from CucumberOptions:");
            return;
         }
      } catch (ClassNotFoundException e) {
         TestLoggerHolder.getLogger().error("Runner class not found: " + runnerClassName + " => " + e.getMessage());
      }
      setTagValueAndLog("See XML File", "Tag value not found, defaulting to: ");
   }

   /**
    * Checks if a tag value is valid
    * 
    * @param tagValue The tag value to check
    * @return {@code true} if valid, otherwise {@code false}
    */
   private static boolean isValidTag(String tagValue) {
      return tagValue != null && !tagValue.trim().isEmpty() && !tagValue.equals(IConstants.NOT_SET_TAG_VALUE);
   }

   /**
    * Sets the tag value and logs the message
    * 
    * @param value      The tag value to set
    * @param logMessage The log message prefix
    */
   private static void setTagValueAndLog(String value, String logMessage) {
      tagValue = value;
      TestLoggerHolder.getLogger().info(logMessage + tagValue);
   }

   /**
    * Attempt to detect the TESTNG runner class dynamically by scanning the current
    * thread's stack trace. This is useful when running tests via TestNG, where the
    * "runnerClass" system property may not be set
    * 
    * @return The fully qualified name of the detected TestNG runner class or null
    *         if not found
    */
   private static String detectTestNGRunnerClass() {
      try {
         for (Class<?> clazz : getAllLoadedClasses()) {
            if (AbstractTestNGCucumberTests.class.isAssignableFrom(clazz)
                  && !clazz.equals(AbstractTestNGCucumberTests.class)) {
               return clazz.getName(); // Return first found runner class
            }
         }
      } catch (Exception e) {
         TestLoggerHolder.getLogger().warn("Failed to detect TestNG Runner Class; " + e.getMessage());
      }
      return null;
   }

   /**
    * Retrieves all loaded classes available in the JVM at runtime Used to scan for
    * TestNG runner classes extending AbstractTestNGCucumberTests
    * 
    * @return
    */
   private static List<Class<?>> getAllLoadedClasses() {
      List<Class<?>> classes = new ArrayList<>();
      try {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         for (Package pkg : Package.getPackages()) {
            String packageName = pkg.getName();
            loadClassesFromPackage(packageName, classLoader, classes);
         }
      } catch (Exception e) {
         TestLoggerHolder.getLogger().warn("Error retrieving loaded classes: " + e.getMessage());
      }
      return classes;
   }

   /**
    * Loads all classes from a given package and adds them to the provided list
    * 
    * @param packageName The package name to scan
    * @param classLoader The class loader to use
    * @param classes     The list to store loaded classes
    */
   private static void loadClassesFromPackage(String packageName, ClassLoader classLoader, List<Class<?>> classes) {
      try {
         for (String className : getClassNamesFromPackage(packageName)) {
            loadClass(className, classLoader, packageName, classes);
         }
      } catch (Exception e) {
         TestLoggerHolder.getLogger().warn("Error processing package: " + e.getMessage());
      }
   }

   /**
    * Loads a single class and adds it to the provided list
    * 
    * @param className   The fully qualified class name
    * @param clasLoader  The class loader to use
    * @param packageName The package the class belongs to (for loging purposes)
    * @param classes     The list to store the loaded class.
    */
   private static void loadClass(String className, ClassLoader classLoader, String packageName,
         List<Class<?>> classes) {

      try {
         classes.add(Class.forName(className, false, classLoader));
      } catch (ClassNotFoundException e) {
         TestLoggerHolder.getLogger().warn("Class not found: " + e.getMessage());
      } catch (LinkageError e) {
         TestLoggerHolder.getLogger().warn("Linkage error when loading class: " + e.getMessage());
      } catch (SecurityException e) {
         TestLoggerHolder.getLogger().warn("Security Exception when accessing package: " + packageName);
      } catch (Exception e) {
         TestLoggerHolder.getLogger()
               .warn("Unexpected error loading class from package " + packageName + ": " + e.getMessage());
      }
   }

   /**
    * Retrieves all classnames from a given package dynamically
    * 
    * @param packageName The name of the package to scan
    * @return A list of fully qualified class names
    */
   private static List<String> getClassNamesFromPackage(String packageName) {
      List<String> classNames = new ArrayList<>();
      try {
         String packagePath = packageName.replace(".", "/");
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         URL packageURL = classLoader.getResource(packagePath);
         if (packageURL != null) {
            File packageDir = new File(packageURL.toURI());

            File[] files = packageDir.listFiles();
            if (files == null) {
               TestLoggerHolder.getLogger().trace("No files found in package directory: " + packageName);
               return classNames;
            }

            for (File file : files) {
               if (file.getName().endsWith(".class")) {
                  String className = packageName + "." + file.getName().replace(".class", "");
                  classNames.add(className);
               }
            }
         } else {
            TestLoggerHolder.getLogger().trace("Package not found: " + packageName);
         }
      } catch (Exception e) {
         TestLoggerHolder.getLogger()
               .trace("Unexpected Error retrieving classes from package: " + packageName + ": " + e.getMessage());
      }
      return classNames;
   }

   /**
    * This method gets the headless value to use from Maven or the config file
    * 
    * @param properties
    */
   private static void getHeadlessFromPropertyFileMaven(Properties properties) {
      // Check if the headless property has been supplied via Maven, or in the
      // configuration file
      if (System.getProperty(IConstants.HEADLESS_VALUE_LOWER_CASE) == null) {
         headless = properties.getProperty(IConstants.HEADLESS_VALUE_LOWER_CASE);
         // If we haven't supplied it in the configuration file or Maven then use the
         // default of FALSE
         if (headless.equals(IConstants.NOT_SET_HEADLESS_VALUE)) {
            headless = IConstants.FALSE;
         }
      } else {
         // If yes: read it from the system
         headless = System.getProperty(IConstants.HEADLESS_VALUE_LOWER_CASE);
      }
   }

   /**
    * This method gets the environment to use from Maven or the config file
    * 
    * @param properties
    */
   private static void getEnvironmentFromPropertyFileMaven(Properties properties) {
      // Check if the environment property has been supplied via Maven, or in the
      // configuration file
      if (System.getProperty(IConstants.ENV_LOWER_CASE) == null) {
         env = properties.getProperty(IConstants.ENV_LOWER_CASE);
         // If we haven't supplied it in the configuration file or Maven then use the
         // default of 'dev'
         if (env.equals(IConstants.NOT_SET_ENV_VALUE)) {
            env = "local";
         }
      } else {
         // If yes: read it from the system
         env = System.getProperty(IConstants.ENV_LOWER_CASE);
      }
   }

   /**
    * This method gets the Max Local Instances value to use from Maven or the
    * config file
    * 
    * @param properties
    */
   private static void getMaxLocalInstancesFromPropertyFileMaven(Properties properties) {
      // Check if the Max Local Instances property has been supplied via Maven, or in
      // the
      // configuration file
      if (System.getProperty(IConstants.MAX_LOCAL_INSTANCES) == null) {
         maxLocalInstances = Integer.valueOf(properties.getProperty(IConstants.MAX_LOCAL_INSTANCES));
      } else {
         // If yes: read it from the system
         maxLocalInstances = Integer.valueOf(System.getProperty(IConstants.MAX_LOCAL_INSTANCES));
      }
   }

   /**
    * This sets the run date time for the report
    * <p>
    * <strong>NB: </strong> It uses the date / time format required by Extent
    * Reports.
    */
   public static void setRunDateTime() {
      runDateTime = DateUtils.getCurrentDateTime(DateTimeFormatConstants.YYYYMMDDHHMMSS);
      System.setProperty("current.date.time", runDateTime);
   }

   /**
    * This gets the environment
    * 
    * @return environment to use
    */
   public static String getEnv() {
      if (env == null || env.trim().isEmpty()) {
         System.err.println("[WARN] env not initialized — defaulting to local");
         env = "local";
      }
      return env;
   }

   /**
    * This gets the headless value
    * 
    * @return headless value to use
    */
   public static String getHeadless() {
      if (headless == null || headless.trim().isEmpty()) {
         System.err.println("[WARN] headless not initialized — defaulting to false");
         headless = "false";
      }
      return headless;
   }

   /**
    * This gets the run date time
    * 
    * @return the test run date time
    */
   public static String getRunDateTime() {
      return runDateTime;
   }

   /**
    * This gets the base URL
    * 
    * @return the base URL
    */
   public static String getBaseUrl() {
      return baseUrl;
   }

   /**
    * Retrieves the configured browser to be used for tests (e.g., chrome,
    * firefox).
    *
    * @return the browser name
    */
   public static String getBrowser() {
      if (browser == null || browser.trim().isEmpty()) {
         System.err.println("[WARN] browser not initialized — defaulting to Chrome");
         browser = "chrome"; //
      }
      return browser;
   }

   /**
    * This gets the tag value
    * 
    * @return the Tags that have been set
    */
   public static String getTagValue() {
      if (tagValue == null || tagValue.trim().isEmpty()) {
         System.err.println("[WARN] tagValue not initialized — defaulting to '@Smoke'");
         tagValue = "@Smoke";
      }
      return tagValue;
   }

   /**
    * This gets the Max Local Instances value
    * 
    * @return maxLocalInstances value to use
    */
   public static int getMaxLocalInstances() {
      return maxLocalInstances;
   }

   /**
    * getter for report username
    * 
    * @return - username
    */
   public static String getReportUsername() {
      return reportUsername;
   }

   /**
    * getter for rep2 report user password
    * 
    * @return - user password
    */
   public static String getReportUserPassword() {
      return reportUserPassword;
   }

   /**
    * Getter for downloadsPath
    * 
    * @return start of the path to the Downloads folder
    */
   public static String getDownloadsPath() {
      return downloadsPath;
   }

   /**
    * This method gets the productsApi page
    * 
    * @return getProductsAPI
    */
   public static String getProductsApi() {
      return getProductsAPI;
   }
}