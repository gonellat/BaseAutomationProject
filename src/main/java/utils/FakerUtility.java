package utils;

import java.util.Locale;
import java.util.Random;

import net.datafaker.Faker;

/**
 * This class uses the Faker library to generate data
 */
public class FakerUtility {

   private FakerUtility() {
      throw new IllegalStateException("Utility class");
   }

   static Faker ukFaker = new Faker(Locale.UK, new Random());

   /**
    * This method returns a AddressLine1
    * 
    * @return String AddressLine1
    */
   public static String addressLine1() {
      TestLoggerHolder.getLogger().info(ukFaker.address().buildingNumber());
      return ukFaker.address().buildingNumber();
   }

   /**
    * This method returns a AddressLine2
    * 
    * @return String AddressLine2
    */
   public static String addressLine2() {
      TestLoggerHolder.getLogger().debug(ukFaker.address().streetName());
      return ukFaker.address().streetName();
   }

   /**
    * This method returns a AddressLine3
    * 
    * @return String AddressLine3
    */
   public static String addressLine3() {
      TestLoggerHolder.getLogger().debug(ukFaker.address().city());
      return ukFaker.address().city();
   }

   /**
    * This method returns a AddressLine4
    * 
    * @return String AddressLine4
    */
   public static String addressLine4() {
      TestLoggerHolder.getLogger().debug(ukFaker.address().state());
      return ukFaker.address().state();
   }

   /**
    * This method returns a company name
    * 
    * @return String company name
    */
   public static String company() {
      TestLoggerHolder.getLogger().debug(ukFaker.company().name());
      return ukFaker.company().name();
   }

   /**
    * This method returns a contact name
    * 
    * @return String contact name
    */
   public static String contact() {
      TestLoggerHolder.getLogger().debug(ukFaker.superhero().name());
      return ukFaker.superhero().name();
   }

   /**
    * This method returns a forename
    * 
    * @return String forename
    */
   public static String forename() {
      TestLoggerHolder.getLogger().debug(ukFaker.name().firstName());
      return ukFaker.name().firstName();
   }

   /**
    * This method returns a job title
    * 
    * @return String job title
    */
   public static String jobTitle() {
      TestLoggerHolder.getLogger().debug(ukFaker.job().title());
      return ukFaker.job().title();
   }

   /**
    * This method returns a postcode
    * 
    * @return String postcode
    */
   public static String postcode() {
      TestLoggerHolder.getLogger().debug(ukFaker.address().zipCode());
      return ukFaker.address().zipCode();
   }

   /**
    * This method returns a surname
    * 
    * @return String surname
    */
   public static String surname() {
      TestLoggerHolder.getLogger().debug(ukFaker.name().lastName());
      return ukFaker.name().lastName();
   }

   /**
    * This method returns a team name
    * 
    * @return String team
    */
   public static String team() {
      TestLoggerHolder.getLogger().debug(ukFaker.team().name());
      return ukFaker.team().name();
   }

   /**
    * This method returns a random string
    * 
    * @return String random string
    */
   public static String random() {
      TestLoggerHolder.getLogger().debug(ukFaker.random());
      return ukFaker.random().toString();
   }

   /**
    * This method returns a phone
    * 
    * @return String phone
    */
   public static String phoneNumber() {
      TestLoggerHolder.getLogger().debug(ukFaker.phoneNumber().phoneNumber());
      return ukFaker.phoneNumber().phoneNumber();
   }

   /**
    * This method returns a URL
    * 
    * @return String URL
    */
   public static String url() {
      TestLoggerHolder.getLogger().debug(ukFaker.company().url());
      return ukFaker.company().url();
   }

   /**
    * This method returns a email
    * 
    * @return String email
    */
   public static String email() {
      TestLoggerHolder.getLogger().debug(ukFaker.internet().emailAddress());
      return ukFaker.internet().emailAddress();
   }

   /**
    * This method returns a uk number plate
    * 
    * @return String vrm
    */
   public static String vrm() {
      TestLoggerHolder.getLogger().debug(ukFaker.regexify("[a-z]{2}[0-9]{2} [a-z]{3}"));
      return (ukFaker.regexify("[a-z]{2}[0-9]{2} [a-z]{3}"));
   }

}
