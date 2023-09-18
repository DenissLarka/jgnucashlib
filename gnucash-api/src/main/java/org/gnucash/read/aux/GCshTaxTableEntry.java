package org.gnucash.read.aux;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;

public interface GCshTaxTableEntry {

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
     * usually ${@link GCshTaxTableEntry#TYPE_PERCENT}.
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
