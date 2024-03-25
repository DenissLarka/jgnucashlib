package org.gnucash.read.impl;


import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashFileImpl {
	private GnucashFile gcshFile = null;

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
		Assert.assertEquals(gcshFile.getNofEntriesAccountMap(), 91);
	}

	@Test
	public void test02() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesTransactionMap(), 11);
	}

	@Test
	public void test03() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesTransactionSplitsMap(), 29);
	}

	@Test
	public void test04() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesGenerInvoiceMap(), 6);
	}

	@Test
	public void test05() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesGenerInvoiceEntriesMap(), 12);
	}

	@Test
	public void test06() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesGenerJobMap(), 2);
	}

	@Test
	public void test07() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesCustomerMap(), 3);
	}

	@Test
	public void test08() throws Exception {
		Assert.assertEquals(gcshFile.getNofEntriesVendorMap(), 3);
	}
}
