package org.gnucash.read.impl.spec;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashGenerInvoiceImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerInvoiceEntry;
import org.gnucash.read.spec.SpecInvoiceCommon;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashCustomerInvoiceImpl {
	private GnucashFile gcshFile = null;
	private GnucashGenerInvoice invcGen = null;
	private GnucashCustomerInvoice invcSpec = null;

	private static final String INVC_1_ID = TestGnucashGenerInvoiceImpl.INVC_1_ID;

	// -----------------------------------------------------------------

	@BeforeMethod
	public void initialize() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		// URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
		// System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
		InputStream gcshFileStream = null;
		try {
			gcshFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME);
		}
		catch (Exception exc) {
			System.err.println("Cannot generate input stream from resource");
			return;
		}

		try {
			gcshFile = new GnucashFileImpl(gcshFileStream);
		}
		catch (Exception exc) {
			System.err.println("Cannot parse GnuCash file");
			exc.printStackTrace();
		}
	}

	// -----------------------------------------------------------------

	@Test
	public void test01_1() throws Exception {
		invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
		invcSpec = new GnucashCustomerInvoiceImpl(invcGen);

		Assert.assertEquals(invcSpec instanceof GnucashCustomerInvoiceImpl, true);
		Assert.assertEquals(invcSpec.getId(), INVC_1_ID);
		Assert.assertEquals(invcSpec.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT), "gncCustomer");
		Assert.assertEquals(invcSpec.getNumber(), "R1730");
		Assert.assertEquals(invcSpec.getDescription(), "Alles ohne Steuern / voll bezahlt");

		Assert.assertEquals(invcSpec.getDateOpened().toString(), "2023-07-29T10:59Z");
		Assert.assertEquals(invcSpec.getDatePosted().toString(), "2023-07-29T10:59Z");
	}

	@Test
	public void test02_1() throws Exception {
		invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
		invcSpec = new GnucashCustomerInvoiceImpl(invcGen);

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(invcGen.getGenerEntries().size(), 2);
		Assert.assertEquals(invcSpec.getGenerEntries().size(), 2);
		Assert.assertEquals(invcSpec.getEntries().size(), 2);

		TreeSet entrList = new TreeSet(); // sort elements of HashSet
		entrList.addAll(invcSpec.getEntries());
		Assert.assertEquals(((GnucashCustomerInvoiceEntry) entrList.toArray()[0]).getId(), "92e54c04b66f4682a9afb48e27dfe397");
		Assert.assertEquals(((GnucashCustomerInvoiceEntry) entrList.toArray()[1]).getId(), "3c67a99b5fe34387b596bb1fbab21a74");
	}

	@Test
	public void test03_1() throws Exception {
		invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
		invcSpec = new GnucashCustomerInvoiceImpl(invcGen);

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 1327.60, invcGen.getInvcAmountWithoutTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 1327.60, invcSpec.getInvcAmountWithoutTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 1327.60, ((SpecInvoiceCommon) invcSpec).getAmountWithoutTaxes().doubleValue());

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 1327.60, invcGen.getInvcAmountWithTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 1327.60, invcSpec.getInvcAmountWithTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 1327.60, ((SpecInvoiceCommon) invcSpec).getAmountWithTaxes().doubleValue());
	}

	@Test
	public void test04_1() throws Exception {
		invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
		invcSpec = new GnucashCustomerInvoiceImpl(invcGen);

		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(invcGen.getPostTransaction().getId(), "c97032ba41684b2bb5d1391c9d7547e9");
		Assert.assertEquals(invcSpec.getPostTransaction().getId(), "c97032ba41684b2bb5d1391c9d7547e9");

		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(invcGen.getPayingTransactions().size(), 1);
		Assert.assertEquals(invcSpec.getPayingTransactions().size(), 1);

		LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) invcSpec.getPayingTransactions();
		Collections.sort(trxList);
		Assert.assertEquals(((GnucashTransaction) trxList.toArray()[0]).getId(), "29557cfdf4594eb68b1a1b710722f991");

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(invcGen.isInvcFullyPaid(), true);
		Assert.assertEquals(invcSpec.isInvcFullyPaid(), true);
		Assert.assertEquals(((SpecInvoiceCommon) invcSpec).isFullyPaid(), true);
	}
}
