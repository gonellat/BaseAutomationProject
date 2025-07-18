package utils;

import java.security.SecureRandom;

/**
 * This class contains utility methods for creating/returning ABC specific data
 */
public class DataCreator {

   // The constructor
   private DataCreator() {
      throw new IllegalStateException("Utility class");
   }

   static SecureRandom rand = new SecureRandom();
}
