package org.gnucash.read.impl;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashTransaction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashTransactionImpl {
  private GnucashFile gcshFile = null;
  private GnucashTransaction trx = null;

  public static final String TRX_1_ID = "32b216aa73a44137aa5b041ab8739058";
  public static final String TRX_2_ID = "c97032ba41684b2bb5d1391c9d7547e9";

  // -----------------------------------------------------------------

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
  public void test01() throws Exception {
    trx = gcshFile.getTransactionByID(TRX_1_ID);
    Assert.assertNotEquals(null, trx);

    Assert.assertEquals(trx.getId(), TRX_1_ID);
    Assert.assertEquals(trx.getBalance().getBigDecimal().doubleValue(), 0.0, ConstTest.DIFF_TOLERANCE);
    Assert.assertEquals(trx.getDescription(), "Dividenderl");
    Assert.assertEquals(trx.getDatePosted().toString(), "2023-08-06T10:59Z");
    Assert.assertEquals(trx.getDateEntered().toString(), "2023-08-06T08:21:44Z");

    Assert.assertEquals(trx.getSplitsCount(), 3);
    Assert.assertEquals(trx.getSplits().get(0).getId(), "7abf90fe15124254ac3eb7ec33f798e7");
    Assert.assertEquals(trx.getSplits().get(1).getId(), "ea08a144322146cea38b39d134ca6fc1");
    Assert.assertEquals(trx.getSplits().get(2).getId(), "5c5fa881869843d090a932f8e6b15af2");
  }

  @Test
  public void test02() throws Exception {
    trx = gcshFile.getTransactionByID(TRX_2_ID);
    Assert.assertNotEquals(null, trx);

    Assert.assertEquals(trx.getId(), TRX_2_ID);
    Assert.assertEquals(trx.getBalance().getBigDecimal().doubleValue(), 0.0, ConstTest.DIFF_TOLERANCE);
    Assert.assertEquals(trx.getDescription(), "Unfug und Quatsch GmbH");
    Assert.assertEquals(trx.getDatePosted().toString(), "2023-07-29T10:59Z");
    Assert.assertEquals(trx.getDateEntered().toString(), "2023-09-13T08:36:54Z");

    Assert.assertEquals(trx.getSplitsCount(), 2);
    Assert.assertEquals(trx.getSplits().get(0).getId(), "f2a67737458d4af4ade616a23db32c2e");
    Assert.assertEquals(trx.getSplits().get(1).getId(), "d17361e4c5a14e84be4553b262839a7b");
  }
}
