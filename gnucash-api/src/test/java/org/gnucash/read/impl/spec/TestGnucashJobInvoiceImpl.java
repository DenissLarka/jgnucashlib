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
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashJobInvoiceEntry;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashJobInvoiceImpl
{
  private static GnucashFile         gcshFile = null;
  private static GnucashGenerInvoice invcGen = null;
  private static GnucashJobInvoice   invcSpec = null;
  
  private static final String INVC_3_ID = TestGnucashGenerInvoiceImpl.INVC_3_ID;
  
  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashJobInvoiceImpl.class);  
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
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);
    
    assertEquals(true, invcSpec instanceof GnucashJobInvoiceImpl);
    assertEquals(INVC_3_ID, invcSpec.getId());
    assertEquals("gncJob", invcSpec.getOwnerType());
    assertEquals("R94871", invcSpec.getNumber());
    assertEquals("With job / with taxes", invcSpec.getDescription());

    assertEquals("2023-09-20T10:59Z", invcSpec.getDateOpened().toString());
    assertEquals("2023-09-20T10:59Z", invcSpec.getDatePosted().toString());
  }

  @Test
  public void test02_1() throws Exception
  {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(3, invcGen.getGenerInvcEntries().size());
    assertEquals(3, invcSpec.getGenerInvcEntries().size());
    assertEquals(3, invcSpec.getEntries().size());

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(invcSpec.getEntries());
    assertEquals("fa483972d10a4ce0abf2a7e1319706e7", 
                 ((GnucashJobInvoiceEntry) entrList.toArray()[0]).getId());
    assertEquals("eb5eb3b7c1e34965b36fb6d5af183e82", 
                 ((GnucashJobInvoiceEntry) entrList.toArray()[1]).getId());
    assertEquals("993eae09ce664094adf63b85509de2bc", 
                 ((GnucashJobInvoiceEntry) entrList.toArray()[2]).getId());
  }

  @Test
  public void test03_1() throws Exception
  {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    // ::TODO
//    assertEquals(1327.60, invcGen.getJobAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
//    assertEquals(1327.60, invcSpec.getJobAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    // ::TODO
//    assertEquals(1327.60, invcGen.getJobAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
//    assertEquals(1327.60, invcSpec.getJobAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
  }

  // ::TODO
  @Test
  public void test04_1() throws Exception
  {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);

    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals("c97032ba41684b2bb5d1391c9d7547e9", invcGen.getPostTransaction().getId());
    assertEquals("c97032ba41684b2bb5d1391c9d7547e9", invcSpec.getPostTransaction().getId());
    
    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1, invcGen.getPayingTransactions().size());
    assertEquals(1, invcSpec.getPayingTransactions().size());

    LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) invcSpec.getPayingTransactions();
    Collections.sort(trxList);
    assertEquals("29557cfdf4594eb68b1a1b710722f991", 
                 ((GnucashTransaction) trxList.toArray()[0]).getId());

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(true, invcGen.isInvcFullyPaid());
    assertEquals(true, invcSpec.isInvcFullyPaid());
    assertEquals(true, invcSpec.isFullyPaid());
  }
}
