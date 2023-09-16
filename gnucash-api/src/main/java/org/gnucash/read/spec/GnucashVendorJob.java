package org.gnucash.read.spec;

import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;

public interface GnucashVendorJob extends GnucashGenerJob {

	String getVendorType();

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
}
