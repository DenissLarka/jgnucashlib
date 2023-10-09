package org.gnucash.read.impl.aux;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collection;

import org.gnucash.ConstTest;
import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermDays;
import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermProximo;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshBillTerms;
import org.gnucash.read.impl.GnucashFileImpl;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshBillTermsImpl
{
  private static final String BLLTRM_1_ID = "599bfe3ab5b84a73bf3acabc5abd5bc7"; // "sofort" (5 Tage)
  private static final String BLLTRM_2_ID = "f4310c65486a47a5a787348b7de6ca40"; // "30-10-3"
  private static final String BLLTRM_3_ID = "f65a46140da94c81a4e1e3c0aa38c32b"; // "nächster-monat-mitte"
    
  private GnucashFile   gcshFile = null;
  private GCshBillTerms bllTrm = null;
  
  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshBillTermsImpl.class);  
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
      Collection<GCshBillTerms> bllTrmList = gcshFile.getBillTerms();
      
      assertEquals(3, bllTrmList.size());

      // ::TODO: Sort array for predictability
      Object[] bllTrmArr = bllTrmList.toArray();
      
      // funny, this parent/child relationship full of redundancies...
      assertEquals(BLLTRM_1_ID, ((GCshBillTerms) bllTrmArr[0]).getId());
      assertEquals(BLLTRM_2_ID, ((GCshBillTerms) bllTrmArr[1]).getId());
      assertEquals(BLLTRM_3_ID, ((GCshBillTerms) bllTrmArr[2]).getId());
  }

  @Test
  public void test02_1_1() throws Exception
  {
      bllTrm = gcshFile.getBillTermsByID(BLLTRM_1_ID);
      
      assertEquals(BLLTRM_1_ID, bllTrm.getId());
      assertEquals("sofort", bllTrm.getName());
      assertEquals(GCshBillTerms.Type.DAYS, bllTrm.getType());

      assertEquals(null, bllTrm.getParentId());
      assertEquals(0, bllTrm.getChildren().size());

      BilltermDays btDays = bllTrm.getDays();
      assertEquals(Integer.valueOf(5), btDays.getBtDaysDueDays());
      assertEquals(null, btDays.getBtDaysDiscDays());
      // assertEquals(null, btDays.getBtDaysDiscount()); // ::TODO
  }

  @Test
  public void test02_1_2() throws Exception
  {
      bllTrm = gcshFile.getBillTermsByName("sofort");
      
      assertEquals(BLLTRM_1_ID, bllTrm.getId());
      assertEquals("sofort", bllTrm.getName());
      assertEquals(GCshBillTerms.Type.DAYS, bllTrm.getType());
      
      assertEquals(null, bllTrm.getParentId());
      assertEquals(0, bllTrm.getChildren().size());

      BilltermDays btDays = bllTrm.getDays();
      assertEquals(Integer.valueOf(5), btDays.getBtDaysDueDays());
      assertEquals(null, btDays.getBtDaysDiscDays());
      // assertEquals(null, btDays.getBtDaysDiscount()); // ::TODO
  }

  @Test
  public void test02_2_1() throws Exception
  {
      bllTrm = gcshFile.getBillTermsByID(BLLTRM_2_ID);
      
      assertEquals(BLLTRM_2_ID, bllTrm.getId());
      assertEquals("30-10-3", bllTrm.getName());
      assertEquals(GCshBillTerms.Type.DAYS, bllTrm.getType());

      assertEquals(null, bllTrm.getParentId());
      assertEquals(0, bllTrm.getChildren().size());

      BilltermDays btDays = bllTrm.getDays();
      assertEquals(Integer.valueOf(30), btDays.getBtDaysDueDays());
      assertEquals(Integer.valueOf(10), btDays.getBtDaysDiscDays());
      // assertEquals("3", btDays.getBtDaysDiscount()); // ::TODO
  }

  @Test
  public void test02_2_2() throws Exception
  {
      bllTrm = gcshFile.getBillTermsByName("30-10-3");
      
      assertEquals(BLLTRM_2_ID, bllTrm.getId());
      assertEquals("30-10-3", bllTrm.getName());
      assertEquals(GCshBillTerms.Type.DAYS, bllTrm.getType());

      assertEquals(null, bllTrm.getParentId());
      assertEquals(0, bllTrm.getChildren().size());

      BilltermDays btDays = bllTrm.getDays();
      assertEquals(Integer.valueOf(30), btDays.getBtDaysDueDays());
      assertEquals(Integer.valueOf(10), btDays.getBtDaysDiscDays());
      // assertEquals("3", btDays.getBtDaysDiscount()); // ::TODO
  }

  @Test
  public void test02_3_1() throws Exception
  {
      bllTrm = gcshFile.getBillTermsByID(BLLTRM_3_ID);
      
      assertEquals(BLLTRM_3_ID, bllTrm.getId());
      assertEquals("nächster-monat-mitte", bllTrm.getName());
      assertEquals(GCshBillTerms.Type.PROXIMO, bllTrm.getType());

      assertEquals(null, bllTrm.getParentId());
      assertEquals(0, bllTrm.getChildren().size());

      BilltermProximo btProx = bllTrm.getProximo();
      assertEquals(Integer.valueOf(15), btProx.getBtProxDueDay());
      assertEquals(Integer.valueOf(3), btProx.getBtProxDiscDay());
      // assertEquals("2", btProx.getBtProxDiscount()); // ::TODO
  }

  @Test
  public void test02_3_2() throws Exception
  {
      bllTrm = gcshFile.getBillTermsByName("nächster-monat-mitte");
      
      assertEquals(BLLTRM_3_ID, bllTrm.getId());
      assertEquals("nächster-monat-mitte", bllTrm.getName());
      assertEquals(GCshBillTerms.Type.PROXIMO, bllTrm.getType());

      assertEquals(null, bllTrm.getParentId());
      assertEquals(0, bllTrm.getChildren().size());

      BilltermProximo btProx = bllTrm.getProximo();
      assertEquals(Integer.valueOf(15), btProx.getBtProxDueDay());
      assertEquals(Integer.valueOf(3), btProx.getBtProxDiscDay());
      // assertEquals("2", btProx.getBtProxDiscount()); // ::TODO
  }
}
