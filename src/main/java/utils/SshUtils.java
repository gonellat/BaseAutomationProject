package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.NotImplementedException;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import constants.DateTimeFormatConstants;

/**
 * SShUtils is a java program that connects to a remote server via ssh Executes
 * a series of commands, and interacts with the .ksh script by providing inputs
 * and capturing outputs
 */

public class SshUtils {

   private static final String HOST_SERVER = "";
   private static final String SSH_USER = "";
   private static final String SSH_USER_PASSWORD = "";
   private static final String REPORT_USERNAME = BaseTestConfiguration.getReportUsername();
   private static final String REPORT_USER_PASSWORD = BaseTestConfiguration.getReportUserPassword();
   private static final String FILES02 = "";

   private static final int PORT = 22;
   private static final String PASSWORD = "Password : ";

   /**
    * Constructor required for Sonar
    */
   private SshUtils() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * This method creates the ssh connection, command and execution. It interacts
    * with the mi_reports.ksh script by providing inputs
    * 
    * @param reportType  - The report type "SERCH_USERS"
    * @param reportLevel - Level the report is required for "National"
    */
   public static void executeMiScripts(String reportType, String reportLevel) {
      Session session = null;
      ChannelShell channel = null;

      try {

         if (BaseTestConfiguration.getEnv().equals("REP2") || BaseTestConfiguration.getEnv().equals("REP3")) {
            session = initializeSession(HOST_SERVER, SSH_USER, SSH_USER_PASSWORD, PORT);
            channel = initializeChannel(session);
            executeScript(channel, reportType, reportLevel);
         } else {
            throw new NotImplementedException("Test environment is not REP2 or REP3");
         }
      } catch (Exception e) {
         TestLoggerHolder.getLogger().info("Unexpected exception", e);
      } finally {
         if (channel != null && channel.isConnected()) {
            channel.disconnect();
            TestLoggerHolder.getLogger().info("Shell channel disconnected...");
         }
         if (session != null && session.isConnected()) {
            session.disconnect();
            TestLoggerHolder.getLogger().info("Ssh session disconnected...");
         }
      }

   }

   /**
    * initialises and connects to ssh session
    * 
    * @param host     - he remote host address
    * @param user     - username for ssh connection
    * @param password - password for ssh connection
    * @param port     - port for ssh connection
    * @return - The connected ssh session
    * @throws JSchException - If error occurs while connection to the session
    */
   private static Session initializeSession(String host, String user, String password, int port) throws JSchException {
      JSch jsch = new JSch();

      Session session = jsch.getSession(user, host, port);
      session.setConfig("PreferredAuthentications", "publickey,password");
      session.setConfig("StrictHostKeyChecking", "no");
      session.setPassword(password);

      TestLoggerHolder.getLogger().info("Connecting session");
      session.connect();

      // Check connection
      if (session.isConnected()) {
         TestLoggerHolder.getLogger().info("SSH session has connected successfully");
      } else {
         TestLoggerHolder.getLogger().info("Failed to connect to SSH session");
      }
      return session;
   }

   /**
    * Initialises and connects the shell channel.
    * 
    * @param session - The connected SSh session
    * @return - The connected shell channel
    * @throws JSchException - If an error occurs while connecting to the channel.
    */
   private static ChannelShell initializeChannel(Session session) throws JSchException {
      ChannelShell channel = (ChannelShell) session.openChannel("shell");
      channel.setPty(true);
      channel.connect();

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      channel = (ChannelShell) session.openChannel("shell");
      channel.setPty(true);
      channel.setOutputStream(outputStream);
      channel.connect();

      if (channel.isConnected()) {
         TestLoggerHolder.getLogger().info("Shell channel connected successfully");
      } else {
         TestLoggerHolder.getLogger().info("Failed to connect to shell channel");
      }
      return channel;
   }

