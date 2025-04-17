package org.gnucash.read.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoiceEntry;

public interface GnucashCustomerInvoiceEntry extends GnucashGenerInvoiceEntry {
	String getInvoiceID();

	GnucashCustomerInvoice getInvoice() throws WrongInvoiceTypeException;

	// -----------------------------------------------------------------

	FixedPointNumber getPrice() throws WrongInvoiceTypeException;

	String getPriceFormatted() throws WrongInvoiceTypeException;

}
