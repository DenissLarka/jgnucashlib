package org.gnucash.basetypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshCmdtyID_Exchange
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshCmdtyID_Exchange.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test02() throws Exception
  {
    GCshCmdtyID_Exchange commCurr = new GCshCmdtyID_Exchange(GCshCmdtyCurrNameSpace.Exchange.EURONEXT, "MBG");
    
    assertEquals(GCshCmdtyCurrID.Type.SECURITY_EXCHANGE, commCurr.getType());
    assertEquals(GCshCmdtyCurrNameSpace.Exchange.EURONEXT, commCurr.getExchange());
    assertEquals("MBG", commCurr.getCode());
    assertEquals("EURONEXT:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    GCshCmdtyID_Exchange commCurr1  = new GCshCmdtyID_Exchange(GCshCmdtyCurrNameSpace.Exchange.EURONEXT, "MBG");
    GCshCmdtyID_Exchange commCurr2 = new GCshCmdtyID_Exchange("EURONEXT", "MBG");
  
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
      GCshCmdtyID_Exchange commCurrPrs = GCshCmdtyID_Exchange.parse("EURONEXT:SAP");
      GCshCmdtyID_Exchange commCurrRef = new GCshCmdtyID_Exchange(GCshCmdtyCurrNameSpace.Exchange.EURONEXT, "SAP");
      
      assertEquals(GCshCmdtyCurrID.Type.SECURITY_EXCHANGE, commCurrPrs.getType());
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
	  GCshCmdtyID_Exchange commCurrPrs = GCshCmdtyID_Exchange.parse("FUXNSTUELL:BURP");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
  }
}
