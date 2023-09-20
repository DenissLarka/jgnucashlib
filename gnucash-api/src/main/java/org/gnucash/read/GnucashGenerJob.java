package org.gnucash.read;

import java.util.Collection;

import org.gnucash.generated.GncV2;
import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.read.aux.GCshOwner;


/**
 * A job needs to be done. Once it or a part of it<br>
 * is done an invoice can be created and later be Paid by the customer<br>
 * of this job.
 * @see GnucashGenerInvoice
 * @see GnucashCustomer
 */
public interface GnucashGenerJob {
  
  /**
   * @deprecated Use {@link GCshOwner#TYPE_CUSTOMER} instead
   */
  public static final String TYPE_CUSTOMER = GCshOwner.TYPE_CUSTOMER;
  /**
   * @deprecated Use {@link GCshOwner#TYPE_VENDOR} instead
   */
  public static final String TYPE_VENDOR   = GCshOwner.TYPE_VENDOR;
  /**
   * @deprecated Use {@link GCshOwner#TYPE_EMPLOYEE} instead
   */
  public static final String TYPE_EMPLOYEE = GCshOwner.TYPE_EMPLOYEE; // Not used yet, for future releases
  /**
   * @deprecated Use {@link GCshOwner#TYPE_JOB} instead
   */
  public static final String TYPE_JOB      = GCshOwner.TYPE_JOB;
  
  	// -----------------------------------------------------------------

  	@SuppressWarnings("exports")
  	GncV2.GncBook.GncGncJob getJwsdpPeer();

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 * @return the file we are associated with
	 */
	GnucashFile getFile();

	// -----------------------------------------------------------------

	/**
	 * @return the unique-id to identify this object with across name- and hirarchy-changes
	 */
	String getId();

	/**
	 * @return all invoices that refer to this job.
	 */
	Collection<GnucashGenerInvoice> getGenerInvoices();

	/**
	 * @return true if the job is still active
	 */
	boolean isJobActive();

	/**
	 *
	 * @return the user-defines number of this job (may contain non-digits)
	 */
	String getNumber();

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
