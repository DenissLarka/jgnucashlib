package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashVendorJob;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashVendorJobImpl
{
  private GnucashFile        gcshFile = null;
  private GnucashGenerJob    jobGener = null;
  private GnucashVendorJob   jobSpec = null;
  
  private static final String JOB_2_ID = TestGnucashGenerJobImpl.JOB_2_ID;

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashVendorJobImpl.class);  
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
    jobGener = gcshFile.getGenerJobByID(JOB_2_ID);
    jobSpec  = new GnucashVendorJobImpl(jobGener);

    assertTrue(jobSpec instanceof GnucashVendorJob);
    assertEquals(JOB_2_ID, jobSpec.getId());
    assertEquals("000002", jobSpec.getNumber());
    assertEquals("Let's buy help", jobSpec.getName());
  }

  @Test
  public void test02() throws Exception
  {
    jobGener = gcshFile.getGenerJobByID(JOB_2_ID);
    jobSpec  = new GnucashVendorJobImpl(jobGener);
      
    // ::TODO
    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(0, jobGener.getPaidInvoices().size());
    assertEquals(0, jobSpec.getPaidInvoices().size());

    // ::TODO
    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1, jobGener.getUnpaidInvoices().size());
    assertEquals(1, jobSpec.getUnpaidInvoices().size());
  }

  @Test
  public void test03() throws Exception
  {
    jobGener = gcshFile.getGenerJobByID(JOB_2_ID);
    jobSpec  = new GnucashVendorJobImpl(jobGener);
      
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    String vendID = "4f16fd55c0d64ebe82ffac0bb25fe8f5";
    assertEquals(vendID, jobGener.getOwnerId());
    assertEquals(vendID, jobSpec.getOwnerId());
    assertEquals(vendID, jobSpec.getVendorId());
  }
}
