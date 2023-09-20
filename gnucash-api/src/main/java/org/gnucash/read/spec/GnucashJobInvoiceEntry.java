package org.gnucash.read.spec;

import org.gnucash.read.GnucashGenerInvoiceEntry;

public interface GnucashJobInvoiceEntry extends GnucashGenerInvoiceEntry 
{
  String getInvoiceID();

  GnucashJobInvoice getInvoice() throws WrongInvoiceTypeException;
}
