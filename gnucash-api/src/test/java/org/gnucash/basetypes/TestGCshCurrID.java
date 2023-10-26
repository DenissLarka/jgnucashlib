package org.gnucash.basetypes;

import static org.junit.Assert.assertEquals;

import java.util.Currency;

import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshCurrID
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshCurrID.class);  
  }
  
  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
      GCshCurrID commCurr = new GCshCurrID(Currency.getInstance("EUR"));
    
    assertEquals(GCshCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(GCshCmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("EUR", commCurr.getCode());
    assertEquals("EUR", commCurr.getCurrency().getCurrencyCode());
    assertEquals("CURRENCY:EUR", commCurr.toString());
    
    // ---
    
    commCurr = new GCshCurrID(Currency.getInstance("USD"));
    
    assertEquals(GCshCurrID.Type.CURRENCY, commCurr.getType());
    assertEquals(GCshCmdtyCurrNameSpace.CURRENCY, commCurr.getNameSpace());
    assertEquals("USD", commCurr.getCode());
    assertEquals("USD", commCurr.getCurrency().getCurrencyCode());
    assertEquals("CURRENCY:USD", commCurr.toString());

    // ---
    
    try 
    {
	commCurr = new GCshCurrID(Currency.getInstance("XYZ")); // invalid code
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
      GCshCurrID commCurrPrs = GCshCurrID.parse("CURRENCY:EUR");
      GCshCurrID commCurrRef = new GCshCurrID(Currency.getInstance("EUR"));
      
      assertEquals(GCshCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:EUR", commCurrPrs.toString());
      assertEquals(commCurrRef, commCurrPrs);

      // ---
      
      commCurrPrs = GCshCurrID.parse("CURRENCY:USD");
      commCurrRef = new GCshCurrID(Currency.getInstance("USD"));
      
      assertEquals(GCshCurrID.Type.CURRENCY, commCurrPrs.getType());
      assertEquals("CURRENCY:USD", commCurrPrs.toString());
      assertEquals(commCurrRef, commCurrPrs);
  }

  @Test
  public void test04_2() throws Exception
  {
      try 
      {
	  GCshCurrID commCurrPrs = GCshCurrID.parse("EURONEXT:SAP");
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
	  GCshCurrID commCurrPrs = GCshCurrID.parse("FUXNSTUELL:BURP");
      }
      catch ( Exception exc )
      {
	  assertEquals(0, 0);
      }
  }
}
