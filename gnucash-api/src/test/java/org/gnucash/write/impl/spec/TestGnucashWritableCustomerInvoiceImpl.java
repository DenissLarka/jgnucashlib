package org.gnucash.write.impl.spec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.gnucash.ConstTest;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashCustomerImpl;
import org.gnucash.read.impl.TestGnucashGenerJobImpl;
import org.gnucash.read.impl.TestGnucashVendorImpl;
import org.gnucash.read.impl.spec.GnucashCustomerInvoiceImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.spec.GnucashWritableCustomerInvoice;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestGnucashWritableCustomerInvoiceImpl {
  private static final String CUST_1_ID = TestGnucashCustomerImpl.CUST_1_ID;
  private static final String CUST_2_ID = TestGnucashCustomerImpl.CUST_2_ID;
  private static final String CUST_3_ID = TestGnucashCustomerImpl.CUST_3_ID;

  private static final String VEND_1_ID = TestGnucashVendorImpl.VEND_1_ID;
  private static final String VEND_2_ID = TestGnucashVendorImpl.VEND_2_ID;
  private static final String VEND_3_ID = TestGnucashVendorImpl.VEND_3_ID;

  private static final String JOB_1_ID = TestGnucashGenerJobImpl.JOB_1_ID;
  private static final String JOB_2_ID = TestGnucashGenerJobImpl.JOB_2_ID;

  static final String INCOME_ACCT_ID = "fed745c4da5c49ebb0fde0f47222b35b"; // Root Account::Erträge::Sonstiges
  static final String RECEIVABLE_ACCT_ID = "7e223ee2260d4ba28e8e9e19ce291f43"; // Root
                                                                               // Account::Aktiva::Forderungen::Unfug_Quatsch

  // ----------------------------

  private GnucashWritableFileImpl gcshInFile = null;
  private GnucashFileImpl gcshOutFile = null;

  private GnucashCustomer cust1 = null;

  private GnucashAccount incomeAcct = null;
  private GnucashAccount receivableAcct = null;

  @BeforeMethod
  public void initialize() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    ApplicationMessages.setup();

    InputStream gcshInFileStream = null;
    try {
      gcshInFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME_IN);
    } catch (Exception exc) {
      System.err.println("Cannot generate input stream from resource");
      return;
    }

    try {
      gcshInFile = new GnucashWritableFileImpl(gcshInFileStream);
    } catch (Exception exc) {
      System.err.println("Cannot parse GnuCash in-file");
      exc.printStackTrace();
    }

    // ----------------------------

    cust1 = gcshInFile.getCustomerByID(CUST_1_ID);

    incomeAcct = gcshInFile.getAccountByID(INCOME_ACCT_ID);
    receivableAcct = gcshInFile.getAccountByID(RECEIVABLE_ACCT_ID);
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception {
    LocalDate postDate = LocalDate.of(2023, 8, 1);
    LocalDate openedDate = LocalDate.of(2023, 8, 3);
    LocalDate dueDate = LocalDate.of(2023, 8, 10);
    GnucashWritableCustomerInvoice invc = gcshInFile.createWritableCustomerInvoice("19327", cust1, incomeAcct,
        receivableAcct, openedDate, postDate, dueDate);

    Assert.assertNotEquals(null, invc);
    String newInvcID = invc.getId();
    // System.out.println("New Invoice ID (1): " + newInvcID);

    Assert.assertEquals(invc.getNumber(), "19327");

    File outFile = Files.createTempFile("gc", ConstTest.GCSH_FILENAME_OUT).toFile();
    // System.err.println("Outfile for TestGnucashWritableCustomerImpl.test01_1: '" + outFile.getPath() + "'");
    outFile.delete(); // sic, the temp. file is already generated (empty),
    // and the GnuCash file writer does not like that.
    gcshInFile.writeFile(outFile);

    // test01_2();
    test01_3(outFile, newInvcID);
    test01_4(outFile, newInvcID);

    // post invoice
    invc.post(incomeAcct, receivableAcct, postDate, dueDate);

    // write to file
    outFile.delete();
    gcshInFile.writeFile(outFile);

    test01_5(outFile, newInvcID);
  }

  private void test01_2(File outFile, String newInvcID) throws ParserConfigurationException, SAXException, IOException {
    // ::TODO
    // Check if generated XML file is valid
  }

  private void test01_3(File outFile, String newInvcID) throws ParserConfigurationException, SAXException, IOException {
    // assertNotEquals(null, outFileGlob);
    // assertEquals( outFileGlob.exists(),true);

    // Build document
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document document = builder.parse(outFile);
    // System.err.println("xxxx XML parsed");

    // Normalize the XML structure
    document.getDocumentElement().normalize();
    // System.err.println("xxxx XML normalized");

    NodeList nList = document.getElementsByTagName("gnc:GncInvoice");
    Assert.assertEquals(nList.getLength(), 7);

    // Last (new) node
    Node lastNode = nList.item(nList.getLength() - 1);
    Assert.assertEquals(Node.ELEMENT_NODE, lastNode.getNodeType());

    Element elt = (Element) lastNode;
    Assert.assertEquals(elt.getElementsByTagName("invoice:id").item(0).getTextContent(), "19327");
    String locNewInvcID = elt.getElementsByTagName("invoice:guid").item(0).getTextContent();
    // System.out.println("New Invoice ID (2): " + locNewInvcID);
    Assert.assertEquals(locNewInvcID, newInvcID);
  }

  // Before post
  private void test01_4(File outFile, String newInvcID) throws Exception {
    // assertNotEquals(null, outFileGlob);
    // assertEquals( outFileGlob.exists(),true);

    gcshOutFile = new GnucashFileImpl(outFile);

    // System.out.println("New Invoice ID (3): " + newInvcID);
    GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
    Assert.assertNotEquals(null, invcGener);
    GnucashCustomerInvoice invcSpec = new GnucashCustomerInvoiceImpl(invcGener);
    Assert.assertNotEquals(null, invcSpec);

    Assert.assertEquals(invcSpec.getNumber(), "19327");
    Assert.assertEquals(invcSpec.getPostAccountId(), null);
    Assert.assertEquals(invcSpec.getPostTransactionId(), null);
  }

  // After post
  private void test01_5(File outFile, String newInvcID) throws Exception {
    // assertNotEquals(null, outFileGlob);
    // assertEquals( outFileGlob.exists(),true);

    gcshOutFile = new GnucashFileImpl(outFile);

    // System.out.println("New Invoice ID (3): " + newInvcID);
    GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
    Assert.assertNotEquals(null, invcGener);
    GnucashCustomerInvoice invcSpec = new GnucashCustomerInvoiceImpl(invcGener);
    Assert.assertNotEquals(null, invcSpec);

    Assert.assertEquals(invcSpec.getNumber(), "19327");
    Assert.assertEquals(invcSpec.getPostAccountId(), RECEIVABLE_ACCT_ID);

    Assert.assertNotEquals(null, invcSpec.getPostTransactionId());
    GnucashTransaction postTrx = gcshOutFile.getTransactionByID(invcSpec.getPostTransactionId());
    Assert.assertNotEquals(null, postTrx);
    Assert.assertEquals(postTrx.getSplits().size(), 2);
    String postTrxFirstSpltId = postTrx.getFirstSplit().getId();
    Assert.assertNotEquals(postTrxFirstSpltId, postTrx);
    String postTrxFirstSpltAcctId = postTrx.getFirstSplit().getAccount().getId();
    Assert.assertNotEquals(postTrxFirstSpltAcctId, postTrx);
    String postTrxSecondSpltAcctId = postTrx.getSecondSplit().getAccount().getId();
    Assert.assertNotEquals(postTrxSecondSpltAcctId, postTrx);
    // System.out.println("ptrx1 " + postTrxFirstSpltAcctId);
    // System.out.println("ptrx2 " + postTrxSecondSpltAcctId);
  }

  // @AfterClass
  // public void after() throws Exception
  // {
  // FileUtils.delete(outFileGlob);
  // }

}
