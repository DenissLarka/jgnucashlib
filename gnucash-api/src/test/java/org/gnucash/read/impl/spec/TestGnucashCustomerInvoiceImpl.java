package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomerInvoiceImpl
{
  private static GnucashFile    gcshFile = null;
  private static GnucashCustVendInvoice invcGen = null;
  private static GnucashCustomerInvoice invcSpec = null;
  
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
    invcGen = gcshFile.getCustVendInvoiceByID("d9967c10fdf1465e9394a3e4b1e7bd79");
    invcSpec = new GnucashCustomerInvoiceImpl(invcGen);
    assertEquals(true, invcSpec instanceof GnucashCustomerInvoiceImpl);
    assertEquals("d9967c10fdf1465e9394a3e4b1e7bd79", invcSpec.getId());
    assertEquals("gncCustomer", invcSpec.getOwnerType());
    // ::TODO
    // assertEquals("xxx", invc.getInvoiceNumber());
    assertEquals(null, invcSpec.getDescription());

    assertEquals("2023-07-29T10:59Z", invcSpec.getDateOpened().toString());
    assertEquals("2023-07-29T10:59Z", invcSpec.getDatePosted().toString());

    // ::TODO
    assertEquals(2, invcGen.getCustVendInvcEntries().size());
    assertEquals(2, invcSpec.getCustVendInvcEntries().size());
    assertEquals(2, invcSpec.getEntries().size());

    // ::TODO
    // assertEquals(1327.60, invc.getAmountWithoutTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(1327.60, invcGen.getInvcAmountWithTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getInvcAmountWithTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getAmountWithTaxes().doubleValue(), Const.DIFF_TOLERANCE);
  }
}
