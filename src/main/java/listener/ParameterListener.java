package listener;

import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestListener;

import utils.TestLoggerHolder;

/**
 * A TestNG listener to hook into test lifecycle events. Used for debugging or
 * parameter tracking.
 */
public class ParameterListener implements ITestListener {

   /**
    * Constructs a new ParameterListener for TestNG parameter monitoring.
    */
   public ParameterListener() {
      // default constructor
   }

   @Override
   public void onStart(ITestContext context) {
      TestLoggerHolder.getLogger().info("ParameterListener Started");
      Map<String, String> xmlParameters = context.getCurrentXmlTest().getAllParameters();
      for (Map.Entry<String, String> entry : xmlParameters.entrySet()) {
         if (System.getProperty(entry.getKey()) == null) {
            System.setProperty(entry.getKey(), entry.getValue());
         }
      }
   }

   // * Order precedence **/
   // 1. Maven (-D parameters).
   // -- To run through Jenkins use mvn clean tests
   // -Dsurefire.suiteXMLFiles="testng.xml" -Dbrowser etc..
   // 2. TestNG XML Parameters
   // -- (If you put the other parameters in here then they will be used).
   // 3. run.properties file (defaults if no values supplied)
   // --if none of the those above are supplied then it wil use the properties file
   // or defaults in the code.

}
