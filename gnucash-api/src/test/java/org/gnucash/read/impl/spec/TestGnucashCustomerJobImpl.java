package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomerJobImpl
{
  private static GnucashFile        gcshFile = null;
  private static GnucashGenerJob    jobGener = null;
  private static GnucashCustomerJob jobSpec = null;
  
  private static final String JOB_1_ID = TestGnucashGenerJobImpl.JOB_1_ID;

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashCustomerJobImpl.class);  
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
    jobGener = gcshFile.getJobByID(JOB_1_ID);
    jobSpec  = new GnucashCustomerJobImpl(jobGener);

    assertTrue(jobSpec instanceof GnucashCustomerJob);
    assertEquals(JOB_1_ID, jobSpec.getId());
    assertEquals("000001", jobSpec.getNumber());
    assertEquals("Do more for others", jobSpec.getName());
  }

  @Test
  public void test02() throws Exception
  {
    jobGener = gcshFile.getJobByID(JOB_1_ID);
    jobSpec  = new GnucashCustomerJobImpl(jobGener);
      
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    assertEquals(1, jobGener.getGenerInvoices().size());
    assertEquals(1, jobSpec.getGenerInvoices().size());
    assertEquals(1, jobSpec.getInvoices().size());
  }

  @Test
  public void test03() throws Exception
  {
    jobGener = gcshFile.getJobByID(JOB_1_ID);
    jobSpec  = new GnucashCustomerJobImpl(jobGener);
      
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    String custID = "f44645d2397946bcac90dff68cc03b76";
    assertEquals(custID, jobGener.getOwnerId());
    assertEquals(custID, jobGener.getOwnerId());
    assertEquals(custID, jobSpec.getCustomerId());
  }
}
