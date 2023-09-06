package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashCustVendInvoice;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomer
{
  private static GnucashFile     gcshFile = null;
  private static GnucashCustomer cust = null;
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashCustomer.class);  
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
    cust = gcshFile.getCustomerByID("5d1dd9afa7554553988669830cc1f696");
    assertEquals("5d1dd9afa7554553988669830cc1f696", cust.getId());
    assertEquals("000001", cust.getNumber());
    assertEquals("Blödfug und Quatsch", cust.getName());
  }

  @Test
  public void test02() throws Exception
  {
    cust = gcshFile.getCustomerByID("5d1dd9afa7554553988669830cc1f696");
    assertEquals(1, cust.getUnpayedInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
    assertEquals("[GnucashCustomerInvoiceImpl: id: d9967c10fdf1465e9394a3e4b1e7bd79 customer-id (dir.): 5d1dd9afa7554553988669830cc1f696 invoice-number: null description: 'null' #entries: 0 dateOpened: 2023-07-29]", 
                 cust.getUnpayedInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0].toString());
  }
}
