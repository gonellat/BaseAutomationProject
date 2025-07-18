package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.NotImplementedException;
import constants.DateTimeFormatConstants;

/**
 * This class contains various ways to manipulate date/times.<br>
 * Please note to help ensure any date/time manipulations don't fall foul of any
 * clock change boundaries, i.e. switching to BST, all date/time manipulations
 * need to be done using ZonedDateTime objects. And any storing of date/times in
 * the DB should be done with OffsetDateTime to ensure they retain a constant
 * value over time, see
 * https://springframework.guru/convert-offsetdatetime-to-zoneddatetime/ for one
 * explanation why.
 */
public class DateUtils {

   /**
    * Constructor required for Sonar
    */
   private DateUtils() {
      throw new IllegalStateException("Utility class");
   }

   private static final String EUROPE_LONDON = "Europe/London";

   private static final String MONTH = "MONTH";
   private static final String YEAR = "YEAR";
   private static final String DAY = "DAY";
   private static final String INVALID_DATE_PROVIDED = "Invalid date provided";

   /**
    * This method gets today's current dateTime +/- as a ZonedDateTime object.<br>
    * <strong>NB: </strong>The time zone is currently hard-coded to
    * "Europe/London".<br>
    * 
    * @return The current dateTime +/- as a ZonedDateTime object
    */
   private static ZonedDateTime getCurrentZonedDateTime() {
      LocalDateTime currentDateTime = LocalDateTime.now();
      ZoneId zoneId = ZoneId.of(EUROPE_LONDON);
      ZonedDateTime zonedDateTime = currentDateTime.atZone(zoneId);

      TestLoggerHolder.getLogger()
            .info(String.format(String.format(
                  "The current date / time value in the generated ZonedDateTime object is = '%1$s'.",
                  zonedDateTime.toString())));

      return zonedDateTime;
   }

   /**
    * This method gets today's dateTime and returns the string in the format
    * requested.
    * 
    * @param dateTimeFormat The dateTime formatting string
    * @return The current dateTime
    */
   public static String getCurrentDateTime(String dateTimeFormat) {
      DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern(dateTimeFormat, Locale.UK);
      String formattedCurrentDateTime = getCurrentZonedDateTime().format(dateTimeformatter);

      TestLoggerHolder.getLogger().info(String.format(
            "The current date / time generated using the specified format is = '%1$s'.", formattedCurrentDateTime));

      return formattedCurrentDateTime;
   }

   /**
    * This method returns a date +/- years
    * 
    * @param dateTime       This is the date(time) to be modified
    * @param dataTimeFormat - This is the required date time format
    * @param years          This is the number of years to add or remove from the
    *                       date
    * @return date today +/- years
    */
   public static String getDatePlusOrMinusYears(ZonedDateTime dateTime, String dataTimeFormat, int years) {
      ZonedDateTime modifiedZonedDateTime = dateTime.plus(Period.ofYears(years));

      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dataTimeFormat, Locale.UK);
      String formattedModifiedZonedDateTime = modifiedZonedDateTime.format(dateTimeFormatter);

      TestLoggerHolder.getLogger().info(
            "After adjusting by the specified number of months {} the adjusted dateTime is = {}.", years,
            formattedModifiedZonedDateTime);

