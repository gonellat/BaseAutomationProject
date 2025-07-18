package utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Shared downloader for Chrome, Firefox, and Edge WebDrivers.
 * <p>
 * Supports:
 * <ul>
 * <li>Browser version detection (Windows/Linux)</li>
 * <li>Online download via Chrome for Testing (CfT) JSON or fallback URL</li>
 * <li>Offline fallback to local binary under {@code drivers/}</li>
 * <li>Automatic system property configuration</li>
 * </ul>
 */
public class DriverDownloader {

   private static final String CFT_JSON_URL = "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json";

   /**
    * Resolves and configures the appropriate WebDriver for the specified browser.
    * <p>
    * Detects the installed browser version (locally or on CI), determines the
    * appropriate driver, downloads and extracts it (online mode), or falls back to
    * a locally bundled driver (offline mode). Sets the corresponding system
    * property:
    * <ul>
    * <li>{@code webdriver.chrome.driver}</li>
    * <li>{@code webdriver.gecko.driver}</li>
    * <li>{@code webdriver.edge.driver}</li>
    * </ul>
    *
    * @param browserName One of: {@code "chrome"}, {@code "firefox"}, or
    *                    {@code "edge"}
    * @return The absolute path to the resolved WebDriver binary
    * @throws RuntimeException if detection or download fails and no local fallback
    *                          is available
    */
   public static String download(String browserName) {
      try {
         String version = detectInstalledVersion(browserName);
         return setupDriverForVersion(browserName, version);
      } catch (Exception e) {
         throw new RuntimeException("Error setting up WebDriver for " + browserName + ": " + e.getMessage(), e);
      }
   }

