package utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import constants.FilePathConstants;
import constants.IConstants;

/**
 * This class contains all the methods used when interacting with an xml file
 */
public class XMLDataHandler {

   private static boolean xmlIsValid = true;
   private static String xmlError = null;

   /* This array holds all the documents used to create and update an entity */
   private static List<Document> xmlDocs = new ArrayList<>();
   /* This array holds all the documents used to search for an entity */
   private static List<Document> xmlSearchDocs = new ArrayList<>();
   /* This array holds all the documents used in viewing for an entity */
   private static List<Document> xmlViewDocs = new ArrayList<>();

   private static final String UTF8 = "UTF-8";

   /**
    * Constructor required for Sonar
    */
   private XMLDataHandler() {
      throw new IllegalStateException("Utility class");
   }

   /**
    * Getter for getXmlError
    * 
    * @return xmlError the error in the xml
    */
   public static String getXmlError() {
      return xmlError;
   }

   /**
    * Getter for xmlIsValid
    * 
    * @return xmlIsValid
    */
   public static boolean isXmlIsValid() {
      return xmlIsValid;
   }

   /**
    * Setter for xmlError
    * 
    * @param xmlError Store the xml error
    */
   public static void setXmlError(String xmlError) {
      XMLDataHandler.xmlError = xmlError;
   }

   /**
    * Setter for xmlIsValid
    * 
    * @param xmlIsValid Store the xml validation boolean
    */
   public static void setXmlIsValid(boolean xmlIsValid) {
      XMLDataHandler.xmlIsValid = xmlIsValid;
   }

   /***********************************************************
    * Create Entity XML Docs
    ***********************************************************/

   /**
    * This method adds the (create) xml document to a list array
    * 
    * @param document The Xml to add
    */
   public static void setXmlDoc(Document document) {
      if (document == null) {
         xmlDocs = new ArrayList<>();
      } else {
         xmlDocs.add(document);
      }
   }

   /**
    * This method gets the list of (create entity) XML documents
    * 
    * @return list of XML documents
    */
   public static List<Document> getXmlDocs() {
      return xmlDocs;
   }

   /**
    * This gets the a specific (create entity) Document from the array
    * 
    * @param whichDoc The integer of the xml in the array to get
    * @return Document xml
    */
   public static Document getXMLDoc(int whichDoc) {
      return xmlDocs.get(whichDoc);
   }

   /**
    * This gets the last (create entity) Document index from the array
    * 
    * @return Document xml
    */
   public static int getLatestXMLDocIndex() {
      return xmlDocs.size() - 1;
   }

   /**
    * This gets the last Document from the array
    * 
    * @return Document xml
    */
   public static Document getLatestXMLDoc() {
      return xmlDocs.get(xmlDocs.size() - 1);
   }

   /**
    * This method creates an xml file from the string passed in
    * 
    * @param xmlStr the string in XML format
    */
   public static void createXML(String xmlStr) {
      try {
         xmlDocs.add(Jsoup.parse(xmlStr, "", Parser.xmlParser()));
      } catch (Exception e) {
         TestReport.logExceptionMessage(e);
      }
   }

   /**
    * This method creates a search xml file from the string passed in
    * 
    * @param xmlStr the string in XML format
    */
   public static void createSearchXML(String xmlStr) {
      try {
         xmlSearchDocs.add(Jsoup.parse(xmlStr, "", Parser.xmlParser()));
      } catch (Exception e) {
         TestReport.logExceptionMessage(e);
      }
   }

   /**
    * This method gets the value of the last tag with a given name
    * 
    * @param whichDoc the xmldocument index in the array
    * @param tagName  the last tag with the name
    * @return the elements text
    */
   public static String getLastElementValueWithTag(int whichDoc, String tagName) {
      Element ele = getXMLDoc(whichDoc).select(tagName).last();
      TestLoggerHolder.getLogger().debug("getLastElementValueWithTag=" + ele);
      if (ele == null) {
         return "";
      }
      return ele.text();
   }

   /**
    * This method gets the value of the last tag with a given name
    * 
    * @param whichDoc the xmldocument index in the array
    * @param tagName  the last tag with the name
    * @return the elements text
    */
   public static String getFirstElementValueWithTag(int whichDoc, String tagName) {
      Element ele = getXMLDoc(whichDoc).select(tagName).first();
      TestLoggerHolder.getLogger().debug("getFirstElementValueWithTag=" + ele);
      if (ele == null) {
         return "";
      }
      return ele.text();
   }

