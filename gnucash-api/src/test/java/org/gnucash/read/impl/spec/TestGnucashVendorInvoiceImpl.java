package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashVendorInvoiceImpl
{
  private static GnucashFile    gcshFile = null;
  private static GnucashVendorBill invc = null;
  
  private static final double VALUE_DIFF_TOLERANCE = 0.001; // ::MAGIC

  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashVendorInvoiceImpl.class);  
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
    invc = new GnucashVendorBillImpl( gcshFile.getCustVendInvoiceByID("4eb0dc387c3f4daba57b11b2a657d8a4") );
    assertEquals(true, invc instanceof GnucashVendorBillImpl);
    assertEquals("4eb0dc387c3f4daba57b11b2a657d8a4", invc.getId());
    assertEquals("gncVendor", invc.getOwnerType());
    // ::TODO
    // assertEquals("xxx", invc.getInvoiceNumber());
    assertEquals("Sie wissen schon: Gefälligkeiten, ne?", invc.getDescription());

    // ::TODO
    // assertEquals("xxx", invc.getDateOpened().toString());
    assertEquals("2023-08-31T10:59Z", invc.getDatePosted().toString());
  }
}
