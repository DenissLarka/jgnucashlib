package org.gnucash.read;

import java.util.Collection;

import org.gnucash.read.aux.GnucashOwner;


/**
 * A job needs to be done. Once it or a part of it<br>
 * is done an invoice can be created and later be Paid by the customer<br>
 * of this job.
 * @see GnucashGenerInvoice
 * @see GnucashCustomer
 */
public interface GnucashGenerJob {
  
  /**
   * @deprecated Use {@link GnucashOwner#TYPE_CUSTOMER} instead
   */
  public static final String TYPE_CUSTOMER = GnucashOwner.TYPE_CUSTOMER;
  /**
   * @deprecated Use {@link GnucashOwner#TYPE_VENDOR} instead
   */
  public static final String TYPE_VENDOR   = GnucashOwner.TYPE_VENDOR;
  /**
   * @deprecated Use {@link GnucashOwner#TYPE_EMPLOYEE} instead
   */
  public static final String TYPE_EMPLOYEE = GnucashOwner.TYPE_EMPLOYEE; // Not used yet, for future releases
  
  // -----------------------------------------------------------------

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
	Collection<GnucashGenerInvoice> getInvoices();

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
