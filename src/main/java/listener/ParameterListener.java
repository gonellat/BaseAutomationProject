package listener;

import java.util.Map;

import org.testng.ITestContext;
import org.testng.ITestListener;

import utils.TestLoggerHolder;

/**
 * TestNG Listener that automatically sets system properties for any parameters defined in the TestNG XMl File
 * 
 * <p>
 * This ensures that parameters specified in the TestNG XML configuration are accessible as system properties during test execution
 * </p>
 * Usage:say
 * - Include this class as a listener in the TestNG XML Files
 */
public class ParameterListener implements ITestListener{
   
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

   //* Order precedence **/
   // 1. Maven (-D parameters).  
   //  -- To run through Jenkins use mvn clean tests -Dsurefire.suiteXMLFiles="testng.xml" -Dbrowser etc..
   // 2. TestNG XML Parameters 
   // -- (If you put the other parameters in here then they will be used).
   // 3. run.properties file (defaults if no values supplied)
   // --if none of the those above are supplied then it wil use the properties file or defaults in the code.
   
}
