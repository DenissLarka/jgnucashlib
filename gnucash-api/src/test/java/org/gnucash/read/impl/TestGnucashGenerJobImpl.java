package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashGenerJobImpl
{
  private GnucashFile     gcshFile = null;
  private GnucashGenerJob job = null;
  
  public static final String JOB_1_ID = "e91b99cd6fbb48a985cbf1e8041f378c";
  public static final String JOB_2_ID = "028cfb5993ef4d6b83206bc844e2fe56";

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashGenerJobImpl.class);  
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
  public void testCust01() throws Exception
  {
    job = gcshFile.getGenerJobByID(JOB_1_ID);
    
    assertEquals(JOB_1_ID, job.getId());
    assertEquals("000001", job.getNumber());
    assertEquals(GnucashGenerJob.TYPE_CUSTOMER, job.getOwnerType());
    assertEquals("Do more for others", job.getName());
  }
  
  @Test
  public void testCust02() throws Exception
  {
    job = gcshFile.getGenerJobByID(JOB_1_ID);
      
    assertEquals(0, job.getPaidInvoices().size());
    assertEquals(1, job.getUnpaidInvoices().size());
  }

  @Test
  public void testCust03() throws Exception
  {
    job = gcshFile.getGenerJobByID(JOB_1_ID);
      
    String custID = "f44645d2397946bcac90dff68cc03b76";
    assertEquals(custID, job.getOwnerId());
  }

  // -----------------------------------------------------------------

  @Test
  public void testVend01() throws Exception
  {
    job = gcshFile.getGenerJobByID(JOB_2_ID);
    
    assertEquals(JOB_2_ID, job.getId());
    assertEquals("000002", job.getNumber());
    assertEquals(GnucashGenerJob.TYPE_VENDOR, job.getOwnerType());
    assertEquals("Let's buy help", job.getName());
  }

  @Test
  public void testVend02() throws Exception
  {
    job = gcshFile.getGenerJobByID(JOB_2_ID);
      
    assertEquals(0, job.getPaidInvoices().size());
    assertEquals(1, job.getUnpaidInvoices().size());
  }

  @Test
  public void testVend03() throws Exception
  {
    job = gcshFile.getGenerJobByID(JOB_2_ID);
      
    String vendID = "4f16fd55c0d64ebe82ffac0bb25fe8f5";
    assertEquals(vendID, job.getOwnerId());
  }
}
