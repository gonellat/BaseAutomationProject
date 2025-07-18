package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

/**
 * Utility class containing pdf functions.
 */
public class PDFUtils {

   /**
    * Constructor required for Sonar
    */
   private PDFUtils() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * This method gets the text from a pdf file
    * 
    * @param pdfFile The pdf file
    * @return all text from the pdf file
    * @throws IOException An Exception if the file cannot be written to or out
    */
   public static String getTextFromPdf(File pdfFile) throws IOException {
      String textFromPdf = null;
      try (PDDocument document = PDDocument.load(pdfFile)) {
         document.getClass();
         if (!document.isEncrypted()) {
            PDFTextStripperByArea strip = new PDFTextStripperByArea();
            strip.setSortByPosition(true);
            PDFTextStripper stripper = new PDFTextStripper();
            textFromPdf = stripper.getText(document);
         }
      }
      return textFromPdf;
   }

   /**
    * This method gets the text from a pdf linked url
    * 
    * @param url The pdf url
    * @return all text from the pdf file
    * @throws IOException An Exception if the file cannot be written to or out
    */
   public static String getTextFromPdf(String url) throws IOException {
      URL pdfURL = URI.create(url).toURL();
      InputStream is = pdfURL.openStream();
      BufferedInputStream bis = new BufferedInputStream(is);
      PDDocument doc = PDDocument.load(bis);
      int pages = -doc.getNumberOfPages();
      TestLoggerHolder.getLogger().info("Number of PDF Pages = " + pages);
      PDFTextStripper strip = new PDFTextStripper();
      strip.setStartPage(1);
      strip.setEndPage(pages);
      String textFromPdf = strip.getText(doc);
      doc.close();
      return textFromPdf;
   }

   /**
    * Read text from the given document page by page into a list of Strings.
    * 
    * @param pdfDoc Document to parse
    * @return List of String page text
    * @throws IOException On a PDF problem
    */
   public static List<String> readPages(PDDocument pdfDoc) throws IOException {
      List<String> result = new ArrayList<>();
      int pages = pdfDoc.getNumberOfPages();
      pdfDoc.getClass();
      PDFTextStripper strip = new PDFTextStripper();
      for (int page = 1; page <= pages; page++) {
         strip.setStartPage(page);
         strip.setEndPage(page);
         String pageText = strip.getText(pdfDoc);
         result.add(pageText);
      }
      return result;
   }
}
