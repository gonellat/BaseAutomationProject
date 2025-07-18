package utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import constants.IConstants;
import listener.ListenerPlugin;
import pages.BasePageClass;

/**
 * This is a utility class to get string and element values from a table. The
 * table in question may either be 1) A header formatted table - i.e. the first
 * row is a header row and each subsequent row is data for those columns 2) A
 * label formatted table - i.e. the first column is the 'label' and then the
 * next column has the data Using the constructor you can get the data (text and
 * elements) for both types of table. This can either be as a list or a map. You
 * can also specify if you want all page data or just the current page of data,
 * or if you just want a row of data on a specific page
 */
public class WebTable extends BasePageClass {

   // The Table Element and Locators
   private WebElement wbTable;
   private String strTableLocator;
   private static final String PAGINATOR = "css::.js-pagination";
   private static final String PAGINATION_PAGE_SIZE = "css::.pagination-pageSize a";
   private static final String RELOAD_LIST = "css::.md-ink-ripple[value='Reload List']";
   private static final String SELECT_PAGE = "css::.pagination-page-select";

   // All Pages Table Strings and Elements
   private List<List<String>> tableData = new ArrayList<>();
   private List<List<WebElement>> tableElementData = new ArrayList<>();

   // Current Page table Strings and Elements
   List<List<String>> values;
   List<List<WebElement>> elementValues;

   // Maps for the string data
   private Map<Integer, Map<String, String>> headerTableMap = new HashMap<>();
   private Map<String, String> labelTableMap = new HashMap<>();

   // Getters / Setters
   /**
    * Getter for headerTableMap
    * 
    * @return headerTableMap
    */
   public Map<Integer, Map<String, String>> getHeaderTableMap() {
      return headerTableMap;
   }

   /**
    * Getter for labelTableMap
    * 
    * @return labelTableMap
    */
   public Map<String, String> getLabelTableMap() {
      return labelTableMap;
   }

   /**
    * Getter for tableData
    * 
    * @return tableData
    */
   public List<List<String>> getTableData() {
      return tableData;
   }

   /**
    * Getter for tableElementData
    * 
    * @return tableElementData
    */
   public List<List<WebElement>> getTableElementData() {
      return tableElementData;
   }

   /**
    * This constructor is used for getting the Table data
    * 
    * @param strTableLocator This is the string type and locator separated by ::
    *                        used to find the table element
    * @param pages           "All" or a string page number e.g. "1" or N/A
    * @param row             "All" or a string row number e.g. "1"
    */
   public WebTable(String strTableLocator, String pages, String row) {
      this.strTableLocator = strTableLocator;
      getTableDataListList(pages, row);
   }

   /**
    * This constructor is use for pagination method only
    */
   public WebTable() {
      // Default Constructor
   }

   /**
    * This method waits for the supplied table to exist. It calls
    * readDataRowsOnScreenListList grabbing the text for each row required into a
    * List/List//String
    * 
    * @param pages - String representation of the pages required e.g. "0" or "All"
    *              or "N/A" (use N/A when you know there won't be page next/number
    *              elements)
    * @param row   - A string representation of the rows required e.g. "0" or "All"
    */
   public void getTableDataListList(String pages, String row) {

      /*
       * Some pages have a reload list button, wait for this to be clickable before
       * continuing to avoid a stale element exception
       */
      setWait(new WebDriverWait(getDriver(), Duration.ofMillis(500)));
      if (isElementExists(getWait(), RELOAD_LIST)) {
         waitForElementToBeClickable(RELOAD_LIST);
      }

      wbTable = waitForVisibilityOfElementLocatedBy(strTableLocator);

      // Get the number of pages
      if (pages.equalsIgnoreCase(IConstants.ALL)) {
         // NOTE:: Although this method can read in multiple pages into tableElementData
         // if you try to interact with any of them
         // it will fail with a stale element as you have navigated away from and then
         // back to the first page.
         // You must therefore get the table again with pages = 2, etc.. in a loop.
         // ALL will only work with tableData as this is just strings not elements.
         ListenerPlugin.addScreenshotToReport("Intial Table View", "");
         // While the page is full...
         setWait(new WebDriverWait(getDriver(), Duration.ofMillis(250)));

         if (isElementExists(getWait(), SELECT_PAGE)) {
            getAllPageData(row);
         } else {
            readPageData(row);
         }
      } else {
         if (!pages.equalsIgnoreCase(IConstants.NA)) {
            // Select the page you want
            selectByVisibleText(PAGINATOR, pages);
         }
         readPageData(row);
      }
   }

