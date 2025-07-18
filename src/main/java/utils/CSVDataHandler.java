package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import constants.FilePathConstants;

/**
 * This class contains utility methods for dealing with csv's
 */
public class CSVDataHandler {

   /**
    * Constructor required for Sonar
    */
   private CSVDataHandler() {
      throw new IllegalStateException("Utility class");
   }

   private static Map<String, String> dataMap = new HashMap<>();

   /**
    * Injects a custom map of CSV key-value data into the context.
    *
    * @param dataMap the data map to set
    */
   public static void setDataMap(Map<String, String> dataMap) {
      CSVDataHandler.dataMap = dataMap;
   }

   /**
    * This method stores a csv as a list of maps
    * 
    * @param filePath       - The string for the data file path
    * @param delimiterValue - The string delimiter
    * @throws IOException - IO Exception for reading the csv
    * @return A map of the csv
    */
   public static List<Map<String, String>> readCsvFileToListMaps(String filePath, char delimiterValue)
         throws IOException {
      List<Map<String, String>> valueMap = new ArrayList<>();
      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
         String line;
         String[] headers = null;
         while ((line = br.readLine()) != null) {
            Map<String, String> lineMap = new HashMap<>();
            String[] values = quoteSplit(line, delimiterValue);
            if (headers == null) {
               headers = values;
            } else {
               for (int i = 0; i < headers.length; i++) {
                  if (i < values.length) {
                     lineMap.put(headers[i], values[i]);
                  } else {
                     lineMap.put(headers[i], "");
                  }
               }
               valueMap.add(lineMap);
            }
         }
      }
      return valueMap;
   }

   /**
    * Reads a CSV file and returns a map of key-value pairs based on the file
    * content.
    *
    * @param filePath  the path to the CSV file
    * @param delimiter the delimiter used in the file (e.g., comma, pipe)
    * @return map of parsed key-value pairs from the CSV
    * @throws IOException if the file cannot be read
    */
   public static Map<String, String> readCSVFile(String filePath, String delimiter) throws IOException {

      dataMap = new HashMap<>();

      try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
         String line;
         String[] headers = null;

         while ((line = br.readLine()) != null) {
            String[] values = line.split(delimiter);

            if (headers == null) {
               headers = values;
            } else {
               for (int i = 0; i < headers.length; i++) {
                  if (i < values.length) {
                     dataMap.put(headers[i], values[i]);
                  } else {
                     dataMap.put(headers[i], "");
                  }
               }
            }
         }
      }

      return dataMap;
   }

   /**
    * This method performs a String.split taking quotes into account.
    *
    * @param line           String to split
    * @param delimiterValue Usually ',' for csv
    * @return String array
    */
   private static String[] quoteSplit(String line, char delimiterValue) {
      final char quote = '"';
      List<String> splitList = new ArrayList<>();
      boolean inQuotes = false;
      StringBuilder sb = new StringBuilder();
      for (char x : line.toCharArray()) {
         boolean found = false;
         if (x == delimiterValue && !inQuotes) {
            splitList.add(sb.toString());
            sb.setLength(0);
            found = true;
         }
         if (x == quote) {
            inQuotes = !inQuotes;
            found = true;
         }
         if (found) {
            continue;
         }
         sb.append(x);
      }
      splitList.add(sb.toString());
      return splitList.toArray(new String[0]);
   }

   /**
    * This method takes csv file and returns the value of if in a list of maps
    * 
    * @param file           - The CSV File to read
    * @param area           - The Data file area
    * @param delimiterValue - the csv delimiter
    * @return List of maps the CSV as a list of maps
    * @throws IOException - General Exception
    */
   public static List<Map<String, String>> readCsvIntoListMap(String file, String area, char delimiterValue)
         throws IOException {
      String filePath = FilePathConstants.DATAPATH.concat(area).concat(File.separator).concat(file);
      return readCsvFileToListMaps(filePath, delimiterValue);
   }

   /**
    * This method gets the latest file with a given prefix
    * 
    * @param directory - the directory where the file is located
    * @param prefix    - the prefix of the file
    * @return - the file name
    */
   public static String findLatestCSVFile(String directory, String prefix) {
      File dir = new File(directory);

      if (!dir.exists() || !dir.isDirectory()) {
         TestLoggerHolder.getLogger().info("{} {}", "Downloads directory not found at: ", directory);
         return "";
      }

      File[] matchingFiles = dir.listFiles(file -> file.isFile() && file.getName().toLowerCase().startsWith(prefix)
            && file.getName().toLowerCase().endsWith(".csv"));

      if (matchingFiles == null || matchingFiles.length == 0) {
         return "";
      }

      return Arrays.stream(matchingFiles).max(Comparator.comparingLong(File::lastModified)).map(File::getAbsolutePath)
            .orElse("");

   }

   /**
    * This method reads all the rows from a CSV file using OpenCSV
    * 
    * @param filename - Path to file
    * @return - List of rows, where each row is an array of string values
    * @throws IOException  - If there is an error reading the file
    * @throws CsvException - If there is an error parsing the CSV
    */
   public static List<String[]> readCsv(String filename) throws IOException, CsvException {
      try (CSVReader reader = new CSVReader(new FileReader(filename))) {
         return reader.readAll();
      }
   }

   /**
    * Retrieves the CSV value for a given key.
    *
    * @param key the key to retrieve
    * @return the corresponding CSV value, or null if not found
    */
   public static String getValueForCSVKey(String key) {
      return dataMap.get(key);
   }

}
