package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class has static methods for dealing with Json Messages
 */
public class JsonDataHandler {

   static final Logger LOG = LogManager.getLogger(JsonDataHandler.class);
   private static final ObjectMapper objectMapper = new ObjectMapper();
   
   /**
    * Constructor required for Sonar
    */
   private JsonDataHandler() {
      throw new IllegalStateException("Utility class");
   }

   static String str = "";

   /**
    * Convert a GSON Json string to pretty print version
    * 
    * @param jsonString the json
    * @return a pretty format of the json
    */
   public static String toPrettyFormat(String jsonString) {
      JsonParser parser = new JsonParser();
      JsonObject json = parser.parse(jsonString).getAsJsonObject();

      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      return gson.toJson(json);
   }
   
    /**
     * This method loads json file and returns it as a string
     * 
     * @param filePath path to json file 
     * @return JSON string String value of the json
     * @throws IOException general error
     */
   public static String readJsonFileAsString(String filePath) throws IOException {
      //Read the file content as a byte array
      byte[] jsonData = Files.readAllBytes(Paths.get(filePath));
      
      // convert the byte array to a String and return it
      LOG.info("JSON data:\n" + jsonData);
      return new String(jsonData);
   }
  
   
   /**
    * This method replaces the value of a specific key in a JSON string
    * 
    * @param jsonString the original JSON string
    * @param key they key whose value needs to be replaced
    * @param newValue the new value to set 
    * @return the updated JSON string
    * @throws IOException general error
    */
   public static String replaceValueInJson(String jsonString, String key, String newValue) throws IOException {
      //parse the JSON string into a JsonNoe
      JsonNode rootNode = objectMapper.readTree(jsonString);
      
      // traverse the JSON and replace the value
      replaceValue(rootNode, key, newValue);
      
      //Convert the updated JsonNode back to a JSON string
      return objectMapper.writeValueAsString(rootNode);
   }

   /**
    * This method recursively traverses the JSON and replaces the value of the specified key
    * 
    * @param node the current JsonNode
    * @param key the key whose value needs to be replaced 
    * @param newValue the new value to set
    */
   public static void replaceValue(JsonNode node, String key, String newValue) {
      if (node.isObject()) {
         ObjectNode objectNode = (ObjectNode) node;
         
         //If the current node has the key, replace its value
         if (objectNode.has(key)) {
            objectNode.put(key,  newValue);
         }
         
         // Recursively process all the fields in the object
         objectNode.fields().forEachRemaining(entry -> replaceValue(entry.getValue(), key, newValue));
      } else if (node.isArray()) {
         // Recursively process all fields in the object 
         for (JsonNode arrayElement : node) {
            replaceValue(arrayElement, key, newValue);
         }
      }
   }
   
   /**
    * This method creates a new GSON Json object from a string (i.e. a template file or response message)
    * @param jsonString The string object to create a json object from
    * @return jsonObject
    */
   public static JsonObject createJsonObject(String jsonString){
      JsonObject jsonObject=new JsonObject();
      JsonParser jsonParser = new JsonParser();
      if ((jsonString != null) && !(jsonString.isEmpty())) {
         jsonObject = (JsonObject)jsonParser.parse(jsonString);
      }
      return jsonObject;
   }
   
}
