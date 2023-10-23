package org.gnucash.currency;

import static org.junit.Assert.assertEquals;

import java.util.Currency;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestCurrencyID
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestCurrencyID.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
      CurrencyID commCurr = new CurrencyID(Currency.getInstance("EUR"));
    
    assertEquals(CurrencyID.Type.CURRENCY, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("EUR", commCurr.getCode());
    assertEquals("EUR", commCurr.getCurrency().getCurrencyCode());
    assertEquals("CURRENCY:EUR", commCurr.toString());
    
    // ---
    
    commCurr = new CurrencyID(Currency.getInstance("USD"));
    
    assertEquals(CurrencyID.Type.CURRENCY, commCurr.getType());
    assertEquals(CmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("USD", commCurr.getCode());
    assertEquals("USD", commCurr.getCurrency().getCurrencyCode());
    assertEquals("CURRENCY:USD", commCurr.toString());

    // ---
    
    try 
    {
	commCurr = new CurrencyID(Currency.getInstance("XYZ")); // invalid code
    }
    catch (Exception exc)
    {
	// correct behaviour: Throw exception 
	assertEquals(0, 0);
    }
  }

  @Test
  public void test04_1() throws Exception
  {
      CurrencyID commCurrPrs = CurrencyID.parse("CURRENCY:EUR");
      CurrencyID commCurrRef = new CurrencyID(Currency.getInstance("EUR"));
      
      assertEquals(CurrencyID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:EUR", commCurrPrs.toString());
      assertEquals(commCurrRef, commCurrPrs);

      // ---
      
      commCurrPrs = CurrencyID.parse("CURRENCY:USD");
      commCurrRef = new CurrencyID(Currency.getInstance("USD"));
      
      assertEquals(CurrencyID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:USD", commCurrPrs.toString());
      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_2() throws Exception
  {
      try 
      {
	  CurrencyID commCurrPrs = CurrencyID.parse("EURONEXT:SAP");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
  }

  @Test
  public void test04_3() throws Exception
  {
      try 
      {
	  CurrencyID commCurrPrs = CurrencyID.parse("FUXNSTUELL:BURP");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
  }
}
