package org.gnucash.read.impl.spec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPackage extends TestCase
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() throws Exception
  {
    TestSuite suite = new TestSuite();
    
    suite.addTest(org.gnucash.read.impl.spec.TestGnucashCustomerInvoiceImpl.suite());
    suite.addTest(org.gnucash.read.impl.spec.TestGnucashVendorInvoiceImpl.suite());

    return suite;
  }
}
