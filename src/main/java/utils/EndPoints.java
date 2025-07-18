package utils;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * This class has the calls for API methods PUT/GET/POST/DELETE
 */
public class EndPoints {

   private static final String LINE_BREAK = "====================";

   /**
    * This method uses RESTAssured to send a "POST" request and return the response
    * 
    * @param uri  The uri of API to call
    * @param body The Payload of the post request
    * @return the post request response
    */
   public Response postRequest(String uri, String body) {
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info("Starting Post");
      TestLoggerHolder.getLogger().info(LINE_BREAK);

      return given().relaxedHTTPSValidation().header("Content-Type", ContentType.XML).log().params().body(body).post(uri);
   }

   /**
    * This method uses RESTAssured to send a "GET" a request and return the response
    * 
    * @param uri the uri of the api
    * @return the response form the get request
    */
   public Response getRequest(String uri) {
      TestLoggerHolder.getLogger().info(LINE_BREAK);
      TestLoggerHolder.getLogger().info("Starting Get");
      TestLoggerHolder.getLogger().info(LINE_BREAK);

      Response response = null;
      TestLoggerHolder.getLogger().info("uriName: " + uri);

      try {
         response = given().relaxedHTTPSValidation().header("Content-Type", ContentType.XML).get(uri);
      } catch (Exception e) {
         TestLoggerHolder.getLogger().info(e);
      }

      if (response != null) {
         String xmlString = response.asString();
         TestLoggerHolder.getLogger().info(xmlString);
      } else {
         throw new NullPointerException("Response is null");
      }

      return response;
   }
}
