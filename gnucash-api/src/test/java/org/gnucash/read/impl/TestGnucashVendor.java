package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashInvoice;
import org.gnucash.read.GnucashVendor;
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
    assertEquals("Lieferfanto", vend.getName());
  }


  @Test
  public void test02() throws Exception
  {
    vend = gcshFile.getVendorByID("087e1a3d43fa4ef9a9bdd4b4797c4231");
    assertEquals(2, vend.getUnpayedInvoices(GnucashInvoice.ReadVariant.DIRECT).size());
    assertEquals("[GnucashVendorInvoiceImpl: id: 4eb0dc387c3f4daba57b11b2a657d8a4 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 invoice-number: null description: 'Sie wissen schon: Gefälligkeiten, ne?' #entries: 0 dateOpened: 2023-08-31]", 
                 vend.getUnpayedInvoices(GnucashInvoice.ReadVariant.DIRECT).toArray()[0].toString());
    assertEquals("[GnucashVendorInvoiceImpl: id: 286fc2651a7848038a23bb7d065c8b67 vendor-id (dir.): 087e1a3d43fa4ef9a9bdd4b4797c4231 invoice-number: null description: 'Dat isjamaol eine schöne jepflejgte Reschnung!' #entries: 0 dateOpened: 2023-08-30]", 
                 vend.getUnpayedInvoices(GnucashInvoice.ReadVariant.DIRECT).toArray()[1].toString());
  }
}
