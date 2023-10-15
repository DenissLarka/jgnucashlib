package org.gnucash.read.aux;

import java.util.Collection;

import org.gnucash.read.GnucashCustomer;



/**
 * Contains Tax-Rates.
 * @see GnucashCustomer
 */

public interface GCshTaxTable {

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
     * @see GCshTaxTable#isInvisible()
     */
    boolean isInvisible();

    /**
     * @return id of the parent-taxtable
     */
    String getParentID();

    /**
     * @return the parent-taxtable
     */
    GCshTaxTable getParent();

    /**
     * @return the entries in this tax-table
     */
    Collection<GCshTaxTableEntry> getEntries();

}
