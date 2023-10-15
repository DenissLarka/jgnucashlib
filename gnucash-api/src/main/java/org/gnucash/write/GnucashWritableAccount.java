package org.gnucash.write;

import java.beans.PropertyChangeListener;
import java.time.LocalDate;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashObject;


/**
 * Account that can be modified.<br/>
 * Supported properties for the propertyChangeListeners:
 * <ul>
 * <li>name</li>
 * <li>currencyID</li>
 * <li>currencyNameSpace</li>
 * <li>description</li>
 * <li>type</li>
 * <li>parentAccount</li>
 * <li>transactionSplits (not giving the old value of the list)</li>
 * </ul>
 */
public interface GnucashWritableAccount extends GnucashAccount, 
                                                GnucashObject,
                                                GnucashWritableObject 
{

    /**
     * @return the file we belong to
     */
    GnucashWritableFile getWritableGnucashFile();

    /**
     * Change the user-definable name. It should contain no newlines but may contain
     * non-ascii and non-western characters.
     *
     * @param name the new name (not null)
     */
    void setName(String name);

    /**
     * Change the user-definable account-number. It should contain no newlines but
     * may contain non-ascii and non-western characters.
     *
     * @param code the new code (not null)
     */
    void setAccountCode(String code);

    /**
     * @param desc the user-defined description (may contain multiple lines and
     *             non-ascii-characters)
     */
    void setDescription(String desc);

    /**
     * Get the sum of all transaction-splits affecting this account in the given
     * time-frame.
     *
     * @param from when to start, inclusive
     * @param to   when to stop, exlusive.
     * @return the sum of all transaction-splits affecting this account in the given
     *         time-frame.
     */
    FixedPointNumber getBalanceChange(LocalDate from, LocalDate to);

    /**
     * Set the type of the account (income, ...).
     *
     * @param type the new type.
     * @see {@link GnucashAccount#getType()}
     */
    void setType(String type);

    /**
     * @param id the new currency
     * @see #setCurrencyNameSpace(String)
     * @see {@link GnucashAccount#getCurrencyID()}
     */
    void setCurrencyID(final String id);

    /**
     * @param id the new namespace
     * @see {@link GnucashAccount#getCurrencyNameSpace()}
     */
    void setCurrencyNameSpace(final String id);

    /**
     * @param newparent the new account or null to make it a top-level-account
     */
    void setParentAccount(GnucashAccount newparent);

    /**
     * If the accountId is invalid, make this a top-level-account.
     *
     * @see {@link #setParentAccount(GnucashAccount)}
     */
    void setParentAccountId(String newparent);

    /**
     * Remove this account from the sytem.<br/>
     * Throws IllegalStateException if this account has splits or childres.
     */
    void remove();

    /**
     * Add a PropertyChangeListener to the listener list. The listener is registered
     * for all properties.
     *
     * @param listener The PropertyChangeListener to be added
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Add a PropertyChangeListener for a specific property. The listener will be
     * invoked only when a call on firePropertyChange names that specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener     The PropertyChangeListener to be removed
     */
    void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener from the listener list. This removes a
     * PropertyChangeListener that was registered for all properties.
     *
     * @param listener The PropertyChangeListener to be removed
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * @param name  the name of the user-defined attribute
     * @param value the value or null if not set
     * @see {@link GnucashObject#getUserDefinedAttribute(String)}
     */
    void setUserDefinedAttribute(final String name, final String value);
}
