package org.gnucash.read.impl.aux;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPackage extends TestCase
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static Test suite() throws Exception
  {
    TestSuite suite = new TestSuite();
    
    suite.addTest(org.gnucash.read.impl.aux.TestGCshBillTermsImpl.suite());
    suite.addTest(org.gnucash.read.impl.aux.TestGCshTaxTableImpl.suite());
    
    suite.addTest(org.gnucash.read.impl.aux.TestGCshPriceImpl.suite());

    return suite;
  }
}
