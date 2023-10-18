package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.read.GnucashFile;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCommodityImpl
{
  public static final String CMDTY_1_NAMESPACE = "EUREX"; //
  public static final String CMDTY_1_ID        = "MBG";   // Mercedes-Benz Group AG
  public static final String CMDTY_2_NAMESPACE = "EUREX"; //
  public static final String CMDTY_2_ID        = "SAP";   // SAP SE
    
  // -----------------------------------------------------------------
    
  private GnucashFile      gcshFile = null;
  private GnucashCommodity cmdty = null;
  
  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashCommodityImpl.class);  
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
    cmdty = gcshFile.getCommodityByID(CMDTY_1_NAMESPACE, CMDTY_1_ID);
    assertNotEquals(null, cmdty);
    
    assertEquals(CMDTY_1_NAMESPACE + ":" + CMDTY_1_ID, cmdty.getNameSpaceId());
    assertEquals(CMDTY_1_NAMESPACE, cmdty.getNameSpace());
    assertEquals(CMDTY_1_ID, cmdty.getId());
    assertEquals("DE0007100000", cmdty.getXCode());
    assertEquals("Mercedes-Benz Group AG", cmdty.getName());
  }
}
