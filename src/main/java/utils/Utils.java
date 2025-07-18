package utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import constants.DateTimeFormatConstants;

/**
 * Miscellaneous utility methods for the Framework
 */
public class Utils {

   /**
    * Constructor required for Sonar
    */
   private Utils() {
      throw new IllegalStateException("Utility class");
   }

   static final SecureRandom rand = new SecureRandom();

   /**
    * This method creates a new unique string based on todays date with a given
    * format
    * 
    * @param format String DateTime format
    * @return the Current Date Time
    */
   public static String createUniqueStringFromDate(String format) {
      return DateUtils.getCurrentDateTime(format);
   }

   /**
    * This method generate a random number between two integers
    * 
    * @param min The lower integer
    * @param max The higher integer
    * @return The random number
    */
   public static String randomNumber(int min, int max) {
      return String.valueOf(min + rand.nextInt((max - min) + 1));
   }

   /**
    * This method adds a comma to the text passed in if it is not blank
    * 
    * @param text The text to check
    * @return text + a comma (if not blank)
    */
   public static String addCommaIfNotBlank(String text) {
      if (!StringUtils.isBlank(text)) {
         text = text + ", ";
      }
      return text;
   }

   /**
    * This utility method take a string xml document number e.g. "first" and
    * translates it into an integer
    * 
    * @param whichDoc The String version of the xml doc to use
    * @return an integer for the index of which xml document to use
    */
   public static int whichDocStringtoInt(String whichDoc) {
      int whichDocInt;
      if (whichDoc.equalsIgnoreCase("first")) {
         whichDocInt = 0;
      } else if (whichDoc.equalsIgnoreCase("second")) {
         whichDocInt = 1;
      } else {
         throw new NotImplementedException("The xml doc defined by " + whichDoc + " does not exist");
      }
      return whichDocInt;
   }

   /**
    * This method converts elements into a document
    * 
    * @param elements JSoup Elements
    * @return JSoup Document
    */
   public static Document convertElementsIntoDoc(Elements elements) {

      Document newDoc = Document.createShell("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      newDoc.parser(Parser.xmlParser());
      newDoc.body().appendChildren(elements.clone());
      return newDoc;
   }

   /**
    * Find the String between brackets '()' and return it
    * 
    * @param text to scan
    * @return text between brackets
    */
   public static String getTextBetweenBrackets(String text) {
      return text.split("\\(")[1].split("\\)")[0];
   }

   /**
    * Compares two maps and returns a list of differences
    * 
    * @param expectedMap This is the expected data to compare against
    * @param actualMap   This is the actual ui data
    * @return A list of differences (if any)
    */
   public static List<String> compareActualExpectedDataMaps(Map<String, String> expectedMap,
         Map<String, String> actualMap) {
      List<String> differences = new ArrayList<>();

      // Normalise both expected and actual maps for consistent comparison
      Map<String, String> normalisedExpectedMap = normaliseMap(expectedMap);
      Map<String, String> normalisedActualMap = normaliseMap(actualMap);

      // Compare expected keys and values against actual map
      for (Map.Entry<String, String> expectedEntry : normalisedExpectedMap.entrySet()) {
         String key = expectedEntry.getKey();
         String expectedValue = expectedEntry.getValue();

         // Handle NOT PRESENT and NODATA
         if ("NOTPRESENT".equals(expectedValue) || "NODATA".equals(expectedValue)) {
            continue;
         }

         // Check for missing keys in actual map
         if (!normalisedActualMap.containsKey(key)) {
            differences.add("Missing Key In Actual data: " + getOriginalKey(expectedMap, key));
         }

         // Compare expected and actual values
         String actualValue = normalisedActualMap.get(key);

         // Handle special case for date comparison "reporteddate"
         if ("reporteddate".equalsIgnoreCase(key)) {
            // Get the date part from a string
            expectedValue = DateUtils.returnDynamicStringDate(expectedValue, DateTimeFormatConstants.DDMMYYYY);
         }

         if (!(expectedValue.equals(actualValue) || actualValue.matches(expectedValue))) {
            differences.add("Value mismatch for key: [" + getOriginalKey(expectedMap, key) + "] - expected: ["
                  + expectedValue + "], but found [" + actualValue + "]");
         }
      }

      for (String key : normalisedActualMap.keySet()) {
         if (!normalisedExpectedMap.containsKey(key)) {
            differences.add("Unexpected key in actual data: " + getOriginalKey(actualMap, key));
         }
      }

      return differences;
   }

   /**
    * Normalises a map by converting its keys using a utility method
    * 
    * @param map The input map with orginal keys
    * @return A new map with normalised keys
    */
   private static Map<String, String> normaliseMap(Map<String, String> map) {
      Map<String, String> normalised = new HashMap<>();
      for (Map.Entry<String, String> entry : map.entrySet()) {
         String normKey = Utils.normaliseKey(entry.getKey());
         normalised.put(normKey, entry.getValue());
      }
      return normalised;
   }

   /**
    * Retrieves the original key from a map, given a normalised key
    * 
    * @param originalMap The original map
    * @param normKey     The normalised key
    * @return The matching original key if found, else returns the normalised key
    */
   private static String getOriginalKey(Map<String, String> originalMap, String normKey) {
      return originalMap.keySet().stream().filter(k -> Utils.normaliseKey(k).equals(normKey)).findFirst()
            .orElse(normKey);
   }

   /**
    * Method to normalise a key (String) by removing all whitespace and converting
    * to lowercase
    * 
    * @param key The original Key (or String)
    * @return A normalised version of the key (String)
    */
   public static String normaliseKey(String key) {
      if (key == null) {
         return null;
      }
      // Remove all spaces and convert to lowercase
      String normalisedKey = key.replace(" ", "").replace("/", "").replace("(", "").replace(")", "").toLowerCase();
      TestLoggerHolder.getLogger().info(normalisedKey);
      return normalisedKey;
   }
}
