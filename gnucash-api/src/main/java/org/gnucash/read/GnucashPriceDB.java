package org.gnucash.read;

import java.time.LocalDate;
import java.util.List;

import org.gnucash.numbers.FixedPointNumber;

public interface GnucashPriceDB {
  /**
   * The gnucash-file is the top-level class to contain everything.
   * 
   * @return the file we are associated with
   */
  GnucashFile getFile();

  List<String> getStocks();

  FixedPointNumber getPrice(String a_Stock, LocalDate d);

}
