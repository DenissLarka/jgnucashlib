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
  
  private static final double VALUE_DIFF_TOLERANCE = 0.001; // ::MAGIC

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
  public void test01() throws Exception
  {
    acct = gcshFile.getAccountByID("bbf77a599bd24a3dbfec3dd1d0bb9f5c");
    assertEquals("bbf77a599bd24a3dbfec3dd1d0bb9f5c", acct.getId());
    assertEquals("BANK", acct.getType());
    assertEquals("Giro RaiBa", acct.getName());
    assertEquals("Root Account::Aktiva::Sichteinlagen::KK::Giro RaiBa", acct.getQualifiedName());
    assertEquals("Girokonto", acct.getDescription());
    assertEquals("EUR", acct.getCurrencyID());
    
    assertEquals("fdffaa52f5b04754901dfb1cf9221494", acct.getParentAccountId());

    assertEquals(1127.00, acct.getBalance().doubleValue(), VALUE_DIFF_TOLERANCE);
    assertEquals(1127.00, acct.getBalanceRecursive().doubleValue(), VALUE_DIFF_TOLERANCE);

    assertEquals(2, acct.getTransactions().size());
    assertEquals("568864bfb0954897ab8578db4d27372f", acct.getTransactions().get(0).getId());
    assertEquals("18a45dfc8a6868c470438e27d6fe10b2", acct.getTransactions().get(1).getId());
  }
}