   /**
    * This method gets the index of xml doc in the array by matching on the record
    * id
    * 
    * @param searchRecordId The record id to find in the doc
    * @return recordIdx The record index
    */
   public static int getDocIndexByRecordId(String searchRecordId) {
      boolean bFound = false;
      int recordIdx = 0;
      for (int x = 0; x < xmlDocs.size(); x++) {
         String recordId = getFirstElementValueWithTag(x, "recordid");
         if (searchRecordId.equals(recordId)) {
            bFound = true;
            recordIdx = x;
            break;
         }
      }
      if (!bFound) {
         throw new NotImplementedException(
               "The record id " + searchRecordId + " cannot be found in any of the xmls for this test");
      } else {
         return recordIdx;
      }

   }

   /**
    * This method checks for an xml file in the array by matching on the record id
    * 
    * @param searchRecordId The record id to find in the doc
    * @return true if the file is listed
    */
   public static boolean isDocListedByRecordId(String searchRecordId) {
      boolean bFound = false;
      for (int x = 0; x < xmlDocs.size(); x++) {
         String recordId = getFirstElementValueWithTag(x, "recordid");
         if (searchRecordId.equals(recordId)) {
            bFound = true;
            break;
         }
      }
      return bFound;
   }

   /**
    * This method gets the all the element with a tag name of xx
    * 
    * @param whichDoc the xmldocument index in the array
    * @param tagName  the tag to find the elements
    * @return elements found
    */
   public static Elements getElementsByTag(int whichDoc, String tagName) {
      return getXMLDoc(whichDoc).getElementsByTag(tagName);
   }

   /**
    * This method gets an Element with a record with a doc
    * 
    * @param whicDoc     Which xml document to view
    * @param whichRecord Which record within the document to view
    * @param page        Which page element to look within
    * @param tag         which tag within page element
    * @return This return the element or null for the tag we are after
    */
   public static Element getTagWithinTag(int whicDoc, int whichRecord, String page, String tag) {
      Element tagElement = null;
      Elements pageElements = getElementsByTag(whicDoc, page);
      if (!pageElements.isEmpty()) {
         Elements tagElements = pageElements.get(whichRecord).select(tag);
         if (!tagElements.isEmpty()) {
            tagElement = tagElements.last();
         }
      }
      return tagElement;
   }

   /**
    * This method adds the Search Preliminary document xml to a list array
    * 
    * @param document The Xml to add
    */
   public static void setSearchXmlDoc(Document document) {
      if (document == null) {
         xmlSearchDocs = new ArrayList<>();
      } else {
         xmlSearchDocs.add(document);
      }
   }

   /**
    * This method gets the list of Search Preliminary document XMLs
    * 
    * @return list of XML documents
    */
   public static List<Document> getSearchXmlDocs() {
      return xmlSearchDocs;
   }

   /**
    * This gets the last Search Document from the array
    * 
    * @return Document xml
    */
   public static Document getLatestSearchXMLDoc() {
      return xmlSearchDocs.get(xmlSearchDocs.size() - 1);
   }

   /**
    * This method extracts the search details from an xml and adds this as a
    * document to the search docs array
    * 
    * @param filePath The filepath of the file containing the search elements
    * @throws IOException Read Exception
    */
   public static void storeSearchXMLPartAsDoc(String filePath) throws IOException {
      TestLoggerHolder.getLogger().info(FilePathConstants.DATAPATH + filePath);
      File xmlFile = new File(filePath);
      Document doc = Jsoup.parse(xmlFile, UTF8, "", Parser.xmlParser());
      Elements searchDetailElements = doc.getElementsByTag("searchdetails");
      Document searchDoc = Jsoup.parse(""); // Creates a blank doc
      searchDoc.appendChildren(searchDetailElements);
      setSearchXmlDoc(searchDoc);
   }

   /**
    * This method adds the Search Preliminary document xml to a list array
    * 
    * @param document The Xml to add
    */
   public static void setViewXmlDoc(Document document) {
      if (document == null) {
         xmlViewDocs = new ArrayList<>();
      } else {
         xmlViewDocs.add(document);
      }
   }

   /**
    * This method gets the list of Search Preliminary document XMLs
    * 
    * @return list of XML documents
    */
   public static List<Document> getViewXmlDocs() {
      return xmlViewDocs;
   }

