package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.spec.GnucashVendorBill;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashVendorImpl
{
  private GnucashFile   gcshFile = null;
  private GnucashVendor vend = null;
  
  public static final String VEND_1_ID = "087e1a3d43fa4ef9a9bdd4b4797c4231";
  public static final String VEND_2_ID = "4f16fd55c0d64ebe82ffac0bb25fe8f5";
  public static final String VEND_3_ID = "bc1c7a6d0a6c4b4ea7dd9f8eb48f79f7";

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashVendorImpl.class);  
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
    vend = gcshFile.getVendorByID(VEND_1_ID);
    
    assertEquals(VEND_1_ID, vend.getId());
    assertEquals("000001", vend.getNumber());
    assertEquals("Lieferfanto AG", vend.getName());
  }

  @Test
  public void test01_2() throws Exception
  {
    vend = gcshFile.getVendorByID(VEND_2_ID);
    
    assertEquals(VEND_2_ID, vend.getId());
    assertEquals("000002", vend.getNumber());
    assertEquals("Super Suppliers Corp.", vend.getName());
  }

  @Test
  public void test01_3() throws Exception
  {
    vend = gcshFile.getVendorByID(VEND_3_ID);
    
    assertEquals(VEND_3_ID, vend.getId());
    assertEquals("000003", vend.getNumber());
    assertEquals("Achetez Chez Nous S.A.", vend.getName());
  }

  @Test
  public void test02_1() throws Exception
  {
    vend = gcshFile.getVendorByID(VEND_1_ID);
    
    assertEquals(1, vend.getNofOpenBills());
    assertEquals(1, vend.getUnpaidBills().size());
    assertEquals(1, vend.getPaidBills().size());
    
    LinkedList<GnucashVendorBill> bllList = (LinkedList<GnucashVendorBill>) vend.getUnpaidBills();
    Collections.sort(bllList);
    assertEquals("4eb0dc387c3f4daba57b11b2a657d8a4", 
                 ((GnucashVendorBill) bllList.toArray()[0]).getId() );

    bllList = (LinkedList<GnucashVendorBill>) vend.getPaidBills();
    Collections.sort(bllList);
    assertEquals("286fc2651a7848038a23bb7d065c8b67", 
                 ((GnucashVendorBill) bllList.toArray()[0]).getId() );
  }

  @Test
  public void test02_2() throws Exception
  {
    vend = gcshFile.getVendorByID(VEND_2_ID);
    
    assertEquals(0, vend.getUnpaidBills().size());
//    assertEquals("[GnucashVendorBillImpl: id: 4eb0dc387c3f4daba57b11b2a657d8a4 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: '1730-383/2' description: 'Sie wissen schon: Gefälligkeiten, ne?' #entries: 1 date-opened: 2023-08-31]", 
//                 vend.getUnpaidInvoices().toArray()[0].toString());
//    assertEquals("[GnucashVendorBillImpl: id: 286fc2651a7848038a23bb7d065c8b67 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: null description: 'Dat isjamaol eine schöne jepflejgte Reschnung!' #entries: 1 date-opened: 2023-08-30]", 
//                 vend.getUnpaidInvoices().toArray()[1].toString());
  }
  
  @Test
  public void test02_3() throws Exception
  {
    vend = gcshFile.getVendorByID(VEND_3_ID);
    
    assertEquals(0, vend.getUnpaidBills().size());
//    assertEquals("[GnucashVendorBillImpl: id: 4eb0dc387c3f4daba57b11b2a657d8a4 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: '1730-383/2' description: 'Sie wissen schon: Gefälligkeiten, ne?' #entries: 1 date-opened: 2023-08-31]", 
//                 vend.getUnpaidInvoices().toArray()[0].toString());
//    assertEquals("[GnucashVendorBillImpl: id: 286fc2651a7848038a23bb7d065c8b67 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 bill-number: null description: 'Dat isjamaol eine schöne jepflejgte Reschnung!' #entries: 1 date-opened: 2023-08-30]", 
//                 vend.getUnpaidInvoices().toArray()[1].toString());
  }
}
