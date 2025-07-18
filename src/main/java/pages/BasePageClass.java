package pages;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import constants.IConstants;
import utils.DriverManager;
import utils.TestLoggerHolder;

/**
 * Base class for all Page Objects. Provides utility methods for element
 * interaction, scrolling, waits, and other common web actions.
 */
public class BasePageClass {

   private WebDriverWait wait;

   // String Constants
   protected static final String STALE_ELEMENT_EXCEPTION_MESSAGE = "StaleElement Exception so retrying";
   protected static final String SLEEP_COUNT = "Sleep, count: {}";

   // String Constants for elements
   private static final String ARGUMENTS_0_CLICK = "arguments[0].click();";
   // Dialog Box
   private static final String DIALOG_COMMIT = "css::.md-confirm-button";
   private static final String DIALOG_CANCEL = "css::.md-cancel-button";
   // Content Box
   private static final String CONTENT_BOX = "css::div[id*='select_container_'][style*='display: block'] .md-text";
   // Locator Ones
   /** tag name locator Constant **/
   public static final String TAG_NAME_BY = "tagName::";

   /** A string locator for H1 tag **/
   private static final String H1OBJECT = TAG_NAME_BY + "h1";
   /** A string locator for errors box tag **/
   private static final String ERROR_BOX = "css::#errorBox";
   /** A string locator for second error box tag **/
   private static final String WARNING_MESSAGE = "css::#errorBox > div.error-text-main-right > div > div > div > a";
   /** A string locator for TBODY tag **/
   public static final String TAG_TABLE_BODY = TAG_NAME_BY + IConstants.TBODY;
   /** A string locator for TH tag **/
   public static final String TAG_TABLE_HEADER = TAG_NAME_BY + IConstants.TH;
   /** A string locator for TR tag **/
   public static final String TAG_TABLE_ROWS = TAG_NAME_BY + IConstants.TR;
   /** A string locator for TD tag **/
   public static final String TAG_TABLE_DATA = TAG_NAME_BY + IConstants.TD;
   /** A string locator for input tag **/
   public static final String TAG_INPUT = TAG_NAME_BY + IConstants.INPUT;
   /** A string locator for input tag **/
   public static final String TAG_DIV = TAG_NAME_BY + IConstants.DIV;
   /** A string locator for li tag **/
   public static final String TAG_LI = TAG_NAME_BY + IConstants.LI;
   /** A string locator for the md-datepicker-input class **/
   public static final String CSS_MD_DATEPICKER_INPUT = "css::" + ".md-datepicker-input";
   /** A string locator for the md-virtual-repeat-container **/
   public static final String CSS_MD_VIRTUAL_REPEAT_CONTAINER = "css::md-virtual-repeat-container";
   /** A string locator for the md-select-value **/
   public static final String CSS_MD_SELECT_VALUE = "css::md-select-value";
   /** A string locator for anchor tags **/
   public static final String TAG_NAME_A = TAG_NAME_BY + "a";
   /** A string locator for input tags **/
   public static final String TAG_NAME_INPUT = TAG_NAME_BY + "input";

   /** A string locator for input tags **/
   public static final String DRIVER_IS_NULL = "Driver is Null";

   /**
    * Default constructor.
    */
   public BasePageClass() {
      // no-op
   }

   /**
    * This method verifies the page tab matches with that passed in
    * 
    * @param tabTitle This is the expected tab title
    */
   public void assertPageTitle(String tabTitle) {
      String actualTitle = getDriver().getTitle();
      assertThat("Tab title is different: " + actualTitle, actualTitle.contains(tabTitle));
   }

   /**
    * This method gets the error text
    * 
    * @return The Error Text
    */
   public String getErrorText() {
      String error = getText(ERROR_BOX);
      TestLoggerHolder.getLogger().info("Error is: {}", error);
      return error;
   }

   /**
    * This method determines whether actual text contains expected text.
    * 
    * @param expected The expected text
    * @param actual   The actual text
    * @return true if the actual text contains the expected text
    */
   public static boolean containsIgnoreCase(String expected, String actual) {
      if (expected == null || actual == null) {
         return false;
      }
      return actual.toLowerCase().contains(expected.toLowerCase());
   }

   /**
    * This method gets the warning message
    * 
    * @return - warning message as String
    */
   public String getWarningMessage() {
      String error = getText(WARNING_MESSAGE);
      TestLoggerHolder.getLogger().info("Error is: {}", error);
      return error;
   }

