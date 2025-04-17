package org.gnucash.read.spec;

import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerJob;

public interface GnucashCustomerJob extends GnucashGenerJob {

	/**
	 * @return the customer this job is from.
	 */
	GnucashCustomer getCustomer();

	/**
	 * @return the id of the customer this job is from.
	 * @see #getCustomer()
	 */
	String getCustomerId();

}
