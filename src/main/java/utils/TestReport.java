package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hamcrest.MatcherAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import constants.IConstants;

/**
 * Central reporting utility to create, manage, and flush Extent test nodes, log
 * messages, capture results in Excel, and link logs into HTML reports.
 */
public class TestReport {

   /**
    * Constructs the TestReport utility class used for test result tracking and
    * reporting.
    */
   public TestReport() {
      // default constructor
   }

   private static ExtentReports extent;
   private static ThreadLocal<ExtentTest> currentTest;
   private static ThreadLocal<ExtentTest> currentTestClass;
   private static Set<String> testCases;
   private static String reportFolder;
   private static final String LINE_BREAK = "========================================";

   /**
    * Retrieves the current test node associated with the current thread
    * 
    * @return The ExtentTest instance for the current thread or null if no test is
    *         active.
    */
   public static ExtentTest getCurrentTest() {
      return currentTest.get();
   }

   /**
    * A Concurrent Hash Map that stores test nodes keyed by their unique test name
    * 
    * This map allows retrieval and removal of test nodes across multiple threads.
    */
   private static ConcurrentHashMap<String, ExtentTest> testNodes = new ConcurrentHashMap<>();

   /**
    * Getter for reportFolder
    * 
    * @return reportFolder
    */
   public static String getReportFolder() {
      return reportFolder;
   }

   /**
    * Setter for reportFolder
    * 
    * @param reportFolder The report folder where the results are placed
    */
   public static void setReportFolder(String reportFolder) {
      TestReport.reportFolder = reportFolder;
   }

   private static final String EXCEL_SUFFIX = ".xlsx";

   /**
    * This method sets up the initialize extent report
    */
   public static void init() {
      extent = new ExtentReports();
      currentTest = new ThreadLocal<>();
      currentTestClass = new ThreadLocal<>();
      testCases = new HashSet<>();

      BaseTestConfiguration.setRunDateTime();

      // This whole method creates the old style Extent Reports and put the xls file
      // inside it.
      String suiteName = System.getProperty("suiteName");
      if (suiteName == null || suiteName.isEmpty()) {
         suiteName = "NonXmlRun";
      }

      createExcelWorkbook();

      TestLoggerHolder.getLogger().info("{} {}", "Extent Report Initialized for suite: ", suiteName);
   }

   /**
    * This message creates an excel worksheet of results
    */
   private static void createExcelWorkbook() {
      // Create a new results xlsx sheet.
      try (XSSFWorkbook wb = new XSSFWorkbook();) {
         Sheet sheet1 = wb.createSheet(IConstants.RESULTS);
         Row header = sheet1.createRow(0);
         header.createCell(0).setCellValue("FeatureName");
         header.createCell(1).setCellValue("TestName");
         header.createCell(2).setCellValue("Status");
         header.createCell(3).setCellValue("Environment");
         header.createCell(4).setCellValue("Browser");
         writeOutResults(wb);
      } catch (RuntimeException rte) {
         // Fail hard with useful info
         throw new RuntimeException(
               "❌ Failed to write Excel report — likely because no Extent report folders exist yet.\n"
                     + "Expected in: " + System.getProperty("user.dir") + "/target/NewStyleReports",
               rte);
      } catch (IOException e) {
         logExceptionMessage(e);
      }
   }

   /**
    * Returns the folder path of the current Extent report.
    *
    * @return the absolute path to the latest report folder
    */
   public static String getLatestExtentReportFolder() {
      String testProjectDir = System.getProperty("user.dir");
      File baseDir = new File(testProjectDir, "target/NewStyleReports");

      if (!baseDir.exists()) {
         boolean created = baseDir.mkdirs();
         if (!created) {
            throw new RuntimeException("❌ Could not create reports folder: " + baseDir.getAbsolutePath());
         }
      }

      File[] dirs = baseDir.listFiles(File::isDirectory);

      if (dirs == null || dirs.length == 0) {
         // ✅ No subfolders yet — return the base dir itself to avoid exception
         return baseDir.getAbsolutePath();
      }

      Arrays.sort(dirs, Comparator.comparing(File::lastModified).reversed());
      return dirs[0].getAbsolutePath();
   }

