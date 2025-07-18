package utils;

import java.io.File;
import java.net.InetAddress;
import java.util.Set;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import constants.IConstants;

/**
 * Manages thread-safe WebDriver instances for each test thread.
 * <p>
 * Supports driver initialization (online and offline), lifecycle management,
 * and cross-browser execution.
 */
public class DriverManager {

   /**
    * Constructor required for Sonar
    */
   private DriverManager() {
      throw new IllegalStateException("Utility class");
   }

   private static ThreadLocal<RemoteWebDriver> driverThreadLocal = new ThreadLocal<>();

   private static final String CHROME_DRIVER_NAME = "chromedriver";
   private static final String GECKO_DRIVER_NAME = "geckodriver";
   private static final String EDGE_DRIVER_NAME = "msedgedriver";
   private static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";
   private static final String WEBDRIVER_GECKO_DRIVER = "webdriver.gecko.driver";
   private static final String WEBDRIVER_EDGE_DRIVER = "webdriver.edge.driver";

   private static final String WINDOWS = "Windows";
   private static final String DISABLE_GPU = "disable-gpu";
   private static final String NO_SANDBOX = "no-sandbox";
   private static final String DISABLE_DEV_SHM_USAGE = "disable-dev-shm-usage";
   private static final String START_MAXIMISED = "start-maximized";
   private static final String LANG_EN_GB = "--lang=en-GB";
   private static final String REMOTE_ALLOW_ORIGINS = "--remote-allow-origins=*";
   private static final String IGNORE_CERTIFICATE_ERRORS = "-ignore-certificate-errors";

   private static final String OS_NAME = "os.name";

   /**
    * This method gets the gecko driver path from system variables
    * 
    * @return the GeckoDriver name
    */
   public static String getGeckoDriverName() {
      return System.getProperty(OS_NAME).startsWith(WINDOWS) ? GECKO_DRIVER_NAME + ".exe" : GECKO_DRIVER_NAME;
   }

   /**
    * This method gets the Chrome driver path from system variables
    * 
    * @return the Chrome Driver name
    */
   public static String getChromeDriverName() {
      return System.getProperty(OS_NAME).startsWith(WINDOWS) ? CHROME_DRIVER_NAME + ".exe" : CHROME_DRIVER_NAME;
   }

   /**
    * This method gets the Edge driver path from system variables
    * 
    * @return the Edge Driver name
    */
   public static String getEdgeDriverName() {
      String edgeDriverPath = System.getProperty(OS_NAME).startsWith(WINDOWS) ? EDGE_DRIVER_NAME + ".exe"
            : EDGE_DRIVER_NAME;
      TestLoggerHolder.getLogger().info(edgeDriverPath);
      return edgeDriverPath;
   }

   /**
    * Attempts to download the correct driver version or fallback to local offline
    * copy.
    *
    * @param browserName the browser name (e.g., "chrome", "firefox", "edge")
    */
   public static void setOrDownloadDriver(String browserName) {
      boolean isWindows = System.getProperty(OS_NAME).toLowerCase().contains("win");
      String driverFileName;
      String systemPropertyKey;

      // Determine file name and system property based on browser
      switch (browserName.toLowerCase()) {
      case IConstants.FIREFOX:
         driverFileName = isWindows ? "geckodriver.exe" : "geckodriver";
         systemPropertyKey = WEBDRIVER_GECKO_DRIVER;
         break;
      case IConstants.EDGE:
         driverFileName = isWindows ? "msedgedriver.exe" : "msedgedriver";
         systemPropertyKey = WEBDRIVER_EDGE_DRIVER;
         break;
      case IConstants.CHROME:
      default:
         driverFileName = isWindows ? "chromedriver.exe" : "chromedriver";
         systemPropertyKey = WEBDRIVER_CHROME_DRIVER;
         break;
      }

      // 1️⃣ Attempt online driver download using your downloader (e.g.,
      // WebDriverManager logic)
      try {
         InetAddress address = InetAddress.getByName("google.com");
         if (address != null) {
            TestLoggerHolder.getLogger().info("✅ Internet detected. Downloading driver for: " + browserName);
            String path = DriverDownloader.download(browserName.toLowerCase());
            System.setProperty(systemPropertyKey, path);
            return;
         }
      } catch (Exception e) {
         TestLoggerHolder.getLogger().warn("⚠️ Online driver detection/download failed: " + e.getMessage(), e);
      }

      // 2️⃣ Fallback: Load pre-bundled driver from test project /drivers folder
      String userDir = System.getProperty("user.dir"); // This will point to the test project root
      File localDriver = new File(userDir + File.separator + "drivers" + File.separator + driverFileName);

      if (!localDriver.exists()) {
         throw new RuntimeException(
               "❌ Offline mode: " + browserName + " driver not found at " + localDriver.getAbsolutePath());
      }

      System.setProperty(systemPropertyKey, localDriver.getAbsolutePath());
      TestLoggerHolder.getLogger()
            .info("📦 Local fallback " + browserName + " driver used from: " + localDriver.getAbsolutePath());
   }

