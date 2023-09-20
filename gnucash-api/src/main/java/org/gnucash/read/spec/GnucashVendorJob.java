package org.gnucash.read.spec;

import java.util.Collection;

import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;

public interface GnucashVendorJob extends GnucashGenerJob {

	/**
	 *
	 * @return the vendor this job is from.
	 */
	GnucashVendor getVendor();

	/**
	 *
	 * @return the id of the vendor this job is from.
	 * @see #getVendor()
	 */
	String getVendorId();
	
	// -----------------------------------------------------------
	
	Collection<GnucashJobInvoice> getInvoices() throws WrongInvoiceTypeException;
	
	public void addInvoice(final GnucashJobInvoice invc);

}
