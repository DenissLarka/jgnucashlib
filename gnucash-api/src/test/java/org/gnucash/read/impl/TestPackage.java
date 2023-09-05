package org.gnucash.read.impl;

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
    
    suite.addTest(org.gnucash.read.impl.TestGnucashAccount.suite());
    suite.addTest(org.gnucash.read.impl.TestGnucashCustomer.suite());
    suite.addTest(org.gnucash.read.impl.TestGnucashVendor.suite());
    suite.addTest(org.gnucash.read.impl.TestGnucashTransaction.suite());
    
    suite.addTest(org.gnucash.read.impl.spec.TestPackage.suite());

    return suite;
  }
}
