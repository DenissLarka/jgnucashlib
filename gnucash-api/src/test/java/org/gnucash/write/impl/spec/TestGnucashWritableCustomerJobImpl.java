package org.gnucash.write.impl.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashCustomerImpl;
import org.gnucash.read.impl.spec.GnucashCustomerJobImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.spec.GnucashWritableCustomerJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashWritableCustomerJobImpl
{
    private static final String CUST_1_ID = TestGnucashCustomerImpl.CUST_1_ID;
    private static final String CUST_2_ID = TestGnucashCustomerImpl.CUST_2_ID;
    private static final String CUST_3_ID = TestGnucashCustomerImpl.CUST_3_ID;

    // ----------------------------

    private GnucashWritableFileImpl gcshInFile = null;
    private GnucashFileImpl         gcshOutFile = null;

    private GnucashCustomer cust1 = null;
    
    // ----------------------------

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
    return new JUnit4TestAdapter(TestGnucashWritableCustomerJobImpl.class);  
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
    
    // ----------------------------
    
    cust1 = gcshInFile.getCustomerByID(CUST_1_ID);
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
      GnucashWritableCustomerJob job = gcshInFile.createWritableCustomerJob(
	      						cust1, "J123", 
	      						"New job for customer 1");
      
      assertNotEquals(null, job);
      String newJobID = job.getId();
//      System.out.println("New Job ID (1): " + newJobID);
      
      assertEquals("J123", job.getNumber());

      File outFile = folder.newFile(ConstTest.GCSH_FILENAME_OUT);
//      System.err.println("Outfile for TestGnucashWritableCustomerImpl.test01_1: '" + outFile.getPath() + "'");
      outFile.delete(); // sic, the temp. file is already generated (empty), 
                        // and the GnuCash file writer does not like that.
      gcshInFile.writeFile(outFile);
      
      // test01_2();
      test01_3(outFile, newJobID);
      test01_4(outFile, newJobID);
  }

  private void test01_2(File outFile, String newJobID) throws ParserConfigurationException, SAXException, IOException 
  {
      // ::TODO
      // Check if generated XML file is valid
  }
  
  private void test01_3(File outFile, String newJobID) throws ParserConfigurationException, SAXException, IOException 
  {
      //    assertNotEquals(null, outFileGlob);
      //    assertEquals(true, outFileGlob.exists());

      // Build document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(outFile);
//      System.err.println("xxxx XML parsed");

      // Normalize the XML structure
      document.getDocumentElement().normalize();
//      System.err.println("xxxx XML normalized");
      
      NodeList nList = document.getElementsByTagName("gnc:GncJob");
      assertEquals(3, nList.getLength());

      // Last (new) node
      Node lastNode = nList.item(nList.getLength() - 1);
      assertEquals(lastNode.getNodeType(), Node.ELEMENT_NODE);
      
      Element elt = (Element) lastNode;
      assertEquals("J123", elt.getElementsByTagName("job:id").item(0).getTextContent());
      String locNewJobID = elt.getElementsByTagName("job:guid").item(0).getTextContent();
//      System.out.println("New Job ID (2): " + locNewJobID);
      assertEquals(newJobID, locNewJobID);
  }

  private void test01_4(File outFile, String newInvcID) throws Exception
  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());

      gcshOutFile = new GnucashFileImpl(outFile);
      
//      System.out.println("New Job ID (3): " + newJobID);
      GnucashGenerJob jobGener = gcshOutFile.getGenerJobByID(newInvcID);
      assertNotEquals(null, jobGener);
      GnucashCustomerJob jobSpec = new GnucashCustomerJobImpl(jobGener);
      assertNotEquals(null, jobSpec);
      
      assertEquals(newInvcID, jobGener.getId());
      assertEquals(newInvcID, jobSpec.getId());
      
      assertEquals(CUST_1_ID, jobGener.getOwnerId());
      assertEquals(CUST_1_ID, jobSpec.getOwnerId());
      assertEquals(CUST_1_ID, jobSpec.getCustomerId());
      
      assertEquals("J123", jobGener.getNumber());
      assertEquals("J123", jobSpec.getNumber());
      
      assertEquals("New job for customer 1", jobGener.getName());
      assertEquals("New job for customer 1", jobSpec.getName());      
  }

//  @AfterClass
//  public void after() throws Exception
//  {
//      FileUtils.delete(outFileGlob);
//  }
}
