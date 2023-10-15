package org.gnucash.read.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoiceEntry;

public interface GnucashVendorBillEntry extends GnucashGenerInvoiceEntry 
{
  String getBillID();

  GnucashVendorBill getBill() throws WrongInvoiceTypeException;
  
  // -----------------------------------------------------------------

  FixedPointNumber getPrice() throws WrongInvoiceTypeException;

  String getPriceFormatted() throws WrongInvoiceTypeException;
  
}
