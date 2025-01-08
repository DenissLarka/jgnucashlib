package org.gnucash.read.impl.auxiliary;

import java.io.InputStream;
import java.util.Collection;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.auxiliary.GCshTaxTableEntry;
import org.gnucash.read.impl.GnucashFileImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGCshTaxTableImpl {
	// DE
	// Note the funny parent/child pair.
	private static final String TAXTABLE_DE_1_1_ID = "3c9690f9f31b4cd0baa936048b833c06"; // DE_USt_Std "parent"
	private static final String TAXTABLE_DE_1_2_ID = "cba6011c826f426fbc4a1a72c3d6c8ee"; // DE_USt_Std "child"
	private static final String TAXTABLE_DE_2_ID = "c518af53a93c4a5cb3e2161b7b358e68"; // DE_USt_red

	// FR
	public static final String TAXTABLE_FR_1_ID = "de4c17d1eb0e4f088ba73d4c697032f0"; // FR_TVA_Std
	private static final String TAXTABLE_FR_2_ID = "e279d5cc81204f1bb6cf672ef3357c0c"; // FR_TVA_red

	// UK
	public static final String TAXTABLE_UK_1_ID = "0bc4e576896a4fb4a2779dcf310f82f1"; // UK_VAT_Std
	private static final String TAXTABLE_UK_2_ID = "9d33a0082d9241ac89aa8e907f30d1db"; // UK_VAT_red

	private static final String TAX_ACCT_ID = "1a5b06dada56466197edbd15e64fd425"; // Root Account::Fremdkapital::Steuerverbindl

	private GnucashFile gcshFile = null;
	private GCshTaxTable taxTab = null;

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
		Collection<GCshTaxTable> taxTableList = gcshFile.getTaxTables();

		Assert.assertEquals(taxTableList.size(), 7);

		// ::TODO: Sort array for predictability
		Object[] taxTableArr = taxTableList.toArray();

		Assert.assertEquals(((GCshTaxTable) taxTableArr[0]).getId(), TAXTABLE_UK_2_ID);
		Assert.assertEquals(((GCshTaxTable) taxTableArr[1]).getId(), TAXTABLE_FR_2_ID);
		Assert.assertEquals(((GCshTaxTable) taxTableArr[2]).getId(), TAXTABLE_DE_1_1_ID);
		Assert.assertEquals(((GCshTaxTable) taxTableArr[3]).getId(), TAXTABLE_UK_1_ID);
		Assert.assertEquals(((GCshTaxTable) taxTableArr[4]).getId(), TAXTABLE_DE_2_ID);
		Assert.assertEquals(((GCshTaxTable) taxTableArr[5]).getId(), TAXTABLE_FR_1_ID);
		Assert.assertEquals(((GCshTaxTable) taxTableArr[6]).getId(), TAXTABLE_DE_1_2_ID);
	}

	@Test
	public void test02_1_1() throws Exception {
		taxTab = gcshFile.getTaxTableByID(TAXTABLE_DE_1_1_ID);

		Assert.assertEquals(taxTab.getId(), TAXTABLE_DE_1_1_ID);
		Assert.assertEquals(taxTab.getName(), "DE_USt_Std");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 19.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test02_1_2() throws Exception {
		taxTab = gcshFile.getTaxTableByName("DE_USt_Std");

		Assert.assertEquals(taxTab.getId(), TAXTABLE_DE_1_1_ID);
		Assert.assertEquals(taxTab.getName(), "DE_USt_Std");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 19.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test02_2_1() throws Exception {
		taxTab = gcshFile.getTaxTableByID(TAXTABLE_DE_1_2_ID);

		Assert.assertEquals(taxTab.getId(), TAXTABLE_DE_1_2_ID);
		Assert.assertEquals(taxTab.getName(), "USt_Std"); // sic, old name w/o prefix "DE_"
		Assert.assertEquals(taxTab.getParentID(), TAXTABLE_DE_1_1_ID);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 19.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test02_2_2() throws Exception {
		taxTab = gcshFile.getTaxTableByName("USt_Std");

		Assert.assertEquals(taxTab.getId(), TAXTABLE_DE_1_2_ID);
		Assert.assertEquals(taxTab.getName(), "USt_Std"); // sic, old name w/o prefix "DE_"
		Assert.assertEquals(taxTab.getParentID(), TAXTABLE_DE_1_1_ID);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 19.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test03_1() throws Exception {
		taxTab = gcshFile.getTaxTableByID(TAXTABLE_DE_2_ID);

		Assert.assertEquals(taxTab.getId(), TAXTABLE_DE_2_ID);
		Assert.assertEquals(taxTab.getName(), "DE_USt_red");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 7.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test03_2() throws Exception {
		taxTab = gcshFile.getTaxTableByName("DE_USt_red");

		Assert.assertEquals(taxTab.getId(), TAXTABLE_DE_2_ID);
		Assert.assertEquals(taxTab.getName(), "DE_USt_red");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 7.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test04_1() throws Exception {
		taxTab = gcshFile.getTaxTableByID(TAXTABLE_FR_1_ID);

		Assert.assertEquals(taxTab.getId(), TAXTABLE_FR_1_ID);
		Assert.assertEquals(taxTab.getName(), "FR_TVA_Std");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 20.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test04_2() throws Exception {
		taxTab = gcshFile.getTaxTableByName("FR_TVA_Std");

		Assert.assertEquals(taxTab.getId(), TAXTABLE_FR_1_ID);
		Assert.assertEquals(taxTab.getName(), "FR_TVA_Std");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 20.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test05_1() throws Exception {
		taxTab = gcshFile.getTaxTableByID(TAXTABLE_FR_2_ID);

		Assert.assertEquals(taxTab.getId(), TAXTABLE_FR_2_ID);
		Assert.assertEquals(taxTab.getName(), "FR_TVA_red");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 10.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}

	@Test
	public void test05_2() throws Exception {
		taxTab = gcshFile.getTaxTableByName("FR_TVA_red");

		Assert.assertEquals(taxTab.getId(), TAXTABLE_FR_2_ID);
		Assert.assertEquals(taxTab.getName(), "FR_TVA_red");
		Assert.assertEquals(taxTab.getParentID(), null);

		Assert.assertEquals(taxTab.getEntries().size(), 1);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), 10.0, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType(), GCshTaxTableEntry.TYPE_PERCENT);
		Assert.assertEquals(((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID(), TAX_ACCT_ID);
	}
}
