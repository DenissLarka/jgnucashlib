package org.gnucash.write.spec;

import org.gnucash.read.GnucashVendor;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerJob;

public interface GnucashWritableVendorJob extends GnucashWritableGenerJob {

	void remove() throws WrongInvoiceTypeException;

	/**
	 * Not used.
	 *
	 * @param a not used.
	 * @see GnucashGenerJob#JOB_TYPE
	 */
	//    void setVendorType(String a);

	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 *
	 * @param newVendor the vendor who issued this job.
	 */
	void setVendor(GnucashVendor newVendor) throws WrongInvoiceTypeException;

}
