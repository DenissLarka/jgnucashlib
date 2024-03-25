package org.gnucash.read.impl;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashGenerInvoiceEntryImpl {
	private GnucashFile gcshFile = null;
	private GnucashGenerInvoiceEntry invcEntr = null;

	private static final String INVCENTR_1_ID = "513589a11391496cbb8d025fc1e87eaa";
	private static final String INVCENTR_2_ID = "0041b8d397f04ae4a2e9e3c7f991c4ec";
	private static final String INVCENTR_3_ID = "83e78ce224d94c3eafc55e33d3d5f3e6";

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
	public void test01() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesGenerInvoiceEntriesMap(), 12);
	}

	@Test
	public void test02_1() throws Exception {
		invcEntr = gcshFile.getGenerInvoiceEntryByID(INVCENTR_1_ID);

		Assert.assertEquals(invcEntr.getId(), INVCENTR_1_ID);
		Assert.assertEquals(invcEntr.getType(), GnucashGenerInvoice.TYPE_VENDOR);
		Assert.assertEquals(invcEntr.getGenerInvoiceID(), "286fc2651a7848038a23bb7d065c8b67");
		Assert.assertEquals(invcEntr.getAction(), null);
		Assert.assertEquals(invcEntr.getDescription(), "Item 1");

		Assert.assertEquals(invcEntr.isBillTaxable(), true);
		Assert.assertEquals(invcEntr.getBillApplicableTaxPercent().doubleValue(), 0.19, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(invcEntr.getBillPrice().doubleValue(), 12.50, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(invcEntr.getQuantity().intValue(), 3);
	}

	@Test
	public void test02_2() throws Exception {
		invcEntr = gcshFile.getGenerInvoiceEntryByID(INVCENTR_2_ID);

		Assert.assertEquals(invcEntr.getId(), INVCENTR_2_ID);
		Assert.assertEquals(invcEntr.getType(), GnucashGenerInvoice.TYPE_VENDOR);
		Assert.assertEquals(invcEntr.getGenerInvoiceID(), "4eb0dc387c3f4daba57b11b2a657d8a4");
		Assert.assertEquals(invcEntr.getAction(), GnucashGenerInvoiceEntry.ACTION_HOURS);
		Assert.assertEquals(invcEntr.getDescription(), "Gefälligkeiten");

		Assert.assertEquals(invcEntr.isBillTaxable(), true);
		// Following: sic, because there is no tax table entry assigned
		// (this is an error in real life, but we have done it on purpose here
		// for the tests).
		Assert.assertEquals( invcEntr.getBillApplicableTaxPercent().doubleValue(), 0.00,ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals( invcEntr.getBillPrice().doubleValue(),13.80, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(invcEntr.getQuantity().intValue(), 3);
	}

	@Test
	public void test02_3() throws Exception {
		invcEntr = gcshFile.getGenerInvoiceEntryByID(INVCENTR_3_ID);

		Assert.assertEquals(invcEntr.getId(), INVCENTR_3_ID);
		Assert.assertEquals(invcEntr.getType(), GnucashGenerInvoice.TYPE_CUSTOMER);
		Assert.assertEquals(invcEntr.getGenerInvoiceID(), "6588f1757b9e4e24b62ad5b37b8d8e07");
		Assert.assertEquals(invcEntr.getAction(), GnucashGenerInvoiceEntry.ACTION_MATERIAL);
		Assert.assertEquals(invcEntr.getDescription(), "Posten 3");

		Assert.assertEquals(invcEntr.isInvcTaxable(), true);
		Assert.assertEquals( invcEntr.getInvcApplicableTaxPercent().doubleValue(), 0.19,ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals( invcEntr.getInvcPrice().doubleValue(),120.00, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(invcEntr.getQuantity().intValue(), 10);
	}

}
