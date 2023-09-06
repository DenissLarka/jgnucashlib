package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomerInvoiceImpl
{
  private static GnucashFile    gcshFile = null;
  private static GnucashCustomerInvoice invc = null;
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashCustomerInvoiceImpl.class);  
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
    invc = new GnucashCustomerInvoiceImpl( gcshFile.getCustVendInvoiceByID("d9967c10fdf1465e9394a3e4b1e7bd79") );
    assertEquals(true, invc instanceof GnucashCustomerInvoiceImpl);
    assertEquals("d9967c10fdf1465e9394a3e4b1e7bd79", invc.getId());
    assertEquals("gncCustomer", invc.getOwnerType());
    // ::TODO
    // assertEquals("xxx", invc.getInvoiceNumber());
    assertEquals(null, invc.getDescription());

    assertEquals("2023-07-29T10:59Z", invc.getDateOpened().toString());
    assertEquals("2023-07-29T10:59Z", invc.getDatePosted().toString());

    // ::TODO
    // assertEquals(1327.60, invc.getAmmountWithoutTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(1327.60, invc.getAmmountWithTaxes().doubleValue(), Const.DIFF_TOLERANCE);
  }
}
