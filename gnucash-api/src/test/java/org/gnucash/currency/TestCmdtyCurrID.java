package org.gnucash.currency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Currency;

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
    CmdtyCurrID commCurr = new CmdtyCurrID(Currency.getInstance("EUR"));
    
    assertEquals(CmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals("EUR", commCurr.getCurrency().getCurrencyCode());
    
    try
    {
	commCurr.getExchange(); // invalid call in this context
    }
    catch ( InvalidCmdtyCurrTypeException exc )
    {
	assertEquals(0, 0);
    }
    
    try
    {
	commCurr.getSecCode(); // invalid call in this context
    }
    catch ( InvalidCmdtyCurrTypeException exc )
    {
	assertEquals(0, 0);
    }
    
    assertEquals("CURRENCY:EUR", commCurr.toString());
    
    // ---
    
    commCurr = new CmdtyCurrID(Currency.getInstance("USD"));
    
    assertEquals(CmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals("USD", commCurr.getCurrency().getCurrencyCode());

    try
    {
	commCurr.getExchange(); // illegal call in this context
    }
    catch ( InvalidCmdtyCurrTypeException exc )
    {
	assertEquals(0, 0);
    }

    try
    {
	commCurr.getSecCode(); // illegal call in this context
    }
    catch ( InvalidCmdtyCurrTypeException exc )
    {
	assertEquals(0, 0);
    }

    assertEquals("CURRENCY:USD", commCurr.toString());

    // ---
    
    try 
    {
	commCurr = new CmdtyCurrID(Currency.getInstance("XYZ")); // invalid code
    }
    catch (Exception exc)
    {
	// correct behaviour: Throw exception 
	assertEquals(0, 0);
    }
  }

  @Test
  public void test02() throws Exception
  {
    CmdtyCurrID commCurr = new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.EURONEXT, "MBG");
    
    assertEquals(CmdtyCurrID.Type.SECURITY_EXCHANGE, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.Exchange.EURONEXT, commCurr.getExchange());
    assertEquals("MBG", commCurr.getSecCode());

    try
    {
	commCurr.getCurrency(); // invalid call in this context
    }
    catch ( InvalidCmdtyCurrTypeException exc )
    {
	assertEquals(0, 0);
    }
    
    assertEquals("EURONEXT:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    CmdtyCurrID commCurr1  = new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.EURONEXT, "MBG");
    CmdtyCurrID commCurr21 = new CmdtyCurrID("EURONEXT", "MBG", true);
    CmdtyCurrID commCurr22 = new CmdtyCurrID("EURONEXT", "MBG", false);
  
    assertEquals(commCurr1.toString(), commCurr21.toString());
    assertEquals(commCurr1.toString(), commCurr21.toString());
    assertEquals(commCurr1.toStringLong(), commCurr21.toStringLong());
    assertEquals(commCurr1.toStringLong(), commCurr21.toStringLong());
    assertEquals(commCurr1, commCurr21);
    assertNotEquals(commCurr1, commCurr22);
      
    // ---

    CmdtyCurrID commCurr31 = new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.NYSE, "MBG");
    CmdtyCurrID commCurr32 = new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.EURONEXT, "DIS");
    
    assertNotEquals(commCurr1, commCurr31);
    assertNotEquals(commCurr1, commCurr32);
    assertNotEquals(commCurr31, commCurr32);
    
    // ---

    CmdtyCurrID commCurr4 = new CmdtyCurrID(Currency.getInstance("EUR"));
    CmdtyCurrID commCurr5 = new CmdtyCurrID(Currency.getInstance("EUR"));
  
    assertEquals(commCurr4, commCurr5);
    assertNotEquals(commCurr1, commCurr4);
    assertNotEquals(commCurr21, commCurr4);
    assertNotEquals(commCurr22, commCurr4);
    assertNotEquals(commCurr31, commCurr4);
    assertNotEquals(commCurr32, commCurr4);
    
    CmdtyCurrID commCurr6 = new CmdtyCurrID(Currency.getInstance("JPY"));
    
    assertNotEquals(commCurr4, commCurr6);
  }
  
  @Test
  public void test04_1() throws Exception
  {
      CmdtyCurrID commCurrPrs = CmdtyCurrID.parse("CURRENCY:EUR");
      CmdtyCurrID commCurrRef = new CmdtyCurrID(Currency.getInstance("EUR"));
      
      assertEquals(CmdtyCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:EUR", commCurrPrs.toString());
      assertEquals(commCurrRef, commCurrPrs);

      // ---
      
      commCurrPrs = CmdtyCurrID.parse("CURRENCY:USD");
      commCurrRef = new CmdtyCurrID(Currency.getInstance("USD"));
      
      assertEquals(CmdtyCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:USD", commCurrPrs.toString());
      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_2() throws Exception
  {
      CmdtyCurrID commCurrPrs = CmdtyCurrID.parse("EURONEXT:SAP");
      CmdtyCurrID commCurrRef = new CmdtyCurrID(CmdtyCurrNameSpace.Exchange.EURONEXT, "SAP");
      
      assertEquals(CmdtyCurrID.Type.SECURITY_EXCHANGE, commCurrPrs.getType());
      assertEquals("EURONEXT:SAP", commCurrPrs.toString());
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
      CmdtyCurrID commCurrPrs = CmdtyCurrID.parse("FUXNSTUELL:BURP");
      CmdtyCurrID commCurrRef = new CmdtyCurrID();
      commCurrRef.setType(CmdtyCurrID.Type.SECURITY_GENERAL);
      commCurrRef.setNameSpaceFree("FUXNSTUELL");
      commCurrRef.setSecCode("BURP");
      
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
