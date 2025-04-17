package org.gnucash.read.impl;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashAccountImpl {
	private GnucashFile gcshFile = null;
	private GnucashAccount acct = null;

	private static final String ACCT_1_ID = "bbf77a599bd24a3dbfec3dd1d0bb9f5c";
	private static final String ACCT_2_ID = "cc2c4709633943c39293bfd73de88c9b";
	private static final String ACCT_3_ID = "5008258df86243ee86d37dee64327c27";
	private static final String ACCT_4_ID = "68a4c19f9a8c48909fc69d0dc18c37a6";
	private static final String ACCT_5_ID = "7e223ee2260d4ba28e8e9e19ce291f43";
	private static final String ACCT_6_ID = "ebc834e7f20e4be38f445d655142d6b1";
	private static final String ACCT_7_ID = "d49554f33a0340bdb6611a1ab5575998";

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
		acct = gcshFile.getAccountByID(ACCT_1_ID);

		Assert.assertEquals(acct.getId(), ACCT_1_ID);
		Assert.assertEquals(acct.getType(), GnucashAccount.TYPE_BANK);
		Assert.assertEquals(acct.getName(), "Giro RaiBa");
		Assert.assertEquals(acct.getQualifiedName(), "Root Account::Aktiva::Sichteinlagen::KK::Giro RaiBa");
		Assert.assertEquals(acct.getDescription(), "Girokonto 1");
		Assert.assertEquals(acct.getCurrencyID(), "EUR");

		Assert.assertEquals(acct.getParentAccountId(), "fdffaa52f5b04754901dfb1cf9221494");

		Assert.assertEquals(acct.getBalance().doubleValue(), 3560.46, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(acct.getBalanceRecursive().doubleValue(), 3560.46, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(acct.getTransactions().size(), 5);
		Assert.assertEquals("568864bfb0954897ab8578db4d27372f", acct.getTransactions().get(0).getId());
		Assert.assertEquals("29557cfdf4594eb68b1a1b710722f991", acct.getTransactions().get(1).getId());
		Assert.assertEquals("67796d4f7c924c1da38f7813dbc3a99d", acct.getTransactions().get(2).getId());
		Assert.assertEquals("18a45dfc8a6868c470438e27d6fe10b2", acct.getTransactions().get(3).getId());
		Assert.assertEquals("ccff780b18294435bf03c6cb1ac325c1", acct.getTransactions().get(4).getId());
	}

	@Test
	public void test01_2() throws Exception {
		acct = gcshFile.getAccountByID(ACCT_2_ID);

		Assert.assertEquals(ACCT_2_ID, acct.getId());
		Assert.assertEquals(GnucashAccount.TYPE_ASSET, acct.getType());
		Assert.assertEquals("Depot RaiBa", acct.getName());
		Assert.assertEquals("Root Account::Aktiva::Depots::Depot RaiBa", acct.getQualifiedName());
		Assert.assertEquals("Aktiendepot 1", acct.getDescription());
		Assert.assertEquals("EUR", acct.getCurrencyID());

		Assert.assertEquals("7ee6fe4de6db46fd957f3513c9c6f983", acct.getParentAccountId());

		// ::TODO
		Assert.assertEquals(acct.getBalance().doubleValue(), 0.00, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(acct.getBalanceRecursive().doubleValue(), 0.00, ConstTest.DIFF_TOLERANCE);

		// ::TODO
		Assert.assertEquals(acct.getTransactions().size(), 0);
		//    assertEquals( acct.getTransactions().get(0).getId(),"568864bfb0954897ab8578db4d27372f");
		//    assertEquals( acct.getTransactions().get(1).getId(),"18a45dfc8a6868c470438e27d6fe10b2");
	}

	@Test
	public void test01_3() throws Exception {
		acct = gcshFile.getAccountByID(ACCT_3_ID);

		Assert.assertEquals(acct.getId(), ACCT_3_ID);
		Assert.assertEquals(acct.getType(), GnucashAccount.TYPE_LIABILITY);
		Assert.assertEquals(acct.getName(), "Fremdkapital");
		Assert.assertEquals(acct.getQualifiedName(), "Root Account::Fremdkapital");
		Assert.assertEquals(acct.getDescription(), "alle Verbindlichkeiten");
		Assert.assertEquals(acct.getCurrencyID(), "EUR");

		Assert.assertEquals(acct.getParentAccountId(), "14305dc80e034834b3f531696d81b493");

		Assert.assertEquals(acct.getBalance().doubleValue(), 0.00, ConstTest.DIFF_TOLERANCE);
		// ::CHECK: Should'nt the value in the following assert be positive
		// (that's how it is displayed in GnuCacsh, after all, at least with
		// standard settings).
		Assert.assertEquals(acct.getBalanceRecursive().doubleValue(), -289.92, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(acct.getTransactions().size(), 0);
	}

	@Test
	public void test01_4() throws Exception {
		acct = gcshFile.getAccountByID(ACCT_4_ID);

		Assert.assertEquals(acct.getId(), ACCT_4_ID);
		Assert.assertEquals(acct.getType(), GnucashAccount.TYPE_PAYABLE);
		Assert.assertEquals(acct.getName(), "Lieferfanto");
		Assert.assertEquals(acct.getQualifiedName(), "Root Account::Fremdkapital::Lieferanten::Lieferfanto");
		Assert.assertEquals(acct.getDescription(), null);
		Assert.assertEquals(acct.getCurrencyID(), "EUR");

		Assert.assertEquals(acct.getParentAccountId(), "a6d76c8d72764905adecd78d955d25c0");

		// ::TODO
		Assert.assertEquals(acct.getBalance().doubleValue(), 0.00, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(acct.getBalanceRecursive().doubleValue(), 0.00, ConstTest.DIFF_TOLERANCE);

		// ::TODO
		Assert.assertEquals(acct.getTransactions().size(), 2);
		Assert.assertEquals(acct.getTransactions().get(0).getId(), "aa64d862bb5e4d749eb41f198b28d73d");
		Assert.assertEquals(acct.getTransactions().get(1).getId(), "ccff780b18294435bf03c6cb1ac325c1");
	}

	@Test
	public void test01_5() throws Exception {
		acct = gcshFile.getAccountByID(ACCT_5_ID);

		Assert.assertEquals(acct.getId(), ACCT_5_ID);
		Assert.assertEquals(acct.getType(), GnucashAccount.TYPE_RECEIVABLE);
		Assert.assertEquals(acct.getName(), "Unfug_Quatsch");
		Assert.assertEquals(acct.getQualifiedName(), "Root Account::Aktiva::Forderungen::Unfug_Quatsch");
		Assert.assertEquals(acct.getDescription(), null);
		Assert.assertEquals(acct.getCurrencyID(), "EUR");

		Assert.assertEquals(acct.getParentAccountId(), "74401ce4880c4f4487c4301027a71bde");

		Assert.assertEquals(acct.getBalance().doubleValue(), 709.95, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(acct.getBalanceRecursive().doubleValue(), 709.95, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(acct.getTransactions().size(), 4);
		Assert.assertEquals(acct.getTransactions().get(0).getId(), "c97032ba41684b2bb5d1391c9d7547e9");
		Assert.assertEquals(acct.getTransactions().get(1).getId(), "29557cfdf4594eb68b1a1b710722f991");
		Assert.assertEquals(acct.getTransactions().get(2).getId(), "9e066e5f3081485ab08539e41bf85495");
		Assert.assertEquals(acct.getTransactions().get(3).getId(), "67796d4f7c924c1da38f7813dbc3a99d");
	}

	@Test
	public void test01_6() throws Exception {
		acct = gcshFile.getAccountByID(ACCT_6_ID);

		Assert.assertEquals(acct.getId(), ACCT_6_ID);
		Assert.assertEquals(acct.getType(), GnucashAccount.TYPE_EQUITY);
		Assert.assertEquals(acct.getName(), "Anfangsbestand");
		Assert.assertEquals(acct.getQualifiedName(), "Root Account::Anfangsbestand");
		Assert.assertEquals(acct.getDescription(), "Anfangsbestand");
		Assert.assertEquals(acct.getCurrencyID(), "EUR");

		Assert.assertEquals(acct.getParentAccountId(), "14305dc80e034834b3f531696d81b493");

		Assert.assertEquals(acct.getBalance().doubleValue(), -4128.00, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(acct.getBalanceRecursive().doubleValue(), -4128.00, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(acct.getTransactions().size(), 2);
		Assert.assertEquals(acct.getTransactions().get(0).getId(), "cc9fe6a245df45ba9b494660732a7755");
		Assert.assertEquals(acct.getTransactions().get(1).getId(), "4307689faade47d8aab4db87c8ce3aaf");
	}

	@Test
	public void test01_7() throws Exception {
		acct = gcshFile.getAccountByID(ACCT_7_ID);

		Assert.assertEquals(acct.getId(), ACCT_7_ID);
		Assert.assertEquals(acct.getType(), GnucashAccount.TYPE_STOCK);
		Assert.assertEquals(acct.getName(), "DE0007100000 Mercedes-Benz");
		Assert.assertEquals(acct.getQualifiedName(), "Root Account::Aktiva::Depots::Depot RaiBa::DE0007100000 Mercedes-Benz");
		Assert.assertEquals(acct.getDescription(), "Mercedes-Benz Group AG");
		Assert.assertEquals(acct.getCurrencyID(), "MBG");

		Assert.assertEquals(acct.getParentAccountId(), ACCT_2_ID);

		Assert.assertEquals(acct.getBalance().doubleValue(), 100.00, ConstTest.DIFF_TOLERANCE);
		Assert.assertEquals(acct.getBalanceRecursive().doubleValue(), 100.00, ConstTest.DIFF_TOLERANCE);

		Assert.assertEquals(acct.getTransactions().size(), 1);
		Assert.assertEquals(acct.getTransactions().get(0).getId(), "cc9fe6a245df45ba9b494660732a7755");
	}
}
