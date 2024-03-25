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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestGnucashWritableVendorBillImpl {
	private static final String VEND_1_ID = TestGnucashVendorImpl.VEND_1_ID;
	private static final String VEND_2_ID = TestGnucashVendorImpl.VEND_2_ID;
	private static final String VEND_3_ID = TestGnucashVendorImpl.VEND_3_ID;

	static final String EXPENSES_ACCT_ID = "7d4c7bf08901493ab346cc24595fdb97"; // Root Account:Aufwendungen:Sonstiges
	static final String PAYABLE_ACCT_ID = "55711b4e6f564709bf880f292448237a"; // Root Account:Fremdkapital:Lieferanten:sonstige

	// ----------------------------

	private GnucashWritableFileImpl gcshInFile = null;
	private GnucashFileImpl gcshOutFile = null;

	private GnucashVendor vend1 = null;

	private GnucashAccount expensesAcct = null;
	private GnucashAccount payableAcct = null;

	// ----------------------------

	@BeforeMethod
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
		// System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
		InputStream gcshInFileStream = null;
		try {
			gcshInFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME_IN);
		}
		catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			gcshInFile = new GnucashWritableFileImpl(gcshInFileStream);
		}
		catch (Exception exc) {
			System.err.println("Cannot parse GnuCash in-file");
			exc.printStackTrace();
		}

		// ----------------------------

		vend1 = gcshInFile.getVendorByID(VEND_1_ID);

		expensesAcct = gcshInFile.getAccountByID(EXPENSES_ACCT_ID);
		payableAcct = gcshInFile.getAccountByID(PAYABLE_ACCT_ID);
	}

	// -----------------------------------------------------------------

	@Test
	public void test01() throws Exception {
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

		Assert.assertNotEquals(null, bll);
		String newInvcID = bll.getId();
		//      System.out.println("New Invoice ID (1): " + newInvcID);

		Assert.assertEquals("19327", bll.getNumber());

		File outFile = Files.createTempFile("gc", ConstTest.GCSH_FILENAME_OUT).toFile();
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


	private void test01_3(File outFile, String newInvcID) throws ParserConfigurationException, SAXException, IOException {


		// Build document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(outFile);

		// Normalize the XML structure
		document.getDocumentElement().normalize();

		NodeList nList = document.getElementsByTagName("gnc:GncInvoice");
		Assert.assertEquals(7, nList.getLength());

		// Last (new) node
		Node lastNode = nList.item(nList.getLength() - 1);
		Assert.assertEquals(lastNode.getNodeType(), Node.ELEMENT_NODE);

		Element elt = (Element) lastNode;
		Assert.assertEquals("19327", elt.getElementsByTagName("invoice:id").item(0).getTextContent());
		String locNewInvcID = elt.getElementsByTagName("invoice:guid").item(0).getTextContent();
		//      System.out.println("New Invoice ID (2): " + locNewInvcID);
		Assert.assertEquals(newInvcID, locNewInvcID);
	}

	// Before post
	private void test01_4(File outFile, String newInvcID) throws Exception {
		//      assertNotEquals(null, outFileGlob);
		//      assertEquals(true, outFileGlob.exists());

		gcshOutFile = new GnucashFileImpl(outFile);

		//      System.out.println("New Invoice ID (3): " + newInvcID);
		GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
		Assert.assertNotEquals(null, invcGener);
		GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGener);
		Assert.assertNotEquals(null, bllSpec);

		Assert.assertEquals("19327", bllSpec.getNumber());
		Assert.assertEquals(null, bllSpec.getPostAccountId());
		Assert.assertEquals(null, bllSpec.getPostTransactionId());
	}

	// After post
	private void test01_5(File outFile, String newInvcID) throws Exception {
		//      assertNotEquals(null, outFileGlob);
		//      assertEquals(true, outFileGlob.exists());

		gcshOutFile = new GnucashFileImpl(outFile);

		//      System.out.println("New Invoice ID (3): " + newInvcID);
		GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
		Assert.assertNotEquals(null, invcGener);
		GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGener);
		Assert.assertNotEquals(null, bllSpec);

		Assert.assertEquals("19327", bllSpec.getNumber());
		Assert.assertEquals(PAYABLE_ACCT_ID, bllSpec.getPostAccountId());

		Assert.assertNotEquals(null, bllSpec.getPostTransactionId());
		GnucashTransaction postTrx = gcshOutFile.getTransactionByID(bllSpec.getPostTransactionId());
		Assert.assertNotEquals(null, postTrx);
		Assert.assertEquals(2, postTrx.getSplits().size());
		String postTrxFirstSpltId = postTrx.getFirstSplit().getId();
		Assert.assertNotEquals(postTrxFirstSpltId, postTrx);
		String postTrxFirstSpltAcctId = postTrx.getFirstSplit().getAccount().getId();
		Assert.assertNotEquals(postTrxFirstSpltAcctId, postTrx);
		String postTrxSecondSpltAcctId = postTrx.getSecondSplit().getAccount().getId();
		Assert.assertNotEquals(postTrxSecondSpltAcctId, postTrx);
		//      System.out.println("ptrx1 " + postTrxFirstSpltAcctId);
		//      System.out.println("ptrx2 " + postTrxSecondSpltAcctId);
	}

	//  @AfterClass
	//  public void after() throws Exception
	//  {
	//      FileUtils.delete(outFileGlob);
	//  }

}
