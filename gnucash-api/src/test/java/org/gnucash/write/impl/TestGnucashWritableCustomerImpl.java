package org.gnucash.write.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.write.GnucashWritableCustomer;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashWritableCustomerImpl
{
    public static GnucashFileWritingImpl gcshInFile = null;
    // public static String outFileNameAbs = null;

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashWritableCustomerImpl.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    InputStream gcshInFileStream = null;
    try 
    {
      gcshInFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME_IN);
    } 
    catch ( Exception exc ) 
    {
      System.err.println("Cannot generate input stream from resource");
      return;
    }
    
    try
    {
      gcshInFile = new GnucashFileWritingImpl(gcshInFileStream);
    }
    catch ( Exception exc )
    {
      System.err.println("Cannot parse GnuCash in-file");
      exc.printStackTrace();
    }
    
//    URL outFileNameAbsURL = classLoader.getResource(ConstTest.GCSH_FILENAME_IN); // sic
//    System.err.println("Out file name (URL): '" + outFileNameAbsURL + "'");
//    outFileNameAbs = outFileNameAbsURL.getPath();
//    outFileNameAbs = outFileNameAbs.replace(ConstTest.GCSH_FILENAME_IN, ConstTest.GCSH_FILENAME_OUT);
//    System.err.println("Out file name: '" + outFileNameAbs + "'");
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
      GnucashWritableCustomer cust = gcshInFile.createWritableCustomer();
      cust.setNumber(GnucashCustomer.getNewNumber(cust));
      cust.setName("Frederic Austerlitz");
      
      gcshInFile.writeFile(new File(ConstTest.GCSH_FILENAME_OUT));
  }
}
