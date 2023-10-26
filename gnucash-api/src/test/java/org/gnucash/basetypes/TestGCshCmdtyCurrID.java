package org.gnucash.basetypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshCmdtyCurrID
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshCmdtyCurrID.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
    GCshCmdtyCurrID commCurr = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "EUR");
    
    assertEquals(GCshCmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(GCshCmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("EUR", commCurr.getCode());
    assertEquals("CURRENCY:EUR", commCurr.toString());
    
    // ---
    
    commCurr = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "USD");
    
    assertEquals(GCshCmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(GCshCmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("USD", commCurr.getCode());
    assertEquals("CURRENCY:USD", commCurr.toString());

    // ---
    
    commCurr = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "XYZ"); // Wrong, but no check on this level
    
    assertEquals(GCshCmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(GCshCmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("XYZ", commCurr.getCode());
    assertEquals("CURRENCY:XYZ", commCurr.toString());

  }

  @Test
  public void test02() throws Exception
  {
    GCshCmdtyCurrID commCurr = new GCshCmdtyCurrID("EURONEXT", "MBG");
    
    assertEquals(GCshCmdtyCurrID.Type.SECURITY_GENERAL, commCurr.getType());
    assertEquals("EURONEXT", commCurr.getNameSpace());
    assertEquals("MBG", commCurr.getCode());
    assertEquals("EURONEXT:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    GCshCmdtyCurrID commCurr1  = new GCshCmdtyCurrID("EURONEXT", "MBG");
    GCshCmdtyCurrID commCurr2 = new GCshCmdtyCurrID("EURONEXT", "MBG");
  
    assertEquals(commCurr1.toString(), commCurr2.toString());
    assertEquals(commCurr1.toStringLong(), commCurr2.toStringLong());
    assertEquals(commCurr1, commCurr2);
      
    // ---

    GCshCmdtyCurrID commCurr31 = new GCshCmdtyCurrID("NYSE", "MBG");
    GCshCmdtyCurrID commCurr32 = new GCshCmdtyCurrID("EURONEXT", "DIS");
    
    assertNotEquals(commCurr1, commCurr31);
    assertNotEquals(commCurr1, commCurr32);
    assertNotEquals(commCurr31, commCurr32);
    
    // ---

    GCshCmdtyCurrID commCurr4 = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "EUR");
    GCshCmdtyCurrID commCurr5 = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "EUR");
  
    assertEquals(commCurr4, commCurr5);
    assertNotEquals(commCurr1, commCurr4);
    assertNotEquals(commCurr2, commCurr4);
    assertNotEquals(commCurr31, commCurr4);
    assertNotEquals(commCurr32, commCurr4);
    
    GCshCmdtyCurrID commCurr6 = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "JPY");
    
    assertNotEquals(commCurr4, commCurr6);
  }
  
  @Test
  public void test04_1() throws Exception
  {
      GCshCmdtyCurrID commCurrPrs = GCshCmdtyCurrID.parse("CURRENCY:EUR");
      GCshCmdtyCurrID commCurrRef = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "EUR");
      
      assertEquals(GCshCmdtyCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:EUR", commCurrPrs.toString());
      assertEquals(commCurrRef.toString(), commCurrPrs.toString());
      assertEquals(commCurrRef.toStringLong(), commCurrPrs.toStringLong());
      assertEquals(commCurrRef, commCurrPrs);

      // ---
      
      commCurrPrs = GCshCmdtyCurrID.parse("CURRENCY:USD");
      commCurrRef = new GCshCmdtyCurrID(GCshCmdtyCurrNameSpace.CURRENCY, "USD");
      
      assertEquals(GCshCmdtyCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:USD", commCurrPrs.toString());
      assertEquals(commCurrRef.toString(), commCurrPrs.toString());
      assertEquals(commCurrRef.toStringLong(), commCurrPrs.toStringLong());
      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_2() throws Exception
  {
      GCshCmdtyCurrID commCurrPrs = GCshCmdtyCurrID.parse("EURONEXT:SAP");
      GCshCmdtyCurrID commCurrRef = new GCshCmdtyCurrID("EURONEXT", "SAP");
      
      assertEquals(GCshCmdtyCurrID.Type.SECURITY_GENERAL, commCurrPrs.getType());
      assertEquals("EURONEXT:SAP", commCurrPrs.toString());
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
      GCshCmdtyCurrID commCurrPrs = GCshCmdtyCurrID.parse("FUXNSTUELL:BURP"); // Wrong, but not check on this level
      GCshCmdtyCurrID commCurrRef = new GCshCmdtyCurrID();
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
