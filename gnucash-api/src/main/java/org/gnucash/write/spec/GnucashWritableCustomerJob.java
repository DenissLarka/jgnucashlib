package org.gnucash.write.spec;

import org.gnucash.read.GnucashCustomer;
import org.gnucash.write.GnucashWritableGenerJob;

public interface GnucashWritableCustomerJob extends GnucashWritableGenerJob 
{

	/**
	 * Not used.
	 * @param a not used.
	 * @see GnucashGenerJob#JOB_TYPE
	 */
	void setCustomerType(String a);

	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 * @param newCustomer the customer who issued this job.
	 */
	void setCustomer(GnucashCustomer newCustomer);

}
