package org.gnucash.basetypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshCmdtyID_SecIdType
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshCmdtyID_SecIdType.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test02() throws Exception
  {
    GCshCmdtyID_SecIdType cmdty = new GCshCmdtyID_SecIdType(GCshCmdtyCurrNameSpace.SecIdType.ISIN, "DE0007100000");
    
    assertEquals(GCshCmdtyCurrID.Type.SECURITY_SECIDTYPE, cmdty.getType());
    assertEquals(GCshCmdtyCurrNameSpace.SecIdType.ISIN, cmdty.getSecIdType());
    assertEquals("DE0007100000", cmdty.getCode());
    assertEquals("ISIN:DE0007100000", cmdty.toString());
  }

  @Test
  public void test03() throws Exception
  {
    GCshCmdtyID_SecIdType cmdty1  = new GCshCmdtyID_SecIdType(GCshCmdtyCurrNameSpace.SecIdType.ISIN, "DE0007100000");
    GCshCmdtyID_SecIdType cmdty2 = new GCshCmdtyID_SecIdType("ISIN", "DE0007100000");
  
    assertEquals(cmdty1.toString(), cmdty2.toString());
    assertEquals(cmdty1.toStringLong(), cmdty2.toStringLong());
    assertEquals(cmdty1, cmdty2);
      
    // ---

//    CommodityID_SecIdType commCurr31 = new CommodityID_SecIdType(CmdtyCurrNameSpace.SecIdType.NYSE, "DE0007100000");
//    CommodityID_SecIdType commCurr32 = new CommodityID_SecIdType(CmdtyCurrNameSpace.SecIdType.EURONEXT, "DIS");
//    
//    assertNotEquals(commCurr1, commCurr31);
//    assertNotEquals(commCurr1, commCurr32);
//    assertNotEquals(commCurr31, commCurr32);
  }
  
  @Test
  public void test04_2() throws Exception
  {
      GCshCmdtyID_SecIdType cmdtyPrs = GCshCmdtyID_SecIdType.parse("ISIN:DE0007164600");
      GCshCmdtyID_SecIdType cmdtyRef = new GCshCmdtyID_SecIdType(GCshCmdtyCurrNameSpace.SecIdType.ISIN, "DE0007164600");
      
      assertEquals(GCshCmdtyCurrID.Type.SECURITY_SECIDTYPE, cmdtyPrs.getType());
      assertEquals("ISIN:DE0007164600", cmdtyPrs.toString());
      assertEquals(cmdtyRef.toString(), cmdtyPrs.toString());
      assertEquals(cmdtyRef.toStringLong(), cmdtyPrs.toStringLong());
      assertEquals(cmdtyRef, cmdtyPrs);

//      // ---
//      
//      commCurrPrs = CommodityID_SecIdType.parse("CURRENCY:USD");
//      commCurrRef = new CommodityID_SecIdType(Currency.getInstance("USD"));
//      
//      assertEquals("CURRENCY:USD", commCurrPrs.toString());
//      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_3() throws Exception
  {
      try 
      {
	  GCshCmdtyID_SecIdType cmdtyPrs = GCshCmdtyID_SecIdType.parse("FUXNSTUELL:BURP");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
  }
}
