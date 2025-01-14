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

  List<String> getCommoditieSpaces();

  List<String> getCommodities();

  List<String> getCommodities(String a_CommoditieSpace);

  FixedPointNumber getPrice(String a_Commoditie, LocalDate d);

  FixedPointNumber getPrice(String a_CommoditieSpace, String a_Commodity, LocalDate d, int depth);

}
