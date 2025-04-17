package org.gnucash.read;

import java.util.Collection;
import java.util.Locale;

import org.gnucash.generated.GncTransaction;
import org.gnucash.numbers.FixedPointNumber;

/**
 * This denotes a single addition or removal of some value from one account in a transaction made up of multiple such
 * splits.
 */
public interface GnucashTransactionSplit extends Comparable<GnucashTransactionSplit> {

	// For the following enumerations cf.:
	// https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/Split.h

	// Note: The following should be chars, but the method where they are
	// used is generated and accepts a String.
	// ::MAGIC
	String CREC = "c"; // cleared
	String YREC = "y"; // reconciled
	String FREC = "f"; // frozen into accounting period
	String NREC = "n"; // not reconciled or cleared
	String VREC = "v"; // void

	// -----------------------------------------------------------------

	@SuppressWarnings("exports")
	GncTransaction.TrnSplits.TrnSplit getJwsdpPeer();

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	GnucashFile getGnucashFile();

	// -----------------------------------------------------------------

	/**
	 * @return the unique-id to identify this object with across name- and hirarchy-changes
	 */
	String getId();

	/**
	 * @return the id of the account we transfer from/to.
	 */
	String getAccountID();

	/**
	 * This may be null if an account-id is specified in the gnucash-file that does not belong to an account.
	 *
	 * @return the account of the account we transfer from/to.
	 */
	GnucashAccount getAccount();

	/**
	 * @return the transaction this is a split of.
	 */
	GnucashTransaction getTransaction();

	/**
	 * The value is in the currency of the transaction!
	 *
	 * @return the value-transfer this represents
	 */
	FixedPointNumber getValue();

	/**
	 * The value is in the currency of the transaction!
	 *
	 * @return the value-transfer this represents
	 */
	String getValueFormatted();

	/**
	 * The value is in the currency of the transaction!
	 *
	 * @param locale the locale to use
	 * @return the value-transfer this represents
	 */
	String getValueFormatted(Locale locale);

	/**
	 * The value is in the currency of the transaction!
	 *
	 * @return the value-transfer this represents
	 */
	String getValueFormattedForHTML();

	/**
	 * The value is in the currency of the transaction!
	 *
	 * @param locale the locale to use
	 * @return the value-transfer this represents
	 */
	String getValueFormattedForHTML(Locale locale);

	/**
	 * @return the balance of the account (in the account's currency) up to this split.
	 */
	FixedPointNumber getAccountBalance();

	/**
	 * @return the balance of the account (in the account's currency) up to this split.
	 */
	String getAccountBalanceFormatted();

	/**
	 * @see GnucashAccount#getBalanceFormatted()
	 */
	String getAccountBalanceFormatted(Locale locale);

	/**
	 * The quantity is in the currency of the account!
	 *
	 * @return the number of items added to the account
	 */
	FixedPointNumber getQuantity();

	/**
	 * The quantity is in the currency of the account!
	 *
	 * @return the number of items added to the account
	 */
	String getQuantityFormatted();

	/**
	 * The quantity is in the currency of the account!
	 *
	 * @param locale the locale to use
	 * @return the number of items added to the account
	 */
	String getQuantityFormatted(Locale locale);

	/**
	 * The quantity is in the currency of the account!
	 *
	 * @return the number of items added to the account
	 */
	String getQuantityFormattedForHTML();

	/**
	 * The quantity is in the currency of the account!
	 *
	 * @param locale the locale to use
	 * @return the number of items added to the account
	 */
	String getQuantityFormattedForHTML(Locale locale);

	/**
	 * @return the user-defined description for this object (may contain multiple lines and non-ascii-characters)
	 */
	String getDescription();

	public String getLotID();

	/**
	 * Get the type of association this split has with an invoice's lot.
	 *
	 * @return null, or one of the ACTION_xyz values defined
	 */
	String getAction();

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
