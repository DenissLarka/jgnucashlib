package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashAccount
{
  private static GnucashFile    gcshFile = null;
  private static GnucashAccount acct = null;
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashAccount.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    InputStream gcshFileStream = null;
    try 
    {
      gcshFileStream = classLoader.getResourceAsStream(Const.GCSH_FILENAME);
    } 
    catch ( Exception exc ) 
    {
      System.err.println("Cannot generate input stream from resource");
      return;
    }
    
    try
    {
      gcshFile = new GnucashFileImpl(gcshFileStream);
    }
    catch ( Exception exc )
    {
      System.err.println("Cannot parse GnuCash file");
      exc.printStackTrace();
    }
  }

  // -----------------------------------------------------------------

  @Test
  public void test01_1() throws Exception
  {
    acct = gcshFile.getAccountByID("bbf77a599bd24a3dbfec3dd1d0bb9f5c");
    assertEquals("bbf77a599bd24a3dbfec3dd1d0bb9f5c", acct.getId());
    assertEquals(GnucashAccount.TYPE_BANK, acct.getType());
    assertEquals("Giro RaiBa", acct.getName());
    assertEquals("Root Account::Aktiva::Sichteinlagen::KK::Giro RaiBa", acct.getQualifiedName());
    assertEquals("Girokonto 1", acct.getDescription());
    assertEquals("EUR", acct.getCurrencyID());
    
    assertEquals("fdffaa52f5b04754901dfb1cf9221494", acct.getParentAccountId());

    assertEquals(3560.46, acct.getBalance().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(3560.46, acct.getBalanceRecursive().doubleValue(), Const.DIFF_TOLERANCE);

    assertEquals(5, acct.getTransactions().size());
    assertEquals("568864bfb0954897ab8578db4d27372f", acct.getTransactions().get(0).getId());
    assertEquals("29557cfdf4594eb68b1a1b710722f991", acct.getTransactions().get(1).getId());
    assertEquals("67796d4f7c924c1da38f7813dbc3a99d", acct.getTransactions().get(2).getId());
    assertEquals("18a45dfc8a6868c470438e27d6fe10b2", acct.getTransactions().get(3).getId());
    assertEquals("ccff780b18294435bf03c6cb1ac325c1", acct.getTransactions().get(4).getId());
  }

  @Test
  public void test01_2() throws Exception
  {
    acct = gcshFile.getAccountByID("cc2c4709633943c39293bfd73de88c9b");
    assertEquals("cc2c4709633943c39293bfd73de88c9b", acct.getId());
    assertEquals(GnucashAccount.TYPE_ASSET, acct.getType());
    assertEquals("Depot RaiBa", acct.getName());
    assertEquals("Root Account::Aktiva::Depots::Depot RaiBa", acct.getQualifiedName());
    assertEquals("Aktiendepot 1", acct.getDescription());
    assertEquals("EUR", acct.getCurrencyID());
    
    assertEquals("7ee6fe4de6db46fd957f3513c9c6f983", acct.getParentAccountId());

    // ::TODO
    assertEquals(0.00, acct.getBalance().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(0.00, acct.getBalanceRecursive().doubleValue(), Const.DIFF_TOLERANCE);

    // ::TODO
    assertEquals(0, acct.getTransactions().size());
//    assertEquals("568864bfb0954897ab8578db4d27372f", acct.getTransactions().get(0).getId());
//    assertEquals("18a45dfc8a6868c470438e27d6fe10b2", acct.getTransactions().get(1).getId());
  }

  @Test
  public void test01_3() throws Exception
  {
    acct = gcshFile.getAccountByID("5008258df86243ee86d37dee64327c27");
    assertEquals("5008258df86243ee86d37dee64327c27", acct.getId());
    assertEquals(GnucashAccount.TYPE_LIABILITY, acct.getType());
    assertEquals("Fremdkapital", acct.getName());
    assertEquals("Root Account::Fremdkapital", acct.getQualifiedName());
    assertEquals("alle Verbindlichkeiten", acct.getDescription());
    assertEquals("EUR", acct.getCurrencyID());
    
    assertEquals("14305dc80e034834b3f531696d81b493", acct.getParentAccountId());

    assertEquals(0.00, acct.getBalance().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(0.00, acct.getBalanceRecursive().doubleValue(), Const.DIFF_TOLERANCE);

    assertEquals(0, acct.getTransactions().size());
  }

  @Test
  public void test01_4() throws Exception
  {
    acct = gcshFile.getAccountByID("68a4c19f9a8c48909fc69d0dc18c37a6");
    assertEquals("68a4c19f9a8c48909fc69d0dc18c37a6", acct.getId());
    assertEquals(GnucashAccount.TYPE_PAYABLE, acct.getType());
    assertEquals("Lieferfanto", acct.getName());
    assertEquals("Root Account::Fremdkapital::Lieferanten::Lieferfanto", acct.getQualifiedName());
    assertEquals(null, acct.getDescription());
    assertEquals("EUR", acct.getCurrencyID());
    
    assertEquals("a6d76c8d72764905adecd78d955d25c0", acct.getParentAccountId());

    // ::TODO
    assertEquals(0.00, acct.getBalance().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(0.00, acct.getBalanceRecursive().doubleValue(), Const.DIFF_TOLERANCE);

    // ::TODO
    assertEquals(0, acct.getTransactions().size());
//  assertEquals("568864bfb0954897ab8578db4d27372f", acct.getTransactions().get(0).getId());
//  assertEquals("18a45dfc8a6868c470438e27d6fe10b2", acct.getTransactions().get(1).getId());
  }
  
  @Test
  public void test01_5() throws Exception
  {
    acct = gcshFile.getAccountByID("7e223ee2260d4ba28e8e9e19ce291f43");
    assertEquals("7e223ee2260d4ba28e8e9e19ce291f43", acct.getId());
    assertEquals(GnucashAccount.TYPE_RECEIVABLE, acct.getType());
    assertEquals("Unfug_Quatsch", acct.getName());
    assertEquals("Root Account::Aktiva::Forderungen::Unfug_Quatsch", acct.getQualifiedName());
    assertEquals(null, acct.getDescription());
    assertEquals("EUR", acct.getCurrencyID());
    
    assertEquals("74401ce4880c4f4487c4301027a71bde", acct.getParentAccountId());

    assertEquals(-2527.60, acct.getBalance().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(-2527.60, acct.getBalanceRecursive().doubleValue(), Const.DIFF_TOLERANCE);

    assertEquals(2, acct.getTransactions().size());
    assertEquals("29557cfdf4594eb68b1a1b710722f991", acct.getTransactions().get(0).getId());
    assertEquals("67796d4f7c924c1da38f7813dbc3a99d", acct.getTransactions().get(1).getId());
  }

  @Test
  public void test01_6() throws Exception
  {
    acct = gcshFile.getAccountByID("ebc834e7f20e4be38f445d655142d6b1");
    assertEquals("ebc834e7f20e4be38f445d655142d6b1", acct.getId());
    assertEquals(GnucashAccount.TYPE_EQUITY, acct.getType());
    assertEquals("Anfangsbestand", acct.getName());
    assertEquals("Root Account::Anfangsbestand", acct.getQualifiedName());
    assertEquals("Anfangsbestand", acct.getDescription());
    assertEquals("EUR", acct.getCurrencyID());
    
    assertEquals("14305dc80e034834b3f531696d81b493", acct.getParentAccountId());

    assertEquals(-4128.00, acct.getBalance().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(-4128.00, acct.getBalanceRecursive().doubleValue(), Const.DIFF_TOLERANCE);

    assertEquals(2, acct.getTransactions().size());
    assertEquals("cc9fe6a245df45ba9b494660732a7755", acct.getTransactions().get(0).getId());
    assertEquals("4307689faade47d8aab4db87c8ce3aaf", acct.getTransactions().get(1).getId());
  }

  @Test
  public void test01_7() throws Exception
  {
    acct = gcshFile.getAccountByID("d49554f33a0340bdb6611a1ab5575998");
    assertEquals("d49554f33a0340bdb6611a1ab5575998", acct.getId());
    assertEquals(GnucashAccount.TYPE_STOCK, acct.getType());
    assertEquals("DE0007100000 Mercedes-Benz", acct.getName());
    assertEquals("Root Account::Aktiva::Depots::Depot RaiBa::DE0007100000 Mercedes-Benz", acct.getQualifiedName());
    assertEquals("Mercedes-Benz Group AG", acct.getDescription());
    assertEquals("MBG", acct.getCurrencyID());
    
    assertEquals("cc2c4709633943c39293bfd73de88c9b", acct.getParentAccountId());

    assertEquals(100.00, acct.getBalance().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(100.00, acct.getBalanceRecursive().doubleValue(), Const.DIFF_TOLERANCE);

    assertEquals(1, acct.getTransactions().size());
    assertEquals("cc9fe6a245df45ba9b494660732a7755", acct.getTransactions().get(0).getId());
  }
}
