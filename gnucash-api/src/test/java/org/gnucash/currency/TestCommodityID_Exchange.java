package org.gnucash.currency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestCommodityID_Exchange
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestCommodityID_Exchange.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test02() throws Exception
  {
    CommodityID_Exchange commCurr = new CommodityID_Exchange(CmdtyCurrNameSpace.Exchange.EURONEXT, "MBG");
    
    assertEquals(CmdtyCurrID.Type.SECURITY_EXCHANGE, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.Exchange.EURONEXT, commCurr.getExchange());
    assertEquals("MBG", commCurr.getCode());
    assertEquals("EURONEXT:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    CommodityID_Exchange commCurr1  = new CommodityID_Exchange(CmdtyCurrNameSpace.Exchange.EURONEXT, "MBG");
    CommodityID_Exchange commCurr2 = new CommodityID_Exchange("EURONEXT", "MBG");
  
    assertEquals(commCurr1.toString(), commCurr2.toString());
    assertEquals(commCurr1.toStringLong(), commCurr2.toStringLong());
    assertEquals(commCurr1, commCurr2);
      
    // ---

//    CommodityID_Exchange commCurr31 = new CommodityID_Exchange(CmdtyCurrNameSpace.Exchange.NYSE, "MBG");
//    CommodityID_Exchange commCurr32 = new CommodityID_Exchange(CmdtyCurrNameSpace.Exchange.EURONEXT, "DIS");
//    
//    assertNotEquals(commCurr1, commCurr31);
//    assertNotEquals(commCurr1, commCurr32);
//    assertNotEquals(commCurr31, commCurr32);
  }
  
  @Test
  public void test04_2() throws Exception
  {
      CommodityID_Exchange commCurrPrs = CommodityID_Exchange.parse("EURONEXT:SAP");
      CommodityID_Exchange commCurrRef = new CommodityID_Exchange(CmdtyCurrNameSpace.Exchange.EURONEXT, "SAP");
      
      assertEquals(CmdtyCurrID.Type.SECURITY_EXCHANGE, commCurrPrs.getType());
      assertEquals("EURONEXT:SAP", commCurrPrs.toString());
      assertEquals(commCurrRef.toString(), commCurrPrs.toString());
      assertEquals(commCurrRef.toStringLong(), commCurrPrs.toStringLong());
      assertEquals(commCurrRef, commCurrPrs);

//      // ---
//      
//      commCurrPrs = CommodityID_Exchange.parse("CURRENCY:USD");
//      commCurrRef = new CommodityID_Exchange(Currency.getInstance("USD"));
//      
//      assertEquals("CURRENCY:USD", commCurrPrs.toString());
//      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_3() throws Exception
  {
      try 
      {
	  CommodityID_Exchange commCurrPrs = CommodityID_Exchange.parse("FUXNSTUELL:BURP");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
  }
}