      return formattedModifiedZonedDateTime;
   }

   /**
    * This method returns date +/- months
    * 
    * @param dateTime       This is the date(time) to be modified
    * @param dataTimeFormat - This is the required date time format
    * @param months         This is the number of months to add or remove from the
    *                       date
    * @return date today +/- months
    */
   public static String getDatePlusOrMinusMonths(ZonedDateTime dateTime, String dataTimeFormat, int months) {
      ZonedDateTime modifiedZonedDateTime = dateTime.plus(Period.ofMonths(months));

      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dataTimeFormat, Locale.UK);
      String formattedModifiedZonedDateTime = modifiedZonedDateTime.format(dateTimeFormatter);

      TestLoggerHolder.getLogger().info(
            "After adjusting by the specified number of months {} the adjusted dateTime is = {}.", months,
            formattedModifiedZonedDateTime);

      return formattedModifiedZonedDateTime;
   }

   /**
    * This method returns date +/- days
    * 
    * @param dateTime       This is the date(time) to be modified
    * @param dataTimeFormat - This is the required date time format
    * @param days           This is the number of days to add or remove from the
    *                       date
    * @return date today +/- days
    */
   public static String getDatePlusOrMinusDays(ZonedDateTime dateTime, String dataTimeFormat, int days) {
      ZonedDateTime modifiedZonedDateTime = dateTime.plus(Period.ofDays(days));

      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dataTimeFormat, Locale.UK);
      String formattedModifiedZonedDateTime = modifiedZonedDateTime.format(dateTimeFormatter);

      TestLoggerHolder.getLogger().info(
            "After adjusting by the specified number of days '%1$s' the adjusted dateTime is = '%2$s'.", days,
            formattedModifiedZonedDateTime);

      return formattedModifiedZonedDateTime;
   }

   /**
    * This method calls a method to add or subtracts months or days from a string
    * date or converts the string date to the provided format returning a string
    * value of the date
    * 
    * @param dateToModify - The date that needs modifying e.g. TODAY+1Day or
    *                     12/04/2024-2Years or TODAY
    * @param format       - the datetime format eg. dd/mm/yyyy
    * @return date in the format requested +/- days/months/years
    */
   public static String adjustDate(String dateToModify, String format) {
      String modifiedZonedDateTimeString = null;

      dateToModify = dateToModify.toUpperCase();
      TestLoggerHolder.getLogger().info("The original date to modify is:" + dateToModify);

      // If the string contains + or - add or subtract the right days/months or years
      // otherwise it is just "TODAY" or a given
      // date
      if (dateToModify.contains("+") || dateToModify.contains("-")) {
         modifiedZonedDateTimeString = addOrRemoveYearsMonthsDays(dateToModify, format);
      } else {
         if (dateToModify.toUpperCase().contains(DateTimeFormatConstants.TODAY)) {
            modifiedZonedDateTimeString = getCurrentDateTime(format);
         } else {
            modifiedZonedDateTimeString = dateToModify;
         }
      }
      return modifiedZonedDateTimeString;
   }

   /**
    * This method takes a todays date and either adds or subtracts months or days
    * from it returning a string value of the date
    * 
    * @param dateToModify - The date that needs modifying e.g. TODAY+1Day or
    *                     12/04/2024-2Years or TODAY
    * @param format       - the datetime format eg. dd/mm/yyyy
    * @return date in the format requested +/- days/months/years
    * @throws ParseException Exception if the dateToModify is in the incorrect
    *                        format
    */
   private static String addOrRemoveYearsMonthsDays(String dateToModify, String format) {

      if (dateToModify == null || dateToModify.isEmpty()) {
         throw new IllegalArgumentException(INVALID_DATE_PROVIDED);
      }

      ZonedDateTime modifiedZonedDateTime = parseInitialDate(dateToModify);
      String modifyByAmount = extractModifyAmount(dateToModify);

      return modifyDate(modifiedZonedDateTime, modifyByAmount, format);

   }

   /**
    * Parses the input data and handles TODAY replacements
    * 
    * @param dateToModify The input date String
    * @return The parsed ZonedDateTime
    */
   private static ZonedDateTime parseInitialDate(String dateToModify) {
      if (dateToModify.contains(DateTimeFormatConstants.TODAY)) {
         return getCurrentZonedDateTime();
      }

      String newDateToModify = extractBaseDate(dateToModify);
      return parseDateWithTimeZone(newDateToModify);
   }

   /**
    * Extracts the base date from the input, handling different formats
    * 
    * @param dateToModify The original date String
    * @return The base date without modification
    */
   private static String extractBaseDate(String dateToModify) {
      if (dateToModify == null || dateToModify.isEmpty()) {
         throw new IllegalArgumentException(INVALID_DATE_PROVIDED);
      }

      // if the dateToModify contains TODAY, replace it with todays date
      if (dateToModify.contains(DateTimeFormatConstants.TODAY)) {
         dateToModify = dateToModify.replace(DateTimeFormatConstants.TODAY,
               getCurrentDateTime(DateTimeFormatConstants.DDMMYYYYDASHES));
      }

      // Normalize case by converting everything to lowercase
      String normalizedDate = dateToModify.toLowerCase();

      if (normalizedDate.contains(DateTimeFormatConstants.TODAY.toLowerCase())) {
         normalizedDate = normalizedDate.replace(DateTimeFormatConstants.TODAY.toLowerCase(),
               getCurrentDateTime(DateTimeFormatConstants.DDMMYYYYDASHES));
      }

      // Remove modification parts (e.g. +1Month)
      normalizedDate = normalizedDate.replaceAll("([+-]\\d+(years?|months?|day)s?)", "").trim();

      // Handle case where date includes a time component
      if (normalizedDate.contains("T")) {
         normalizedDate = normalizedDate.split("T")[0];
      } else if (normalizedDate.contains(" ")) {
         normalizedDate = normalizedDate.split(" ")[0];
      }

      // Ensure we only keep a valid date format (YYYY-MM-DD or DD/MM/YYYY)
      if (normalizedDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
         return normalizedDate;
      } else if (normalizedDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
         return normalizedDate;
      } else if (normalizedDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
         return normalizedDate;
      } else {
         throw new IllegalArgumentException("Invalid date format:" + dateToModify);
      }
   }

   /**
    * Parses the base date (without modifications) into a ZonedDateTime object
    * Ensures a proper time component (T00:00:00Z) if missing
    * 
    * @param dateToModify The date string with possible modifications (e.g.
    *                     13/02/2024 + 1 year)
    * @return A ZonedDateTime object representing the parsed date
    */
   private static ZonedDateTime parseDateWithTimeZone(String dateToModify) {
      if (dateToModify == null || (dateToModify.isEmpty())) {
         throw new IllegalArgumentException(INVALID_DATE_PROVIDED);
      }

      // Extract base date (removes modifications like "+1Month"
      String baseDate = extractBaseDate(dateToModify);
      TestLoggerHolder.getLogger().info("Extracted Base Date for parsing: " + baseDate);

      // Append default time if not present
      String dateTimeWithZone = baseDate + "T00:00:00Z";
      TestLoggerHolder.getLogger().info("Formatted Date with Time: " + dateTimeWithZone);

      // Identify format and create appropriate formatter
      DateTimeFormatter formatter;
      if (baseDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
         formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
      } else if (baseDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
         formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ssX");
      } else if (baseDate.matches("\\d{2}-\\d{2}-\\d{4}")) {
         formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ssX");
      } else {
         throw new IllegalArgumentException("Unsupported date format:" + baseDate);
      }

      return ZonedDateTime.parse(dateTimeWithZone, formatter);
   }

   /**
    * Extracts the modification amount from the data string
    * 
    * @param dateToModify The date string containing modification details
    * @return The extracted modification amount
    */
   private static String extractModifyAmount(String dateToModify) {
      String modifyAmount = dateToModify.replace(DateTimeFormatConstants.TODAY, "")
            .replace(extractBaseDate(dateToModify), "").trim();
      TestLoggerHolder.getLogger().info("modifyAmount:" + modifyAmount);
      return modifyAmount;
   }

   /**
    * Modifies the date based on the extracted modification amount
    * 
    * @param date           The original ZonedDateTime
    * @param modifyByAmount The modification (e.g. +1Year, -2Months)
    * @param format         The expected output format
    * @return The modified date in the the specified format
    */
   private static String modifyDate(ZonedDateTime date, String modifyByAmount, String format) {
      if (modifyByAmount.isEmpty())
         return formatDate(date, format);

      int amount;
      if (modifyByAmount.toUpperCase().contains(YEAR)) {
         amount = parseModificationValue(modifyByAmount, YEAR);
         date = date.plusYears(amount);
      } else if (modifyByAmount.toUpperCase().contains(MONTH)) {
         amount = parseModificationValue(modifyByAmount, MONTH);
         date = date.plusMonths(amount);
      } else if (modifyByAmount.toUpperCase().contains(DAY)) {
         amount = parseModificationValue(modifyByAmount, DAY);
         date = date.plusDays(amount);
      } else {
         throw new NotImplementedException("Modification type not supported: " + modifyByAmount);
      }
      return formatDate(date, format);
   }

   /**
    * Parses the numeric modification value
    * 
    * @param modifyByAmount The modification string e.g. YEAR+1
    * @param type           The modification type (YEAR, MONTH, DAY)
    * @return The parsed integer value
    */
   private static int parseModificationValue(String modifyByAmount, String type) {
      // Normalize input to lowercase for case insensitive handling
      String normalized = modifyByAmount.toLowerCase();

      TestLoggerHolder.getLogger().info("Parsing modification value from: " + normalized + " for type: " + type);

      // ensure type is also handled case-insensitively
      String typeLower = type.toLowerCase();

      // Use regex to extract numeric value before the modification type
      Pattern pattern = Pattern.compile("([+-]?\\d+)\\s*" + typeLower + "s?");
      Matcher matcher = pattern.matcher(normalized);

      if (matcher.find()) {
         int value = Integer.parseInt(matcher.group(1)); // Extract and convert to integer
         TestLoggerHolder.getLogger().info("Parsed value:" + value + " for type:" + type);
         return value;
      } else {
         TestLoggerHolder.getLogger()
               .warn("No valid modification value found for type: " + type + " in string: " + modifyByAmount);
         throw new IllegalArgumentException("Invalid modification format:" + modifyByAmount);
      }
   }

   /**
    * Formats the ZonedDateTime to the specified format
    * 
    * @param date   The ZonedDateTime object
    * @param format the output format
    * @return the formatted date string
    */
   private static String formatDate(ZonedDateTime date, String format) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.UK);
      String returnDateFormat = date.format(formatter);
      TestLoggerHolder.getLogger().info(returnDateFormat);
      return returnDateFormat;
   }

   /**
    * This method calculates whether 2 dates times are with 'tolerance' minutes of
    * each other
    * 
    * @param tolerance        - the allows time difference in minutes allowed
    * @param pattern          - the format of the date time strings
    * @param expectedDateTime - The expected DateTime
    * @param actualDateTime   - the actual dateTime
    * @return boolean if the datetime is within the allowable limit
    */
   public static boolean isStringDateTimeWithinMinutesOfAnotherStringDateTime(int tolerance, String pattern,
         String expectedDateTime, String actualDateTime) {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC);
      ZonedDateTime expected = ZonedDateTime.parse(expectedDateTime, formatter);
      ZonedDateTime actual = ZonedDateTime.parse(actualDateTime, formatter);

      long diff = ChronoUnit.MINUTES.between(actual, expected);
      TestLoggerHolder.getLogger().info("Time diff" + diff);
      boolean rtnValue = diff <= tolerance;
      TestLoggerHolder.getLogger().info("rtnValue= " + rtnValue);
      return (rtnValue);
   }

   /**
    * This method changes the format of a string date to a different format
    * 
    * @param date              The original date to change
    * @param dateTimeFormat    The old date time format
    * @param newDateTimeFormat The new date time format
    * @return The string date in a the new format
    * @throws ParseException ParseException if date cannot be parsed
    */
   public static String convertDateStringToDifferentFormat(String date, String dateTimeFormat,
         String newDateTimeFormat) throws ParseException {
      SimpleDateFormat format1 = new SimpleDateFormat(dateTimeFormat);
      SimpleDateFormat format2 = new SimpleDateFormat(newDateTimeFormat);
      Date dateConverted = format1.parse(date);
      return format2.format(dateConverted);
   }

   /**
    * This method verifies if a datetime is in the correct format
    * 
    * @param date           - The string date to check
    * @param dataTimeFormat - The format that the date should be
    * @return true or false if it is the correct format
    */
   public static boolean verifyStringMatchesFormat(String date, String dataTimeFormat) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dataTimeFormat, Locale.UK);
      try {
         dateTimeFormatter.parse(date);
      } catch (Exception e) {
         return false;
      }
      return true;
   }

   /**
    * This method gets the current year
    * 
    * @return current year in the format yyyy
    */
   public static String getCurrentYear() {
      LocalDate currentDate = LocalDate.now();
      String currentYear = currentDate.format(DateTimeFormatter.ofPattern("yyyy"));
      TestLoggerHolder.getLogger().info("currentYear:" + currentYear);
      return currentYear;
   }

   /**
    * This method gets the current time
    * 
    * @return current time in the format HH:mm
    */
   public static String getCurrentTime() {
      String currentDateTime = DateUtils.adjustDate(DateTimeFormatConstants.TODAY,
            DateTimeFormatConstants.DDMMYYYYHHMM);
      String currentTime = currentDateTime.substring(currentDateTime.indexOf(" ") + 1);
      TestLoggerHolder.getLogger().info("currentTime:" + currentTime);
      return currentTime;
   }

   /**
    * Removes the time portion from a datetime string formatted as dd/MM/yyyy
    * HH;mm:ss
    * 
    * @param dateTime The full date time
    * @return The date without the time
    */
   public static String stripTimeFromDate(String dateTime) {
      if (dateTime == null || !dateTime.contains(" ")) {
         return dateTime; // Return as if no time present
      }
      return dateTime.split(" ")[0]; // Keep only the date portion
   }

   /**
    * This method either 1) Finds the string between # symbols then calls
    * adjustDate to find the right date. Returns the full string with and actual
    * date 2) If no # symbols are found just returns the adjusted date
    * 
    * @param input      The original String e.g. The Report Date is #TODAY-2Months#
    *                   12:00" or TODAY
    * @param dateFormat The expected output date format
    * @return The updated string with the date expression replaced by its formatted
    *         value.
    */
   public static String returnDynamicStringDate(String input, String dateFormat) {
      Pattern pattern = Pattern.compile("#([^#]+)#");
      Matcher matcher = pattern.matcher(input);

      if (matcher.find()) {
         // Extract substring without '#' characters
         String dateExpression = matcher.group(1);
         // Convert the expression into a formatted date string
         String formattedDate = DateUtils.adjustDate(dateExpression, dateFormat);
         // Replace the first occurrence of the the pattern with the formatted date
         return matcher.replaceFirst(formattedDate);
      } else {
         return DateUtils.adjustDate(input, dateFormat);
      }
   }

   /**
    * This method adjusts a UI date string by subtracting one day if the given date
    * falls under British Summer Time (BST)
    * 
    * @param uiDateString The date string from the UI (expected in the format
    *                     'dd/MM/yyyy'
    * @return A date string in 'dd/MM/yyyy', adjusted to match the database format
    *         which is GMT
    * @throws IllegalArgumentException if the input date string is null, empty or
    *                                  not in the expected format
    */
   public static String adjustUIDateForBST(String uiDateString) throws IllegalArgumentException {

      if (uiDateString == null || uiDateString.isEmpty()) {
         throw new IllegalArgumentException("uiDateString cannot be null or empty");
      }

      try {
         // Parse incoming string to LocalDate
         LocalDate uiDate = LocalDate.parse(uiDateString,
               DateTimeFormatter.ofPattern(DateTimeFormatConstants.DDMMYYYY));

         // Determine the timezone offset for the UI date in London Timezone
         ZoneId londonZone = ZoneId.of(EUROPE_LONDON);
         ZonedDateTime zonedDate = uiDate.atStartOfDay(londonZone);
         ZoneOffset offset = zonedDate.getOffset();

         if (offset.equals(ZoneOffset.ofHours(1))) {
            return uiDate.minusDays(1).format(DateTimeFormatter.ofPattern(DateTimeFormatConstants.DDMMYYYY));
         } else {
            return uiDate.format(DateTimeFormatter.ofPattern(DateTimeFormatConstants.DDMMYYYY));
         }
      } catch (DateTimeParseException e) {
         throw new IllegalArgumentException("Invalid UI date Format. Expected dd/MM/yyyy, got: " + uiDateString, e);
      }
   }
}
