package utils;

/**
 * Utility class to retrieve or generate encoded passwords for secure
 * transmission or storage.
 */
public class GetEncodedPassword {

   /**
    * Default constructor for GetEncodedPassword.
    */
   public GetEncodedPassword() {
      // default constructor
   }

   /**
    * Method to get an encoded password (Change the string as required)
    * 
    * @param args The Sting argument required to run this java class
    */
   public static void main(String[] args) {
      EncodeAndDecodeString.outputTheEncodedString("cGFzc3dvcmQ=");
   }

}
