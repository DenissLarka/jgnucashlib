package org.gnucash.read.impl.aux;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.aux.GCshTaxTableEntry;
import org.gnucash.read.impl.GnucashFileImpl;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshTaxTableImpl
{
  // DE
  // Note the funny parent/child pair.
  private static final String TAXTABLE_DE_1_1_ID = "3c9690f9f31b4cd0baa936048b833c06"; // DE_USt_Std "parent"
  private static final String TAXTABLE_DE_1_2_ID = "cba6011c826f426fbc4a1a72c3d6c8ee"; // DE_USt_Std "child"
  private static final String TAXTABLE_DE_2_ID   = "c518af53a93c4a5cb3e2161b7b358e68"; // DE_USt_red
    
  // FR
  private static final String TAXTABLE_FR_1_ID   = "de4c17d1eb0e4f088ba73d4c697032f0"; // FR_USt_Std
  private static final String TAXTABLE_FR_2_ID   = "e279d5cc81204f1bb6cf672ef3357c0c"; // FR_USt_red
    
  private static final String TAX_ACCT_ID        = "1a5b06dada56466197edbd15e64fd425"; // Root Account::Fremdkapital::Steuerverbindl

  private GnucashFile  gcshFile = null;
  private GCshTaxTable taxTab = null;
  
  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshTaxTableImpl.class);  
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
  public void test01() throws Exception
  {
      Collection<GCshTaxTable> taxTableList = gcshFile.getTaxTables();
      
      assertEquals(5, taxTableList.size());

      // ::TODO: Sort array for predictability
      Object[] taxTableArr = taxTableList.toArray();
      
      // funny, this parent/child relationship full of redundancies...
      assertEquals(TAXTABLE_DE_1_1_ID, ((GCshTaxTable) taxTableArr[1]).getId());
      assertEquals(TAXTABLE_DE_1_2_ID, ((GCshTaxTable) taxTableArr[4]).getId());
      
      // Here, it's what you would expect:
      assertEquals(TAXTABLE_DE_2_ID, ((GCshTaxTable) taxTableArr[2]).getId());
      assertEquals(TAXTABLE_FR_1_ID, ((GCshTaxTable) taxTableArr[3]).getId());
      assertEquals(TAXTABLE_FR_2_ID, ((GCshTaxTable) taxTableArr[0]).getId());
  }

  @Test
  public void test02_1() throws Exception
  {
      taxTab = gcshFile.getTaxTableByID(TAXTABLE_DE_1_1_ID);
      
      assertEquals("DE_USt_Std", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(19.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.TYPE_PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test02_2() throws Exception
  {
      taxTab = gcshFile.getTaxTableByID(TAXTABLE_DE_1_2_ID);
      
      assertEquals("USt_Std", taxTab.getName()); // sic, old name w/o prefix "DE_"
      assertEquals(TAXTABLE_DE_1_1_ID, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(19.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.TYPE_PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test03() throws Exception
  {
      taxTab = gcshFile.getTaxTableByID(TAXTABLE_DE_2_ID);
      
      assertEquals("DE_USt_red", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(7.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.TYPE_PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }


  @Test
  public void test04() throws Exception
  {
      taxTab = gcshFile.getTaxTableByID(TAXTABLE_FR_1_ID);
      
      assertEquals("FR_USt_Std", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(20.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.TYPE_PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }

  @Test
  public void test05() throws Exception
  {
      taxTab = gcshFile.getTaxTableByID(TAXTABLE_FR_2_ID);
      
      assertEquals("FR_USt_red", taxTab.getName());
      assertEquals(null, taxTab.getParentID());

      assertEquals(1, taxTab.getEntries().size());
      assertEquals(10.0, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAmount().doubleValue(), ConstTest.DIFF_TOLERANCE );
      assertEquals(GCshTaxTableEntry.TYPE_PERCENT, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getType() );
      assertEquals(TAX_ACCT_ID, ((GCshTaxTableEntry) taxTab.getEntries().toArray()[0]).getAccountID() );
  }
}