   /**
    * This method takes a string and returns a given locator. It splits the string
    * on ':' The first part is the locator type and second part is the locator
    * value. It returns the 'by' locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return locator
    */
   public By byLocator(String strElement) {
      String locatorType = strElement.split("::")[0];
      String locatorValue = strElement.split("::")[1];

      // For Testing Purposes and Debugging
      TestLoggerHolder.getLogger().debug("Retrieving object of type '{}' and value '{}'", locatorType, locatorValue);

      if ("id".equalsIgnoreCase(locatorType))
         return By.id(locatorValue);
      else if (("classname".equalsIgnoreCase(locatorType)) || ("class".equalsIgnoreCase(locatorType)))
         return By.className(locatorValue);
      else if (("linktext".equalsIgnoreCase(locatorType)) || ("link".equalsIgnoreCase(locatorType)))
         return By.linkText(locatorValue);
      else if ("partiallinktext".equalsIgnoreCase(locatorType))
         return By.partialLinkText(locatorValue);
      else if (("cssselector".equalsIgnoreCase(locatorType)) || ("css".equalsIgnoreCase(locatorType)))
         return By.cssSelector(locatorValue);
      else if ("xpath".equalsIgnoreCase(locatorType))
         return By.xpath(locatorValue);
      else if ("name".equalsIgnoreCase(locatorType))
         return By.name(locatorValue);
      else if ("tagName".equalsIgnoreCase(locatorType))
         return By.tagName(locatorValue);
      else
         throw new IllegalArgumentException("Unknown locator type '" + locatorType + "'");
   }

   /**
    * This method is a wrapper method for clearing the text of an object identified
    * by a locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    */
   public void clearElement(String strElement) {
      try {
         waitForElementToBeClickable(strElement).clear();
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         // Note: no need to user driver.findElement as the driver holds nothing about
         // the page. It's just a way of interacting with it.
         waitForElementToBeClickable(strElement).clear();
      }
   }

   /**
    * Clicks the element identified by the provided selector.
    *
    * @param strElement locator in format "css::button" or "xpath:://button"
    */
   public void click(String strElement) {
      boolean buttonClickedOK = false;
      int attempts = 0;
      // Wait for the element to be clickable then click it.
      while (!buttonClickedOK && attempts < 5) {
         try {
            waitForElementToBeClickable(strElement).click();
            buttonClickedOK = true;
         } catch (ElementClickInterceptedException e) {
            TestLoggerHolder.getLogger().info("{} {}", "Wait due to Click Intercepted Exception, attempt : ", attempts);
            sleep(1);
            sendKeys(strElement, Keys.TAB);
         } catch (StaleElementReferenceException e) {
            TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         }
         attempts++;
      }

      if (!buttonClickedOK) {
         // This will fail but we can output the exception..
         waitForElementToBeClickable(strElement).click();
      }
   }

   /**
    * This method clicks outside
    */
   public static void clickOutside() {
      Actions action = new Actions(getDriver());
      action.moveByOffset(0, 0).click().build().perform();
   }

   /**
    * This method clicks on an element. Note where possible try to use
    * click(strElement)
    * 
    * @param ele The WebElement to click
    */
   public void clickElement(WebElement ele) {
      waitForElementToBeClickableElement(ele).click();
   }

