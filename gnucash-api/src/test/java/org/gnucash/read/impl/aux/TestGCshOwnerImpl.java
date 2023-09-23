package org.gnucash.read.impl.aux;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.generated.GncV2.GncBook.GncGncInvoice.InvoiceOwner;
import org.gnucash.generated.GncV2.GncBook.GncGncJob.JobOwner;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.aux.GCshOwner.JIType;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashCustomerImpl;
import org.gnucash.read.impl.TestGnucashGenerJobImpl;
import org.gnucash.read.impl.TestGnucashVendorImpl;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshOwnerImpl
{
  private static GnucashFile gcshFile = null;
  private static GCshOwner   owner = null;
  
  private static final String CUST_1_ID = TestGnucashCustomerImpl.CUST_1_ID;
  private static final String VEND_1_ID = TestGnucashVendorImpl.VEND_1_ID;
  private static final String JOB_1_ID  = TestGnucashGenerJobImpl.JOB_1_ID;
  
  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshOwnerImpl.class);  
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
    owner = new GCshOwnerImpl(GCshOwner.TYPE_CUSTOMER, CUST_1_ID, gcshFile);
    
    assertEquals(JIType.INVOICE, owner.getJIType());
    assertEquals(GCshOwner.TYPE_CUSTOMER, owner.getInvcType());
    
    assertEquals(CUST_1_ID, owner.getId());
    assertEquals(CUST_1_ID, owner.getInvcOwner().getOwnerId().getValue());

    try 
    {
	JobOwner dummy = owner.getJobOwner(); // must throw exception
	assertEquals(0, 1);
    }
    catch ( Exception exc )
    {
	assertEquals(0, 0);
    }
  }

  @Test
  public void test02() throws Exception
  {
    owner = new GCshOwnerImpl(GCshOwner.TYPE_VENDOR, VEND_1_ID, gcshFile);
    
    assertEquals(JIType.INVOICE, owner.getJIType());
    assertEquals(GCshOwner.TYPE_VENDOR, owner.getInvcType());
    
    assertEquals(VEND_1_ID, owner.getId());
    assertEquals(VEND_1_ID, owner.getInvcOwner().getOwnerId().getValue());

    try 
    {
	JobOwner dummy = owner.getJobOwner(); // must throw exception
	assertEquals(0, 1);
    }
    catch ( Exception exc )
    {
	assertEquals(0, 0);
    }
  }


  @Test
  public void test03() throws Exception
  {
    owner = new GCshOwnerImpl(GCshOwner.TYPE_JOB, JOB_1_ID, gcshFile);
    
    assertEquals(JIType.JOB, owner.getJIType());
    
    try 
    {
	String dummy = owner.getInvcType(); // must throw exception
	assertEquals(0, 1);
    }
    catch ( Exception exc )
    {
	assertEquals(0, 0);
    }
    
    assertEquals(JOB_1_ID, owner.getId());
    assertEquals(JOB_1_ID, owner.getJobOwner().getOwnerId().getValue());
    
    try 
    {
	InvoiceOwner dummy = owner.getInvcOwner(); // must throw exception
	assertEquals(0, 1);
    }
    catch ( Exception exc )
    {
	assertEquals(0, 0);
    }
  }
}