   /**
    * Initializes the appropriate WebDriver based on configuration (Chrome,
    * Firefox, Edge). Downloads the driver if online, or uses local fallback.
    */
   public static void initDriver() {
      if (driverThreadLocal.get() != null) {
         return; // driver already initialised
      }

      String browser = BaseTestConfiguration.getBrowser();
      RemoteWebDriver webDriver;

      switch (browser.toUpperCase()) {
      case IConstants.FIREFOX:
         webDriver = createFirefoxDriver();
         break;
      case IConstants.EDGE:
         webDriver = createEdgeDriver();
         break;
      case IConstants.CHROME:
      default:
         webDriver = createChromeDriver();
         break;
      }

      driverThreadLocal.set(webDriver);
      TestLoggerHolder.getLogger().info("Webdriver initialised for thread:" + Thread.currentThread());
   }

   /**
    * Creates a Chrome webdriver instance
    * 
    * @return Chrome WebDriver instance
    */
   public static RemoteWebDriver createChromeDriver() {
      ChromeOptions chromeOptions = new ChromeOptions();
      chromeOptions.addArguments(DISABLE_GPU);
      chromeOptions.addArguments(NO_SANDBOX);
      chromeOptions.addArguments(DISABLE_DEV_SHM_USAGE);
      chromeOptions.addArguments(START_MAXIMISED);
      chromeOptions.addArguments(LANG_EN_GB);
      chromeOptions.addArguments(REMOTE_ALLOW_ORIGINS);
      chromeOptions.addArguments(IGNORE_CERTIFICATE_ERRORS);

      if (BaseTestConfiguration.getHeadless().equalsIgnoreCase("true")) {
         chromeOptions.addArguments("--headless=new");
      }

      TestLoggerHolder.getLogger().info("Initilising Chromedriver...");

      return createWebDriver(chromeOptions);
   }

   /**
    * Creates a Firefox webdriver instance
    * 
    * @return Firefox WebDriver instance
    */
   public static RemoteWebDriver createFirefoxDriver() {
      FirefoxOptions ffOptions = new FirefoxOptions();
      ffOptions.addArguments("--" + DISABLE_GPU);
      ffOptions.addArguments("--" + NO_SANDBOX);
      ffOptions.addArguments("--" + DISABLE_DEV_SHM_USAGE);
      ffOptions.addArguments("--" + START_MAXIMISED);
      ffOptions.addArguments("--" + LANG_EN_GB);
      ffOptions.addArguments("--" + IGNORE_CERTIFICATE_ERRORS);

      if (BaseTestConfiguration.getHeadless().equalsIgnoreCase("true")) {
         ffOptions.addArguments("--headless=new");
      }

      TestLoggerHolder.getLogger().info("Initilising FirefoxDriver...");
      return createWebDriver(ffOptions);
   }

