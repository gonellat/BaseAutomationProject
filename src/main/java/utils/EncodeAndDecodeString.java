package utils;

import org.apache.commons.codec.binary.Base64;

/**
 * This class has methods for encoding a string and decoding it
 */
public class EncodeAndDecodeString {

   // The constructor
   private EncodeAndDecodeString() {
      throw new IllegalStateException("EncodeAndDecodeString class");
   }

   /**
    * This method takes a base64 encoded string and returns the decoded value
    * 
    * @param theString The is the string to decode
    * @return decoded String
    */
   public static String decodeString(String theString) {
      return String.valueOf(Base64.decodeBase64(theString));
   }

   /**
    * This method restores the encoded string
    * 
    * @param theString This is the password encoded
    * @return The decoded password
    */
   public static String restoreEncodedString(String theString) {

      byte[] decodedBytes = Base64.decodeBase64(theString);
      return new String(decodedBytes);

   }

   /**
    * This method takes a string and returns the base 64 encoded version
    * 
    * @param theString The is the string to decode
    * @return the base 64 encoded string
    */
   public static byte[] encodeString(String theString) {
      return Base64.encodeBase64(theString.getBytes());
   }

   /**
    * This method calls the encodeString and outputs the value.
    * 
    * @param theString the String to encode
    */
   public static void outputTheEncodedString(String theString) {
      TestLoggerHolder.getLogger().info(String.valueOf(encodeString(theString)));
   }

   /**
    * This method calls the decodeString and outputs the value.
    * 
    * @param theString the String to decode
    */
   public static void outputTheDecodedString(String theString) {
      TestLoggerHolder.getLogger().info(decodeString(theString));
   }
}
