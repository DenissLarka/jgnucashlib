package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.gnucash.Const;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomer
{
  private static GnucashFile     gcshFile = null;
  private static GnucashCustomer cust = null;
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashCustomer.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    gcshFile = new GnucashFileImpl(new File(Const.GCSH_FILE_NAME));
  }

  @Test
  public void test01() throws Exception
  {
    cust = gcshFile.getCustomerByID("5d1dd9afa7554553988669830cc1f696");
    assertEquals("Blödfug und Quatsch", cust.getName());
  }
}
