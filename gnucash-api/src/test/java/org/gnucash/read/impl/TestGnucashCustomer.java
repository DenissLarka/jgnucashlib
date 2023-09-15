package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomer
{
  private static GnucashFile     gcshFile = null;
  private static GnucashCustomer cust = null;
  
  private static final String CUST_1_ID = "5d1dd9afa7554553988669830cc1f696";
  private static final String CUST_2_ID = "f44645d2397946bcac90dff68cc03b76";
  private static final String CUST_3_ID = "1d2081e8a10e4d5e9312d9fff17d470d";

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
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
  public void test01_1() throws Exception
  {
    cust = gcshFile.getCustomerByID(CUST_1_ID);
    
    assertEquals(CUST_1_ID, cust.getId());
    assertEquals("000001", cust.getNumber());
    assertEquals("Unfug und Quatsch GmbH", cust.getName());
  }

  @Test
  public void test01_2() throws Exception
  {
    cust = gcshFile.getCustomerByID(CUST_2_ID);
    
    assertEquals(CUST_2_ID, cust.getId());
    assertEquals("000002", cust.getNumber());
    assertEquals("Is That So Ltd.", cust.getName());
  }

  @Test
  public void test01_3() throws Exception
  {
    cust = gcshFile.getCustomerByID(CUST_3_ID);
    
    assertEquals(CUST_3_ID, cust.getId());
    assertEquals("000003", cust.getNumber());
    assertEquals("N'importe Quoi S.A.", cust.getName());
  }

  @Test
  public void test02_1() throws Exception
  {
    cust = gcshFile.getCustomerByID(CUST_1_ID);
    
    assertEquals(1, cust.getPaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
    assertEquals("d9967c10fdf1465e9394a3e4b1e7bd79", 
                 ((GnucashCustomerInvoice) cust.getPaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0]).getId());
    assertEquals(1, cust.getNofOpenInvoices());
    assertEquals(1, cust.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
    assertEquals("6588f1757b9e4e24b62ad5b37b8d8e07", 
                 ((GnucashCustomerInvoice) cust.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0]).getId());
  }

  @Test
  public void test02_2() throws Exception
  {
    cust = gcshFile.getCustomerByID(CUST_2_ID);
    
    assertEquals(0, cust.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
//    assertEquals("[GnucashCustomerInvoiceImpl: id: d9967c10fdf1465e9394a3e4b1e7bd79 customer-id (dir.): 5d1dd9afa7554553988669830cc1f696 invoice-number: 'R1730' description: 'null' #entries: 0 date-opened: 2023-07-29]", 
//                 cust.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0].toString());
  }

  @Test
  public void test02_3() throws Exception
  {
    cust = gcshFile.getCustomerByID(CUST_3_ID);
    
    assertEquals(0, cust.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
//    assertEquals("[GnucashCustomerInvoiceImpl: id: d9967c10fdf1465e9394a3e4b1e7bd79 customer-id (dir.): 5d1dd9afa7554553988669830cc1f696 invoice-number: 'R1730' description: 'null' #entries: 0 date-opened: 2023-07-29]", 
//                 cust.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0].toString());
  }
}