   /**
    * Downloads, extracts, and configures the appropriate WebDriver for the given
    * browser and version. If the driver cannot be fetched online, falls back to a
    * local copy in the {@code drivers/} directory.
    *
    * @param browser One of: {@code "chrome"}, {@code "firefox"}, or {@code "edge"}
    * @param version Exact browser version string (e.g. {@code "138.0.7204.98"})
    * @return The absolute path to the WebDriver binary
    * @throws RuntimeException if the download fails and no local fallback is found
    */
   public static String setupDriverForVersion(String browser, String version) {
      String platform = detectPlatform();
      Path driverDir = Paths.get("drivers", browser + "driver-" + version);

      try {
         trustAllCerts();
         String downloadUrl = null;

         // Chrome download
         if (browser.equals("chrome")) {
            downloadUrl = getUrlFromCfT(version, platform, browser);
            if (downloadUrl == null) {
               downloadUrl = String.format(
                     "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/%s/%s/chromedriver-%s.zip",
                     version, platform, platform);
            }

            // Edge download
         } else if (browser.equals("edge")) {
            String edgeZipPlatform = platform.equals("win64") ? "edgedriver_win64"
                  : platform.equals("win32") ? "edgedriver_win32"
                        : platform.equals("linux64") ? "edgedriver_linux64"
                              : platform.equals("mac-x64") ? "edgedriver_mac64" : null;

            if (edgeZipPlatform == null) {
               throw new RuntimeException("Unsupported platform for Edge: " + platform);
            }

            downloadUrl = String.format("https://msedgedriver.azureedge.net/%s/%s.zip", version, edgeZipPlatform);

            // Firefox download (always use latest geckodriver release)
         } else if (browser.equals("firefox")) {
            String geckoPlatform = platform.equals("win64") ? "win64"
                  : platform.equals("linux64") ? "linux64" : platform.equals("mac-x64") ? "macos" : null;

            if (geckoPlatform == null) {
               throw new RuntimeException("Unsupported platform for Firefox: " + platform);
            }

            downloadUrl = String.format(
                  "https://github.com/mozilla/geckodriver/releases/latest/download/geckodriver-latest-%s.zip",
                  geckoPlatform);
         } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
         }

         // Download + extract driver
         Path driverBinary = downloadAndExtractDriver(downloadUrl, driverDir);
         System.setProperty("webdriver." + browser + ".driver", driverBinary.toAbsolutePath().toString());
         return driverBinary.toAbsolutePath().toString();

      } catch (Exception e) {
         // Offline fallback
         String driverExeName = switch (browser) {
         case "chrome" -> "chromedriver";
         case "firefox" -> "geckodriver";
         case "edge" -> "msedgedriver";
         default -> throw new IllegalArgumentException("Unknown browser: " + browser);
         };
         Path fallback = Paths.get("drivers", driverExeName + (platform.startsWith("win") ? ".exe" : ""));
         if (Files.exists(fallback)) {
            System.setProperty("webdriver." + browser + ".driver", fallback.toAbsolutePath().toString());
            return fallback.toAbsolutePath().toString();
         }

         throw new RuntimeException(
               "Offline mode: local " + browser + "driver not found at " + fallback.toAbsolutePath(), e);
      }
   }

   /**
    * Attempts to locate the download URL for the given Chrome version and platform
    * in Google's Chrome for Testing (CfT) last-known-good versions JSON index.
    *
    * @param version  Full browser version (e.g., {@code "138.0.7204.98"})
    * @param platform CfT platform identifier (e.g., {@code "win64"},
    *                 {@code "linux64"})
    * @param browser  Browser name (must be {@code "chrome"} or this method will
    *                 return {@code null})
    * @return A valid ChromeDriver ZIP URL, or {@code null} if not found in the
    *         index
    * @throws IOException if the JSON index cannot be retrieved or parsed
    */
   private static String getUrlFromCfT(String version, String platform, String browser) throws IOException {
      if (!browser.equals("chrome"))
         return null;

      URL url = URI.create(CFT_JSON_URL).toURL();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("User-Agent", "Mozilla/5.0");

      int status = conn.getResponseCode();
      if (status != 200)
         return null;

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
         String json = reader.lines().reduce("", (a, b) -> a + b);
         String search = "\"version\":\"" + version + "\"";
         int versionIndex = json.indexOf(search);
         if (versionIndex == -1)
            return null;

         String needle = "\"platform\":\"" + platform + "\",\"url\":\"";
         int urlStart = json.indexOf(needle, versionIndex);
         if (urlStart == -1)
            return null;

         int start = json.indexOf("https://", urlStart);
         int end = json.indexOf("\"", start);
         return json.substring(start, end);
      }
   }

   /**
    * Downloads and extracts a WebDriver ZIP archive, and locates the actual driver
    * binary (e.g., {@code chromedriver.exe}, {@code geckodriver}, etc.).
    *
    * @param zipUrl    The URL to download the ZIP file from
    * @param outputDir The target directory for extraction
    * @return The absolute path to the extracted WebDriver binary
    * @throws IOException if the ZIP file cannot be downloaded or does not contain
    *                     a valid binary
    */
   private static Path downloadAndExtractDriver(String zipUrl, Path outputDir) throws IOException {
      Files.createDirectories(outputDir);
      Path extractedDriver = null;

      try (InputStream in = URI.create(zipUrl).toURL().openStream(); ZipInputStream zipIn = new ZipInputStream(in)) {

         ZipEntry entry;
         while ((entry = zipIn.getNextEntry()) != null) {
            Path filePath = outputDir.resolve(entry.getName());
            if (!entry.isDirectory()) {
               Files.createDirectories(filePath.getParent());
               try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath))) {
                  byte[] buffer = new byte[4096];
                  int read;
                  while ((read = zipIn.read(buffer)) != -1) {
                     bos.write(buffer, 0, read);
                  }
               }
               if (filePath.getFileName().toString().startsWith("chromedriver")
                     || filePath.getFileName().toString().startsWith("geckodriver")
                     || filePath.getFileName().toString().startsWith("msedgedriver")) {
                  extractedDriver = filePath;
               }
            }
            zipIn.closeEntry();
         }
      }

      if (extractedDriver == null || !Files.exists(extractedDriver)) {
         throw new IOException("WebDriver binary not found in extracted ZIP");
      }

      extractedDriver.toFile().setExecutable(true);
      return extractedDriver;
   }

   /**
    * Detects the currently installed version of the specified browser.
    * <ul>
    * <li>On Windows: uses registry queries</li>
    * <li>On Linux/macOS: uses known shell commands</li>
    * </ul>
    *
    * @param browser One of: {@code "chrome"}, {@code "firefox"}, or {@code "edge"}
    * @return The full browser version string (e.g., {@code "138.0.7204.98"})
    * @throws RuntimeException if the version cannot be detected
    */
   private static String detectInstalledVersion(String browser) {
      String os = System.getProperty("os.name").toLowerCase();

      if (os.contains("win")) {
         return detectVersionWindows(browser);
      } else {
         return detectVersionShell(browser);
      }
   }

   /**
    * Detects the installed browser version on Windows. Supports Chrome, Edge, and
    * Firefox.
    *
    * @param browser The browser name: "chrome", "firefox", or "edge"
    * @return The detected version string (e.g., "140.0.4")
    * @throws RuntimeException if the version cannot be detected
    */
   private static String detectVersionWindows(String browser) {
      String[] regPaths;

      switch (browser.toLowerCase()) {
      case "chrome":
         regPaths = new String[] { "reg query \"HKCU\\Software\\Google\\Chrome\\BLBeacon\" /v version",
               "reg query \"HKLM\\Software\\Google\\Chrome\\BLBeacon\" /v version" };
         break;

      case "edge":
         regPaths = new String[] { "reg query \"HKCU\\Software\\Microsoft\\Edge\\BLBeacon\" /v version",
               "reg query \"HKLM\\Software\\Microsoft\\Edge\\BLBeacon\" /v version" };
         break;

      case "firefox":
         regPaths = new String[] { "reg query \"HKCU\\Software\\Mozilla\\Mozilla Firefox\" /v CurrentVersion",
               "reg query \"HKLM\\Software\\Mozilla\\Mozilla Firefox\" /v CurrentVersion",
               "reg query \"HKLM\\Software\\Wow6432Node\\Mozilla\\Mozilla Firefox\" /v CurrentVersion" };
         break;

      default:
         throw new IllegalArgumentException("Unsupported browser: " + browser);
      }

      for (String regQuery : regPaths) {
         try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", regQuery);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
               if (line.toLowerCase().contains("version")) {
                  return line.replaceAll(".*\\s+REG_SZ\\s+", "").trim();
               }
            }
         } catch (Exception ignored) {
            // try next registry path
         }
      }

      // Fallback for Firefox: read from application.ini
      if (browser.equalsIgnoreCase("firefox")) {
         return detectFirefoxVersionFromIni();
      }

      throw new RuntimeException(browser + " version not detected on Windows.");
   }

   /**
    * Detects the installed Firefox version by reading application.ini from the
    * default install location on Windows.
    *
    * @return the Firefox version string (e.g. "140.0.4")
    */
   private static String detectFirefoxVersionFromIni() {
      String iniPath = "C:\\Program Files\\Mozilla Firefox\\application.ini";
      Path path = Paths.get(iniPath);

      if (!Files.exists(path)) {
         throw new RuntimeException("Firefox application.ini not found at: " + iniPath);
      }

      try (BufferedReader reader = Files.newBufferedReader(path)) {
         String line;
         while ((line = reader.readLine()) != null) {
            if (line.startsWith("Version=")) {
               return line.replace("Version=", "").trim();
            }
         }
      } catch (IOException e) {
         throw new RuntimeException("Failed to read Firefox application.ini", e);
      }

      throw new RuntimeException("Version not found in Firefox application.ini");
   }

   /**
    * Linux/macOS browser version detection using standard shell commands.
    *
    * @param browser The browser name (chrome, firefox, or edge)
    * @return The detected version string
    * @throws RuntimeException if no known command returns a version
    */
   private static String detectVersionShell(String browser) {
      String[] commands = switch (browser) {
      case "chrome" -> new String[] { "google-chrome --version", "google-chrome-stable --version" };
      case "firefox" -> new String[] { "firefox --version" };
      case "edge" -> new String[] { "microsoft-edge --version", "edge --version" };
      default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
      };

      for (String cmd : commands) {
         try {
            Process process = new ProcessBuilder("bash", "-c", cmd).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            process.waitFor();

            if (output != null && output.toLowerCase().contains(browser)) {
               return output.replaceAll("[^0-9.]", "").trim();
            }
         } catch (Exception ignored) {
        	 TestLoggerHolder.getLogger().info("Ignore");
         }
      }

      throw new RuntimeException("Could not detect " + browser + " version using shell.");
   }

   /**
    * Detects the current OS and architecture, and returns the appropriate platform
    * identifier used by Chrome for Testing (e.g., {@code "win64"},
    * {@code "linux64"}).
    *
    * @return Platform string compatible with CfT ZIP structures
    */
   private static String detectPlatform() {
      String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      String arch = System.getProperty("os.arch").contains("64") ? "64" : "32";
      if (os.contains("win"))
         return "win" + arch;
      if (os.contains("mac"))
         return "mac-x64";
      if (os.contains("linux"))
         return "linux" + arch;
      throw new RuntimeException("Unsupported OS: " + os);
   }

   /**
    * (DEV ONLY) Disables all SSL certificate validation globally. Useful for
    * environments with SSL interception or untrusted certificates.
    * <p>
    * Not recommended for production use.
    */
   private static void trustAllCerts() {
      try {
         TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
               return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
               TestLoggerHolder.getLogger().info("Not Required");
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                TestLoggerHolder.getLogger().info("Not Required");
            }
         } };
         SSLContext sc = SSLContext.getInstance("TLS");
         sc.init(null, trustAll, new SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
         HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
      } catch (Exception ignored) {
         TestLoggerHolder.getLogger().info("Ignored");
      }
   }
}