   /**
    * This gets the last View Document from the array
    * 
    * @return Document xml
    */
   public static Document getLatestViewXMLDoc() {
      return xmlViewDocs.get(xmlViewDocs.size() - 1);
   }

   /**
    * This gets the a specific (view entity) Document from the array
    * 
    * @param whichDoc The integer of the xml in the array to get
    * @return Document xml
    */
   public static Document getXMLViewDoc(int whichDoc) {
      return xmlViewDocs.get(whichDoc);
   }

   /**
    * This method extracts the view details from an xml and adds this as a document
    * to the view docs array
    * 
    * @param filePath The filepath of the file containing the view elements
    * @throws IOException Read Exception
    */
   public static void storeViewXMLPartAsDoc(String filePath) throws IOException {
      TestLoggerHolder.getLogger().info(FilePathConstants.DATAPATH + filePath);
      File xmlFile = new File(filePath);
      Document doc = Jsoup.parse(xmlFile, UTF8, "", Parser.xmlParser());
      Elements viewFullRecordElements = doc.getElementsByTag("viewfullrecord");
      if (!viewFullRecordElements.isEmpty()) {
         Document viewDoc = Jsoup.parse(""); // Creates a blank doc
         if (!viewFullRecordElements.isEmpty()) {
            viewDoc.appendChildren(viewFullRecordElements);
         }
         setViewXmlDoc(viewDoc);
      } else {
         setViewXmlDoc(doc);
      }
   }

   /***********************************************************
    * Common Utlities
    ***********************************************************/

   /**
    * This method creates an xml file from the string passed in and returns it
    * 
    * @param xmlStr the string in XML format
    * @return the xml
    */
   public static Document createAndReturnXML(String xmlStr) {
      Document xml = null;
      try {
         xml = Jsoup.parse(xmlStr, "", Parser.xmlParser());
      } catch (Exception e) {
         TestReport.logExceptionMessage(e);
      }
      return xml;
   }

   /**
    * This method gets a file and parses it as an xml file
    * 
    * @param filePath the string in XML format
    * @throws IOException - Exception
    * @return Document
    */
   public static Document getDocFromFilePath(String filePath) throws IOException {
      File xmlFile = new File(filePath);
      TestLoggerHolder.getLogger().info(filePath);
      return Jsoup.parse(xmlFile, UTF8, "", Parser.xmlParser());
   }

   /**
    * This method gets elements within another element (pagename) with a given tag
    * name
    * 
    * @param pageElement The element to find other elements within
    * @param innerTag    the tag within the element with a given name
    * @return elements
    */
   public static Elements getInnerElementsWithName(Element pageElement, String innerTag) {
      return pageElement.getElementsByTag(innerTag);
   }

   /**
    * This method stores the xml scenario data for the create entity
    * 
    * @param filePath the name of the xml to store
    * @throws IOException if the xml cannot be accessed
    */
   public static void storeXML(String filePath) throws IOException {
      TestLoggerHolder.getLogger().info(FilePathConstants.DATAPATH + filePath);
      File xmlFile = new File(FilePathConstants.DATAPATH + filePath);
      setXmlDoc(Jsoup.parse(xmlFile, UTF8, "", Parser.xmlParser()));
   }

   /**
    * This method resets the local variables to null
    */
   public static void resetVariables() {
      setXmlIsValid(true);
      setXmlError(null);
      setXmlDoc(null);
      setSearchXmlDoc(null);
   }

