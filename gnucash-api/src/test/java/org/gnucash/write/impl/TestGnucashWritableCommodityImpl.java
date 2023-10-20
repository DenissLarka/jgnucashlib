package org.gnucash.write.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.gnucash.ConstTest;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.CmdtyCurrNameSpace;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.write.GnucashWritableCommodity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashWritableCommodityImpl
{
    private GnucashWritableFileImpl gcshInFile = null;
    private String outFileGlobNameAbs = null;
    private File outFileGlob = null;

    // https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
    @SuppressWarnings("exports")
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashWritableCommodityImpl.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    InputStream gcshInFileStream = null;
    try 
    {
      gcshInFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME_IN);
    } 
    catch ( Exception exc ) 
    {
      System.err.println("Cannot generate input stream from resource");
      return;
    }
    
    try
    {
      gcshInFile = new GnucashWritableFileImpl(gcshInFileStream);
    }
    catch ( Exception exc )
    {
      System.err.println("Cannot parse GnuCash in-file");
      exc.printStackTrace();
    }
    
    URL outFileNameAbsURL = classLoader.getResource(ConstTest.GCSH_FILENAME_IN); // sic
//    System.err.println("Out file name (glob, URL): '" + outFileNameAbsURL + "'");
    outFileGlobNameAbs = outFileNameAbsURL.getPath();
    outFileGlobNameAbs = outFileGlobNameAbs.replace(ConstTest.GCSH_FILENAME_IN, ConstTest.GCSH_FILENAME_OUT);
//    System.err.println("Out file name (glob): '" + outFileGlobNameAbs + "'");
    outFileGlob = new File(outFileGlobNameAbs);
  }

  // -----------------------------------------------------------------

  @Test
  public void test01_1() throws Exception
  {
      GnucashWritableCommodity cmdty = gcshInFile.createWritableCommodity();
      cmdty.setQualifId(new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.NASDAQ, "SCAM"));
      cmdty.setName("Scam and Screw Corp.");
      
      File outFile = folder.newFile(ConstTest.GCSH_FILENAME_OUT);
//      System.err.println("Outfile for TestGnucashWritableCommodityImpl.test01_1: '" + outFile.getPath() + "'");
      outFile.delete(); // sic, the temp. file is already generated (empty), 
                        // and the GnuCash file writer does not like that.
      gcshInFile.writeFile(outFile);
      
      // copy file
      if ( outFileGlob.exists() )
	  FileUtils.delete(outFileGlob);
      FileUtils.copyFile(outFile, outFileGlob);
  }

  // -----------------------------------------------------------------

