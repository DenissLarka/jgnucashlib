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
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.SpecInvoiceCommon;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashVendorBillImpl {
	private GnucashFile gcshFile = null;
	private GnucashGenerInvoice bllGen = null;
	private GnucashVendorBill bllSpec = null;

	private static final String BLL_1_ID = TestGnucashGenerInvoiceImpl.INVC_4_ID;
	private static final String BLL_2_ID = TestGnucashGenerInvoiceImpl.INVC_2_ID;

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
		bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		Assert.assertEquals(bllSpec instanceof GnucashVendorBillImpl, true);
		Assert.assertEquals(bllSpec.getId(), BLL_1_ID);
		Assert.assertEquals(bllSpec.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT), "gncVendor");
		Assert.assertEquals(bllSpec.getNumber(), "1730-383/2");
		Assert.assertEquals(bllSpec.getDescription(), "Sie wissen schon: Gefälligkeiten, ne?");

		Assert.assertEquals(bllSpec.getDateOpened().toString(), "2023-08-31T10:59Z");
		// ::TODO
		Assert.assertEquals(bllSpec.getDatePosted().toString(), "2023-08-31T10:59Z");
	}

	@Test
	public void test01_2() throws Exception {
		bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		Assert.assertEquals(bllSpec instanceof GnucashVendorBillImpl, true);
		Assert.assertEquals(bllSpec.getId(), BLL_2_ID);
		Assert.assertEquals(bllSpec.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT), "gncVendor");
		Assert.assertEquals(bllSpec.getNumber(), "2740921");
		Assert.assertEquals(bllSpec.getDescription(), "Dat isjamaol eine schöne jepflejgte Reschnung!");

		Assert.assertEquals(bllSpec.getDateOpened().toString(), "2023-08-30T10:59Z");
		// ::TODO
		Assert.assertEquals(bllSpec.getDatePosted().toString(), "2023-08-30T10:59Z");
	}

	@Test
	public void test02_1() throws Exception {
		bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(bllGen.getGenerEntries().size(), 1);
		Assert.assertEquals(bllSpec.getGenerEntries().size(), 1);
		Assert.assertEquals(bllSpec.getEntries().size(), 1);

		TreeSet entrList = new TreeSet(); // sort elements of HashSet
		entrList.addAll(bllSpec.getEntries());
		Assert.assertEquals(((GnucashVendorBillEntry) entrList.toArray()[0]).getId(), "0041b8d397f04ae4a2e9e3c7f991c4ec");
	}

	@Test
	public void test02_2() throws Exception {
		bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(bllGen.getGenerEntries().size(), 2);
		Assert.assertEquals(bllSpec.getGenerEntries().size(), 2);
		Assert.assertEquals(bllSpec.getEntries().size(), 2);

		TreeSet entrList = new TreeSet(); // sort elements of HashSet
		entrList.addAll(bllSpec.getEntries());
		Assert.assertEquals(((GnucashVendorBillEntry) entrList.toArray()[1]).getId(), "513589a11391496cbb8d025fc1e87eaa");
		Assert.assertEquals(((GnucashVendorBillEntry) entrList.toArray()[0]).getId(), "dc3c53f07ff64199ad4ea38988b3f40a");
	}

	@Test
	public void test03_1() throws Exception {
		bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 41.40, bllGen.getBillAmountWithoutTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 41.40, bllSpec.getBillAmountWithoutTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 41.40, ((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes().doubleValue());

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		// Note: due to (purposefully) incorrect booking, the gross amount
		// of this bill is *not* 49.27 EUR, but 41.40 EUR (its net amount).
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 41.40, bllGen.getBillAmountWithTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 41.40, bllSpec.getBillAmountWithTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 41.40, ((SpecInvoiceCommon) bllSpec).getAmountWithTaxes().doubleValue());
	}

	@Test
	public void test03_2() throws Exception {
		bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 79.11, bllGen.getBillAmountWithoutTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 79.11, bllSpec.getBillAmountWithoutTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 79.11, ((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes().doubleValue());

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 94.14, bllGen.getBillAmountWithTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 94.14, bllSpec.getBillAmountWithTaxes().doubleValue());
		Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 94.14, ((SpecInvoiceCommon) bllSpec).getAmountWithTaxes().doubleValue());
	}

	@Test
	public void test04_1() throws Exception {
		bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		//    assertEquals( invcGen.getPostTransaction(),"xxx");
		//    assertEquals( invcSpec.getPostTransaction(),"xxx");

		// ::TODO
		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(bllSpec.getPayingTransactions().size(), 0);
		Assert.assertEquals(bllSpec.getPayingTransactions().size(), 0);

		//    LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) bllSpec.getPayingTransactions();
		//    Collections.sort(trxList);
		//    assertEquals(
		//                 ((GnucashTransaction) bllSpec.getPayingTransactions().toArray()[0]).getId(),"xxx");

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(bllGen.isBillFullyPaid(), false);
		Assert.assertEquals(bllSpec.isBillFullyPaid(), false);
		Assert.assertEquals(((SpecInvoiceCommon) bllSpec).isFullyPaid(), false);
	}

	@Test
	public void test04_2() throws Exception {
		bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
		bllSpec = new GnucashVendorBillImpl(bllGen);

		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(bllGen.getPostTransaction().getId(), "aa64d862bb5e4d749eb41f198b28d73d");
		Assert.assertEquals(bllSpec.getPostTransaction().getId(), "aa64d862bb5e4d749eb41f198b28d73d");

		// Note: That the following two return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(bllGen.getPayingTransactions().size(), 1);
		Assert.assertEquals(bllSpec.getPayingTransactions().size(), 1);

		LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) bllSpec.getPayingTransactions();
		Collections.sort(trxList);
		Assert.assertEquals(((GnucashTransaction) trxList.toArray()[0]).getId(), "ccff780b18294435bf03c6cb1ac325c1");

		// Note: That the following three return the same result
		// is *not* trivial (in fact, a serious implemetation error was
		// found with this test)
		Assert.assertEquals(bllGen.isBillFullyPaid(), true);
		Assert.assertEquals(bllSpec.isBillFullyPaid(), true);
		Assert.assertEquals(((SpecInvoiceCommon) bllSpec).isFullyPaid(), true);
	}
}
