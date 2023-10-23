package org.gnucash.currency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestCommodityID_MIC
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestCommodityID_MIC.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test02() throws Exception
  {
    CommodityID_MIC commCurr = new CommodityID_MIC(CmdtyCurrNameSpace.MIC.XFRA, "MBG");
    
    assertEquals(CmdtyCurrID.Type.SECURITY_MIC, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.MIC.XFRA, commCurr.getMIC());
    assertEquals("MBG", commCurr.getCode());
    assertEquals("XFRA:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    CommodityID_MIC commCurr1  = new CommodityID_MIC(CmdtyCurrNameSpace.MIC.XFRA, "MBG");
    CommodityID_MIC commCurr2 = new CommodityID_MIC("XFRA", "MBG");
  
    assertEquals(commCurr1.toString(), commCurr2.toString());
    assertEquals(commCurr1.toStringLong(), commCurr2.toStringLong());
    assertEquals(commCurr1, commCurr2);
      
    // ---

//    CommodityID_MIC commCurr31 = new CommodityID_MIC(CmdtyCurrNameSpace.MIC.NYSE, "MBG");
//    CommodityID_MIC commCurr32 = new CommodityID_MIC(CmdtyCurrNameSpace.MIC.EURONEXT, "DIS");
//    
//    assertNotEquals(commCurr1, commCurr31);
//    assertNotEquals(commCurr1, commCurr32);
//    assertNotEquals(commCurr31, commCurr32);
  }
  
  @Test
  public void test04_2() throws Exception
  {
      CommodityID_MIC commCurrPrs = CommodityID_MIC.parse("XFRA:SAP");
      CommodityID_MIC commCurrRef = new CommodityID_MIC(CmdtyCurrNameSpace.MIC.XFRA, "SAP");
      
      assertEquals(CmdtyCurrID.Type.SECURITY_MIC, commCurrPrs.getType());
      assertEquals("XFRA:SAP", commCurrPrs.toString());
      assertEquals(commCurrRef.toString(), commCurrPrs.toString());
      assertEquals(commCurrRef.toStringLong(), commCurrPrs.toStringLong());
      assertEquals(commCurrRef, commCurrPrs);

//      // ---
//      
//      commCurrPrs = CommodityID_MIC.parse("CURRENCY:USD");
//      commCurrRef = new CommodityID_MIC(Currency.getInstance("USD"));
//      
//      assertEquals("CURRENCY:USD", commCurrPrs.toString());
//      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_3() throws Exception
  {
      try 
      {
	  CommodityID_MIC commCurrPrs = CommodityID_MIC.parse("FUXNSTUELL:BURP");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
  }
}
