package org.gnucash.read.impl;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.gnucash.Const;
import org.gnucash.ConstTest;
import org.gnucash.generated.GncPricedb;
import org.gnucash.generated.Price;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.numbers.FixedPointNumber;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestGnucashPriceDB {
  private GnucashFileImpl gcshFile = null;
  private GncPricedb pricedb;

  @BeforeMethod
  public void initialize() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("TestGnucashPriceDB GnuCash test file: '" + gcshFile + "'");
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
  public void testPriceDB01_1() throws Exception {
    pricedb = gcshFile.getPriceDB();
    List<Price> prices = pricedb.getPrice();

    Assert.assertEquals(prices.size(), 6);
  }

  @Test
  public void testPriceDB01_2() throws Exception {
    // System.out.println("testPriceDB01_2");
    pricedb = gcshFile.getPriceDB();
    List<Price> prices = pricedb.getPrice();

    Price l_price = prices.getFirst();
    Assert.assertEquals(l_price.getPriceCommodity().getCmdtyId(), "MBG");
  }

  @Test
  public void testPriceDB01_3() throws Exception {
    // System.out.println("testPriceDB01_3");
    pricedb = gcshFile.getPriceDB();
    GnucashPriceDBImpl pdb = new GnucashPriceDBImpl(gcshFile);

    // Gebruik SimpleDateFormat om een datum te parseren
    String stime = "23:59:59";
    String dateString = "2012-03-02" + " " + stime;
    // DateTimeFormatter formatter = new DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    LocalDate parsedDate = LocalDate.parse(dateString, formatter);
    FixedPointNumber pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    Assert.assertEquals(pr.toString(), "51200000.000000");
  }

  @Test
  public void testPriceDB01_4() throws Exception {
    // System.out.println("testPriceDB01_4");
    pricedb = gcshFile.getPriceDB();
    GnucashPriceDBImpl pdb = new GnucashPriceDBImpl(gcshFile);

    // Gebruik SimpleDateFormat om een datum te parseren
    String stime = "23:59:59";
    String dateString = "2012-03-05" + " " + stime;
    // DateTimeFormatter formatter = new DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    LocalDate parsedDate = LocalDate.parse(dateString, formatter);
    FixedPointNumber pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    Assert.assertEquals(pr.toString(), "51390000.000000");
  }

  @Test
  public void testPriceDB01_5() throws Exception {
    // System.out.println("testPriceDB01_4");
    pricedb = gcshFile.getPriceDB();
    GnucashPriceDBImpl pdb = new GnucashPriceDBImpl(gcshFile);

    // Gebruik SimpleDateFormat om een datum te parseren
    String stime = "23:59:59";
    String dateString = "2012-03-02" + " " + stime;
    // DateTimeFormatter formatter = new DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    LocalDate parsedDate = LocalDate.parse(dateString, formatter);
    FixedPointNumber pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    Assert.assertEquals(pr.toString(), "51200000.000000");
  }

  @Test
  public void testPriceDB01_6() throws Exception {
    // System.out.println("testPriceDB01_4");
    pricedb = gcshFile.getPriceDB();
    GnucashPriceDBImpl pdb = new GnucashPriceDBImpl(gcshFile);

    // Gebruik SimpleDateFormat om een datum te parseren
    String stime = "23:59:59";
    String dateString = "2012-03-01" + " " + stime;
    // DateTimeFormatter formatter = new DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    LocalDate parsedDate = LocalDate.parse(dateString, formatter);
    FixedPointNumber pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    Assert.assertEquals(pr.toString(), "51080000.000000");
  }

  @Test
  public void testPriceDB01_7() throws Exception {
    // System.out.println("testPriceDB01_4");
    pricedb = gcshFile.getPriceDB();
    GnucashPriceDBImpl pdb = new GnucashPriceDBImpl(gcshFile);

    // Gebruik SimpleDateFormat om een datum te parseren
    String stime = "23:59:59";
    String dateString = "2023-07-20" + " " + stime;
    // DateTimeFormatter formatter = new DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    LocalDate parsedDate = LocalDate.parse(dateString, formatter);
    FixedPointNumber pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    Assert.assertEquals(pr.toString(), "145");
  }

  @Test
  public void testPriceDB01_8() throws Exception {
    // System.out.println("testPriceDB01_4");
    pricedb = gcshFile.getPriceDB();
    GnucashPriceDBImpl pdb = new GnucashPriceDBImpl(gcshFile);

    // Gebruik SimpleDateFormat om een datum te parseren
    String stime = "01:59:59";
    String dateString = "2023-07-18" + " " + stime;
    // DateTimeFormatter formatter = new DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    LocalDate parsedDate = LocalDate.parse(dateString, formatter);
    FixedPointNumber pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    Assert.assertEquals(pr.toString(), "125.000000");
  }
}
