package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Utility class containing database functions.
 */
public class DbUtils {

   /**
    * Constructor required for Sonar
    */
   private DbUtils() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * This method calls the correct db connection method dependent on the db type
    * 
    * @param db Either File or Abcd
    * @return The DB Connection
    * @throws SQLException The SQL Exception if we can't connect
    */
   public static Connection getDbConnection(String db) throws SQLException {
      if (db.equals("File")) {
         return getDbFileConnection();
      } else {
         throw new NotImplementedException("The db " + db + " has not been implemented");
      }
   }

   /**
    * Get the connection of the db
    * 
    * @return
    */
   private static Connection getDbFileConnection() {
      return null;
   }

   /**
    * This method gets a file content into a string and returns it
    * 
    * @param fileName The filename of the file that has the required sql
    * @return The SQL as string
    * @throws IOException Read Exception
    */
   public static String getSql(String fileName) throws IOException {
      String filePath = fileName;
      String sqlContent = Files.readString(Paths.get(filePath));
      TestLoggerHolder.getLogger().info("sqlContent: " + sqlContent);
      return sqlContent;
   }

   /**
    * This method runs and sql query against the given database
    * 
    * @param db  This is the database we want to connect to and execute the sql -
    *            either FILE, ABCD or SEARCH
    * @param sql The SQL for select queries
    * @return The index status
    */
   public static List<Map<String, Object>> executeSQL(String db, String sql) {
      TestLoggerHolder.getLogger().info(sql);
      ResultSet resultSet = null;
      List<Map<String, Object>> rows = new ArrayList<>();

      try (Connection connection = getDbConnection(db);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
         resultSet = preparedStatement.executeQuery();

         ResultSetMetaData metaData = resultSet.getMetaData();
         int columnCount = metaData.getColumnCount();

         while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
               row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            rows.add(row);
         }
      } catch (SQLException e) {
         throw new io.cucumber.core.exception.CucumberException("Failed to run sql: " + e.getMessage());
      }
      return rows;
   }

   /**
    * This method runs an update/delete/insert statement against the given
    * database.
    * 
    * @param db  This is the database we want to connect to and execute the sql -
    *            either FILE, ABCD or SEARCH
    * @param sql The SQL for update queries
    * @return integer of the number of rows affected
    */
   public static int executeUpdate(String db, String sql) {
      TestLoggerHolder.getLogger().info(sql);
      int rowsAffected = 0;
      try (Connection connection = getDbConnection(db);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
         rowsAffected = preparedStatement.executeUpdate();
         TestLoggerHolder.getLogger().info("preparedStatement has run");
      } catch (SQLException e) {
         throw new io.cucumber.core.exception.CucumberException("Failed to run sql: " + e.getMessage());
      }
      return rowsAffected;
   }
}
