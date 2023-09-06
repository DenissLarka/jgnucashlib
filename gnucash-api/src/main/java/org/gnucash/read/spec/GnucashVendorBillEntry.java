package org.gnucash.read.spec;

import org.gnucash.read.GnucashCustVendInvoiceEntry;

public interface GnucashVendorBillEntry extends GnucashCustVendInvoiceEntry 
{
  String getBillID();

  GnucashVendorBill getBill() throws WrongInvoiceTypeException;
}
