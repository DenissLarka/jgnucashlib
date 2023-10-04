package org.gnucash.write.impl.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashVendorImpl;
import org.gnucash.read.impl.spec.GnucashVendorBillImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.spec.GnucashWritableVendorBill;
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

public class TestGnucashWritableVendorBillImpl
{
    private static final String VEND_1_ID = TestGnucashVendorImpl.VEND_1_ID;
    private static final String VEND_2_ID = TestGnucashVendorImpl.VEND_2_ID;
    private static final String VEND_3_ID = TestGnucashVendorImpl.VEND_3_ID;

    private static final String EXPENSES_ACCT_ID   = "xxx"; // xxx
    private static final String PAYABLE_ACCT_ID    = "xxx"; // xxx
    
    // ----------------------------

    private GnucashWritableFileImpl gcshInFile = null;
    private GnucashFileImpl         gcshOutFile = null;

    private GnucashVendor   vend1 = null;
    
    private GnucashAccount  expensesAcct = null;
    private GnucashAccount  payableAcct = null;
    
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
    return new JUnit4TestAdapter(TestGnucashWritableVendorBillImpl.class);  
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
    
    vend1 = gcshInFile.getVendorByID(VEND_1_ID);
    
    expensesAcct   = gcshInFile.getAccountByID(EXPENSES_ACCT_ID);
    payableAcct    = gcshInFile.getAccountByID(PAYABLE_ACCT_ID);
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
      LocalDate postDate = LocalDate.of(2023, 8, 1);
      LocalDate openedDate = LocalDate.of(2023, 8, 3);
      LocalDate dueDate = LocalDate.of(2023, 8, 10);
      GnucashWritableVendorBill bll = gcshInFile.createWritableVendorBill("19327", 
	      							vend1, 
	      							expensesAcct, payableAcct, 
	      							openedDate, postDate, dueDate);
      
//      GnucashWritableCustomerInvoiceEntry entr = invc.createEntry(acct2, 
//                                                                  new FixedPointNumber(12), 
//                                                                  new FixedPointNumber(13));

      assertNotEquals(null, bll);
      String newInvcID = bll.getId();
//      System.out.println("New Invoice ID (1): " + newInvcID);
      
      assertEquals("19327", bll.getNumber());

      File outFile = folder.newFile(ConstTest.GCSH_FILENAME_OUT);
//      System.err.println("Outfile for TestGnucashWritableCustomerImpl.test01_1: '" + outFile.getPath() + "'");
      outFile.delete(); // sic, the temp. file is already generated (empty), 
                        // and the GnuCash file writer does not like that.
      gcshInFile.writeFile(outFile);
      
      // test01_2();
      test01_3(outFile, newInvcID);
      test01_4(outFile, newInvcID);
      
      // post invoice
      bll.post(expensesAcct, payableAcct, postDate, dueDate);
      
      // write to file
      outFile.delete();
      gcshInFile.writeFile(outFile);
      
      test01_5(outFile, newInvcID);
  }

  private void test01_2(File outFile, String newInvcID) throws ParserConfigurationException, SAXException, IOException 
  {
      // ::TODO
      // Check if generated XML file is valid
  }
  
  private void test01_3(File outFile, String newInvcID) throws ParserConfigurationException, SAXException, IOException 
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
      
      NodeList nList = document.getElementsByTagName("gnc:GncInvoice");
      assertEquals(7, nList.getLength());

      // Last (new) node
      Node lastNode = nList.item(nList.getLength() - 1);
      assertEquals(lastNode.getNodeType(), Node.ELEMENT_NODE);
      
      Element elt = (Element) lastNode;
      assertEquals("19327", elt.getElementsByTagName("invoice:id").item(0).getTextContent());
      String locNewInvcID = elt.getElementsByTagName("invoice:guid").item(0).getTextContent();
//      System.out.println("New Invoice ID (2): " + locNewInvcID);
      assertEquals(newInvcID, locNewInvcID);
  }

  // Before post
  private void test01_4(File outFile, String newInvcID) throws Exception
  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());

      gcshOutFile = new GnucashFileImpl(outFile);
      
//      System.out.println("New Invoice ID (3): " + newInvcID);
      GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
      assertNotEquals(null, invcGener);
      GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGener);
      assertNotEquals(null, bllSpec);
      
      assertEquals("19327", bllSpec.getNumber());
      assertEquals(null, bllSpec.getPostAccountId());      
      assertEquals(null, bllSpec.getPostTransactionId());
  }

  // After post
  private void test01_5(File outFile, String newInvcID) throws Exception
  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());

      gcshOutFile = new GnucashFileImpl(outFile);
      
//      System.out.println("New Invoice ID (3): " + newInvcID);
      GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
      assertNotEquals(null, invcGener);
      GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGener);
      assertNotEquals(null, bllSpec);
      
      assertEquals("19327", bllSpec.getNumber());
      assertEquals(PAYABLE_ACCT_ID, bllSpec.getPostAccountId());
      
      assertNotEquals(null, bllSpec.getPostTransactionId());
      GnucashTransaction postTrx = gcshOutFile.getTransactionByID(bllSpec.getPostTransactionId());
      assertNotEquals(null, postTrx);
      assertEquals(2, postTrx.getSplits().size());
      String postTrxFirstSpltId = postTrx.getFirstSplit().getId();
      assertNotEquals(postTrxFirstSpltId, postTrx);
      String postTrxFirstSpltAcctId = postTrx.getFirstSplit().getAccount().getId();
      assertNotEquals(postTrxFirstSpltAcctId, postTrx);
      String postTrxSecondSpltAcctId = postTrx.getSecondSplit().getAccount().getId();
      assertNotEquals(postTrxSecondSpltAcctId, postTrx);
//      System.out.println("ptrx1 " + postTrxFirstSpltAcctId);
//      System.out.println("ptrx2 " + postTrxSecondSpltAcctId);
  }

//  @AfterClass
//  public void after() throws Exception
//  {
//      FileUtils.delete(outFileGlob);
//  }

}
