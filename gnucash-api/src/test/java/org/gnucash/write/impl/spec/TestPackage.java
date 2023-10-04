package org.gnucash.write.impl.spec;

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
    
    suite.addTest(org.gnucash.write.impl.spec.TestGnucashWritableCustomerInvoiceImpl.suite());
    suite.addTest(org.gnucash.write.impl.spec.TestGnucashWritableVendorBillImpl.suite());
    suite.addTest(org.gnucash.write.impl.spec.TestGnucashWritableJobInvoiceImpl.suite());

    return suite;
  }
}
