package org.gnucash.read.spec;

import java.util.Collection;

import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerJob;

public interface GnucashCustomerJob extends GnucashGenerJob {

	/**
	 *
	 * @return the customer this job is from.
	 */
	GnucashCustomer getCustomer();

	/**
	 *
	 * @return the id of the customer this job is from.
	 * @see #getCustomer()
	 */
	String getCustomerId();
	
	// -----------------------------------------------------------
	
	Collection<GnucashJobInvoice> getInvoices() throws WrongInvoiceTypeException;

	public void addInvoice(final GnucashJobInvoice invc);
	
}
