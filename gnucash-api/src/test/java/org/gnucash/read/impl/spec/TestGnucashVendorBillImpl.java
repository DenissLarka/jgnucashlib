package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashGenerInvoiceImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.SpecInvoiceCommon;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashVendorBillImpl
{
  private GnucashFile            gcshFile = null;
  private GnucashGenerInvoice bllGen = null;
  private GnucashVendorBill      bllSpec = null;
  
  private static final String BLL_1_ID = TestGnucashGenerInvoiceImpl.INVC_4_ID;
  private static final String BLL_2_ID = TestGnucashGenerInvoiceImpl.INVC_2_ID;

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
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
    bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);
    
    assertEquals(true, bllSpec instanceof GnucashVendorBillImpl);
    assertEquals(BLL_1_ID, bllSpec.getId());
    assertEquals("gncVendor", bllSpec.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT));
    assertEquals("1730-383/2", bllSpec.getNumber());
    assertEquals("Sie wissen schon: Gefälligkeiten, ne?", bllSpec.getDescription());

    assertEquals("2023-08-31T10:59Z", bllSpec.getDateOpened().toString());
    // ::TODO
    assertEquals("2023-08-31T10:59Z", bllSpec.getDatePosted().toString());
  }

  @Test
  public void test01_2() throws Exception
  {
    bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);
    
    assertEquals(true, bllSpec instanceof GnucashVendorBillImpl);
    assertEquals(BLL_2_ID, bllSpec.getId());
    assertEquals("gncVendor", bllSpec.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT));
    assertEquals("2740921", bllSpec.getNumber());
    assertEquals("Dat isjamaol eine schöne jepflejgte Reschnung!", bllSpec.getDescription());

    assertEquals("2023-08-30T10:59Z", bllSpec.getDateOpened().toString());
    // ::TODO
    assertEquals("2023-08-30T10:59Z", bllSpec.getDatePosted().toString());
  }

  @Test
  public void test02_1() throws Exception
  {
    bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1, bllGen.getGenerEntries().size());
    assertEquals(1, bllSpec.getGenerEntries().size());
    assertEquals(1, bllSpec.getEntries().size());

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(bllSpec.getEntries());
    assertEquals("0041b8d397f04ae4a2e9e3c7f991c4ec", 
                 ((GnucashVendorBillEntry) entrList.toArray()[0]).getId());
  }

  @Test
  public void test02_2() throws Exception
  {
    bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(2, bllGen.getGenerEntries().size());
    assertEquals(2, bllSpec.getGenerEntries().size());
    assertEquals(2, bllSpec.getEntries().size());

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(bllSpec.getEntries());
    assertEquals("513589a11391496cbb8d025fc1e87eaa", 
                 ((GnucashVendorBillEntry) entrList.toArray()[1]).getId());
    assertEquals("dc3c53f07ff64199ad4ea38988b3f40a", 
                 ((GnucashVendorBillEntry) entrList.toArray()[0]).getId());
  }

  @Test
  public void test03_1() throws Exception
  {
    bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(41.40, bllGen.getBillAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(41.40, bllSpec.getBillAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(41.40, ((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    // Note: due to (purposefully) incorrect booking, the gross amount
    // of this bill is *not* 49.27 EUR, but 41.40 EUR (its net amount).
    assertEquals(41.40, bllGen.getBillAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(41.40, bllSpec.getBillAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(41.40, ((SpecInvoiceCommon) bllSpec).getAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
  }

  @Test
  public void test03_2() throws Exception
  {
    bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(79.11, bllGen.getBillAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(79.11, bllSpec.getBillAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(79.11, ((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(94.14, bllGen.getBillAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(94.14, bllSpec.getBillAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(94.14, ((SpecInvoiceCommon) bllSpec).getAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
  }

  @Test
  public void test04_1() throws Exception
  {
    bllGen = gcshFile.getGenerInvoiceByID(BLL_1_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);

    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
//    assertEquals("xxx", invcGen.getPostTransaction());
//    assertEquals("xxx", invcSpec.getPostTransaction());
    
    // ::TODO
    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(0, bllSpec.getPayingTransactions().size());
    assertEquals(0, bllSpec.getPayingTransactions().size());
    
//    LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) bllSpec.getPayingTransactions();
//    Collections.sort(trxList);
//    assertEquals("xxx", 
//                 ((GnucashTransaction) bllSpec.getPayingTransactions().toArray()[0]).getId());

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(false, bllGen.isBillFullyPaid());
    assertEquals(false, bllSpec.isBillFullyPaid());
    assertEquals(false, ((SpecInvoiceCommon) bllSpec).isFullyPaid());
  }

  @Test
  public void test04_2() throws Exception
  {
    bllGen = gcshFile.getGenerInvoiceByID(BLL_2_ID);
    bllSpec = new GnucashVendorBillImpl(bllGen);

    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals("aa64d862bb5e4d749eb41f198b28d73d", bllGen.getPostTransaction().getId());
    assertEquals("aa64d862bb5e4d749eb41f198b28d73d", bllSpec.getPostTransaction().getId());
    
    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1, bllGen.getPayingTransactions().size());
    assertEquals(1, bllSpec.getPayingTransactions().size());
    
    LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) bllSpec.getPayingTransactions();
    Collections.sort(trxList);
    assertEquals("ccff780b18294435bf03c6cb1ac325c1", 
                 ((GnucashTransaction) trxList.toArray()[0]).getId());
    
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(true, bllGen.isBillFullyPaid());
    assertEquals(true, bllSpec.isBillFullyPaid());
    assertEquals(true, ((SpecInvoiceCommon) bllSpec).isFullyPaid());
  }
}
