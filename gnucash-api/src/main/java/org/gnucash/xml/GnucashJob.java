/**
 * GnucashJob.java
 * License: GPLv3 or later
 * Created on 14.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 *
 *
 * -----------------------------------------------------------
 * major Changes:
 *  14.05.2005 - initial version
 * ...
 *
 */
package org.gnucash.xml;

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
     * @see #getCustomerType()
     */
    String CUSTOMETYPE_CUSTOMER = "gncCustomer";

    /**
     * Not used.
     * @return CUSTOMETYPE_CUSTOMER
     */
    String getCustomerType();

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
}
