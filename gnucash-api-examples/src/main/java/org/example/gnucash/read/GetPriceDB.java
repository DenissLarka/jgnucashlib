package org.example.gnucash.read;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.gnucash.Const;
import org.gnucash.generated.GncPricedb;
import org.gnucash.generated.Price;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashPriceDBImpl;

public class GetPriceDB {
  // BEGIN Example data -- adapt to your needs
  private static String gcshFileName = "example_in.gnucash";
  // END Example data

  // -----------------------------------------------------------------

  public static void main(String[] args) {
    try {
      GetPriceDB tool = new GetPriceDB();
      tool.kernel();
    } catch (Exception exc) {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  protected void kernel() throws Exception {
    GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));

    GncPricedb pricdb = gcshFile.getPriceDB();
    List<Price> prices = pricdb.getPrice();
    prices.forEach(price -> {
      // System.out.println("Price source: " + price.getPriceSource());
      System.out.println("Price type: " + price.getPriceType());
      // System.out.println("Commidity space: " + price.getPriceCommodity().getCmdtySpace());
      System.out.println("Commidity id: " + price.getPriceCommodity().getCmdtyId());
      System.out.println("Price value: " + price.getPriceValue());
      // System.out.println("Price cur cmdty id: " + price.getPriceCurrency().getCmdtySpace());
      // System.out.println("Price cur cmdty id: " + price.getPriceCurrency().getCmdtyId());
      System.out.println("Ts date: " + price.getPriceTime().getTsDate());
      System.out.println("====================");
    });

    GnucashPriceDBImpl pdb = new GnucashPriceDBImpl(gcshFile);
    List<String> aap = pdb.getCommodities();
    System.out.println("Commidities: " + aap);

    // Gebruik SimpleDateFormat om een datum te parseren
    String stime = "23:59:59";
    String dateString = "2012-03-02" + " " + stime;
    // DateTimeFormatter formatter = new DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);
    LocalDate parsedDate = LocalDate.parse(dateString, formatter);
    FixedPointNumber pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    dateString = "2012-03-05" + " " + stime;
    parsedDate = LocalDate.parse(dateString, formatter);
    pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    dateString = "2022-03-05" + " " + stime;
    parsedDate = LocalDate.parse(dateString, formatter);
    pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    dateString = "2023-07-18" + " " + stime;
    parsedDate = LocalDate.parse(dateString, formatter);
    pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

    dateString = "2023-07-19" + " " + stime;
    parsedDate = LocalDate.parse(dateString, formatter);
    pr = pdb.getPrice("SAP", parsedDate);
    System.out.println(dateString + " " + pr.toString());

  }

  // -----------------------------------------------------------------

}
