package org.gnucash.read.impl.spec;

import java.io.InputStream;
import java.util.TreeSet;

import org.gnucash.ConstTest;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashGenerInvoiceImpl;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashJobInvoiceEntry;
import org.gnucash.read.spec.SpecInvoiceCommon;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashJobInvoiceImpl {
  private GnucashFile gcshFile = null;
  private GnucashGenerInvoice invcGen = null;
  private GnucashJobInvoice invcSpec = null;

  private static final String INVC_3_ID = TestGnucashGenerInvoiceImpl.INVC_3_ID;

  @BeforeMethod
  public void initialize() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    ApplicationMessages.setup();

    InputStream gcshFileStream = null;
    try {
      gcshFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME);
    } catch (Exception exc) {
      System.err.println("Cannot generate input stream from resource");
      return;
    }

    try {
      gcshFile = new GnucashFileImpl(gcshFileStream);
    } catch (Exception exc) {
      System.err.println("Cannot parse GnuCash file");
      exc.printStackTrace();
    }
  }

  // -----------------------------------------------------------------

  @Test
  public void test01_1() throws Exception {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);

    Assert.assertEquals(invcSpec instanceof GnucashJobInvoiceImpl, true);
    Assert.assertEquals(invcSpec.getId(), INVC_3_ID);
    Assert.assertEquals(invcSpec.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT), "gncJob");
    Assert.assertEquals(invcSpec.getNumber(), "R94871");
    Assert.assertEquals(invcSpec.getDescription(), "With customer job / with taxes");

    Assert.assertEquals(invcSpec.getDateOpened().toString(), "2023-09-20T10:59Z");
    Assert.assertEquals(invcSpec.getDatePosted().toString(), "2023-09-20T10:59Z");
  }

  @Test
  public void test02_1() throws Exception {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);

    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implemetation error was
    // found with this test)
    Assert.assertEquals(invcGen.getGenerEntries().size(), 3);
    Assert.assertEquals(invcSpec.getGenerEntries().size(), 3);
    Assert.assertEquals(invcSpec.getEntries().size(), 3);

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(invcSpec.getEntries());
    Assert.assertEquals(((GnucashJobInvoiceEntry) entrList.toArray()[0]).getId(), "fa483972d10a4ce0abf2a7e1319706e7");
    Assert.assertEquals(((GnucashJobInvoiceEntry) entrList.toArray()[1]).getId(), "eb5eb3b7c1e34965b36fb6d5af183e82");
    Assert.assertEquals(((GnucashJobInvoiceEntry) entrList.toArray()[2]).getId(), "993eae09ce664094adf63b85509de2bc");
  }

  @Test
  public void test03_1() throws Exception {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);

    Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 4125.0,
        ((SpecInvoiceCommon) invcSpec).getAmountWithoutTaxes().doubleValue());

    Assert.assertEquals(ConstTest.DIFF_TOLERANCE, 4908.75,
        ((SpecInvoiceCommon) invcSpec).getAmountWithTaxes().doubleValue());
  }

  // ::TODO
  @Test
  public void test04_1() throws Exception {
    invcGen = gcshFile.getGenerInvoiceByID(INVC_3_ID);
    invcSpec = new GnucashJobInvoiceImpl(invcGen);

    Assert.assertEquals(invcGen.getPayingTransactions().size(), 0);
    Assert.assertEquals(invcSpec.getPayingTransactions().size(), 0);

    Assert.assertEquals(invcGen.isInvcFullyPaid(), false);
    Assert.assertEquals(invcSpec.isInvcFullyPaid(), false);
    Assert.assertEquals(((SpecInvoiceCommon) invcSpec).isFullyPaid(), false);
  }
}
