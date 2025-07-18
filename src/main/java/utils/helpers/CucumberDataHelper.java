package utils.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains all additional properties that cannot be stored elsewhere
 * and are used throughout the framework
 */
public class CucumberDataHelper {

   /**
    * Constructor required for Sonar
    */
   public CucumberDataHelper() {
      throw new IllegalStateException("Utility class");
   }

   private static String initialUserLoggedOn;
   private static String updateUserLoggedOn;
   private static String initialChannel;
   private static List<String> loggedInUserOrder = new ArrayList<>();
   private static String outputFilename = "";

   private static Map<String, String> urns = new HashMap<>();

   /**
    * Getter for the initialUserLoggedOn value
    * 
    * @return The intial user who logged on
    */
   public static String getInitialUserLoggedOn() {
      return initialUserLoggedOn;
   }

   /**
    * Setter for the initialUserLoggedOn value
    * 
    * @param initialUserLoggedOn the Original User who logged on
    */
   public static void setInitialUserLoggedOn(String initialUserLoggedOn) {
      CucumberDataHelper.initialUserLoggedOn = initialUserLoggedOn;
   }

   /**
    * Getter for the updateUserLoggedOn value
    * 
    * @return The update user who logged on
    */
   public static String getUpdateUserLoggedOn() {
      return updateUserLoggedOn;
   }

   /**
    * Setter for the updateUserLoggedOn value
    * 
    * @param updateUserLoggedOn the Second User who logged on
    */
   public static void setUpdateUserLoggedOn(String updateUserLoggedOn) {
      CucumberDataHelper.updateUserLoggedOn = updateUserLoggedOn;
   }

   /**
    * Getter for the initialChannel value
    * 
    * @return The channel used to log on
    */
   public static String getInitialChannel() {
      return initialChannel;
   }

   /**
    * Setter for the initialChannel value
    * 
    * @param initialChannel the Original Channel used for log on
    */
   public static void setInitialChannel(String initialChannel) {
      CucumberDataHelper.initialChannel = initialChannel;
   }

   /**
    * This method resets the properties in this class
    */
   public static void resetVariables() {
      setInitialUserLoggedOn("");
      setInitialChannel("");
      resetUrns();
      loggedInUserOrder.clear();
   }

   /**
    * Setter for the logged in user value
    * 
    * @param user list of users
    */
   public static void setLoggedInUserOrder(String user) {
      loggedInUserOrder.add(user);
   }

   /**
    * Getter for the Logged in users value
    * 
    * @return The list of users
    */
   public static List<String> getLoggedInUserOrder() {
      return loggedInUserOrder;
   }

   /**
    * Get the Map of URNs by entity type
    * 
    * @return Map of URNs
    */
   public static Map<String, String> getUrns() {
      return urns;
   }

   /**
    * Assign a new empty URNs Map
    */
   public static void resetUrns() {
      urns = new HashMap<>();
   }

   /**
    * Get the output file name
    * 
    * @return outputFilename
    */
   public static String getOutputFilename() {
      return outputFilename;
   }

   /**
    * Set the output file name
    * 
    * @param outputFileName - The name to set it to
    */
   public static void setOutputFilename(String outputFileName) {
      outputFilename = outputFileName;
   }
}
