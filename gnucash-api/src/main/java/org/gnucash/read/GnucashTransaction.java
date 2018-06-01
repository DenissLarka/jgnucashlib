/**
 * GnucashTransaction.java
 * License: GPLv3 or later
 * Created on 05.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 *
 *
 * -----------------------------------------------------------
 * major Changes:
 *  05.05.2005 - initial version
 * ...
 *
 */
package org.gnucash.read;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.gnucash.numbers.FixedPointNumber;

/**
 * created: 05.05.2005
 * TODO write a comment what this type does here
 *
 *
 * It is comparable and sorts primarily on the date the transaction happened
 * and secondarily on the date it was entered.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashTransaction extends Comparable<GnucashTransaction> {

    /**
     *
     * @return the unique-id to identify this object with across name- and hirarchy-changes
     */
    String getId();

    /**
     * @return the user-defined description for this object (may contain multiple lines and non-ascii-characters)
     */
    String getDescription();
    
    /**
     * 
     * @return the transaction-number.
     */
    String getTransactionNumber();

    /**
     * The gnucash-file is the top-level class to contain everything.
     * @return the file we are associated with
     */
    GnucashFile getGnucashFile();

    /**
     * Do not modify the returned collection!
     * @return all splits of this transaction.
     */
    List<GnucashTransactionSplit> getSplits();

    /**
     * Get a split of this transaction it's id.
     * @param id the id to look for
     * @return null if not found
     */
    GnucashTransactionSplit getSplitByID(String id);

    /**
     *
     * @return the first split of this transaction or null.
     */
    GnucashTransactionSplit getFirstSplit();

    /**
     * @return the second split of this transaction or null.
     */
    GnucashTransactionSplit getSecondSplit();

    /**
     *
     * @return the number of splits in this transaction.
     */
    int getSplitsCount();

    /**
     *
     * @return the date the transaction was entered into the system
     */
    LocalDateTime getDateEntered();

    /**
     *
     * @return the date the transaction happened
     */
    LocalDateTime getDatePosted();

    /**
     *
     * @return date the transaction happened
     */
    String getDatePostedFormatted();

    /**
     *
     * @return true if the sum of all splits adds up to zero.
     */
    boolean isBalanced();

    /**
     * @return "ISO4217" for a currency "FUND" or a fond,...
     * @see {@link GnucashAccount#CURRENCYNAMESPACE_CURRENCY}
     * @see {@link GnucashAccount#CURRENCYNAMESPACE_FUND}
     */
    String getCurrencyNameSpace();

    /**
     * The name of the currency in the given namespace
     * e.g. "EUR" for euro in namespace "ISO4217"= {@link GnucashAccount#CURRENCYNAMESPACE_CURRENCY}
     * @see {@link #getCurrencyNameSpace()}
     */
    String getCurrencyID();


    /**
     * The result is in the currency of the transaction.<br/>
     * if the transaction is unbalanced, get sum of all split-values.
     * @return the sum of all splits
     * @see #isBalanced()
     */
    FixedPointNumber getBalance();
    /**
     * The result is in the currency of the transaction.
     * @see GnucashTransaction#getBalance()
     */
    String getBalanceFormatet();
    /**
     * The result is in the currency of the transaction.
     * @see GnucashTransaction#getBalance()
     */
    String getBalanceFormatet(Locale loc);

    /**
     * The result is in the currency of the transaction.<br/>
     * if the transaction is unbalanced, get the missing split-value to balance it.
     * @return the sum of all splits
     * @see #isBalanced()
     */
    FixedPointNumber getNegatedBalance();
    /**
     * The result is in the currency of the transaction.
     * @see GnucashTransaction#getNegatedBalance()
     */
    String getNegatedBalanceFormatet();
    /**
     * The result is in the currency of the transaction.
     * @see GnucashTransaction#getNegatedBalance()
     */
    String getNegatedBalanceFormatet(Locale loc);

    /**
     * @return all keys that can be used with ${@link #getUserDefinedAttribute(String)}}.
     */
    Collection<String> getUserDefinedAttributeKeys();

    /**
     * @param name the name of the user-defined attribute
     * @return the value or null if not set
     */
    String getUserDefinedAttribute(final String name);
}
