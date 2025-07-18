package constants;

/**
 * The repository for the project wide constants.
 */

public class IConstants {

   /**
    * Constructor required for Sonar
    */
   private IConstants() {
      throw new IllegalStateException("Utility class");
   }

   public static final String GET_PRODUCTS_API = "GET_PRODUCTS_API";
   public static final String URL= "URL";
   
   // Plugin Ones
   /** reports Constant **/
   public static final String REPORTS_FOLDER = "target/reports/";
   /** .json Constant **/
   public static final String JSON_SUFFIX = ".json";
   /** Results Constant **/
   public static final String RESULTS = "Results";
   /** Results Constant **/
   public static final String RESULTS_LOWERCASE = "results";
   /** Results Constant **/
   public static final String RUNNING = "Running";

   // Local / Maven Run Configuration ones
   /** https://www.rep-3.abc.ext.pri:2443 constant */
   public static final String BASE_URL = "https://www.rep-3.abc.ext.pri:2443";
   /** Base URL constant */
   public static final String BASE_URL_LOWERCASE = "baseUrl";
   /** cucumber.filter.tags Constant **/
   public static final String CUCUMBER_FILTER_TAGS = "cucumber.filter.tags";
   /** Max Local Instances Constant **/
   public static final String MAX_LOCAL_INSTANCES = "maxLocalInstances";
   /** dev Constant **/
   public static final String DEV = "dev";
   /** REP2 Constant **/
   public static final String STAGE = "STAGE";
   /** REP2 Constant **/
   public static final String QA = "QA";
   /** REP2 Constant **/
   public static final String LOCAL = "LOCAL";
   /** ENV Constant **/
   public static final String ENV = "ENV";
   /** env Constant **/
   public static final String ENV_LOWER_CASE = "env";
   /** {{apiVersion}} Constant **/
   public static final String NOT_SET_API_VERSION = "{{apiVersion}}";
   /** {{baseUrl}} constant **/
   public static final String NOT_SET_BASE_URL = "{{baseUrl}}";
   /** {{env}} Constant **/
   public static final String NOT_SET_ENV_VALUE = "{{env}}";
   /** {{tagValue}} Constant **/
   public static final String NOT_SET_TAG_VALUE = "{{tagValue}}";
   /** {{browser}} Constant **/
   public static final String NOT_SET_BROWSER_VALUE = "{{browser}}";
   /** tagValue lowercase Constant **/
   public static final String TAG_VALUE_LOWER_CASE = "tagValue";
   /** headless lowercase Constant **/
   public static final String HEADLESS_VALUE_LOWER_CASE = "headless";
   /** {{headless}} Constant **/
   public static final String NOT_SET_HEADLESS_VALUE = "{{headless}}";
   /** Smoke Constant **/
   public static final String DEFAULT_TAG_VALUE = "Smoke";

   

   // True / False Ones
   /** TRUE Constant **/
   public static final String TRUE = "TRUE";
   /** true Constant **/
   public static final String TRUE_LOWERCASE = "true";
   /** FALSE Constant **/
   public static final String FALSE = "FALSE";

   // Valid or Invalid Ones
   /** yes Constant **/
   public static final String VALID = "Valid";
   /** no Constant **/
   public static final String INVALID = "InValid";

   // Yes or No N/A Ones
   /** yes Constant **/
   public static final String YES = "yes";
   /** Through ASW Constant **/
   public static final Object YES_THROUGH_ASW = "Yes";
   /** no Constant **/
   public static final String NO = "no";
   /** N/A Constant **/
   public static final String NA = "N/A";

   // HTML ones
   /** tbody Constant **/
   public static final String TBODY = "tbody";
   /** tr Constant **/
   public static final String TR = "tr";
   /** td Constant **/
   public static final String TD = "td";
   /** th Constant **/
   public static final String TH = "th";
   /** input Constant **/
   public static final String INPUT = "input";
   /** li Constant **/
   public static final String LI = "li";
   /** div Constant **/
   public static final String DIV = "div";
   
   // Order Ones
   /** ASCENDING Constant **/
   public static final String ASCENDING = "ASCENDING";
   /** DESCENDING Constant **/
   public static final String DESCENDING = "DESCENDING";

   // Accept or Reject Ones
   /** Accept Constant **/
   public static final String ACCEPT = "ACCEPT";
   /** Reject Constant **/
   public static final String REJECT = "REJECT";

   // Browser Ones
   /** Firefox Constant **/
   public static final String FIREFOX = "FIREFOX";
   /** Chrome Constant **/
   public static final String CHROME = "CHROME";
   /** Edge Constant **/
   public static final String EDGE = "EDGE";
   /** browser Constant **/
   public static final String BROWSER = "browser";

     // Enabled / Disabled
   /** Disabled Constant **/
   public static final String DISABLED = "disabled";
   /** Enabled Constant **/
   public static final String ENABLED = "enabled";

      // Header / Footer
   /** All Constant **/
   public static final String HEADER = "header";
   /** All Constant **/
   public static final String FOOTER = "footer";

   // OTHER
   /** All Constant **/
   public static final String ALL = "All";
   /** All Dates Constant **/
   public static final String ALL_DATES = "alldates";
   /** Attachments Constant **/
   public static final String ATTACHMENTS = "attachments";
   /** Blank Constant **/
   public static final String BLANK = "BLANK";
   /** Blank (No Value) **/
   public static final String BLANK_NO_VALUE = " ";
   /** Choose Constant **/
   public static final String CHOOSE = "Choose :";
   /** Closed Constant **/
   public static final String CLOSED = "Closed";
   /** Data Provider Constant **/
   public static final String DATA_PROVIDER = "dataprovider";
   /** Delete Constant **/
   public static final String DELETE = "DELETE";
   /** Description Constant **/
   public static final String DESCRIPTION = "description";
   /** Test Constant **/
   public static final String TEST = "Test";
      /** Replace Empty Constant **/
   public static final String EMPTY = "Empty";
   /** File Of Files Lower case **/
   public static final String FILE_OF_FILES_LOWERCASE = "fileoffiles";
   /** FILE NAME Constant **/
   public static final String FILE_NAME = "FILE_NAME";  
   /** NO Change Constant **/
   public static final String NOCHANGE = "NOCHANGE";
   /** Number Constant **/
   public static final String NUMBER = "Number :";
   /** This is the number of MI files in the downloads directory */
   public static final int NUMBER_OF_MI_FILES_IN_DIRECTORY = 0;
   /** Replace Constant **/
   public static final String REPLACE = "<<REPLACE>>";
   /** Replace Value Constant **/
   public static final String REPLACE_VALUE = "'XXXXX'";
   /** Scenario Data **/
   public static final String SCENARIO_DATA = "scenariodata";
   /** Smoke Constant **/
   public static final String SMOKE_LOWER_CASE = "smoke";
   /** Value Constant **/
   public static final String VALUE = "value";
   /** XML Line Break Constant **/
   public static final String XML_LINE_BREAK = "\n";
   
}
