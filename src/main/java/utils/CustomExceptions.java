package utils;

/**
 * Class for specific custom RuntimeExceptions
 */
@SuppressWarnings("serial")
public class CustomExceptions {

   /**
    * Constructor required for Sonar
    */
   private CustomExceptions() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * This class contains methods for extending RuntimeException for
    * LoggerUtilException's
    */
   public static class LoggerUtilException extends RuntimeException {
      /**
       * Constructs a new LoggerUtilException with the specified detail message and
       * cause.
       * 
       * @param message The detail message
       * @param cause   the cause
       */
      public LoggerUtilException(String message, Throwable cause) {
         super(message, cause);
      }
   }

   /**
    * This class contains methods for extending RuntimeException for URLException's
    */
   public static class URLException extends RuntimeException {

      /**
       * Constructs a new URLException with the specified detail message and cause.
       * 
       * @param message The detail message
       * @param cause   the cause
       */
      public URLException(String message, Throwable cause) {
         super(message, cause);
      }
   }
}
