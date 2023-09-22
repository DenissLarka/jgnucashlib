package org.gnucash.read.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoiceEntry;

public interface GnucashJobInvoiceEntry extends GnucashGenerInvoiceEntry 
{
  String getInvoiceID();

  GnucashJobInvoice getInvoice() throws WrongInvoiceTypeException;
  
  // -----------------------------------------------------------------

  FixedPointNumber getPrice() throws WrongInvoiceTypeException;

  String getPriceFormatted() throws WrongInvoiceTypeException;
  
}