//  @Test
//  public void test01_2() throws Exception
//  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());
//
//      // Check if generated document is valid
//      // ::TODO: in fact, not even the input document is.
//      // Build document
//      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//      DocumentBuilder builder = factory.newDocumentBuilder(); 
//      Document document = builder.parse(outFileGlob);
//      System.err.println("xxxx XML parsed");
//
//      // https://howtodoinjava.com/java/xml/read-xml-dom-parser-example/
//      Schema schema = null;
//      String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
//      SchemaFactory factory1 = SchemaFactory.newInstance(language);
//      schema = factory1.newSchema(outFileGlob);
//
//      Validator validator = schema.newValidator();
//      DOMResult validResult = null; 
//      validator.validate(new DOMSource(document), validResult);
//      System.out.println("yyy: " + validResult);
//      // assertEquals(validResult);
//  }

  @Test
  public void test01_3() throws Exception
  {
      assertNotEquals(null, outFileGlob);
      assertEquals(true, outFileGlob.exists());

      // Build document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(outFileGlob);
//      System.err.println("xxxx XML parsed");

      // Normalize the XML structure
      document.getDocumentElement().normalize();
//      System.err.println("xxxx XML normalized");
      
      NodeList nList = document.getElementsByTagName("gnc:commodity");
      assertEquals(5, nList.getLength()); // <-- CAUTION: includes "CURRENCY:EUR" and "template:template"

      // Last (new) node
      Node lastNode = nList.item(nList.getLength() - 1);
      assertEquals(lastNode.getNodeType(), Node.ELEMENT_NODE);
      Element elt = (Element) lastNode;
      assertEquals("Scam and Screw Corp.", elt.getElementsByTagName("cmdty:name").item(0).getTextContent());
      assertEquals(CmdtyCurrNameSpace.Exchange.NASDAQ.toString(), elt.getElementsByTagName("cmdty:space").item(0).getTextContent());
      assertEquals("SCAM", elt.getElementsByTagName("cmdty:id").item(0).getTextContent());
  }

  // -----------------------------------------------------------------

  @Test
  public void test02_1() throws Exception
  {
      GnucashWritableCommodity cmdty1 = gcshInFile.createWritableCommodity();
      cmdty1.setQualifId(new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.NASDAQ, "SCAM"));
      cmdty1.setName("Scam and Screw Corp.");
      
      GnucashWritableCommodity cmdty2 = gcshInFile.createWritableCommodity();
      cmdty2.setQualifId(new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.XETRA, "TEUR"));
      cmdty2.setName("Total Überteuert AG");
      
      GnucashWritableCommodity cmdty3 = gcshInFile.createWritableCommodity();
      cmdty3.setQualifId(new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.EURONEXT, "FOUS"));
      cmdty3.setName("Ils sont fous ces dingos!");
      
      File outFile = folder.newFile(ConstTest.GCSH_FILENAME_OUT);
      // System.err.println("Outfile for TestGnucashWritableCommodityImpl.test02_1: '" + outFile.getPath() + "'");
      outFile.delete(); // sic, the temp. file is already generated (empty), 
                        // and the GnuCash file writer does not like that.
      gcshInFile.writeFile(outFile);
      
      // copy file
      if ( outFileGlob.exists() )
	  FileUtils.delete(outFileGlob);
      FileUtils.copyFile(outFile, outFileGlob);
  }
  
  @Test
  public void test02_3() throws Exception
  {
      assertNotEquals(null, outFileGlob);
      assertEquals(true, outFileGlob.exists());

      // Build document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(outFileGlob);
//      System.err.println("xxxx XML parsed");

      // Normalize the XML structure
      document.getDocumentElement().normalize();
//      System.err.println("xxxx XML normalized");
      
      NodeList nList = document.getElementsByTagName("gnc:commodity");
      assertEquals(7, nList.getLength()); // <-- CAUTION: includes "CURRENCY:EUR" and "template:template"

      // Last three nodes (the new ones)
      Node node = nList.item(nList.getLength() - 3);
      assertEquals(node.getNodeType(), Node.ELEMENT_NODE);
      Element elt = (Element) node;
      assertEquals("Scam and Screw Corp.", elt.getElementsByTagName("cmdty:name").item(0).getTextContent());
      assertEquals(CmdtyCurrNameSpace.Exchange.NASDAQ.toString(), elt.getElementsByTagName("cmdty:space").item(0).getTextContent());
      assertEquals("SCAM", elt.getElementsByTagName("cmdty:id").item(0).getTextContent());

      node = nList.item(nList.getLength() - 2);
      assertEquals(node.getNodeType(), Node.ELEMENT_NODE);
      elt = (Element) node;
      assertEquals("Total Überteuert AG", elt.getElementsByTagName("cmdty:name").item(0).getTextContent());
      assertEquals(CmdtyCurrNameSpace.Exchange.XETRA.toString(), elt.getElementsByTagName("cmdty:space").item(0).getTextContent());
      assertEquals("TEUR", elt.getElementsByTagName("cmdty:id").item(0).getTextContent());

      node = nList.item(nList.getLength() - 1);
      assertEquals(node.getNodeType(), Node.ELEMENT_NODE);
      elt = (Element) node;
      assertEquals("Ils sont fous ces dingos!", elt.getElementsByTagName("cmdty:name").item(0).getTextContent());
      assertEquals(CmdtyCurrNameSpace.Exchange.EURONEXT.toString(), elt.getElementsByTagName("cmdty:space").item(0).getTextContent());
      assertEquals("FOUS", elt.getElementsByTagName("cmdty:id").item(0).getTextContent());
  }

//  @AfterClass
//  public void after() throws Exception
//  {
//      FileUtils.delete(outFileGlob);
//  }

}
