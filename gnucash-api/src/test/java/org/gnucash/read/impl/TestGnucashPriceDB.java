package org.gnucash.read.impl;

import java.io.InputStream;
import java.util.List;

import org.gnucash.ConstTest;
import org.gnucash.generated.GncPricedb;
import org.gnucash.generated.Price;
import org.gnucash.read.GnucashFile;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashPriceDB {
  private GnucashFile gcshFile = null;
  private GncPricedb pricedb;

  @BeforeMethod
  public void initialize() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
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
  public void testPriceDB01_1() throws Exception {
    pricedb = gcshFile.getPriceDB();
    List<Price> prices = pricedb.getPrice();

    Assert.assertEquals(prices.size(), 6);
  }
}