   /**
    * This method is a wrapper class for clicking a div containing text within a
    * content box
    * 
    * @param textToClick String the text to click in the div
    */
   public void clickMdSelectItem(String textToClick) {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(10)));
      List<WebElement> divElements = findElementsBy(CONTENT_BOX);
      boolean itemClicked = false;
      // reverse order because some menus start with a blank item
      for (int x = divElements.size() - 1; x >= 0; x--) {
         // Some times the div box is displayed with no text so put in an iterative wait
         // until its populated..
         int count = 0;
         while (StringUtils.isBlank(divElements.get(x).getText()) && count < 5) {
            TestLoggerHolder.getLogger().info(SLEEP_COUNT, count);
            sleep(0.5);
            count++;
         }
         if (divElements.get(x).getText().equalsIgnoreCase(textToClick)) {
            divElements.get(x).click();
            itemClicked = true;
            // Pause needed for refreshing divElements
            sleep(0.5);
            divElements.clear();
            break;
         }
      }
      if (!itemClicked) {
         throw new NotImplementedException("No item could be found to click under the content box " + CONTENT_BOX
               + " containing the text " + textToClick);
      }
   }

   /**
    * This method gets the default item in the drop down list
    * 
    * @return The Default Item
    */
   public String getDefaultItemInDropdownList() {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(10)));
      List<WebElement> divElements = findElementsBy(CONTENT_BOX);
      return divElements.get(0).getText();
   }

   /**
    * Get list of values within the dropdown box
    * 
    * @return - List of values
    */
   public List<String> getDropdownList() {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(10)));
      List<WebElement> divElements = findElementsBy(CONTENT_BOX);
      List<String> optionTexts = new ArrayList<>();

      // reverse order because some menus start with a blank item
      for (int x = divElements.size() - 1; x >= 0; x--) {
         // Some times the div box is displayed with no text so put in an iterative wait
         // until its populated..

         int count = 0;
         while (StringUtils.isBlank(divElements.get(x).getText()) && count < 5) {
            TestLoggerHolder.getLogger().info(SLEEP_COUNT, count);
            sleep(0.5);
            count++;
         }
         String text = divElements.get(x).getText();
         optionTexts.add(text);
      }
      return optionTexts;
   }

   /**
    * This method is a wrapper class for clicking the first item within a content
    * box
    * 
    * @return The text of the first item
    */
   public String clickMdSelectFirstItem() {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(10)));
      List<WebElement> divElements = findElementsBy(CONTENT_BOX);

      String firstItem = "";

      // Some times the div box is displayed with no text so put in an iterative wait
      int count = 0;
      while (StringUtils.isBlank(divElements.get(0).getText()) && count < 5) {
         TestLoggerHolder.getLogger().info(SLEEP_COUNT, count);
         sleep(0.5);
         count++;
      }
      if (StringUtils.isBlank(divElements.get(0).getText())) {
         throw new NotImplementedException("No item could be found to click under the content box " + CONTENT_BOX);
      } else {
         firstItem = divElements.get(0).getText();
         divElements.get(0).click();
         // Pause needed for refreshing divElements
         sleep(0.5);
         divElements.clear();
      }
      TestLoggerHolder.getLogger().info("Usergroup: {}", firstItem);
      return firstItem;
   }

   /**
    * This method first enters the text into the box. This may bring up a filtered
    * list of items in a list which is then clicked on.
    * 
    * @param mainTextBox - The string locator for the main box the user sees
    * @param text        - The text to enter/click
    */
   public void clickLiInUl(String mainTextBox, String text) {
      // first enter the text
      sendKeys(mainTextBox, text);
      // We need to determine if a list box is now visible so we can select one of the
      // items from it.
      // The box is contained with a div element. We can't tell which one our UL is in
      // so get all of them.
      List<WebElement> elements = findElementsBy(CSS_MD_VIRTUAL_REPEAT_CONTAINER);
      boolean enterClicked = false;
      // For each box container...
      for (int x = 0; x < elements.size(); x++) {
         if (!enterClicked) {
            // Get the div hidden attribute. If it is not true then get the list items
            enterClicked = clickLiItemIfParentIsNotHidden(x, elements, text, enterClicked);
         } else {
            break;
         }
      }
      // If there was no list item box just click enter
      if (!enterClicked) {
         sendKeys(mainTextBox, Keys.ENTER);
      }
   }

   /**
    * This method called from clickLiInUl and determines if the container box for
    * our list of items is visible or not. If it is not then if the item we
    * supplied is available it is clicked.
    * 
    * @param x            This the current box container element index
    * @param elements     - This is the full list of box containers
    * @param uList        - This is the list box we are interested in that has the
    *                     li items
    * @param text         - This is the text to click
    * @param enterClicked - This is a boolean value to determine whether the item
    *                     was clicked or not
    * @return The clicked value
    */
   private boolean clickLiItemIfParentIsNotHidden(int x, List<WebElement> elements, String text,
         boolean enterClicked) {

      WebElement container = elements.get(x);
      if (doesElementHaveAttributeWithValue(container, "aria-hidden", "false")) {
         List<WebElement> liItems = elements.get(x).findElements(By.tagName("li"));
         if (!liItems.isEmpty()) {
            for (int y = 0; y < liItems.size(); y++) {
               if (liItems.get(y).getText().equals(text)) {
                  sleep(0.5);
                  liItems.get(y).click();
                  setWait(new WebDriverWait(getDriver(), Duration.ofMillis(5000)));
                  getWait().until(ExpectedConditions.attributeContains(container, "aria-hidden", "true"));
                  enterClicked = true;
                  break;
               }
            }
         } else {
            throw new NotImplementedException("THE_OBJECT" + " " + elements.get(x) + " has no items");
         }
      }
      return enterClicked;
   }

   /**
    * This method clicks commit on the dialog box
    */
   public void clickDialogCommit() {

      TestLoggerHolder.getLogger().info("Click the Commit button on the dialog");
      click(DIALOG_COMMIT);
      setWait(new WebDriverWait(getDriver(), Duration.ofMillis(500)));
      // Sometime it doesn't actually click commit, so check for the existence of the
      // dialog box, if its still there click it again.
      int attempts = 0;
      while (isElementExists(getWait(), "css::md-dialog-container ng-scope") && attempts < 5) {
         TestLoggerHolder.getLogger().info("Click the Commit button again as it didn't work, attempt {}",
               attempts++);
         click(DIALOG_COMMIT);
      }
      TestLoggerHolder.getLogger().info("Clicked the Commit button on the dialog");

   }

   /**
    * This method clicks cancel on the dialog box
    */
   public void clickDialogCancel() {
      TestLoggerHolder.getLogger().info("Click the Cancel button on the dialog");
      click(DIALOG_CANCEL);
   }

   /**
    * This method finds and returns an element found by its locator within a web
    * element. e.g. an item within a TR
    * 
    * @param outerElement          The Web element that contains the inner element
    *                              to find
    * @param innerElementByLocator The string locator for the inner web element to
    *                              find
    * @return WebElement
    */
   public WebElement findElementBy(WebElement outerElement, String innerElementByLocator) {
      waitForElementToBeClickableElement(outerElement);
      return outerElement.findElement(byLocator(innerElementByLocator));
   }

   /**
    * This method finds and returns a list of elements found by the locator within
    * a web element. e.g. TD items within a TR
    * 
    * @param outerElement           The Web element that contains the inner element
    *                               to find
    * @param innerElementsByLocator The string locator for the inner web elements
    *                               to find
    * @return List of WebElements
    */
   public List<WebElement> findElementsBy(WebElement outerElement, String innerElementsByLocator) {
      waitForElementToBeClickableElement(outerElement);
      return outerElement.findElements(byLocator(innerElementsByLocator));
   }

   /**
    * This method gets all the elements located within another element
    * 
    * @param strElement: The String locator for the web element
    * @return Returns a list of the web elements found using the locator
    */
   public List<WebElement> findElementsBy(String strElement) {
      By by = byLocator(strElement);
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      try {
         getWait().until(ExpectedConditions.presenceOfElementLocated(by));
         return getDriver().findElements(by);
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         getWait().until(ExpectedConditions.presenceOfElementLocated(by));
         return getDriver().findElements(by);
      }

   }

   /**
    * This method is a wrapper for getting an object attribute identified by a
    * locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @param attribute  The elements attribute to get
    * @return String
    */
   public String getAttribute(String strElement, String attribute) {
      // Get The locator
      By by = byLocator(strElement);
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      try {
         return getWait().until(ExpectedConditions.presenceOfElementLocated(by)).getAttribute(attribute);
      } catch (Exception e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         return getWait().until(ExpectedConditions.visibilityOfElementLocated(by)).getAttribute(attribute);
      }
   }

   /**
    * This method is a wrapper for getting an object css value identified by a
    * locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @param attribute  The elements attribute to get
    * @return WebElement
    */
   public String getCSSValue(String strElement, String attribute) {
      // Get The locator
      By by = byLocator(strElement);
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      try {
         return getWait().until(ExpectedConditions.presenceOfElementLocated(by)).getCssValue(attribute);
      } catch (Exception e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         by = byLocator(strElement);
         return getWait().until(ExpectedConditions.visibilityOfElementLocated(by)).getCssValue(attribute);
      }
   }

   /**
    * This method is a wrapper for getting an object attribute
    * 
    * @param element   This is the element who's attribute you wish to get
    * @param attribute The elements attribute to get
    * @return String
    */
   public String getElementAttribute(WebElement element, String attribute) {
      return element.getAttribute(attribute);
   }

   /**
    * This method is a wrapper for determine if an element has an attribute
    * 
    * @param element   This is the webelement to interrogate
    * @param attribute The element attribute to get
    * @return result boolean if the attribute is found or not.
    */
   public boolean hasAttribute(WebElement element, String attribute) {
      Boolean result = false;
      String value = element.getAttribute(attribute);
      if (value != null) {
         result = true;
      }
      return result;
   }

   /**
    * This method is a wrapper for determining if a webelement has an attribute
    * with a value
    * 
    * @param webElement This is the element that you want to get the attribute for
    * @param attribute  The elements attribute to get
    * @param value      This is the attributes value we want to check
    * @return boolean
    */
   public boolean doesElementHaveAttributeWithValue(WebElement webElement, String attribute, String value) {
      boolean rtnValue = false;
      if (webElement.getAttribute(attribute).equals(value)) {
         rtnValue = true;
      }
      return rtnValue;
   }

   /**
    * Getter for the RemoteWebDriver
    * 
    * @return driver
    */
   public static RemoteWebDriver getDriver() {
      return (DriverManager.getCurrentDriver());
   }

   /**
    * This method gets a the first selected option
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return The text in the first select option
    */
   public String getFirstSelected(String strElement) {
      waitForElementToBeClickable(strElement);
      Select select = select(strElement);
      WebElement option = select.getFirstSelectedOption();
      return option.getText();
   }

   /**
    * This method gets a list of all the select options
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return List Of Strings from the Select element
    */
   public List<String> getSelectList(String strElement) {
      List<String> options = new ArrayList<>();
      waitForElementToBeClickable(strElement);
      Select select = select(strElement);
      for (int x = 0; x < select.getOptions().size(); x++) {
         options.add(select.getOptions().get(x).getText());
      }
      return options;
   }

   /**
    * This method gets the text from an element
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return WebElement
    */
   public String getText(String strElement) {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      try {
         By by = byLocator(strElement);
         return getWait().until(ExpectedConditions.visibilityOfElementLocated(by)).getText();
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         By by = byLocator(strElement);
         return getWait().until(ExpectedConditions.visibilityOfElementLocated(by)).getText();
      }
   }

   /**
    * This method returns the current web driver wait
    * 
    * @return wait time in seconds to wait
    */
   public WebDriverWait getWait() {
      return wait;
   }

   /**
    * This method is a wrapper method waiting for an object to be displayed
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return true if it is displayed
    */
   public boolean isDisplayed(String strElement) {
      try {
         waitForVisibilityOfElementLocatedBy(strElement);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   /**
    * This method clicks away usually to take focus of the current element
    */
   public static void clickAway() {
      WebElement bodyElement = getDriver().findElement(By.tagName(("body")));
      bodyElement.click();
   }

   /**
    * This method is a wrapper method to wait for an object to exist, returns true
    * if does.
    * 
    * @param wait       A WebDriver wait object
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return boolean
    */
   public boolean isElementExists(WebDriverWait wait, String strElement) {
      By by = byLocator(strElement);
      setWait(wait);
      try {
         getWait().until(ExpectedConditions.presenceOfElementLocated(by));
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   /**
    * This method returns true if the element is enabled
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return WebElement
    */
   public boolean isEnabled(String strElement) {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(5)));

      try {
         if (isElementExists(getWait(), strElement)) {
            return getDriver().findElement(byLocator(strElement)).isEnabled();
         } else {
            return false;
         }
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         if (isElementExists(wait, strElement)) {
            return getDriver().findElement(byLocator(strElement)).isEnabled();
         } else {
            return false;
         }
      }
   }

   /**
    * This method refreshes the current page
    */
   public static void refresh() {
      getDriver().navigate().refresh();
   }

   /**
    * This method navigates back to the previous page
    */
   public static void navigateBack() {
      getDriver().navigate().back();
   }

   /**
    * This method gets the page header
    * 
    * @return Returns a string for the Page Header text
    */
   public String returnPageHeader() {
      WebElement title = waitForVisibilityOfElementLocatedBy(H1OBJECT);
      return title.getText();
   }

   /**
    * This method returns the object if it is visible
    * 
    * @param wait       The WebDriverWait element defining how long to wait
    * @param strElement The string value of the web element to find
    * @return The web element if it is visible
    */
   public WebElement rtnElementIfVisible(WebDriverWait wait, String strElement) {
      By by = byLocator(strElement);
      WebElement object = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
      if (object == null) {
         throw new NotImplementedException("THE_OBJECT" + " " + strElement + " is not visible");
      }
      return object;
   }

   /**
    * This method returns the object if it is exists
    * 
    * @param wait       The WebDriverWait element defining how long to wait
    * @param strElement The string value of the web element to find
    * @return The web element if it is visible
    */
   public WebElement rtnElementIfExists(WebDriverWait wait, String strElement) {
      By by = byLocator(strElement);
      WebElement object = wait.until(ExpectedConditions.presenceOfElementLocated(by));
      if (object == null) {
         throw new NotImplementedException("THE_OBJECT" + " " + strElement + " does not exist");
      }
      return object;
   }

   /**
    * Wrapper method for returning a Select Element
    * 
    * @param selectElement This is the string type and locator separated by :: used
    *                      to find the element
    * @return Select Element
    */
   public Select select(String selectElement) {
      WebElement dropDownElement = waitForElementToBeClickable(selectElement);
      return new Select(dropDownElement);
   }

   /**
    * This method selects a given option with given text in a select drop down
    * element
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @param text       This is text to select
    */
   public void selectByVisibleText(String strElement, String text) {
      try {
         waitForElementToBeClickable(strElement);
         Select select = select(strElement);
         select.selectByVisibleText(text);
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         waitForElementToBeClickable(strElement);
         Select select = select(strElement);
         select.selectByVisibleText(text);
      }
   }

   /**
    * This is a wrapper method for sending keystrokes to an object identified by a
    * locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @param text       This is the text to send to the WebElement
    */
   public void sendKeys(String strElement, String text) {
      try {
         clearElement(strElement);
         waitForElementToBeClickable(strElement).sendKeys(text);
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         waitForElementToBeClickable(strElement).sendKeys(text);
      }
   }

   /**
    * This is a wrapper method for sending a file to an object identified by a
    * locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @param filePath   This is the string path to the file to send to the
    *                   WebElement
    */
   public void sendKeysForFileUpload(String strElement, String filePath) {
      try {
         wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
         WebElement button = rtnElementIfExists(wait, strElement);
         button.sendKeys(filePath);
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
         WebElement button = rtnElementIfExists(wait, strElement);
         button.sendKeys(filePath);
      }
   }

   /**
    * This is a wrapper method for sending keystrokes to a WebElement provided
    * 
    * @param element - the WebElement to send input to
    * @param text    - the string text to input
    */
   public void sendKeys(WebElement element, String text) {
      getWait().until(ExpectedConditions.elementToBeClickable(element));
      element.clear();
      element.sendKeys(text);
   }

   /**
    * This is a wrapper method for sending keystrokes to an object identified by a
    * locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @param key        This is the keys to send to the WebElement
    */
   public void sendKeys(String strElement, Keys key) {
      try {
         waitForElementToBeClickable(strElement).sendKeys(key);
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         waitForElementToBeClickable(strElement).sendKeys(key);
      }
   }

   /**
    * This method sets the explicit wait time
    * 
    * @param wait time in seconds to wait
    */
   public void setWait(WebDriverWait wait) {
      this.wait = wait;
   }

   /**
    * This method waits for 1 second * value passed in
    * 
    * @param timeout The timeout in seconds to wait
    */
   public static void sleep(double timeout) {
      long sleepTime = 1000;
      try {
         Thread.sleep((long) (timeout * sleepTime));
      } catch (InterruptedException e) {
         TestLoggerHolder.getLogger().error("Interrupted Exception {}", e.getMessage(), e);
         Thread.currentThread().interrupt();
      }
   }

   /**
    * This method switches the focus to a new frame
    * 
    * @param frame The sting locator for the frame to switch to
    */
   public void switchToFrame(String frame) {
      WebElement frameToSwitchTo = waitForElementToBeClickable(frame);
      getDriver().switchTo().frame(frameToSwitchTo);
   }

   /**
    * This method switches the focus back to the default content
    */
   public void switchToDefaultContent() {
      getDriver().switchTo().defaultContent();
   }

   /**
    * This method switches to the main window
    */
   public void switchToMainWindow() {
      // Get The main tab
      String mainWindow = getDriver().getWindowHandle();
      // Iterate and close all the other tabs
      for (String handle : getDriver().getWindowHandles()) {
         if (!handle.equals(mainWindow)) {
            getDriver().switchTo().window(handle);
            break;
         }
      }
   }

   /**
    * This method waits for the invisibility of an element
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    */
   public void waitForInVisibilityOfElementLocatedBy(String strElement) {
      By by = byLocator(strElement);
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      try {
         getWait().until(ExpectedConditions.invisibilityOfElementLocated(by));
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
      }
   }

   /**
    * This method waits for the visibility of an element
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return Returns the element if is is visible
    */
   public WebElement waitForVisibilityOfElementLocatedBy(String strElement) {
      By by = byLocator(strElement);
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      try {
         return getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         return getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
      }
   }

   /**
    * This method is a wrapper class for waiting for an element to be clickable by
    * a locator
    * 
    * @param strElement This is the string type and locator separated by :: used to
    *                   find the element
    * @return WebElement
    */
   public WebElement waitForElementToBeClickable(String strElement) {
      // Get the locator
      By by = byLocator(strElement);
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      return getWait().until(ExpectedConditions.elementToBeClickable(by));
   }

   /**
    * This method is a wrapper class for waiting for an element to be clickable by
    * a locator. Should only be used for Table elements - otherwise use
    * waitForElementToBeClickable(string)
    * 
    * @param element This is WebElement to wait for
    * @return element this is the WebElement
    */
   public WebElement waitForElementToBeClickableElement(WebElement element) {
      // Get the locator
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      return getWait().until(ExpectedConditions.elementToBeClickable(element));
   }

   /**
    * This method waits until specific text appears
    * 
    * @param expectedText - The text we expect to see
    * @param strElement   - The element to wait for
    * @return - true or false
    */
   public boolean waitForTextContains(String expectedText, String strElement) {
      try {
         By by = byLocator(strElement);
         wait = getWait();
         return wait.until(ExpectedConditions.textToBePresentInElementLocated(by, expectedText));
      } catch (StaleElementReferenceException e) {
         return false;
      }
   }

   /**
    * Waits for an element containing specific text, refreshing the page if
    * necessary
    * 
    * @param statusLocator The locator status
    * @param expectedText  The expected text
    * @param maxAttempts   max attempts
    */
   public void waitForTextContainsWithRefresh(String statusLocator, String expectedText, int maxAttempts) {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(10)));

      for (int attempt = 1; attempt <= maxAttempts; attempt++) {
         try {
            WebElement statusElement = wait
                  .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(statusLocator)));
            boolean isTextPresent = Boolean.TRUE
                  .equals(wait.until(ExpectedConditions.textToBePresentInElement(statusElement, expectedText)));

            if (isTextPresent) {
               return;
            }

         } catch (TimeoutException | StaleElementReferenceException e) {
            getDriver().navigate().refresh();
         }
      }
   }

   /**
    * Wait for the URL Title to contain the text provided
    * 
    * @param partialUrl The expect partial URL
    */
   public void waitForUrlTitle(String partialUrl) {
      // Get the locator
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      getWait().until(ExpectedConditions.urlContains(partialUrl));
      TestLoggerHolder.getLogger().info(getDriver().getCurrentUrl());
      TestLoggerHolder.getLogger().info("");
   }

   /**
    * This method verifies the page header text to contain provided text
    * 
    * @param text - The expected header title
    */
   public void waitForHeaderTextTitleToContain(String text) {
      boolean bFound = false;
      int attempts = 0;
      // Wait up to 2 mins..
      while (!bFound && attempts < 120) {
         String actualHeader = getAttribute(H1OBJECT, "innerText");
         if (!StringUtils.isBlank(actualHeader) && actualHeader.toLowerCase().contains(text.toLowerCase())) {
            TestLoggerHolder.getLogger().info("Correct Page found");
            bFound = true;
         } else {
            TestLoggerHolder.getLogger().info("{} {}", "Correct Page not found, waiting...iteration:", attempts);
            sleep(1);
            attempts++;
         }
      }
   }

   /***************************************************************************/
   /**
    * ACTIONS /
    ***************************************************************************/

   /**
    * This method uses actions to move to the element
    * 
    * @param strElement String locator for the element that the focus needs to be
    *                   moved to
    */
   public void actionMoveToElement(String strElement) {
      WebElement element = waitForVisibilityOfElementLocatedBy(strElement);
      Actions moveToElement = new Actions(getDriver()).moveToElement(element);
      moveToElement.build().perform();
   }

   /**
    * This method uses actions to double click an element
    * 
    * @param strElement - The string locator for the element
    */
   public void actionDoubleClick(String strElement) {
      WebElement element = waitForElementToBeClickable(strElement);
      Actions clickElement = new Actions(getDriver()).doubleClick(element);
      clickElement.build().perform();
   }

   /**
    * This method uses actions to right click an element
    * 
    * @param strElement - The string locator for the element
    */
   public void actionRightClick(String strElement) {
      WebElement element = waitForElementToBeClickable(strElement);
      Actions clickElement = new Actions(getDriver()).contextClick(element);
      clickElement.build().perform();
   }

   /**
    * This method uses actions to move mouse by Offset
    * 
    * @param xoffset - The integer values of horizontal offset
    * @param yoffset - The integer values of vertical offset
    */
   public void moveByOffsetAndClick(int xoffset, int yoffset) {
      Actions moveByOffset = new Actions(getDriver()).moveByOffset(xoffset, yoffset).click();
      moveByOffset.build().perform();
   }

   /***************************************************************************/
   /**
    * ALERTS /
    ***************************************************************************/

   /**
    * This method clicks accept on an alert
    */
   public void clickAcceptAlert() {
      Alert confirmAlert = switchToAlert();
      confirmAlert.accept();
   }

   /**
    * This method clicks cancel on an alert
    */
   public void clickCancelAlert() {
      Alert confirmAlert = switchToAlert();
      confirmAlert.dismiss();
   }

   /**
    * This method switches to an alert
    * 
    * @return the alert
    */
   public Alert switchToAlert() {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      wait.until(ExpectedConditions.alertIsPresent());
      return getDriver().switchTo().alert();
   }

   /***************************************************************************/
   /**
    * JAVASCRIPT /
    ***************************************************************************/

   /**
    * This method performs a click action on object identified by its id and using
    * javascript
    * 
    * @param strElementId- The id of the element to click
    */
   public void jsClickId(String strElementId) {
      ((JavascriptExecutor) getDriver())
            .executeScript("document.getElementById('" + strElementId + "').click(strElement);");
   }

   /**
    * This method performs a click action on object identified by its id and using
    * javascript
    * 
    * @param strElement- The id of the element to click
    */
   public void jsClickElement(String strElement) {
      try {
         WebElement element = waitForElementToBeClickable(strElement);
         ((JavascriptExecutor) getDriver()).executeScript(ARGUMENTS_0_CLICK, element);
         waitForJavascriptToComplete();
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         WebElement element = waitForElementToBeClickable(strElement);
         ((JavascriptExecutor) getDriver()).executeScript(ARGUMENTS_0_CLICK, element);
         waitForJavascriptToComplete();
      }
   }

   /**
    * This method performs a click action on WebElement
    * 
    * @param element- The element to click
    */
   public void jsClickElement(WebElement element) {
      try {
         ((JavascriptExecutor) getDriver()).executeScript(ARGUMENTS_0_CLICK, element);
         waitForJavascriptToComplete();
      } catch (StaleElementReferenceException e) {
         TestLoggerHolder.getLogger().info(STALE_ELEMENT_EXCEPTION_MESSAGE);
         ((JavascriptExecutor) getDriver()).executeScript(ARGUMENTS_0_CLICK, element);
         waitForJavascriptToComplete();
      }
   }

   /**
    * This method scrolls to the top of the screen
    */
   public void jsScrollToTopOfScreen() {
      ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0,0)", "");
      waitForJavascriptToComplete();
   }

   /**
    * This method uses javascript to set the focus on an element
    * 
    * @param strElement The string locator for the element
    */
   public void jsSetFocus(String strElement) {
      sendKeys(strElement, Keys.SHIFT);
      String strElementConstant = strElement.replace("css::#", "");
      ((JavascriptExecutor) getDriver())
            .executeScript("document.getElementById('" + strElementConstant + "').focus();");
   }

   /**
    * Scrolls the window to the top using JavaScript.
    */
   public void jsWindowScrollToTop() {
      ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0,0);");
      waitForJavascriptToComplete();
   }

   /**
    * This method scrolls to the vertical position specified
    * 
    * @param veriticalPos the is the vertical position to scroll to
    */
   public void jsWindowScrollBy(int veriticalPos) {
      ((JavascriptExecutor) getDriver()).executeScript("window.scrollBy(0," + veriticalPos + ")", "");
      waitForJavascriptToComplete();
   }

   /**
    * This method gets the body height
    * 
    * @return Returns the body height
    */
   public int jsGetBodyHeight() {
      return ((Number) ((JavascriptExecutor) getDriver()).executeScript("return document.body.scrollHeight", ""))
            .intValue();
   }

   /**
    * This method gets the window height
    * 
    * @return returns the window height
    */
   public int jsGetWindowHeight() {
      return ((Number) ((JavascriptExecutor) getDriver()).executeScript("return window.innerHeight", "")).intValue();
   }

   /**
    * This method waits for the javascript to to be complete
    */
   public void waitForJavascriptToComplete() {
      setWait(new WebDriverWait(getDriver(), Duration.ofSeconds(15)));
      wait.until((ExpectedCondition<Boolean>) driverNew -> ((JavascriptExecutor) getDriver())
            .executeScript("return document.readyState").equals("complete"));
   }
}
