package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashFileImpl
{
  private GnucashFile    gcshFile = null;

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashFileImpl.class);  
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
      gcshFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME);
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
    assertEquals(92, gcshFile.getNofEntriesAccountMap());
  }

  @Test
  public void test02() throws Exception
  {    
    assertEquals(12, gcshFile.getNofEntriesTransactionMap());
  }

  @Test
  public void test03() throws Exception
  {    
    assertEquals(31, gcshFile.getNofEntriesTransactionSplitsMap());
  }

  @Test
  public void test04() throws Exception
  {    
    assertEquals(6, gcshFile.getNofEntriesGenerInvoiceMap());
  }

  @Test
  public void test05() throws Exception
  {    
    assertEquals(12, gcshFile.getNofEntriesGenerInvoiceEntriesMap());
  }

  @Test
  public void test06() throws Exception
  {    
    assertEquals(2, gcshFile.getNofEntriesGenerJobMap());
  }

  @Test
  public void test07() throws Exception
  {    
    assertEquals(3, gcshFile.getNofEntriesCustomerMap());
  }

  @Test
  public void test08() throws Exception
  {    
    assertEquals(3, gcshFile.getNofEntriesVendorMap());
  }
}
