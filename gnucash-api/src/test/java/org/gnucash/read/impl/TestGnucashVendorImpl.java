package org.gnucash.read.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.aux.GCshBillTerms;
import org.gnucash.read.impl.aux.TestGCshBillTermsImpl;
import org.gnucash.read.impl.aux.TestGCshTaxTableImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashVendorImpl {
	private GnucashFile gcshFile = null;
	private GnucashVendor vend = null;

	public static final String VEND_1_ID = "087e1a3d43fa4ef9a9bdd4b4797c4231";
	public static final String VEND_2_ID = "4f16fd55c0d64ebe82ffac0bb25fe8f5";
	public static final String VEND_3_ID = "bc1c7a6d0a6c4b4ea7dd9f8eb48f79f7";

	private static final String TAXTABLE_UK_1_ID = TestGCshTaxTableImpl.TAXTABLE_UK_1_ID;

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
		vend = gcshFile.getVendorByID(VEND_1_ID);

		Assert.assertEquals(vend.getId(), VEND_1_ID);
		Assert.assertEquals(vend.getNumber(), "000001");
		Assert.assertEquals(vend.getName(), "Lieferfanto AG");

		Assert.assertEquals(vend.getTaxTableID(), null);

		Assert.assertEquals(vend.getTermsID(), BLLTRM_1_ID);
		Assert.assertEquals(vend.getTerms().getName(), "sofort");
		Assert.assertEquals(vend.getTerms().getType(), GCshBillTerms.Type.DAYS);
		// etc., cf. class TestGCshBillTermsImpl
	}

	@Test
	public void test01_2() throws Exception {
		vend = gcshFile.getVendorByID(VEND_2_ID);

		Assert.assertEquals(vend.getId(), VEND_2_ID);
		Assert.assertEquals(vend.getNumber(), "000002");
		Assert.assertEquals(vend.getName(), "Super Suppliers Corp.");

		Assert.assertEquals(vend.getTaxTableID(), TAXTABLE_UK_1_ID);

		Assert.assertEquals(vend.getTermsID(), null);
	}

	@Test
	public void test01_3() throws Exception {
		vend = gcshFile.getVendorByID(VEND_3_ID);

		Assert.assertEquals(vend.getId(), VEND_3_ID);
		Assert.assertEquals(vend.getNumber(), "000003");
		Assert.assertEquals(vend.getName(), "Achetez Chez Nous S.A.");

		Assert.assertEquals(vend.getTaxTableID(), null);

		Assert.assertEquals(vend.getTermsID(), BLLTRM_2_ID);
		Assert.assertEquals(vend.getTerms().getName(), "30-10-3");
		Assert.assertEquals(vend.getTerms().getType(), GCshBillTerms.Type.DAYS);
		// etc., cf. class TestGCshBillTermsImpl
	}

	@Test
	public void test02_1() throws Exception {
		vend = gcshFile.getVendorByID(VEND_1_ID);

		Assert.assertEquals(vend.getNofOpenBills(), 1);
		Assert.assertEquals(vend.getUnpaidBills_direct().size(), 1);
		Assert.assertEquals(vend.getPaidBills_direct().size(), 1);

		LinkedList<GnucashVendorBill> bllList = (LinkedList<GnucashVendorBill>) vend.getUnpaidBills_direct();
		Collections.sort(bllList);
		Assert.assertEquals(((GnucashVendorBill) bllList.toArray()[0]).getId(), "4eb0dc387c3f4daba57b11b2a657d8a4");

		bllList = (LinkedList<GnucashVendorBill>) vend.getPaidBills_direct();
		Collections.sort(bllList);
		Assert.assertEquals(((GnucashVendorBill) bllList.toArray()[0]).getId(), "286fc2651a7848038a23bb7d065c8b67");
	}

	@Test
	public void test02_2() throws Exception {
		vend = gcshFile.getVendorByID(VEND_2_ID);

		Assert.assertEquals(vend.getUnpaidBills_direct().size(), 0);

	}

	@Test
	public void test02_3() throws Exception {
		vend = gcshFile.getVendorByID(VEND_3_ID);

		Assert.assertEquals(vend.getUnpaidBills_direct().size(), 0);

	}
}
