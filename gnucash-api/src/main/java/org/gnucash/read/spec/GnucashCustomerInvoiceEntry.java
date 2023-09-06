package org.gnucash.read.spec;

import org.gnucash.read.GnucashCustVendInvoiceEntry;

public interface GnucashCustomerInvoiceEntry extends GnucashCustVendInvoiceEntry 
{
  String getInvoiceID();

  GnucashCustomerInvoice getInvoice() throws WrongInvoiceTypeException;
}
