package org.gnucash.write.spec;

import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerJob;

public interface GnucashWritableCustomerJob extends GnucashWritableGenerJob {

	void remove() throws WrongInvoiceTypeException;

	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 *
	 * @param newCustomer the customer who issued this job.
	 */
	void setCustomer(GnucashCustomer newCustomer) throws WrongInvoiceTypeException;

}