   /**
    * This method executes the commands to generate the search results
    * 
    * @param channel     - The ssh shell
    * @param reportType  - The report type "SERCH_USERS"
    * @param reportLevel - Level the report is required for "National"
    * @throws Exception - If an error occurs
    */
   public static void executeScript(ChannelShell channel, String reportType, String reportLevel) throws Exception {
      InputStream in = channel.getInputStream();
      OutputStream out = channel.getOutputStream();

      if (BaseTestConfiguration.getEnv().equals("REP2")) {

         sendCommand(out, FILES02);
         sendCommand(out, "cd /oracle_oflow/ABC/MI_REPORTS");
         TestLoggerHolder.getLogger().info("Executing MI script");
         sendCommand(out, "./mi_reports.ksh");

         provideInput(in, out, "Username : ", REPORT_USERNAME);
         provideInput(in, out, PASSWORD, REPORT_USER_PASSWORD);
         provideInput(in, out, "Please Enter the name of the Report Type : ", reportType);

         provideInput(in, out, "Please Enter a value for parameter 1. :", reportLevel);
         provideInput(in, out, "Please Enter a value for parameter 2. :",
               DateUtils.getCurrentDateTime(DateTimeFormatConstants.DDMMMYYYYDASHES));
         provideInput(in, out, "Please Enter a value for parameter 3. :",
               DateUtils.getCurrentDateTime(DateTimeFormatConstants.DDMMMYYYYDASHES));

         waitForOutput(in, "Running the report procedure process_mi_report");
         waitForOutput(in, "Report Ran successfully");
      }
   }

   /**
    * This method sends a command to the remote shell
    * 
    * @param out     The output stream of the SSH channel.
    * @param command The command to be sent
    * @throws IOException
    * @throws InterruptedException
    */
   private static void sendCommand(OutputStream out, String command) throws IOException {
      out.write((command + "\n").getBytes());
      out.flush();
   }

   /**
    * This method provides input to the script when a specific prompt is detected.
    * 
    * @param in     The input stream of the SSH channel.
    * @param out    The output stream of the SSH channel
    * @param prompt The Prompt to wait for (e.g., Username : ").
    * @param input  The input to send in response to the prompt.
    * @throws IOException Read/Write Exception
    */
   private static void provideInput(InputStream in, OutputStream out, String prompt, String input) throws IOException {
      // Read output of the command
      byte[] buffer = new byte[1024];
      StringBuilder output = new StringBuilder();
      TestLoggerHolder.getLogger().info("Waiting for prompt: {}", prompt);
      Integer x = 0;
      while ((true)) {
         x++;
         while (in.available() > 0) {
            int i = in.read(buffer, 0, 1024);
            if (i < 0)
               break;
            String chunk = new String(buffer, 0, i);
            output.append(chunk);
         }
         if (x == 30) {
            TestLoggerHolder.getLogger().info("Prompt text received : {}", output);
            throw new IOException("Stuck for 30 seconds waiting on prompt.");
         }

         if (output.toString().contains(prompt)) {
            out.write((input + "\n").getBytes());
            out.flush();
            TestLoggerHolder.getLogger().info("Sent Text: {}", input);
            return;
         }
         try {
            Thread.sleep(1000);
         } catch (InterruptedException ex) {
            TestLoggerHolder.getLogger().info("{} {}", "Error: ", ex);
         }
      }
   }

   /**
    * This Method waits for an message to appear in the output
    * 
    * @param in              The input stream of the SSH channel.
    * @param expectedMessage The message to wait for (e.g., "Report Ran
    *                        successfully").
    * @throws IOException Read/Write Exception
    */
   private static void waitForOutput(InputStream in, String expectedMessage) throws IOException {
      // Read output of the command
      byte[] buffer = new byte[1024];
      StringBuilder output = new StringBuilder();

      while (true) {
         while (in.available() > 0) {
            int i = in.read(buffer, 0, 1024);
            if (i < 0)
               break;
            String chunk = new String(buffer, 0, i);
            output.append(chunk);

            if (output.toString().contains(expectedMessage)) {
               TestLoggerHolder.getLogger().info("{} {}", "Expected Message:- ", expectedMessage);
               return;
            }
         }
      }
   }

}
