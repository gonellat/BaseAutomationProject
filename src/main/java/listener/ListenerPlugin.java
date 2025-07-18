package listener;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.aventstack.extentreports.service.ExtentService;

import constants.IConstants;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import pages.BasePageClass;
import utils.*;
import utils.helpers.CucumberDataHelper;

/**
 * Cucumber event listener responsible for logging, Extent Report setup, test
 * lifecycle hooks, and screenshot integration.
 */
public class ListenerPlugin implements ConcurrentEventListener, io.cucumber.plugin.Plugin {

   private static final TestReport testReport = new TestReport();
   private static final String LINE_BREAK = "============================================================";

   private static String featureName;
   private static String stepKeyword;
   private static String stepName;
   private static String testCaseName;
   private static ThreadLocal<List<String>> testCaseTags = new ThreadLocal<>();

   /**
    * Constructs a new ListenerPlugin for handling Cucumber test events.
    */
   public ListenerPlugin() {
      // default constructor
   }

   /**
    * Handles Cucumber event when a test run begins.
    *
    * @param event the test run start event
    */
   public void onTestRunStarted(TestRunStarted event) {
      Logger globalLogger = DynamicRoutingUtil.createLoggerForTest("GlobalRun", "global");
      TestLoggerHolder.setLogger(globalLogger);
      globalLogger.info("Test Run Started " + LocalDateTime.now());
      globalLogger.info("Extent Suite: " + System.getProperty("suiteName", "NonXmlRun"));
      populateExtentEnvInfo();
   }

   /**
    * Handles Cucumber event when a scenario starts.
    *
    * @param event the scenario start event
    */
   public void onTestCaseStarted(TestCaseStarted event) {
      TestReport.closeThreadLocalCollections();

      String rawFeatureName = event.getTestCase().getUri().toString();
      String featureNameWithoutExtension = rawFeatureName.replace(".feature", "");
      String featureName = rawFeatureName.substring(rawFeatureName.lastIndexOf("/") + 1).replace(".feature", "");
      String testName = event.getTestCase().getName();
      String uniqueName = testName + " - " + System.currentTimeMillis();

      setFeatureName(featureName);
      setTestCaseName(uniqueName);
      testCaseTags.set(event.getTestCase().getTags());

      Logger scenarioLogger = DynamicRoutingUtil.createLoggerForTest(testName, featureName);
      TestLoggerHolder.setLogger(scenarioLogger);
      TestLoggerHolder.getLogger().info("Logs will go into target/logs/" + featureNameWithoutExtension);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      scenarioLogger.info("▶ Starting: " + uniqueName);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info(LINE_BREAK);

      testReport.createTest(featureName, uniqueName, testCaseTags.get());

      ExtentTest currentTest = TestReport.getCurrentTest();
      if (currentTest != null) {
         currentTest.info("Test node created");
      }

      if (testCaseTags.get().contains("@Web")) {
         DriverManager.getCurrentDriver();
      }

      TestLoggerHolder.getLogger().info("Reset any variables before the test runs");
      XMLDataHandler.resetVariables();
      CucumberDataHelper.resetVariables();
   }

   /**
    * Handles Cucumber event when a scenario finishes.
    *
    * @param event the scenario finish event
    */
   public void onTestCaseFinished(TestCaseFinished event) {
      Status status = event.getResult().getStatus();
      String testName = getTestCaseName();
      String reason = event.getResult().getError() != null ? event.getResult().getError().getMessage() : "";

      switch (status) {
      case PASSED -> testReport.pass(testName, getFeatureName());
      case SKIPPED -> testReport.skip(testName, reason, getFeatureName());
      case FAILED -> {
         boolean isXFail = event.getTestCase().getTags().stream().anyMatch(t -> t.equalsIgnoreCase("@xFail"));
         if (isXFail)
            testReport.xfail(testName, reason, getFeatureName());
         else
            testReport.fail(testName, reason, getFeatureName());
      }
      default -> testReport.skip(testName, "Undefined Step", getFeatureName());
      }

      if (DriverManager.getCurrentDriver() != null && testCaseTags.get().contains("@Web")) {
         DriverManager.closeDriver();
      }

      TestReport.linkLogToReport();
      TestReport.removeTest(testName);
      TestReport.closeThreadLocalCollections();
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info("⏹ Finished: " + testName);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.clear();
   }

   /**
    * Handler for test step start events. Logs step name and keyword.
    */
   public static final EventHandler<TestStepStarted> stepStartedHandler = event -> {
      if (event.getTestStep() instanceof PickleStepTestStep step) {
         setStepKeyword(step.getStep().getKeyword());
         setStepName(step.getStep().getText());
         TestLoggerHolder.getLogger().info("🟢 Step: " + stepKeyword + stepName);
      }
   };

