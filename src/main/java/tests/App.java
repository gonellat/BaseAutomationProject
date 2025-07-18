package tests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a dummy class required for Sonarqube
 */
public class App {
   private static final Logger LOG = LogManager.getLogger(App.class);

   /**
    * Default constructor.
    */
   public App() {
      // no-op
   }

   /**
    * This method is a dummy method just used so that sonar actually scans
    * 
    * @param args This should always be a blank string array
    */
   public static void main(String[] args) {
      LOG.info("Test the application");
   }
}