   /**
    * Creates a Edge webdriver instance
    * 
    * @return Firefox WebDriver instance
    */
   public static RemoteWebDriver createEdgeDriver() {
      EdgeOptions edgeOptions = new EdgeOptions();
      edgeOptions.addArguments(DISABLE_GPU);
      edgeOptions.addArguments(NO_SANDBOX);
      edgeOptions.addArguments(DISABLE_DEV_SHM_USAGE);
      edgeOptions.addArguments(START_MAXIMISED);
      edgeOptions.addArguments(LANG_EN_GB);
      edgeOptions.addArguments(IGNORE_CERTIFICATE_ERRORS);

      if (BaseTestConfiguration.getHeadless().equalsIgnoreCase("true")) {
         edgeOptions.addArguments("--headless=new");
      }

      TestLoggerHolder.getLogger().info("Initilising EdgeDriver...");
      return createWebDriver(edgeOptions);
   }

   /**
    * Helper method to create WebDriver (Local or Selenium Grid)
    * 
    * @param capabilities
    * @return
    */
   private static RemoteWebDriver createWebDriver(Object options) {

      if (options instanceof ChromeOptions) {
         return new ChromeDriver((ChromeOptions) options);
      } else if (options instanceof FirefoxOptions) {
         return new FirefoxDriver((FirefoxOptions) options);
      } else if (options instanceof EdgeOptions) {
         return new EdgeDriver((EdgeOptions) options);
      } else {
         throw new IllegalArgumentException("Unsupported browser options");
      }
   }

   /**
    * Returns the current thread-local WebDriver instance.
    * <p>
    * If none exists, a new driver is initialized automatically.
    *
    * @return WebDriver instance for the current thread
    */
   public static RemoteWebDriver getCurrentDriver() {
      if (driverThreadLocal.get() == null) {
         TestLoggerHolder.getLogger().warn("Driver is not initialised - initialising now");
         initDriver();
      } else {
         try {
            driverThreadLocal.get().getTitle(); // Ensures the session is active
         } catch (Exception e) {
            TestLoggerHolder.getLogger().warn("WebDriver session is stale. Reinitialising...");
            closeDriver(); // Close Stale instance
            initDriver(); // Reinitialise
         }
      }
      return driverThreadLocal.get();
   }

   /**
    * Gets the current URL of the active browser session
    * 
    * @return The current URL as a string, or null if the driver is not initialised
    */
   public static String getCurrentDriverUrl() {
      return (driverThreadLocal.get() != null) ? driverThreadLocal.get().getCurrentUrl() : null;
   }

   /**
    * This method closes all other tabs other than the main one.
    */
   public static void closeOtherTabs() {
      // Get The main tab
      String mainWindow = driverThreadLocal.get().getWindowHandle();

      // Iterate and close all the other tabs
      for (String handle : driverThreadLocal.get().getWindowHandles()) {
         if (!handle.equals(mainWindow)) {
            driverThreadLocal.get().switchTo().window(handle);
            driverThreadLocal.get().close();
         }
      }

      driverThreadLocal.get().switchTo().window(mainWindow);
   }

   /**
    * Closes all browser windows for the current thread and cleans up the driver
    * instance.
    * <p>
    * Supports handling multiple tabs/windows.
    */
   public static void closeDriver() {
      TestLoggerHolder.getLogger().info("About to quit the driver");
      if (driverThreadLocal != null) {
         try {
            Set<String> windows = driverThreadLocal.get().getWindowHandles();
            if (windows.size() > 1) {
               String mainWindow = driverThreadLocal.get().getWindowHandle();
               for (String handle : windows) {
                  if (!handle.equals(mainWindow)) {
                     driverThreadLocal.get().switchTo().window(handle);
                     driverThreadLocal.get().close();
                  }
               }
               driverThreadLocal.get().switchTo().window(mainWindow);
               driverThreadLocal.get().close();
            } else {
               driverThreadLocal.get().quit();
            }
         } catch (Exception e) {
            TestLoggerHolder.getLogger().error("Error closing driver windows: " + e.getMessage());
         } finally {
            driverThreadLocal.get().quit();
            driverThreadLocal.remove();
         }
      } else {
         TestLoggerHolder.getLogger().info("driverThreadLocal is null, cannot close driver.");
      }
   }

   /**
    * This method refreshes the page
    */
   public static void refresh() {
      TestLoggerHolder.getLogger().info("Refresh the page");
      driverThreadLocal.get().navigate().refresh();
   }
}