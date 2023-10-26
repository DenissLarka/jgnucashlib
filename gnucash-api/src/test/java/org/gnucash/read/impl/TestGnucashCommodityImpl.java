package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.util.Collection;

import org.gnucash.ConstTest;
import org.gnucash.basetypes.GCshCmdtyCurrID;
import org.gnucash.basetypes.GCshCmdtyCurrNameSpace;
import org.gnucash.basetypes.GCshCmdtyID_Exchange;
import org.gnucash.basetypes.GCshCmdtyID_SecIdType;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.read.GnucashFile;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCommodityImpl
{
  // Mercedes-Benz Group AG
  public static final GCshCmdtyCurrNameSpace.Exchange CMDTY_1_EXCH = GCshCmdtyCurrNameSpace.Exchange.EURONEXT;
  public static final String CMDTY_1_ID    = "MBG";   
  public static final String CMDTY_1_ISIN  = "DE0007100000";
  
  // SAP SE
  public static final GCshCmdtyCurrNameSpace.Exchange CMDTY_2_EXCH = GCshCmdtyCurrNameSpace.Exchange.EURONEXT;
  public static final String CMDTY_2_ID    = "SAP";   
  public static final String CMDTY_2_ISIN  = "DE0007164600";
    
  // AstraZeneca Plc
  // Note that in the SecIDType variants, the ISIN/CUSIP/SEDOL/WKN/whatever
  // is stored twice in the object, redundantly
  public static final GCshCmdtyCurrNameSpace.SecIdType CMDTY_3_SECIDTYPE = GCshCmdtyCurrNameSpace.SecIdType.ISIN;
  public static final String CMDTY_3_ID    = "GB0009895292";   
  public static final String CMDTY_3_ISIN  = CMDTY_3_ID;
    
  // -----------------------------------------------------------------
    
  private GnucashFile      gcshFile = null;
  private GnucashCommodity cmdty = null;
  
  private GCshCmdtyCurrID cmdtyCurrID1 = null;
  private GCshCmdtyCurrID cmdtyCurrID2 = null;
  private GCshCmdtyCurrID cmdtyCurrID3 = null;
  
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
    
    // ---
    
    cmdtyCurrID1 = new GCshCmdtyID_Exchange(CMDTY_1_EXCH, CMDTY_1_ID);
    cmdtyCurrID2 = new GCshCmdtyID_Exchange(CMDTY_2_EXCH, CMDTY_2_ID);
    cmdtyCurrID3 = new GCshCmdtyID_SecIdType(CMDTY_3_SECIDTYPE, CMDTY_3_ID);
  }

  // -----------------------------------------------------------------
  
  @Test 
  public void test00() throws Exception
  {
      // Cf. TestCmdtyCurrID -- let's just double-check 
      assertEquals(CMDTY_1_EXCH.toString() + GCshCmdtyCurrID.SEPARATOR + CMDTY_1_ID, cmdtyCurrID1.toString());
      assertEquals(CMDTY_2_EXCH.toString() + GCshCmdtyCurrID.SEPARATOR + CMDTY_2_ID, cmdtyCurrID2.toString());
      assertEquals(CMDTY_3_SECIDTYPE.toString() + GCshCmdtyCurrID.SEPARATOR + CMDTY_3_ID, cmdtyCurrID3.toString());
  }
  
  // ------------------------------

  @Test
  public void test01_1() throws Exception
  {
    cmdty = gcshFile.getCommodityByQualifID(CMDTY_1_EXCH, CMDTY_1_ID);
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID1.toString(), cmdty.getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID1, cmdty.getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, cmdty.getQualifId()); // not trivial!
    assertEquals(CMDTY_1_ISIN, cmdty.getXCode());
    assertEquals("Mercedes-Benz Group AG", cmdty.getName());
  }

  @Test
  public void test01_2() throws Exception
  {
    cmdty = gcshFile.getCommodityByQualifID(cmdtyCurrID1.toString());
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID1.toString(), cmdty.getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID1, cmdty.getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, cmdty.getQualifId()); // not trivial!
    assertEquals(CMDTY_1_ISIN, cmdty.getXCode());
    assertEquals("Mercedes-Benz Group AG", cmdty.getName());
  }

  @Test
  public void test01_3() throws Exception
  {
    cmdty = gcshFile.getCommodityByXCode(CMDTY_1_ISIN);
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID1.toString(), cmdty.getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID1, cmdty.getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, cmdty.getQualifId()); // not trivial!
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
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID1, 
	            ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, 
//	        ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId()); // not trivial!
    assertEquals(CMDTY_1_ISIN, 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getXCode());
    assertEquals("Mercedes-Benz Group AG", 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getName());

    cmdtyList = gcshFile.getCommoditiesByName("BENZ");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID1, 
	            ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, 
//	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    
    cmdtyList = gcshFile.getCommoditiesByName(" MeRceDeS-bEnZ  ");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    assertEquals(cmdtyCurrID1.toString(), 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID1, 
	            ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, 
//	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId()); // not trivial!
  }

  // ------------------------------

  @Test
  public void test02_1() throws Exception
  {
    cmdty = gcshFile.getCommodityByQualifID(CMDTY_3_SECIDTYPE.toString(), CMDTY_3_ID);
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID3.toString(), cmdty.getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID3, cmdty.getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, cmdty.getQualifId()); // not trivial!
    assertEquals(CMDTY_3_ISIN, cmdty.getXCode());
    assertEquals("AstraZeneca Plc", cmdty.getName());
  }

  @Test
  public void test02_2() throws Exception
  {
    cmdty = gcshFile.getCommodityByQualifID(cmdtyCurrID3.toString());
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID3.toString(), cmdty.getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID3, cmdty.getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, cmdty.getQualifId()); // not trivial!
    assertEquals(CMDTY_3_ISIN, cmdty.getXCode());
    assertEquals("AstraZeneca Plc", cmdty.getName());
  }

  @Test
  public void test02_3() throws Exception
  {
    cmdty = gcshFile.getCommodityByXCode(CMDTY_3_ISIN);
    assertNotEquals(null, cmdty);
    
    assertEquals(cmdtyCurrID3.toString(), cmdty.getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID3, cmdty.getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, cmdty.getQualifId()); // not trivial!
    assertEquals(CMDTY_3_ISIN, cmdty.getXCode());
    assertEquals("AstraZeneca Plc", cmdty.getName());
  }

  @Test
  public void test02_4() throws Exception
  {
    Collection<GnucashCommodity> cmdtyList = gcshFile.getCommoditiesByName("astra");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    
    assertEquals(cmdtyCurrID3.toString(), 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID3, 
	            ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, 
//	        ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId()); // not trivial!
    assertEquals(CMDTY_3_ISIN, 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getXCode());
    assertEquals("AstraZeneca Plc", 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getName());

    cmdtyList = gcshFile.getCommoditiesByName("BENZ");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID3, 
	            ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, 
//	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    
    cmdtyList = gcshFile.getCommoditiesByName(" aStrAzENeCA  ");
    assertNotEquals(null, cmdtyList);
    assertEquals(1, cmdtyList.size());
    assertEquals(cmdtyCurrID3.toString(), 
	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId().toString());
    // *Not* equal because of class
    assertNotEquals(cmdtyCurrID3, 
	            ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId());
    // ::TODO: Convert to CommodityID_Exchange, then it should be equal
//    assertEquals(cmdtyCurrID1, 
//	         ((GnucashCommodity) cmdtyList.toArray()[0]).getQualifId()); // not trivial!
  }
}