   /**
    * This method iterates and gets all data from all the pages.
    * 
    * @param row - A string representation of the rows required e.g. "0" or "All"
    */
   private void getAllPageData(String row) {
      List<String> pageOptions = getSelectList(SELECT_PAGE);
      TestLoggerHolder.getLogger().info("pages:" + pageOptions);
      for (int x = 0; x < pageOptions.size(); x++) {
         // Wait for the table to be present
         waitForElementToBeClickable(SELECT_PAGE);
         wbTable = waitForVisibilityOfElementLocatedBy(strTableLocator);

         TestLoggerHolder.getLogger().info("Reading page " + x);
         // Get the table row count
         int currentPageRowCount = wbTable.findElements(By.tagName(IConstants.TR)).size();
         TestLoggerHolder.getLogger().info("Current Page RowCount=" + currentPageRowCount);
         readPageData(row);
         if (x + 1 != pageOptions.size()) {
            selectByVisibleText(SELECT_PAGE, pageOptions.get(x + 1));
         }
      }
      // Go back to the first page
      if (pageOptions.size() > 1) {
         selectByVisibleText(SELECT_PAGE, "1");
      }
   }

   /**
    * This method calls other methods to read in the page/or row data
    * 
    * @param row - This is the row to read
    */
   public void readPageData(String row) {
      values = new ArrayList<>();
      elementValues = new ArrayList<>();
      readDataRowsOnScreenListList(row);
      // Add the values to the all pages arrays
      tableData.addAll(values);
      tableElementData.addAll(elementValues);
   }

   /**
    * This method reads text from all rows in displayed table on current web page
    * 
    * @param row string number to be converted to int if a specific row is required
    */
   private void readDataRowsOnScreenListList(String row) {

      // Get the rows
      List<WebElement> rows = wbTable.findElements(By.tagName(IConstants.TR));
      if (rows.isEmpty()) {
         throw new InvalidOperationException("There are no rows in the table " + strTableLocator);
      }

      // All rows
      if (row.equals(IConstants.ALL)) {
         // Iterate the rows..
         for (int x = 0; x < rows.size(); x++) {
            getRowCellData(rows.get(x), x);
         }
      } else {
         // Single row only
         int x = Integer.parseInt(row);
         getRowCellData(rows.get(x), x);
      }
   }

   /**
    * This method gets the cell data for each cell in the row
    * 
    * @param row       WebElement of the row
    * @param rowNumber used in the exception
    */
   private void getRowCellData(WebElement row, int rowNumber) {
      List<String> rowList = new ArrayList<>();
      List<WebElement> rowElementList = new ArrayList<>();

      // Get the cells in the row
      List<WebElement> columns = row.findElements(By.tagName(IConstants.TD));
      List<WebElement> headers = row.findElements(By.tagName(IConstants.TH));
      // If no cells then throw an error
      if (columns.isEmpty() && headers.isEmpty()) {
         throw new InvalidOperationException(
               "There are no table cells in the table " + strTableLocator + " row: " + rowNumber);
      } else if (columns.size() == 1) {
         // if there is only one cell data for the row then clear it as this means there
         // is no data
         // it has a text like "No User Trackers Found"
         TestLoggerHolder.getLogger().info("The table row only has one column");
      }

      // Iterate the cells
      for (int j = 0; j < columns.size(); j++) {
         WebElement column = columns.get(j);

         // Store the element in a list
         rowElementList.add(column);

         if (!headers.isEmpty()) {
            WebElement header = headers.get(j);
            rowList.add(header.getText().trim().replace("\n", " "));
         }
         // Store the text in a list
         rowList.add(column.getText().trim().replace("\n", " "));
      }

      // Add the row data to a current page list
      if (!rowList.isEmpty()) {
         values.add(rowList);
      }
      if (!rowElementList.isEmpty()) {
         elementValues.add(rowElementList);
      }
   }

