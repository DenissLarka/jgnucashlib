package org.gnucash.write.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.gnucash.ConstTest;
import org.gnucash.read.GnucashVendor;
import org.gnucash.write.GnucashWritableVendor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestGnucashWritableVendorImpl
{
    private GnucashWritableFileImpl gcshInFile = null;
    private String outFileGlobNameAbs = null;
    private File outFileGlob = null;


  @BeforeMethod
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
      GnucashWritableVendor vend = gcshInFile.createWritableVendor();
      vend.setNumber(GnucashVendor.getNewNumber(vend));
      vend.setName("Norma Jean Baker");
      
      File outFile = Files.createTempFile("gc", ConstTest.GCSH_FILENAME_OUT).toFile();
      // System.err.println("Outfile for TestGnucashWritableVendorImpl.test01_1: '" + outFile.getPath() + "'");
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
      Assert.assertNotEquals(null, outFileGlob);
      Assert.assertEquals(true, outFileGlob.exists());

      // Build document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(outFileGlob);
//      System.err.println("xxxx XML parsed");

      // Normalize the XML structure
      document.getDocumentElement().normalize();
//      System.err.println("xxxx XML normalized");
      
      NodeList nList = document.getElementsByTagName("gnc:GncVendor");
      Assert.assertEquals(4, nList.getLength());

      // Last (new) node
      Node lastNode = nList.item(nList.getLength() - 1);
      Assert.assertEquals(lastNode.getNodeType(), Node.ELEMENT_NODE);
      Element elt = (Element) lastNode;
      Assert.assertEquals("Norma Jean Baker", elt.getElementsByTagName("vendor:name").item(0).getTextContent());
      Assert.assertEquals("000004", elt.getElementsByTagName("vendor:id").item(0).getTextContent());
  }

  // -----------------------------------------------------------------

  @Test
  public void test02_1() throws Exception
  {
      GnucashWritableVendor vend1 = gcshInFile.createWritableVendor();
      vend1.setNumber(GnucashVendor.getNewNumber(vend1));
      vend1.setName("Norma Jean Baker");
      
      GnucashWritableVendor vend2 = gcshInFile.createWritableVendor();
      vend2.setNumber(GnucashVendor.getNewNumber(vend2));
      vend2.setName("Madonna Louise Ciccone");
      
      GnucashWritableVendor vend3 = gcshInFile.createWritableVendor();
      vend3.setNumber(GnucashVendor.getNewNumber(vend3));
      vend3.setName("Rowan Atkinson");
      
      File outFile = Files.createTempFile("gc", ConstTest.GCSH_FILENAME_OUT).toFile();
//      System.err.println("Outfile for TestGnucashWritableVendorImpl.test02_1: '" + outFile.getPath() + "'");
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
      Assert.assertNotEquals(null, outFileGlob);
      Assert.assertEquals(true, outFileGlob.exists());

      // Build document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(outFileGlob);
//      System.err.println("xxxx XML parsed");

      // Normalize the XML structure
      document.getDocumentElement().normalize();
//      System.err.println("xxxx XML normalized");
      
      NodeList nList = document.getElementsByTagName("gnc:GncVendor");
      Assert.assertEquals(6, nList.getLength());

      // Last three nodes (the new ones)
      Node node = nList.item(nList.getLength() - 3);
      Assert.assertEquals(node.getNodeType(), Node.ELEMENT_NODE);
      Element elt = (Element) node;
      Assert.assertEquals("Norma Jean Baker", elt.getElementsByTagName("vendor:name").item(0).getTextContent());
      Assert.assertEquals("000004", elt.getElementsByTagName("vendor:id").item(0).getTextContent());

      node = nList.item(nList.getLength() - 2);
      Assert.assertEquals(node.getNodeType(), Node.ELEMENT_NODE);
      elt = (Element) node;
      Assert.assertEquals("Madonna Louise Ciccone", elt.getElementsByTagName("vendor:name").item(0).getTextContent());
      Assert.assertEquals("000005", elt.getElementsByTagName("vendor:id").item(0).getTextContent());

      node = nList.item(nList.getLength() - 1);
      Assert.assertEquals(node.getNodeType(), Node.ELEMENT_NODE);
      elt = (Element) node;
      Assert.assertEquals("Rowan Atkinson", elt.getElementsByTagName("vendor:name").item(0).getTextContent());
      Assert.assertEquals("000006", elt.getElementsByTagName("vendor:id").item(0).getTextContent());
  }



}
