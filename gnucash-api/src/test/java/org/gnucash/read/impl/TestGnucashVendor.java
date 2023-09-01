package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.gnucash.Const;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashVendor;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashVendor
{
  private static GnucashFile     gcshFile = null;
  private static GnucashVendor   vend = null;
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashVendor.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    gcshFile = new GnucashFileImpl(new File(Const.GCSH_FILE_NAME));
  }

  @Test
  public void test01() throws Exception
  {
    vend = gcshFile.getVendorByID("087e1a3d43fa4ef9a9bdd4b4797c4231");
    assertEquals("Lieferfanto", vend.getName());
  }
}
