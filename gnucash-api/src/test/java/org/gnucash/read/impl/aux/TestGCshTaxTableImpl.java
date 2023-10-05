package org.gnucash.read.impl.aux;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collection;

import org.gnucash.ConstTest;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashFileImpl;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGCshTaxTableImpl
{
  private static final String TAXTABLE_1_ID = "3c9690f9f31b4cd0baa936048b833c06";
  private static final String TAXTABLE_2_ID = "cba6011c826f426fbc4a1a72c3d6c8ee";
    
  private GnucashFile  gcshFile = null;
  private GCshTaxTable taxTab = null;
  
  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGCshTaxTableImpl.class);  
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
      Collection<GCshTaxTable> taxTableList = gcshFile.getTaxTables();
      
      assertEquals(2, taxTableList.size());
      
      assertEquals(TAXTABLE_1_ID, ((GCshTaxTable) taxTableList.toArray()[0]).getId());
      assertEquals(TAXTABLE_2_ID, ((GCshTaxTable) taxTableList.toArray()[1]).getId());
  }
}
