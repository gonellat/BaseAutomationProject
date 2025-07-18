package utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * This class is for updating image files for testing purposes
 */
public class ImageTextOverlay {

   /**
    * Constructor
    */
   private ImageTextOverlay() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * This method adds a text overlay to an existing image and saves it to the
    * output path
    * 
    * @param inputPath  Path to image
    * @param outputPath Path to new image with text (date time)
    * @throws IOException General exception
    */
   public static void addTextToImage(String inputPath, String outputPath) throws IOException {
      BufferedImage originalImage = ImageIO.read(new File(inputPath));

      BufferedImage modifiedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
            originalImage.getType());

      Graphics2D g2d = modifiedImage.createGraphics();
      g2d.drawImage(originalImage, 0, 0, null);

      g2d.setColor(Color.WHITE);
      g2d.setFont(new Font("Arial", Font.BOLD, 24));

      String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
      String uniqueText = "Test Run: " + timeStamp;

      g2d.setColor(Color.BLACK);
      g2d.drawString(uniqueText, 21, 51);

      g2d.setColor(Color.WHITE);
      g2d.drawString(uniqueText, 20, 50);

      g2d.dispose();

      ImageIO.write(modifiedImage, "jpg", new File(outputPath));
      TestLoggerHolder.getLogger().info("Image modified with text overlay and saved to " + outputPath);
   }

}