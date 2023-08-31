/**
 * GnucashJob.java
 * License: GPLv3 or later
 * Created on 14.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * <p>
 * <p>
 * -----------------------------------------------------------
 * major Changes:
 * 14.05.2005 - initial version
 * ...
 */
package org.gnucash.read;

import java.util.Collection;


/**
 * created: 14.05.2005 <br>
 * A job needs to be done. Once it or a part of it<br>
 * is done an invoice can be created and later be payed by the customer<br>
 * of this job.
 * @see GnucashInvoice
 * @see GnucashCustomer
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */

public interface GnucashJob {
  
  // ::MAGIC
  final static String TYPE_CUSTOMER = "gncCustomer";
  final static String TYPE_VENDOR   = "gncVendor";

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 * @return the file we are associated with
	 */
	GnucashFile getFile();

	/**
	 * @return the unique-id to identify this object with across name- and hirarchy-changes
	 */
	String getId();

	/**
	 * @return all invoices that refer to this job.
	 */
	Collection getInvoices();

	/**
	 * @return true if the job is still active
	 */
	boolean isJobActive();

	/**
	 *
	 * @return the user-defines number of this job (may contain non-digits)
	 */
	String getJobNumber();

	/**
	 *
	 * @return the name the user gave to this job.
	 */
	String getName();
	
	// ----------------------------
	
	   /**
     * Not used.
     * @return CUSTOMETYPE_CUSTOMER
     */
    String getOwnerType();

    /**
     *
     * @return the id of the customer this job is from.
     * @see #getCustomer()
     */
    String getOwnerId();
}
