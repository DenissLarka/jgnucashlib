package org.gnucash.read.aux;

import java.util.Collection;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;



/**
 * Contains Tax-Rates.
 * @see GnucashCustomer
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
