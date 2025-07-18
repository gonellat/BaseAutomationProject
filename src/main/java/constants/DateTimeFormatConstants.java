package constants;

/**
 * This class contains all the Date Format Constants
 */
public class DateTimeFormatConstants {

   /**
    * Constructor required for Sonar
    */
   private DateTimeFormatConstants() {
      throw new IllegalStateException("Utility class");
   }

   // Date Time Formats
   /** Date Format yyyy-MM-dd Constant **/
   public static final String YYYYMMDD = "yyyy-MM-dd";
   /** Date Format dd-MMM-yy Constant **/
   public static final String DDMMMYY = "dd-MMM-yy";
   /** Date Format dd-MM-yyyy Constant **/
   public static final String DDMMYYYYDASHES = "dd-MM-yyyy";
   /** Date Format dd-MMM-yyyy Constant **/
   public static final String DDMMMYYYYDASHES = "dd-MMM-yyyy";
   /** Date Format dd/MM/yyyy Constant **/
   public static final String DDMMYYYY = "dd/MM/yyyy";
   /** Date Format dd/MM/yyyy HH:mm Constant **/
   public static final String DDMMYYYYHHMM = "dd/MM/yyyy HH:mm";
   /** Date Format ddMMyyHHmmssSSS Constant **/
   public static final String DDMMYYHHMMSSSSS = "ddMMyyHHmmssSSS";
   /** Date Format yyy-MM-dd-HH-mm-ss Constant **/
   public static final String YYYYMMDDHHMMSS = "yyyy-MM-dd-HH-mm-ss";
   /** Date Format dd-MMM-yy-HH-mm-ss Constant **/
   public static final String DDMMMYYHHMMSS = "dd-MMM-yy HH:mm:ss";
   /** Date Format yyyy-MM-dd'T'HH:mm:ssXXX Constant **/
   public static final String YYYYMMDDTHHMMSSXXX = "yyyy-MM-dd'T'HH:mm:ssXXX";
   /** Date Format EEEE dd/MM/yyyy Constant **/
   public static final String EEEEEDDMMYYYY = "EEEE dd/MM/yyyy HH:mm";
   /** Time Format HH:MM Constant **/
   public static final String HHMM = "HH:mm";

   // Days/Weeks/Months
   /** Days Constant **/
   public static final String DAYS = "Days";
   /** Weeks Constant **/
   public static final String WEEKS = "Weeks";
   /** Months Constant **/
   public static final String MONTHS = "Months";
   /** Today Constant */
   public static final String TODAY = "TODAY";

   /** Default Date Constant */
   public static final String ANGLE_BRACKET_DEFAULT_DATE = "<default";

   /** This is the allowable time in minutes allowed between date times */
   public static final int TIME_DIFF_TOLERANCE = 25;
}
