package org.gnucash.read.spec;

import org.gnucash.read.GnucashGenerInvoiceEntry;

public interface GnucashCustomerInvoiceEntry extends GnucashGenerInvoiceEntry 
{
  String getInvoiceID();

  GnucashCustomerInvoice getInvoice() throws WrongInvoiceTypeException;
}
