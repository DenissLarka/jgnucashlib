package org.gnucash.read.impl;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.gnucash.Const;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashTransaction;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashTransaction
{
  private static GnucashFile        gcshFile = null;
  private static GnucashTransaction trx = null;
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashTransaction.class);  
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
      gcshFileStream = classLoader.getResourceAsStream(Const.GCSH_FILENAME);
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
    trx = gcshFile.getTransactionByID("32b216aa73a44137aa5b041ab8739058");
    assertEquals("32b216aa73a44137aa5b041ab8739058", trx.getId());
    // assertEquals(0.00, trx.getBalance());
    assertEquals("Dividenderl", trx.getDescription());
    assertEquals("2023-08-06T10:59Z", trx.getDatePosted().toString());
    assertEquals("2023-08-06T08:21:44Z", trx.getDateEntered().toString());
        
    assertEquals(3, trx.getSplitsCount());
    assertEquals("7abf90fe15124254ac3eb7ec33f798e7", trx.getSplits().get(0).getId());
    assertEquals("ea08a144322146cea38b39d134ca6fc1", trx.getSplits().get(1).getId());
    assertEquals("5c5fa881869843d090a932f8e6b15af2", trx.getSplits().get(2).getId());
  }
}
