package constants;

import java.io.File;

/**
 * This class contains all the File Path Constants
 */
public class FilePathConstants {

   /**
    * Constructor required for Sonar
    */
   private FilePathConstants() {
      throw new IllegalStateException("Utility class");
   }

   /** user.dir Constant **/
   public static final String USER_DIR = "user.dir";

   /** DATAPATH Constant **/
   public static final String DATAPATH = System.getProperty(USER_DIR).concat(File.separator).concat("src").concat(File.separator)
         .concat("test").concat(File.separator).concat("resources").concat(File.separator).concat("data").concat(File.separator);
   
}