   /**
    * This method writes out the excel result report to the reports location
    * 
    * @param wb
    */
   private static void writeOutResults(XSSFWorkbook wb) {
      try {
         setReportFolder(getLatestExtentReportFolder());
         String outputPath = getReportFolder() + File.separator + IConstants.RESULTS + EXCEL_SUFFIX;

         try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
            wb.write(fileOut);
         }
      } catch (IOException e) {
         logExceptionMessage(e);
      }
   }

   /**
    * Creates a new test node for the given test case and unique test name,
    * assigning any specific categories.
    * 
    * The test node is started in a concurrent map for future reference and it is
    * also set in a ThreadLocal for backward compatibility.
    * 
    * @param testCase   The identifier for the test case (e,g, test class name or
    *                   feature name)
    * @param testName   The unique test name, typically including a timestamp
    * @param categories A list of categories or tags to assign to the test node.
    */
   public synchronized void createTest(String testCase, String testName, List<String> categories) {
      if (currentTestClass.get() == null || testCases.add(testCase)) {
         ExtentTest test = extent.createTest(testName);
         currentTestClass.set(test);
         if (!categories.isEmpty()) {
            for (String category : categories) {
               test.assignCategory(category);
            }
         }
      }
      ExtentTest node = currentTestClass.get().createNode(testName);

      testNodes.put(testName, node);

      currentTest.set(node);
      extent.flush();
   }

   /**
    * This method is called when the test passes
    * 
    * @param testName    The name of the scenario
    * @param featureName The name of the feature
    */
   public void pass(String testName, String featureName) {
      currentTest.get().pass("PASSED: " + testName);
      currentTest.remove();
      extent.flush();
      addResultToExcel(testName, "PASS", featureName);
   }

   /**
    * This method is called when the test fails and so the test can be skipped
    * 
    * @param testName    The name of the test
    * @param reason      Reason why the test failed
    * @param featureName The name of the feature
    */
   public void xfail(String testName, String reason, String featureName) {
      currentTest.get().assignCategory("XFAIL");
      skip(testName, reason, featureName);
      addResultToExcel(testName, "XFAIL", featureName);
   }

   /**
    * This method is called when the test is skipped
    * 
    * @param testName    The name of the test
    * @param reason      Reason why the test skipped
    * @param featureName The name of the feature
    */
   public void skip(String testName, String reason, String featureName) {
      currentTest.get().skip("SKIPPED: " + testName);
      currentTest.get().skip(reason);
      currentTest.remove();
      extent.flush();
      addResultToExcel(testName, "SKIP", featureName);
   }

   /**
    * This method is called when the test is failed
    * 
    * @param testName    The name of the test
    * @param reason      Reason why the test failed
    * @param featureName The name of the feature
    */
   public void fail(String testName, String reason, String featureName) {
      currentTest.get().fail("FAILED: " + testName);
      currentTest.get().fail(reason);
      currentTest.remove();
      extent.flush();
      addResultToExcel(testName, "FAIL", featureName);
   }

   /**
    * This method logs information into the report
    * 
    * @param message The message to log in the report
    */
   public void log(String message) {
      currentTest.get().log(Status.INFO, message);
      extent.flush();
   }

   /**
    * Clears the ThreadLocal collections used for storing the current test and test
    * class.
    * 
    * This is typically called after a test case finishes to prevent stale
    * references
    */
   public static void closeThreadLocalCollections() {
      currentTest.remove();
      currentTestClass.remove();
   }

   /**
    * This method adds results to the excel worksheet
    * 
    * @param testName
    * @param testStatus
    * @param featureName
    */
   private void addResultToExcel(String testName, String testStatus, String featureName) {
      try (FileInputStream fileInput = new FileInputStream(
            getReportFolder().concat(File.separator).concat(IConstants.RESULTS + EXCEL_SUFFIX));
            XSSFWorkbook workbook = new XSSFWorkbook(fileInput)) {
         XSSFSheet resultSheet = workbook.getSheet(IConstants.RESULTS);
         Row resultRow = resultSheet.createRow(resultSheet.getLastRowNum() + 1);
         resultRow.createCell(0).setCellValue(featureName);
         resultRow.createCell(1).setCellValue(testName);
         resultRow.createCell(2).setCellValue(testStatus);
         resultRow.createCell(3).setCellValue(BaseTestConfiguration.getEnv());
         resultRow.createCell(4).setCellValue(BaseTestConfiguration.getBrowser());

         FileOutputStream fileOutput = new FileOutputStream(
               getReportFolder().concat(File.separator).concat(IConstants.RESULTS + EXCEL_SUFFIX));
         workbook.write(fileOutput);
      } catch (Exception exception) {
         TestLoggerHolder.getLogger().info("Error occured while reading the excel file");
         logExceptionMessage(exception);
      }
   }

   /**
    * This method will log a XML message split into two parts, the header and
    * separately the message body. The XML message body is logged in "pretty
    * format".
    * <p>
    * <strong>NB: </strong> This is for the Extent Reports generated by the
    * 'extentreports-cucumber6-adapter' plug-in.
    * 
    * @param messageHeader The message header of the XML
    * @param messageBody   The message body of the XML
    */
   public static void extentReportLogInfoXMLMessage(String messageHeader, String messageBody) {
      ExtentCucumberAdapter.getCurrentStep().info(LINE_BREAK);
      ExtentCucumberAdapter.getCurrentStep().info(messageHeader);
      ExtentCucumberAdapter.getCurrentStep().info(MarkupHelper.createCodeBlock(messageBody, CodeLanguage.XML));
   }

   /**
    * This method will log a basic "Fail" (i.e. Error) message in the current test
    * step.
    * <p>
    * <strong>NB: </strong> This is for the Extent Reports generated by the
    * 'extentreports-cucumber6-adapter' plug-in.
    * 
    * @param message The message to enter
    */
   public static void extentReportLogFailureMessage(String message) {
      ExtentCucumberAdapter.getCurrentStep().fail(LINE_BREAK);
      ExtentCucumberAdapter.getCurrentStep().fail(message);
   }

   /**
    * This method will log a basic "Warning" message in the current test step.
    * <p>
    * <strong>NB: </strong> This is for the Extent Reports generated by the
    * 'extentreports-cucumber6-adapter' plug-in.
    * 
    * @param message Output the message in the report
    */
   public static void extentReportLogWarningMessage(String message) {
      ExtentCucumberAdapter.getCurrentStep().warning(LINE_BREAK);
      ExtentCucumberAdapter.getCurrentStep().warning(message);
   }

   /**
    * This method will log a basic "Info" message in the current test step.
    * <p>
    * <strong>NB: </strong> This is for the Extent Reports generated by the
    * 'extentreports-cucumber6-adapter' plug-in.
    * 
    * @param message The message to enter
    */

   public static void extentReportLogInfoMessage(String message) {
      ExtentCucumberAdapter.getCurrentStep().info(LINE_BREAK);
      ExtentCucumberAdapter.getCurrentStep().info(MarkupHelper.createCodeBlock(message, CodeLanguage.XML));
   }

   /**
    * This method logs the current exception information in multiple places
    * 
    * @param e The current exception to log the information from
    */
   public static void logExceptionMessage(Exception e) {
      TestLoggerHolder.getLogger().error("An exception has occurred: " + e.toString());
      MatcherAssert.assertThat("An exception has occurred: " + e.toString(), false);
   }

   /**
    * Retrieves the test node associated with the given unique test name
    * 
    * @param testName the unique test name
    * @return the ExtentTestNode, or null if no such test exists
    */
   public static ExtentTest getTest(String testName) {
      return testNodes.get(testName);
   }

   /**
    * Removes the test node associated with the given unique test name from the
    * internal collection
    * 
    * @param testName The unique test name
    */
   public static void removeTest(String testName) {
      testNodes.remove(testName);
   }

   /**
    * Adds a log file link to the current ExtentTest report. Shows a direct link
    * locally, and a static message in GitHub Actions.
    */
   public static void linkLogToReport() {
      String rawPath = TestLoggerHolder.getLogFilePath();
      String htmlLink = rawPath.replace("\\", "/");
      // For Spark HTML, make the link relative to ExtentSpark.html
      String relativeToSpark = "../../../../../" + htmlLink.replaceFirst("^target/", "");

      if (rawPath != null && !rawPath.isEmpty()) {
         if (System.getenv().containsKey("GITHUB_ACTIONS")) {
            ExtentCucumberAdapter.getCurrentStep()
                  .info("View logs in GitHub Actions: <strong>all-test-artifacts.zip → logs/</strong>");
         } else {
            ExtentCucumberAdapter.getCurrentStep().info("<span style='font-weight:bold;'>Log File:</span> <a href='"
                  + relativeToSpark + "' target='_blank'>Open</a>");
            extent.flush(); // ensure it's written to the file immediately
         }
      }
   }
}