package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashVendorBillImpl
{
  private static GnucashFile            gcshFile = null;
  private static GnucashCustVendInvoice bllGen = null;
  private static GnucashVendorBill      bllSpec = null;
  
  private static final double VALUE_DIFF_TOLERANCE = 0.001; // ::MAGIC

  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashVendorBillImpl.class);  
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
    bllGen = gcshFile.getCustVendInvoiceByID("4eb0dc387c3f4daba57b11b2a657d8a4");
    bllSpec = new GnucashVendorBillImpl(bllGen);
    
    assertEquals(true, bllSpec instanceof GnucashVendorBillImpl);
    assertEquals("4eb0dc387c3f4daba57b11b2a657d8a4", bllSpec.getId());
    assertEquals("gncVendor", bllSpec.getOwnerType());
    assertEquals("1730-383/2", bllSpec.getNumber());
    assertEquals("Sie wissen schon: Gefälligkeiten, ne?", bllSpec.getDescription());

    assertEquals("2023-08-31T10:59Z", bllSpec.getDateOpened().toString());
    // ::TODO
    assertEquals("2023-08-31T10:59Z", bllSpec.getDatePosted().toString());
  }

  @Test
  public void test02() throws Exception
  {
    bllGen = gcshFile.getCustVendInvoiceByID("4eb0dc387c3f4daba57b11b2a657d8a4");
    bllSpec = new GnucashVendorBillImpl(bllGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1, bllGen.getCustVendInvcEntries().size());
    assertEquals(1, bllSpec.getCustVendInvcEntries().size());
    assertEquals(1, bllSpec.getEntries().size());

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(41.40, bllGen.getBillAmountWithoutTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(41.40, bllSpec.getBillAmountWithoutTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(41.40, bllSpec.getAmountWithoutTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    // Note: due to (purposefully) incorrect booking, the gross amount
    // of this bill is *not* 49.27 EUR, but 41.40 EUR (its net amount).
    assertEquals(41.40, bllGen.getBillAmountWithTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(41.40, bllSpec.getBillAmountWithTaxes().doubleValue(), Const.DIFF_TOLERANCE);
    assertEquals(41.40, bllSpec.getAmountWithTaxes().doubleValue(), Const.DIFF_TOLERANCE);
  }
}
