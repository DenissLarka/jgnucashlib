package org.gnucash.currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestCommodityID
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestCommodityID.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
    try 
    {
	CommodityID commCurr = new CommodityID(CmdtyCurrNameSpace.CURRENCY, "EUR");
    }
    catch ( Exception exc ) 
    {
	assertEquals(0, 0);
    }
  }

  @Test
  public void test02() throws Exception
  {
      CommodityID commCurr = new CommodityID("EURONEXT", "MBG");
    
    assertEquals(CmdtyCurrID.Type.SECURITY_GENERAL, commCurr.getType());
    assertEquals("EURONEXT", commCurr.getNameSpace());
    assertEquals("MBG", commCurr.getCode());
    assertEquals("EURONEXT:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    CommodityID commCurr1  = new CommodityID("EURONEXT", "MBG");
    CommodityID commCurr2 = new CommodityID("EURONEXT", "MBG");
  
    assertEquals(commCurr1.toString(), commCurr2.toString());
    assertEquals(commCurr1.toStringLong(), commCurr2.toStringLong());
    assertEquals(commCurr1, commCurr2);
      
    // ---

    CommodityID commCurr31 = new CommodityID("NYSE", "MBG");
    CommodityID commCurr32 = new CommodityID("EURONEXT", "DIS");
    
    assertNotEquals(commCurr1, commCurr31);
    assertNotEquals(commCurr1, commCurr32);
    assertNotEquals(commCurr31, commCurr32);
    
    // ---

    CommodityID commCurr4 = new CommodityID(CmdtyCurrNameSpace.CURRENCY, "EUR");
    CommodityID commCurr5 = new CommodityID(CmdtyCurrNameSpace.CURRENCY, "EUR");
  
    assertEquals(commCurr4, commCurr5);
    assertNotEquals(commCurr1, commCurr4);
    assertNotEquals(commCurr2, commCurr4);
    assertNotEquals(commCurr31, commCurr4);
    assertNotEquals(commCurr32, commCurr4);
    
    CommodityID commCurr6 = new CommodityID(CmdtyCurrNameSpace.CURRENCY, "JPY");
    
    assertNotEquals(commCurr4, commCurr6);
  }
  
  @Test
  public void test04_1() throws Exception
  {
      try 
      {
	CommodityID commCurrPrs = CommodityID.parse("CURRENCY:EUR");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
      
      // ---
      
      try 
      {
	CommodityID commCurrPrs = CommodityID.parse("CURRENCY:USD");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
      
  }

  @Test
  public void test04_2() throws Exception
  {
      CommodityID commCurrPrs = CommodityID.parse("XFRA:SAP");
      CommodityID commCurrRef = new CommodityID("XFRA", "SAP");
      
      assertEquals(CmdtyCurrID.Type.SECURITY_GENERAL, commCurrPrs.getType());
      assertEquals("XFRA:SAP", commCurrPrs.toString());
      assertEquals(commCurrRef.toString(), commCurrPrs.toString());
      assertEquals(commCurrRef.toStringLong(), commCurrPrs.toStringLong());
      assertEquals(commCurrRef, commCurrPrs);

//      // ---
//      
//      commCurrPrs = CmdtyCurrID.parse("CURRENCY:USD");
//      commCurrRef = new CmdtyCurrID(Currency.getInstance("USD"));
//      
//      assertEquals("CURRENCY:USD", commCurrPrs.toString());
//      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_3() throws Exception
  {
      CommodityID commCurrPrs = CommodityID.parse("FUXNSTUELL:BURP"); // Wrong, but not check on this level
      CommodityID commCurrRef = new CommodityID();
      commCurrRef.setType(CmdtyCurrID.Type.SECURITY_GENERAL);
      commCurrRef.setNameSpace("FUXNSTUELL");
      commCurrRef.setCode("BURP");
      
      assertEquals(CmdtyCurrID.Type.SECURITY_GENERAL, commCurrPrs.getType());
      assertEquals("FUXNSTUELL:BURP", commCurrPrs.toString());
      assertEquals(commCurrRef, commCurrPrs);

//      // ---
//      
//      commCurrPrs = CmdtyCurrID.parse("CURRENCY:USD");
//      commCurrRef = new CmdtyCurrID(Currency.getInstance("USD"));
//      
//      assertEquals("CURRENCY:USD", commCurrPrs.toString());
//      assertEquals(commCurrRef, commCurrPrs);
  }
}
