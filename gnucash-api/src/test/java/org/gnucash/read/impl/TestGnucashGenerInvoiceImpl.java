package org.gnucash.read.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import org.gnucash.ConstTest;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashTransaction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashGenerInvoiceImpl {
  private GnucashFile gcshFile = null;
  private GnucashGenerInvoice invc = null;

  public static final String INVC_1_ID = "d9967c10fdf1465e9394a3e4b1e7bd79";
  public static final String INVC_2_ID = "286fc2651a7848038a23bb7d065c8b67";
  public static final String INVC_3_ID = "b1e981f796b94ca0b17a9dccb91fedc0";
  public static final String INVC_4_ID = "4eb0dc387c3f4daba57b11b2a657d8a4";

  @BeforeMethod
  public void initialize() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    ApplicationMessages.setup();

    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
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
  public void test01() throws Exception {
    Assert.assertEquals(gcshFile.getNofEntriesGenerInvoiceMap(), 6);
  }

  // -----------------------------------------------------------------

  @Test
  public void testCust01_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_1_ID);

    Assert.assertEquals(invc.getId(), INVC_1_ID);
    Assert.assertEquals(invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT), "gncCustomer");
    Assert.assertEquals(invc.getNumber(), "R1730");
    Assert.assertEquals(invc.getDescription(), "Alles ohne Steuern / voll bezahlt");

    Assert.assertEquals(invc.getDateOpened().toString(), "2023-07-29T10:59Z");
    Assert.assertEquals(invc.getDatePosted().toString(), "2023-07-29T10:59Z");
  }

  @Test
  public void testCust02_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_1_ID);

    Assert.assertEquals(invc.getGenerEntries().size(), 2);

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(invc.getGenerEntries());
    Assert.assertEquals(((GnucashGenerInvoiceEntry) entrList.toArray()[0]).getId(), "92e54c04b66f4682a9afb48e27dfe397");
    Assert.assertEquals(((GnucashGenerInvoiceEntry) entrList.toArray()[1]).getId(), "3c67a99b5fe34387b596bb1fbab21a74");
  }

  @Test
  public void testCust03_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_1_ID);

    Assert.assertEquals(invc.getInvcAmountWithoutTaxes().doubleValue(), 1327.60, ConstTest.DIFF_TOLERANCE);

    Assert.assertEquals(invc.getInvcAmountWithTaxes().doubleValue(), 1327.60, ConstTest.DIFF_TOLERANCE);
  }

  @Test
  public void testCust04_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_1_ID);

    Assert.assertEquals(invc.getPostTransaction().getId(), "c97032ba41684b2bb5d1391c9d7547e9");
    Assert.assertEquals(invc.getPayingTransactions().size(), 1);

    LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) invc.getPayingTransactions();
    Collections.sort(trxList);
    Assert.assertEquals(((GnucashTransaction) trxList.toArray()[0]).getId(), "29557cfdf4594eb68b1a1b710722f991");

    Assert.assertEquals(invc.isInvcFullyPaid(), true);
  }

  // -----------------------------------------------------------------

  @Test
  public void testVend01_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_4_ID);

    Assert.assertEquals(invc.getId(), INVC_4_ID);
    Assert.assertEquals(invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT), "gncVendor");
    Assert.assertEquals(invc.getNumber(), "1730-383/2");
    Assert.assertEquals(invc.getDescription(), "Sie wissen schon: Gefälligkeiten, ne?");

    Assert.assertEquals(invc.getDateOpened().toString(), "2023-08-31T10:59Z");
    // ::TODO
    Assert.assertEquals(invc.getDatePosted().toString(), "2023-08-31T10:59Z");
  }

  @Test
  public void testVend01_2() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_2_ID);

    Assert.assertEquals(invc.getId(), INVC_2_ID);
    Assert.assertEquals(invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT), "gncVendor");
    Assert.assertEquals(invc.getNumber(), "2740921");
    Assert.assertEquals(invc.getDescription(), "Dat isjamaol eine schöne jepflejgte Reschnung!");

    Assert.assertEquals(invc.getDateOpened().toString(), "2023-08-30T10:59Z");
    // ::TODO
    Assert.assertEquals(invc.getDatePosted().toString(), "2023-08-30T10:59Z");
  }

  @Test
  public void testVend02_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_4_ID);

    Assert.assertEquals(invc.getGenerEntries().size(), 1);

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(invc.getGenerEntries());
    Assert.assertEquals(((GnucashGenerInvoiceEntry) entrList.toArray()[0]).getId(), "0041b8d397f04ae4a2e9e3c7f991c4ec");
  }

  @Test
  public void testVend02_2() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_2_ID);

    Assert.assertEquals(invc.getGenerEntries().size(), 2);

    TreeSet entrList = new TreeSet(); // sort elements of HashSet
    entrList.addAll(invc.getGenerEntries());
    Assert.assertEquals(((GnucashGenerInvoiceEntry) entrList.toArray()[1]).getId(), "513589a11391496cbb8d025fc1e87eaa");
    Assert.assertEquals(((GnucashGenerInvoiceEntry) entrList.toArray()[0]).getId(), "dc3c53f07ff64199ad4ea38988b3f40a");
  }

  @Test
  public void testVend03_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_4_ID);

    Assert.assertEquals(invc.getBillAmountWithoutTaxes().doubleValue(), 41.40, ConstTest.DIFF_TOLERANCE);
    // Note: due to (purposefully) incorrect booking, the gross amount
    // of this bill is *not* 49.27 EUR, but 41.40 EUR (its net amount).
    Assert.assertEquals(invc.getBillAmountWithTaxes().doubleValue(), 41.40, ConstTest.DIFF_TOLERANCE);
  }

  @Test
  public void testVend03_2() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_2_ID);

    Assert.assertEquals(invc.getBillAmountWithoutTaxes().doubleValue(), 79.11, ConstTest.DIFF_TOLERANCE);
    Assert.assertEquals(invc.getBillAmountWithTaxes().doubleValue(), 94.14, ConstTest.DIFF_TOLERANCE);
  }

  @Test
  public void testVend04_1() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_4_ID);

    // assertEquals("xxx", invc.getPostTransaction());

    // ::TODO
    Assert.assertEquals(invc.getPayingTransactions().size(), 0);

    // LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) bllSpec.getPayingTransactions();
    // Collections.sort(trxList);
    // assertEquals("xxx",
    // ((GnucashTransaction) bllSpec.getPayingTransactions().toArray()[0]).getId());

    Assert.assertEquals(invc.isBillFullyPaid(), false);
  }

  @Test
  public void testVend04_2() throws Exception {
    invc = gcshFile.getGenerInvoiceByID(INVC_2_ID);

    Assert.assertEquals(invc.getPostTransaction().getId(), "aa64d862bb5e4d749eb41f198b28d73d");
    Assert.assertEquals(invc.getPayingTransactions().size(), 1);

    LinkedList<GnucashTransaction> trxList = (LinkedList<GnucashTransaction>) invc.getPayingTransactions();
    Collections.sort(trxList);
    Assert.assertEquals(((GnucashTransaction) trxList.toArray()[0]).getId(), "ccff780b18294435bf03c6cb1ac325c1");

    Assert.assertEquals(invc.isBillFullyPaid(), true);
  }
}
