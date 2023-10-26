package org.gnucash.basetypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshCmdtyID
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshCmdtyID.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
    try 
    {
	GCshCmdtyID commCurr = new GCshCmdtyID(GCshCmdtyCurrNameSpace.CURRENCY, "EUR");
    }
    catch ( Exception exc ) 
    {
	assertEquals(0, 0);
    }
  }

  @Test
  public void test02() throws Exception
  {
      GCshCmdtyID commCurr = new GCshCmdtyID("EURONEXT", "MBG");
    
    assertEquals(GCshCmdtyCurrID.Type.SECURITY_GENERAL, commCurr.getType());
    assertEquals("EURONEXT", commCurr.getNameSpace());
    assertEquals("MBG", commCurr.getCode());
    assertEquals("EURONEXT:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    GCshCmdtyID commCurr1  = new GCshCmdtyID("EURONEXT", "MBG");
    GCshCmdtyID commCurr2 = new GCshCmdtyID("EURONEXT", "MBG");
  
    assertEquals(commCurr1.toString(), commCurr2.toString());
    assertEquals(commCurr1.toStringLong(), commCurr2.toStringLong());
    assertEquals(commCurr1, commCurr2);
      
    // ---

    GCshCmdtyID commCurr31 = new GCshCmdtyID("NYSE", "MBG");
    GCshCmdtyID commCurr32 = new GCshCmdtyID("EURONEXT", "DIS");
    
    assertNotEquals(commCurr1, commCurr31);
    assertNotEquals(commCurr1, commCurr32);
    assertNotEquals(commCurr31, commCurr32);
    
    // ---

    GCshCmdtyID commCurr4 = new GCshCmdtyID(GCshCmdtyCurrNameSpace.CURRENCY, "EUR");
    GCshCmdtyID commCurr5 = new GCshCmdtyID(GCshCmdtyCurrNameSpace.CURRENCY, "EUR");
  
    assertEquals(commCurr4, commCurr5);
    assertNotEquals(commCurr1, commCurr4);
    assertNotEquals(commCurr2, commCurr4);
    assertNotEquals(commCurr31, commCurr4);
    assertNotEquals(commCurr32, commCurr4);
    
    GCshCmdtyID commCurr6 = new GCshCmdtyID(GCshCmdtyCurrNameSpace.CURRENCY, "JPY");
    
    assertNotEquals(commCurr4, commCurr6);
  }
  
  @Test
  public void test04_1() throws Exception
  {
      try 
      {
	GCshCmdtyID commCurrPrs = GCshCmdtyID.parse("CURRENCY:EUR");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
      
      // ---
      
      try 
      {
	GCshCmdtyID commCurrPrs = GCshCmdtyID.parse("CURRENCY:USD");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
      
  }

  @Test
  public void test04_2() throws Exception
  {
      GCshCmdtyID commCurrPrs = GCshCmdtyID.parse("XFRA:SAP");
      GCshCmdtyID commCurrRef = new GCshCmdtyID("XFRA", "SAP");
      
      assertEquals(GCshCmdtyCurrID.Type.SECURITY_GENERAL, commCurrPrs.getType());
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
      GCshCmdtyID commCurrPrs = GCshCmdtyID.parse("FUXNSTUELL:BURP"); // Wrong, but not check on this level
      GCshCmdtyID commCurrRef = new GCshCmdtyID();
      commCurrRef.setType(GCshCmdtyCurrID.Type.SECURITY_GENERAL);
      commCurrRef.setNameSpace("FUXNSTUELL");
      commCurrRef.setCode("BURP");
      
      assertEquals(GCshCmdtyCurrID.Type.SECURITY_GENERAL, commCurrPrs.getType());
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
