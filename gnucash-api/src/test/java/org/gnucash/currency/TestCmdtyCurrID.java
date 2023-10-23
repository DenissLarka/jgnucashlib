package org.gnucash.currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestCmdtyCurrID
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestCmdtyCurrID.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
    CmdtyCurrID commCurr = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "EUR");
    
    assertEquals(CmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("EUR", commCurr.getCode());
    assertEquals("CURRENCY:EUR", commCurr.toString());
    
    // ---
    
    commCurr = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "USD");
    
    assertEquals(CmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("USD", commCurr.getCode());
    assertEquals("CURRENCY:USD", commCurr.toString());

    // ---
    
    commCurr = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "XYZ"); // Wrong, but no check on this level
    
    assertEquals(CmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("XYZ", commCurr.getCode());
    assertEquals("CURRENCY:XYZ", commCurr.toString());

  }

  @Test
  public void test02() throws Exception
  {
    CmdtyCurrID commCurr = new CmdtyCurrID("EURONEXT", "MBG");
    
    assertEquals(CmdtyCurrID.Type.SECURITY_GENERAL, commCurr.getType());
    assertEquals("EURONEXT", commCurr.getNameSpace());
    assertEquals("MBG", commCurr.getCode());
    assertEquals("EURONEXT:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    CmdtyCurrID commCurr1  = new CmdtyCurrID("EURONEXT", "MBG");
    CmdtyCurrID commCurr2 = new CmdtyCurrID("EURONEXT", "MBG");
  
    assertEquals(commCurr1.toString(), commCurr2.toString());
    assertEquals(commCurr1.toStringLong(), commCurr2.toStringLong());
    assertEquals(commCurr1, commCurr2);
      
    // ---

    CmdtyCurrID commCurr31 = new CmdtyCurrID("NYSE", "MBG");
    CmdtyCurrID commCurr32 = new CmdtyCurrID("EURONEXT", "DIS");
    
    assertNotEquals(commCurr1, commCurr31);
    assertNotEquals(commCurr1, commCurr32);
    assertNotEquals(commCurr31, commCurr32);
    
    // ---

    CmdtyCurrID commCurr4 = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "EUR");
    CmdtyCurrID commCurr5 = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "EUR");
  
    assertEquals(commCurr4, commCurr5);
    assertNotEquals(commCurr1, commCurr4);
    assertNotEquals(commCurr2, commCurr4);
    assertNotEquals(commCurr31, commCurr4);
    assertNotEquals(commCurr32, commCurr4);
    
    CmdtyCurrID commCurr6 = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "JPY");
    
    assertNotEquals(commCurr4, commCurr6);
  }
  
  @Test
  public void test04_1() throws Exception
  {
      CmdtyCurrID commCurrPrs = CmdtyCurrID.parse("CURRENCY:EUR");
      CmdtyCurrID commCurrRef = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "EUR");
      
      assertEquals(CmdtyCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:EUR", commCurrPrs.toString());
      assertEquals(commCurrRef.toString(), commCurrPrs.toString());
      assertEquals(commCurrRef.toStringLong(), commCurrPrs.toStringLong());
      assertEquals(commCurrRef, commCurrPrs);

      // ---
      
      commCurrPrs = CmdtyCurrID.parse("CURRENCY:USD");
      commCurrRef = new CmdtyCurrID(CmdtyCurrNameSpace.CURRENCY, "USD");
      
      assertEquals(CmdtyCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:USD", commCurrPrs.toString());
      assertEquals(commCurrRef.toString(), commCurrPrs.toString());
      assertEquals(commCurrRef.toStringLong(), commCurrPrs.toStringLong());
      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_2() throws Exception
  {
      CmdtyCurrID commCurrPrs = CmdtyCurrID.parse("EURONEXT:SAP");
      CmdtyCurrID commCurrRef = new CmdtyCurrID("EURONEXT", "SAP");
      
      assertEquals(CmdtyCurrID.Type.SECURITY_GENERAL, commCurrPrs.getType());
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
      CmdtyCurrID commCurrPrs = CmdtyCurrID.parse("FUXNSTUELL:BURP"); // Wrong, but not check on this level
      CmdtyCurrID commCurrRef = new CmdtyCurrID();
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
