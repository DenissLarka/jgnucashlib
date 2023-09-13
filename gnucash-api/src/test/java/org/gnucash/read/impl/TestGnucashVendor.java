package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.spec.GnucashVendorBill;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashVendor
{
  private static GnucashFile   gcshFile = null;
  private static GnucashVendor vend = null;
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashVendor.class);  
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
    vend = gcshFile.getVendorByID("087e1a3d43fa4ef9a9bdd4b4797c4231");
    assertEquals("087e1a3d43fa4ef9a9bdd4b4797c4231", vend.getId());
    assertEquals("000001", vend.getNumber());
    assertEquals("Lieferfanto AG", vend.getName());

    vend = gcshFile.getVendorByID("4f16fd55c0d64ebe82ffac0bb25fe8f5");
    assertEquals("4f16fd55c0d64ebe82ffac0bb25fe8f5", vend.getId());
    assertEquals("000002", vend.getNumber());
    assertEquals("Super Suppliers Corp.", vend.getName());

    vend = gcshFile.getVendorByID("bc1c7a6d0a6c4b4ea7dd9f8eb48f79f7");
    assertEquals("bc1c7a6d0a6c4b4ea7dd9f8eb48f79f7", vend.getId());
    assertEquals("000003", vend.getNumber());
    assertEquals("Achetez Chez Nous S.A.", vend.getName());
  }


  @Test
  public void test02() throws Exception
  {
    vend = gcshFile.getVendorByID("087e1a3d43fa4ef9a9bdd4b4797c4231");
//  assertEquals("xxx", 
//  ((GnucashVendorBill) vend.getPaidBills(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[1]).getId() );
    assertEquals(2, vend.getNofOpenBills());
    assertEquals(2, vend.getUnpaidBills(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
    assertEquals("4eb0dc387c3f4daba57b11b2a657d8a4", 
                 ((GnucashVendorBill) vend.getUnpaidBills(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0]).getId() );
    assertEquals("286fc2651a7848038a23bb7d065c8b67", 
                 ((GnucashVendorBill) vend.getUnpaidBills(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[1]).getId() );

    vend = gcshFile.getVendorByID("4f16fd55c0d64ebe82ffac0bb25fe8f5");
    assertEquals(0, vend.getUnpaidBills(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
//    assertEquals("[GnucashVendorBillImpl: id: 4eb0dc387c3f4daba57b11b2a657d8a4 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: '1730-383/2' description: 'Sie wissen schon: Gefälligkeiten, ne?' #entries: 1 date-opened: 2023-08-31]", 
//                 vend.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0].toString());
//    assertEquals("[GnucashVendorBillImpl: id: 286fc2651a7848038a23bb7d065c8b67 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: null description: 'Dat isjamaol eine schöne jepflejgte Reschnung!' #entries: 1 date-opened: 2023-08-30]", 
//                 vend.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[1].toString());

    vend = gcshFile.getVendorByID("bc1c7a6d0a6c4b4ea7dd9f8eb48f79f7");
    assertEquals(0, vend.getUnpaidBills(GnucashCustVendInvoice.ReadVariant.DIRECT).size());
//    assertEquals("[GnucashVendorBillImpl: id: 4eb0dc387c3f4daba57b11b2a657d8a4 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: '1730-383/2' description: 'Sie wissen schon: Gefälligkeiten, ne?' #entries: 1 date-opened: 2023-08-31]", 
//                 vend.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[0].toString());
//    assertEquals("[GnucashVendorBillImpl: id: 286fc2651a7848038a23bb7d065c8b67 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: null description: 'Dat isjamaol eine schöne jepflejgte Reschnung!' #entries: 1 date-opened: 2023-08-30]", 
//                 vend.getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant.DIRECT).toArray()[1].toString());
  }
}
