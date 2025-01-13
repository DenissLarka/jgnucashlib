package org.gnucash.read.impl;

import java.time.LocalDate;
import java.util.List;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashPriceDB;

public class GnucashPriceDBImpl implements GnucashPriceDB {

  private final GnucashFile myFile;

  public GnucashPriceDBImpl(final GnucashFile myFile) {
    super();
    this.myFile = myFile;
  }

  @Override
  public GnucashFile getFile() {
    return myFile;
  }

  @Override
  public List<String> getStocks() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FixedPointNumber getPrice(String a_Stock, LocalDate d) {
    // TODO Auto-generated method stub
    return null;
  }

}
