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

import org.gnucash.numbers.FixedPointNumber;



/**
 * created: 22.09.2005 <br>
 * Contains Tax-Rates.
 * @see GnucashCustomer
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */

public interface GnucashTaxTable {
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
     *
     * @return the name the user gave to this job.
     */
    String getName();

    /**
     * @see GnucashTaxTable#isInvisible()
     */
    boolean isInvisible();

    /**
     * @return id of the parent-taxtable
     */
    String getParentID();

    /**
     * @return the parent-taxtable
     */
    GnucashTaxTable getParent();

    /**
     * @return the entries in this tax-table
     */
    Collection<TaxTableEntry> getEntries();

    /**
     * (c) 2005 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
     * Project: gnucashReader<br/>
     * GnucashTaxTableImpl.java<br/>
     * created: 22.09.2005 16:37:34 <br/>
     * <br/><br/>
     * Entry in the Taxtable
     * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
     */
    public interface TaxTableEntry {

        /**
         * @see ${@link #getType()}
         */
        String TYPE_PERCENT = "PERCENT";

        /**
         * @return the amount the tax is ("16" for "16%")
         * @see ${@link #getType()}
         */
        FixedPointNumber getAmount();

        /**
         * usually ${@link TaxTableEntry#TYPE_PERCENT}.
         * @see ${@link #getAmount())
         */
        String getType();

        /**
         * @return Returns the account.
         * @see ${@link #myAccount}
         */
        GnucashAccount getAccount();
        /**
         * @return Returns the accountID.
         * @see ${@link #myAccountID}
         */
        String getAccountID();


    }
}
