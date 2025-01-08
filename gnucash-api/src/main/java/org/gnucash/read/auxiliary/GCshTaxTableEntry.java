package org.gnucash.read.auxiliary;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;

public interface GCshTaxTableEntry {

    /**
     * @see ${@link #getType()}
     */
    public static final String TYPE_VALUE   = "VALUE";
    public static final String TYPE_PERCENT = "PERCENT";
    
    // ---------------------------------------------------------------

    /**
     * usually ${@link GCshTaxTableEntry#TYPE_PERCENT}.
     * @see ${@link #getAmount())
     */
    String getType();

    /**
     * @return the amount the tax is ("16" for "16%")
     * @see ${@link #getType()}
     */
    FixedPointNumber getAmount();

    /**
     * @return Returns the accountID.
     * @see ${@link #myAccountID}
     */
    String getAccountID();

    /**
     * @return Returns the account.
     * @see ${@link #myAccount}
     */
    GnucashAccount getAccount();
    
    // ---------------------------------------------------------------

    /**
     * @return Returns the accountID.
     * @see ${@link #myAccountID}
     */
    void setAccountID(final String acctID);

    /**
     * @return Returns the account.
     * @see ${@link #myAccount}
     */
    void setAccount(final GnucashAccount acct);
}