   /**
    * This method converts a List/List/String to a hashmap This is used for label
    * format table i.e. has 2 columns
    */
   public void convertLabelTableFormatToMap() {
      // Iterate through the list
      for (int x = 0; x < tableData.size(); x++) {
         if (tableData.get(x).size() > 1) {
            labelTableMap.put(tableData.get(x).get(0), tableData.get(x).get(1));
         }
      }
   }

   /**
    * This method converts a List/List/String to a hashmap This is used for label
    * format table i.e. has 2 columns. Stops adding items at the first key, whereas
    * convertLabelTableFormatToMap() will get the last instance of the key. For
    * instance if there are three 'URN' keys in the table, this method will add the
    * first, not the last.
    */
   public void convertLabelTableFormatToMapUsingFirst() {
      // Iterate through the list
      for (int x = 0; x < tableData.size(); x++) {
         if (tableData.get(x).size() > 1) {
            String key = tableData.get(x).get(0);
            labelTableMap.putIfAbsent(key, tableData.get(x).get(1));
         }
      }
   }

   /**
    * This method converts a List/List/String to a hashmap This is used for header
    * format table i.e. multiple columns and the first row is a header / keys
    */
   public void convertHeaderTableFormatToMap() {
      // Iterate through the list
      for (int x = 0; x < tableData.size(); x++) {
         // Don't add the first header row into the map
         if (x > 0) {
            // Create a new map for that list data
            Map<String, String> detailMap = new HashMap<>();
            for (int i = 0; i < tableData.get(0).size(); i++) {
               // The Key is in the first row, current column
               // The value is in the current row, current column
               detailMap.put(tableData.get(0).get(i), tableData.get(x).get(i));
            }
            // Add the map to the full map
            headerTableMap.put(x, detailMap);
         }
      }
   }

   /**
    * This method gets the data in a particular cell
    * 
    * @param row - row number as the user sees it
    * @param col - column number as the user sees it
    * @return The cell data
    */
   public String getCellData(int row, int col) {
      return tableData.get(row - 1).get(col - 1);
   }

   /**
    * This method gets the number of rows in the table
    * 
    * @return the row count
    */
   public int getRowCount() {
      return tableData.size();
   }

   /**
    * This method gets the number of columns in the table
    * 
    * @return the number of columns in the table
    */
   public int getColCount() {
      return tableData.get(0).size();
   }

   /**
    * This method clicks the number of results per page
    * 
    * @param resultsPerPage - Number of Results per page to click either 10, 20 or
    *                       50
    */
   public void clickNumberOfResultsPerPage(String resultsPerPage) {
      List<WebElement> divs = findElementsBy(PAGINATION_PAGE_SIZE);
      for (int x = 0; x < divs.size(); x++) {
         if (divs.get(x).getText().equals(resultsPerPage)) {
            clickElement(divs.get(x));
            break;
         }
      }
   }

   /**
    * This method gets the number of rows with value in a specified column
    * 
    * @param col - column number
    * @return - The number of rows
    */
   public int getNumberOfRowsInColumnWithValues(int col) {
      int x = getRowCount();
      int noOfRowsWithValues = 0;
      for (int i = 1; i <= x; i++) {
         if (!StringUtils.isBlank(getCellData(i, col))) {
            noOfRowsWithValues++;
         }
      }
      return noOfRowsWithValues;
   }

}
