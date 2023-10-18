package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.util.Collection;

import org.gnucash.ConstTest;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.read.GnucashFile;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCommodityImpl
{
  // Mercedes-Benz Group AG
  public static final String CMDTY_1_NAMESPACE = "EUREX";
  public static final String CMDTY_1_ID        = "MBG";   
  public static final String CMDTY_1_ISIN      = "DE0007100000";
  
  // SAP SE
  public static final String CMDTY_2_NAMESPACE = "EUREX";
  public static final String CMDTY_2_ID        = "SAP";   
  public static final String CMDTY_2_ISIN      = "DE0007164600";
    
  // -----------------------------------------------------------------
    
  private GnucashFile      gcshFile = null;
  private GnucashCommodity cmdty = null;
  
  private CmdtyCurrID cmdtyCurrID1 = new CmdtyCurrID(CMDTY_1_NAMESPACE, CMDTY_1_ID);
  private CmdtyCurrID cmdtyCurrID2 = new CmdtyCurrID(CMDTY_2_NAMESPACE, CMDTY_2_ID);
  
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
  public void test00() throws Exception
  {
      // Cf. TestCmdtyCurrID -- let's just double-check 
      assertEquals(CMDTY_1_NAMESPACE + CmdtyCurrID.SEPARATOR + CMDTY_1_ID, cmdtyCurrID1.toString());
      assertEquals(CMDTY_2_NAMESPACE + CmdtyCurrID.SEPARATOR + CMDTY_2_ID, cmdtyCurrID2.toString());
  }
  
  // ------------------------------

  @Test
  public void test01_1() throws Exception
  {
    cmdty = gcshFile.getCommodityByQualifID(CMDTY_1_NAMESPACE, CMDTY_1_ID);
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID1.toString(), cmdty.getQualifId());
    assertEquals(CMDTY_1_NAMESPACE, cmdty.getNameSpace());
    assertEquals(CMDTY_1_ID, cmdty.getId());
    assertEquals(CMDTY_1_ISIN, cmdty.getXCode());
    assertEquals("Mercedes-Benz Group AG", cmdty.getName());
  }

  @Test
  public void test01_2() throws Exception
  {
    cmdty = gcshFile.getCommodityByQualifID(cmdtyCurrID1.toString());
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID1.toString(), cmdty.getQualifId());
    assertEquals(CMDTY_1_NAMESPACE, cmdty.getNameSpace());
    assertEquals(CMDTY_1_ID, cmdty.getId());
    assertEquals(CMDTY_1_ISIN, cmdty.getXCode());
    assertEquals("Mercedes-Benz Group AG", cmdty.getName());
  }

  @Test
  public void test01_3() throws Exception
  {
    cmdty = gcshFile.getCommodityByXCode(CMDTY_1_ISIN);
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID1.toString(), cmdty.getQualifId());
    assertEquals(CMDTY_1_NAMESPACE, cmdty.getNameSpace());
    assertEquals(CMDTY_1_ID, cmdty.getId());
    assertEquals(CMDTY_1_ISIN, cmdty.getXCode());
    assertEquals("Mercedes-Benz Group AG", cmdty.getName());
  }

  @Test
  public void test01_4() throws Exception
  {
    Collection<GnucashCommodity> cmdtyList = gcshFile.getCommoditiesByName("mercedes");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    
    assertEquals(cmdtyCurrID1.toString(), 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    assertEquals(CMDTY_1_NAMESPACE, 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getNameSpace());
    assertEquals(CMDTY_1_ID, 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getId());
    assertEquals(CMDTY_1_ISIN, 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getXCode());
    assertEquals("Mercedes-Benz Group AG", 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getName());

    cmdtyList = gcshFile.getCommoditiesByName("BENZ");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    assertEquals(cmdtyCurrID1.toString(), 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    
    cmdtyList = gcshFile.getCommoditiesByName(" MeRceDeS-bEnZ  ");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    assertEquals(CMDTY_1_NAMESPACE + CmdtyCurrID.SEPARATOR + CMDTY_1_ID, 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
  }
}