   /**
    * Merge data from one xml file into another, using an xsl stylesheet
    * 
    * @param mainDoc base Document
    * @param subDoc  document containing data to be merged in
    * @param xslPath Path to an xsl stylesheet giving instructions about the merge
    * @return Document with new data merged in
    * @throws Exception General Exception
    */
   @SuppressWarnings("java:S2755") // We need to access a file from xsl, which Sonar thinks is a security risk
   public static Document mergeXml(Document mainDoc, Document subDoc, String xslPath) throws Exception {
      // Prepare xml for processing
      String mainText = mainDoc.getElementsByTag(IConstants.SCENARIO_DATA).toString();
      String subText = subDoc.getElementsByTag(IConstants.SCENARIO_DATA).toString();
      Source xmlSource = new StreamSource(new StringReader(mainText));
      Source xslSource = new StreamSource(xslPath);
      // Create Transformer
      TransformerFactory tf = TransformerFactory.newInstance();
      tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "file");
      Transformer t = tf.newTransformer(xslSource);
      // xslt 1.0 can only handle Files from within a stylesheet
      FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
            .asFileAttribute(PosixFilePermissions.fromString("rwx------"));
      File tempFile = Files.createTempFile("subXml-", ".xml", attr).toFile();
      tempFile.deleteOnExit();
      // Write substitution xml to File
      FileUtils.write(tempFile, subText, UTF8);
      // Pass the filename as a param to the xslt
      t.setParameter("fileName", tempFile.getPath());
      // Create a StringWriter so we can retrieve the result from the Stream
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      // Perform the Transform
      t.transform(xmlSource, result);
      // Get the raw xml
      String stringResult = result.getWriter().toString();
      // Parse the raw xml into a Document
      Document resultDoc = Jsoup.parse(stringResult, "", Parser.xmlParser());
      return (resultDoc);
   }

   /**
    * This method validate whether an xml file is valid or not against an xsd
    * 
    * @param xmlName The name of the xml to validate
    * @param xsdName The name of the xsd to use to validate
    */
   public static void validateXML(String xmlName, String xsdName) {

      String xsdPath = FilePathConstants.DATAPATH.concat(xsdName);
      String xmlPath = FilePathConstants.DATAPATH.concat(xmlName);

      try {
         SchemaFactory factory = SchemaFactory.newDefaultInstance();
         Schema schema = factory.newSchema(new File(xsdPath));
         Validator validator = schema.newValidator();
         validator.validate(new StreamSource(new File(xmlPath)));
      } catch (IOException e) {
         TestLoggerHolder.getLogger().info("{} {}", "Exception: ", e.getMessage());
         setXmlError(e.getMessage());
         xmlIsValid = false;
      } catch (SAXException e1) {
         TestLoggerHolder.getLogger().info("{} {}", "SAX Exception: ", e1.getMessage());
         setXmlError(e1.getMessage());
         xmlIsValid = false;
      }
   }

   /**
    * This method gets a list of files from a file list
    * 
    * @param pathOfFileofFiles This the path to the xml file containing the list of
    *                          files
    * @return List of files as strings
    * @throws IOException Exception if the file cannot be read
    */
   public static List<String> getFileFromFileOfFiles(String pathOfFileofFiles) throws IOException {
      TestLoggerHolder.getLogger().info("{} {}", "PathOfFileOfFiles=", pathOfFileofFiles);
      File file = new File(pathOfFileofFiles);
      Document xmlDoc = Jsoup.parse(file, UTF8, "", Parser.xmlParser());
      List<String> dataFileList = new ArrayList<>();
      Elements dataFile = xmlDoc.getElementsByTag("datafile");
      for (int x = 0; x < dataFile.size(); x++) {
         dataFileList.add(dataFile.get(x).text());
      }
      return dataFileList;
   }

   /**
    * This method takes an xml and validates it against and xsd
    * 
    * @param xsd       The XML Schema
    * @param xmlString The XML file represented as a string
    * @throws SAXException SAX Exception
    * @throws IOException  Read Write Exception
    */
   public static void validateXmlStringAgainstSchema(String xsd, String xmlString) throws SAXException, IOException {
      // Create a Schema Factory based on W3C
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      try {
         factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
         factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      } catch (SAXException e) {
         throw new AssertionError("Failed to configure XML Parsder for secure processing", e);
      }

      // Load the XSD
      Schema schema = factory.newSchema(new File(xsd));

      // Create a validator from the schema
      Validator validator = schema.newValidator();

      // Validate the xsd
      validator.validate((new StreamSource(new StringReader(xmlString))));
   }

   /**
    * This method gets the value of the last tag with a given name
    * 
    * @param tagName the XML tag to search for
    * @return the tag value, or null if not found
    */
   public static String getLastElementValueWithTag(String tagName) {
      Element ele = xmlDocs.get(getLatestXMLDocIndex()).select(tagName).last();
      TestLoggerHolder.getLogger().debug("getLastElementValueWithTag=" + ele);
      return ele.text();
   }

   /**
    * Gets the list of values for the given node name.
    *
    * @param nodeName the node name to extract values from
    * @return list of matching node values
    */
   public static Elements getNodesCalled(String nodeName) {
      return xmlDocs.get(getLatestXMLDocIndex()).getElementsByTag(nodeName);
   }

}
