package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerInvoiceEntry;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomerInvoiceImpl
{
  private static GnucashFile            gcshFile = null;
  private static GnucashGenerInvoice invcGen = null;
  private static GnucashCustomerInvoice invcSpec = null;
  
  private static final String INVC_1_ID = "d9967c10fdf1465e9394a3e4b1e7bd79";
  
  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
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
    invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
    invcSpec = new GnucashCustomerInvoiceImpl(invcGen);
    
    assertEquals(true, invcSpec instanceof GnucashCustomerInvoiceImpl);
    assertEquals(INVC_1_ID, invcSpec.getId());
    assertEquals("gncCustomer", invcSpec.getOwnerType());
    assertEquals("R1730", invcSpec.getNumber());
    assertEquals("Alles ohne Steuern / voll bezahlt", invcSpec.getDescription());

    assertEquals("2023-07-29T10:59Z", invcSpec.getDateOpened().toString());
    assertEquals("2023-07-29T10:59Z", invcSpec.getDatePosted().toString());
  }

  @Test
  public void test02_1() throws Exception
  {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
    invcSpec = new GnucashCustomerInvoiceImpl(invcGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(2, invcGen.getGenerInvcEntries().size());
    assertEquals(2, invcSpec.getGenerInvcEntries().size());
    assertEquals(2, invcSpec.getEntries().size());

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(invcSpec.getEntries());
    assertEquals("92e54c04b66f4682a9afb48e27dfe397", 
                 ((GnucashCustomerInvoiceEntry) entrList.toArray()[0]).getId());
    assertEquals("3c67a99b5fe34387b596bb1fbab21a74", 
                 ((GnucashCustomerInvoiceEntry) entrList.toArray()[1]).getId());
  }

  @Test
  public void test03_1() throws Exception
  {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
    invcSpec = new GnucashCustomerInvoiceImpl(invcGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1327.60, invcGen.getInvcAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getInvcAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getAmountWithoutTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1327.60, invcGen.getInvcAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getInvcAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals(1327.60, invcSpec.getAmountWithTaxes().doubleValue(), ConstTest.DIFF_TOLERANCE);
  }

  @Test
  public void test04_1() throws Exception
  {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_1_ID);
    invcSpec = new GnucashCustomerInvoiceImpl(invcGen);

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
  
  @Test
  public void test04_2() throws Exception
  {
    // ::TODO
  }
}
