package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashTransaction;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashTransactionImpl
{
  private GnucashFile        gcshFile = null;
  private GnucashTransaction trx = null;
  
  public static final String TRX_1_ID = "32b216aa73a44137aa5b041ab8739058";
  public static final String TRX_2_ID = "c97032ba41684b2bb5d1391c9d7547e9";

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashTransactionImpl.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    InputStream gcshFileStream = null;
    try 
    {
      gcshFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME);
    } 
    catch ( Exception exc ) 
    {
      System.err.println("Cannot generate input stream from resource");
      return;
    }
    
    try
    {
      gcshFile = new GnucashFileImpl(gcshFileStream);
    }
    catch ( Exception exc )
    {
      System.err.println("Cannot parse GnuCash file");
      exc.printStackTrace();
    }
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
    trx = gcshFile.getTransactionByID(TRX_1_ID);
    assertNotEquals(null, trx);
    
    assertEquals(TRX_1_ID, trx.getId());
    assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals("Dividenderl", trx.getDescription());
    assertEquals("2023-08-06T10:59Z", trx.getDatePosted().toString());
    assertEquals("2023-08-06T08:21:44Z", trx.getDateEntered().toString());
        
    assertEquals(3, trx.getSplitsCount());
    assertEquals("7abf90fe15124254ac3eb7ec33f798e7", trx.getSplits().get(0).getId());
    assertEquals("ea08a144322146cea38b39d134ca6fc1", trx.getSplits().get(1).getId());
    assertEquals("5c5fa881869843d090a932f8e6b15af2", trx.getSplits().get(2).getId());
  }
  
  @Test
  public void test02() throws Exception
  {
    trx = gcshFile.getTransactionByID(TRX_2_ID);
    assertNotEquals(null, trx);
    
    assertEquals(TRX_2_ID, trx.getId());
    assertEquals(0.0, trx.getBalance().getBigDecimal().doubleValue(), ConstTest.DIFF_TOLERANCE);
    assertEquals("Unfug und Quatsch GmbH", trx.getDescription());
    assertEquals("2023-07-29T10:59Z", trx.getDatePosted().toString());
    assertEquals("2023-09-13T08:36:54Z", trx.getDateEntered().toString());
        
    assertEquals(2, trx.getSplitsCount());
    assertEquals("f2a67737458d4af4ade616a23db32c2e", trx.getSplits().get(0).getId());
    assertEquals("d17361e4c5a14e84be4553b262839a7b", trx.getSplits().get(1).getId());
  }
}
