package org.gnucash.read.impl;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.auxiliary.GCshBillTerms;
import org.gnucash.read.impl.auxiliary.TestGCshBillTermsImpl;
import org.gnucash.read.impl.auxiliary.TestGCshTaxTableImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashCustomerImpl {
	private GnucashFile gcshFile = null;
	private GnucashCustomer cust = null;

	public static final String CUST_1_ID = "5d1dd9afa7554553988669830cc1f696"; // Unfug und Quatsch GmbH
	public static final String CUST_2_ID = "f44645d2397946bcac90dff68cc03b76"; // Is That So Ltd.
	public static final String CUST_3_ID = "1d2081e8a10e4d5e9312d9fff17d470d"; // N'importe Quoi S.A.

	private static final String TAXTABLE_FR_1_ID = TestGCshTaxTableImpl.TAXTABLE_FR_1_ID;

	private static final String BLLTRM_1_ID = TestGCshBillTermsImpl.BLLTRM_1_ID;
	private static final String BLLTRM_2_ID = TestGCshBillTermsImpl.BLLTRM_2_ID;
	private static final String BLLTRM_3_ID = TestGCshBillTermsImpl.BLLTRM_3_ID;

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
		cust = gcshFile.getCustomerByID(CUST_1_ID);

		Assert.assertEquals(cust.getId(), CUST_1_ID);
		Assert.assertEquals(cust.getNumber(), "000001");
		Assert.assertEquals(cust.getName(), "Unfug und Quatsch GmbH");

		Assert.assertEquals(cust.getDiscount().doubleValue(), 0.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(cust.getCredit().doubleValue(), 0.0, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(cust.getTaxTableID(), null);

		Assert.assertEquals(cust.getTermsID(), BLLTRM_2_ID);
		Assert.assertEquals(cust.getTerms().getName(), "30-10-3");
		Assert.assertEquals(cust.getTerms().getType(), GCshBillTerms.Type.DAYS);
		// etc., cf. class TestGCshBillTermsImpl
	}

	@Test
	public void test01_2() throws Exception {
		cust = gcshFile.getCustomerByID(CUST_2_ID);

		Assert.assertEquals(cust.getId(), CUST_2_ID);
		Assert.assertEquals(cust.getNumber(), "000002");
		Assert.assertEquals(cust.getName(), "Is That So Ltd.");

		Assert.assertEquals(cust.getDiscount().doubleValue(), 3.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(cust.getCredit().doubleValue(), 2000.0, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(cust.getTaxTableID(), null);
		Assert.assertEquals(cust.getTermsID(), null);
	}

	@Test
	public void test01_3() throws Exception {
		cust = gcshFile.getCustomerByID(CUST_3_ID);

		Assert.assertEquals(cust.getId(), CUST_3_ID);
		Assert.assertEquals(cust.getNumber(), "000003");
		Assert.assertEquals(cust.getName(), "N'importe Quoi S.A.");

		Assert.assertEquals(cust.getDiscount().doubleValue(), 0.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(cust.getCredit().doubleValue(), 0.0, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(cust.getTaxTableID(), TAXTABLE_FR_1_ID);
		Assert.assertEquals(cust.getTaxTable().getName(), "FR_TVA_Std");
		Assert.assertEquals(cust.getTaxTable().getEntries().size(), 1);
		// etc., cf. class TestGCshTaxTableImpl

		Assert.assertEquals(cust.getTermsID(), null);
	}

	@Test
	public void test02_1() throws Exception {
		cust = gcshFile.getCustomerByID(CUST_1_ID);

		Assert.assertEquals(cust.getPaidInvoices_direct().size(), 1);
		Assert.assertEquals(((GnucashCustomerInvoice) cust.getPaidInvoices_direct().toArray()[0]).getId(), "d9967c10fdf1465e9394a3e4b1e7bd79");
		Assert.assertEquals(cust.getNofOpenInvoices(), 1);
		Assert.assertEquals(cust.getUnpaidInvoices_direct().size(), 1);
		Assert.assertEquals(((GnucashCustomerInvoice) cust.getUnpaidInvoices_direct().toArray()[0]).getId(), "6588f1757b9e4e24b62ad5b37b8d8e07");
	}

	@Test
	public void test02_2() throws Exception {
		cust = gcshFile.getCustomerByID(CUST_2_ID);

		Assert.assertEquals(cust.getUnpaidInvoices_direct().size(), 0);
		//    assertEquals(
		//                 cust.getUnpaidInvoices(GnucashGenerInvoice.ReadVariant.DIRECT).toArray()[0].toString(),"[GnucashCustomerInvoiceImpl: id: d9967c10fdf1465e9394a3e4b1e7bd79 customer-id (dir.): 5d1dd9afa7554553988669830cc1f696 invoice-number: 'R1730' description: 'null' #entries: 0 date-opened: 2023-07-29]");
	}

	@Test
	public void test02_3() throws Exception {
		cust = gcshFile.getCustomerByID(CUST_3_ID);

		Assert.assertEquals(cust.getUnpaidInvoices_direct().size(), 0);
		//    assertEquals(
		//                 cust.getUnpaidInvoices(GnucashGenerInvoice.ReadVariant.DIRECT).toArray()[0].toString(),"[GnucashCustomerInvoiceImpl: id: d9967c10fdf1465e9394a3e4b1e7bd79 customer-id (dir.): 5d1dd9afa7554553988669830cc1f696 invoice-number: 'R1730' description: 'null' #entries: 0 date-opened: 2023-07-29]");
	}
}
