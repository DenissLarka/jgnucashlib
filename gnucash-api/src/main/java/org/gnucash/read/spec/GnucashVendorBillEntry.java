package org.gnucash.read.spec;

import org.gnucash.read.GnucashGenerInvoiceEntry;

public interface GnucashVendorBillEntry extends GnucashGenerInvoiceEntry 
{
  String getBillID();

  GnucashVendorBill getBill() throws WrongInvoiceTypeException;
}
