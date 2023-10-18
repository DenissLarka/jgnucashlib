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
    assertEquals(CurrencyNameSpace.NAMESPACE_CURRENCY, commCurr.getNameSpace());
    assertEquals("EUR", commCurr.getCurrency().getCurrencyCode());
    assertEquals(null, commCurr.getSecCode());

    assertEquals("CURRENCY:EUR", commCurr.toString());
    
    // ---
    
    commCurr = new CmdtyCurrID(Currency.getInstance("USD"));
    
    assertEquals(CmdtyCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(CurrencyNameSpace.NAMESPACE_CURRENCY, commCurr.getNameSpace());
    assertEquals("USD", commCurr.getCurrency().getCurrencyCode());
    assertEquals(null, commCurr.getSecCode());

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
    CmdtyCurrID commCurr = new CmdtyCurrID(CurrencyNameSpace.NAMESPACE_EUREX, "MBG");
    
    assertEquals(CmdtyCurrID.Type.SECURITY, commCurr.getType());
    assertEquals(CurrencyNameSpace.NAMESPACE_EUREX, commCurr.getNameSpace());
    assertEquals(null, commCurr.getCurrency());
    assertEquals("MBG", commCurr.getSecCode());

    assertEquals("EUREX:MBG", commCurr.toString());
  }

  @Test
  public void test03() throws Exception
  {
    CmdtyCurrID commCurr1 = new CmdtyCurrID(CurrencyNameSpace.NAMESPACE_EUREX, "MBG");
    CmdtyCurrID commCurr2 = new CmdtyCurrID("EUREX", "MBG");
  
    assertEquals(commCurr1, commCurr2);
    
    // ---

    CmdtyCurrID commCurr31 = new CmdtyCurrID(CurrencyNameSpace.NAMESPACE_AMEX, "MBG");
    CmdtyCurrID commCurr32 = new CmdtyCurrID(CurrencyNameSpace.NAMESPACE_EUREX, "DIS");
    
    assertNotEquals(commCurr1, commCurr31);
    assertNotEquals(commCurr1, commCurr32);
    assertNotEquals(commCurr31, commCurr32);
    
    // ---

    CmdtyCurrID commCurr4 = new CmdtyCurrID(Currency.getInstance("EUR"));
    CmdtyCurrID commCurr5 = new CmdtyCurrID(Currency.getInstance("EUR"));
  
    assertEquals(commCurr4, commCurr5);
    assertNotEquals(commCurr1, commCurr4);
    assertNotEquals(commCurr2, commCurr4);
    assertNotEquals(commCurr31, commCurr4);
    assertNotEquals(commCurr32, commCurr4);
    
    CmdtyCurrID commCurr6 = new CmdtyCurrID(Currency.getInstance("JPY"));
    
    assertNotEquals(commCurr4, commCurr6);
  }
}