   /**
    * Handler for test step finish events. Logs results and captures screenshots if
    * applicable.
    */
   public static final EventHandler<TestStepFinished> stepFinishedHandler = event -> {
      if (event.getTestStep() instanceof PickleStepTestStep) {
         ExtentTest current = TestReport.getCurrentTest();
         if (current != null)
            current.info("✅ Step Finished: " + getStepKeyword() + getStepName());

         if (event.getResult().getError() != null) {
            TestLoggerHolder.getLogger().error("❌ Error: " + event.getResult().getError());
         }

         if (event.getTestCase().getTags().contains("@Web")) {
            ExtentCucumberAdapter.getCurrentStep().info("📸 Screenshot:");
            addScreenShot();
         }
      }
   };

   /**
    * Handles Cucumber event when the test run has finished.
    *
    * @param event the test run finish event
    */
   public void onTestRunFinished(TestRunFinished event) {
      TestReport.closeThreadLocalCollections();

      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info("= Test Run Finished");
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info("");
      TestLoggerHolder.clear();
   }

   /**
    * Captures and attaches a screenshot with descriptive text.
    *
    * @param text  description to log in the report
    * @param frame optional frame reference (currently unused)
    */
   public static void addScreenshotToReport(String text, String frame) {
      ExtentCucumberAdapter.getCurrentStep().info(text);
      if (!StringUtils.isBlank(frame)) {
         scrollAndShot(); // takes scrolling screenshots for frames
      } else {
         addScreenShot(); // single screen
      }
   }

   private static void scrollAndShot() {
      RemoteWebDriver driver = DriverManager.getCurrentDriver();
      BasePageClass bpc = new BasePageClass();

      bpc.jsWindowScrollToTop();
      int windowHeight = bpc.jsGetWindowHeight();
      int bodyHeight = bpc.jsGetBodyHeight();
      int shot = 0;

      do {
         shot++;
         ExtentCucumberAdapter.getCurrentStep().info(MediaEntityBuilder
               .createScreenCaptureFromBase64String(driver.getScreenshotAs(OutputType.BASE64)).build());
         bpc.jsWindowScrollBy(windowHeight);
      } while (bodyHeight > shot * windowHeight);
   }

   /**
    * This method takes a screenshot. If the browser is firefox then it
    * automatically takes a screen shot of the whole window. For other browsers
    * this is not possible so you have to iterate down large windows taking
    * separate screenshots. Note: This is not the whole of an embedded frame (with
    * a scroll bar) hence the conditional block
    */
   private static void addScreenShot() {
      RemoteWebDriver driver = DriverManager.getCurrentDriver();
      if (driver != null) {
         try {
            if (BaseTestConfiguration.getBrowser().equals(IConstants.FIREFOX)) {
               ExtentCucumberAdapter.getCurrentStep()
                     .info(MediaEntityBuilder
                           .createScreenCaptureFromBase64String(
                                 ((FirefoxDriver) driver).getFullPageScreenshotAs(OutputType.BASE64))
                           .build());
            } else {
               scrollAndShot();
            }
         } catch (Exception e) {
            TestReport.logExceptionMessage(e);
         }
      }
   }

   private void populateExtentEnvInfo() {
      ExtentService.getInstance().setSystemInfo("Browser", BaseTestConfiguration.getBrowser());
      ExtentService.getInstance().setSystemInfo("Headless", BaseTestConfiguration.getHeadless());
      ExtentService.getInstance().setSystemInfo("Env", BaseTestConfiguration.getEnv());
      ExtentService.getInstance().setSystemInfo("Tags", BaseTestConfiguration.getTagValue());
   }

   @Override
   public void setEventPublisher(EventPublisher publisher) {
      publisher.registerHandlerFor(TestRunStarted.class, this::onTestRunStarted);
      publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
      publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
      publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
      publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
      publisher.registerHandlerFor(TestRunFinished.class, this::onTestRunFinished);
   }

   /**
    * Get The Feature Name
    * 
    * @return The Feature Name
    */
   public static String getFeatureName() {
      return featureName;
   }

   /**
    * Sets The Feature Name
    * 
    * @param name The current feature name
    */
   public static void setFeatureName(String name) {
      featureName = name;
      System.setProperty(featureName, "featureName");
   }

   /**
    * Get The Step Keyword
    * 
    * @return The Step Keyword
    */
   public static String getStepKeyword() {
      return stepKeyword;
   }

   /**
    * Sets the keyword of the current step (e.g., Given, When).
    * 
    * @param keyword The keyword to store
    */
   public static void setStepKeyword(String keyword) {
      stepKeyword = keyword;
   }

   /**
    * Gets the current step name
    * 
    * @return The Step Name
    */
   public static String getStepName() {
      return stepName;
   }

   /**
    * Sets the current step name
    * 
    * @param name The Step Name to store
    */
   public static void setStepName(String name) {
      stepName = name;
   }

   /**
    * Gets the current test case name
    * 
    * @return The Test Case Name
    */
   public static String getTestCaseName() {
      return testCaseName;
   }

   /**
    * Sets the current test case name
    * 
    * @param name The Test Case Name to store
    */
   public static void setTestCaseName(String name) {
      testCaseName = name;
   }
}
