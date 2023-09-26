package org.gnucash.write.impl.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.gnucash.ConstTest;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashCustomerImpl;
import org.gnucash.read.impl.spec.GnucashCustomerInvoiceImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.spec.GnucashWritableCustomerInvoice;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashWritableCustomerInvoiceImpl
{
    private static final String CUST_1_ID = TestGnucashCustomerImpl.CUST_1_ID;
    private static final String CUST_2_ID = TestGnucashCustomerImpl.CUST_2_ID;
    private static final String CUST_3_ID = TestGnucashCustomerImpl.CUST_3_ID;

    private static final String ACCT_1_ID = "7e223ee2260d4ba28e8e9e19ce291f43"; // Root Account::Aktiva::Forderungen::Unfug_Quatsch
    private static final String ACCT_2_ID = "fed745c4da5c49ebb0fde0f47222b35b"; // Root Account::Erträge::Sonstiges
    
    // ----------------------------

    private GnucashWritableFileImpl gcshInFile = null;
    private String outFileGlobNameAbs = null;
    private File outFileGlob = null;

    private GnucashCustomer cust1 = null;
    private GnucashAccount  acct1 = null;
    private GnucashAccount  acct2 = null;
    
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
    return new JUnit4TestAdapter(TestGnucashWritableCustomerInvoiceImpl.class);  
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
    
    // ----------------------------
    
    cust1 = gcshInFile.getCustomerByID(CUST_1_ID);
    acct1 = gcshInFile.getAccountByID(ACCT_1_ID);
    acct2 = gcshInFile.getAccountByID(ACCT_2_ID);
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
      LocalDate dueDate = LocalDate.of(2023, 8, 2);
      GnucashWritableCustomerInvoice invc = gcshInFile.createWritableCustomerInvoice("19327", cust1, acct2, dueDate);
      
//      GnucashWritableCustomerInvoiceEntry entr = invc.createEntry(acct2, 
//                                                                  new FixedPointNumber(12), 
//                                                                  new FixedPointNumber(13));

      assertNotEquals(null, invc);
      String newInvcID = invc.getId();
      System.out.println("New Invoice ID (1): " + newInvcID);
      
      assertEquals("19327", invc.getNumber());
      assertEquals(ACCT_2_ID, invc.getAccountIDToTransferMoneyTo());

      File outFile = folder.newFile(ConstTest.GCSH_FILENAME_OUT);
//      System.err.println("Outfile for TestGnucashWritableCustomerImpl.test01_1: '" + outFile.getPath() + "'");
      outFile.delete(); // sic, the temp. file is already generated (empty), 
                        // and the GnuCash file writer does not like that.
      gcshInFile.writeFile(outFile);
      
      // test01_2();
      test01_3(outFile, newInvcID);
      test01_4(outFile, newInvcID);
  }

  // @Test
  private void test01_3(File outFile, String newInvcID) throws Exception
  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());

      // Build document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(outFile);
//      System.err.println("xxxx XML parsed");

      // Normalize the XML structure
      document.getDocumentElement().normalize();
//      System.err.println("xxxx XML normalized");
      
      NodeList nList = document.getElementsByTagName("gnc:GncInvoice");
      assertEquals(7, nList.getLength());

      // Last (new) node
      Node lastNode = nList.item(nList.getLength() - 1);
      assertEquals(lastNode.getNodeType(), Node.ELEMENT_NODE);
      Element elt = (Element) lastNode;
      assertEquals("19327", elt.getElementsByTagName("invoice:id").item(0).getTextContent());
      String locNewInvcID = elt.getElementsByTagName("invoice:guid").item(0).getTextContent();
      System.out.println("New Invoice ID (2): " + locNewInvcID);
      
      assertEquals(newInvcID, locNewInvcID);
  }

  // @Test
  private void test01_4(File outFile, String newInvcID) throws Exception
  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());
      GnucashFileImpl gcshFile = new GnucashFileImpl(outFile);
      
      System.out.println("New Invoice ID (3): " + newInvcID);
      GnucashGenerInvoice invcGener = gcshFile.getGenerInvoiceByID(newInvcID);
      GnucashCustomerInvoice invcSpec = new GnucashCustomerInvoiceImpl(invcGener);
      
      assertEquals("19327", invcSpec.getNumber());
      assertEquals(ACCT_2_ID, invcSpec.getAccountIDToTransferMoneyTo());
      
      String postTrxID = invcSpec.getPostTransaction().getId();
      GnucashTransaction postTrx = gcshFile.getTransactionByID(postTrxID);
      String postTrxFirstSpltAcctId = postTrx.getFirstSplit().getAccount().getId();
      String postTrxSecondSpltAcctId = postTrx.getSecondSplit().getAccount().getId();
  }

//  @AfterClass
//  public void after() throws Exception
//  {
//      FileUtils.delete(outFileGlob);
//  }

}
