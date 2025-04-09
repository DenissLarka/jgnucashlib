package org.gnucash.read;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.gnucash.numbers.FixedPointNumber;

/**
 * An account is a collection of transactions that start or end there. <br>
 * You can compare it's functionality to an abstracted bank account. <br>
 * It has a balance, may have a parent-account(@see #getParentAccount()) and child-accounts(@see #getSubAccounts()) to form
 * a tree. <br>
 *
 * @see #getParentAccount()
 */
public interface GnucashAccount extends Comparable<GnucashAccount> {

	// For the following types cf.:
	// https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/Account.h
	//
	// Examples (from German accounting):
	//
	// - TYPE_BANK = "BANK"; Girokonto, Tagesgeldkonto
	// - TYPE_CASH = "CASH"; Kasse
	// - TYPE_CREDIT = "CREDIT"; "Kreditkarte"
	// - TYPE_ASSET = "ASSET"; Vermögensgegenstaende, "1. Forderungen aus
	// Lieferungen und Leistungen"
	// - TYPE_LIABILITY = "LIABILITY"; Verbindlichkeiten ggueber Lieferanten
	// - TYPE_STOCK = "STOCK"; Aktie
	// - TYPE_MUTUAL = "MUTUAL"; Investment-Fonds
	// - TYPE_CURRENCY = "CURRENCY";
	// - TYPE_INCOME = "INCOME"; "Umsatzerloese 16% USt"
	// - TYPE_EXPENSE = "EXPENSE"; "private Ausgaben"
	// - TYPE_EQUITY = "EQUITY"; "Anfangsbestand"
	// - TYPE_RECEIVABLE = "RECEIVABLE"; "Forderungen aus Lieferungen und
	// Leistungen"
	// - TYPE_PAYABLE = "PAYABLE"; "Verbindlichkeiten ggueber Lieferant xyz"
	// - TYPE_ROOT = "ROOT"; guess ;-)
	// - TYPE_TRADING = "TRADING";

	// ::MAGIC
	String TYPE_BANK = "BANK";
	String TYPE_CASH = "CASH";
	String TYPE_CREDIT = "CREDIT";
	String TYPE_ASSET = "ASSET";
	String TYPE_LIABILITY = "LIABILITY";
	String TYPE_STOCK = "STOCK";
	String TYPE_MUTUAL = "MUTUAL";
	String TYPE_CURRENCY = "CURRENCY";
	String TYPE_INCOME = "INCOME";
	String TYPE_EXPENSE = "EXPENSE";
	String TYPE_EQUITY = "EQUITY";
	String TYPE_RECEIVABLE = "RECEIVABLE";
	String TYPE_PAYABLE = "PAYABLE";
	String TYPE_ROOT = "ROOT";
	String TYPE_TRADING = "TRADING";

	// -----------------------------------------------------------------

	/**
	 * @return the unique id for that account (not meaningfull to human users)
	 */
	String getId();

	/**
	 * @return a user-defined description to acompany the name of the account. Can
	 * encompass many lines.
	 */
	String getDescription();

	/**
	 * @return the account-number
	 */
	String getCode();

	/**
	 * @return user-readable name of this account. Does not contain the name of
	 * parent-accounts
	 */
	String getName();

	/**
	 * get name including the name of the parent.accounts.
	 *
	 * @return e.g. "Aktiva::test::test2"
	 */
	String getQualifiedName();

	/**
	 * @return null if the account is below the root
	 */
	String getParentAccountId();

	/**
	 * @return the parent-account we are a child of or null if we are a top-level
	 * account
	 */
	GnucashAccount getParentAccount();

	/**
	 * The returned collection is never null and is sorted by Account-Name.
	 *
	 * @return all child-accounts
	 * @see #getChildren()
	 */
	Collection<GnucashAccount> getSubAccounts();

	/**
	 * The returned collection is never null and is sorted by Account-Name.
	 *
	 * @return all child-accounts
	 */
	Collection<GnucashAccount> getChildren();

	// ----------------------------

	/**
	 * @return the type-string for this account.
	 * @see #TYPE_ASSET
	 * @see #TYPE_INCOME
	 * @see #TYPE_LIABILITY
	 * @see #TYPE_PAYABLE
	 * @see #TYPE_RECEIVABLE there are other types too
	 */
	String getType();

	/**
	 */
	String getCurrencyNameSpace();

	/**
	 *
	 * @see {@link #getCurrencyNameSpace()}
	 */
	String getCurrencyID();

	/**
	 * The returned list ist sorted by the natural order of the Transaction-Splits.
	 *
	 * @return all splits
	 * @link GnucashTransactionSplit
	 */
	List<? extends GnucashTransactionSplit> getTransactionSplits();

	/**
	 * The returned list ist sorted by the natural order of the Transaction-Splits.
	 *
	 * @return all splits
	 * @link GnucashTransaction
	 */
	List<GnucashTransaction> getTransactions();

	/**
	 * @param split split to add to this transaction
	 */
	void addTransactionSplit(GnucashTransactionSplit split);

	/**
	 * same as getBalance(new Date()).<br/>
	 * ignores transactions after the current date+time<br/>
	 * Be aware that the result is in the currency of this account!
	 *
	 * @return the balance
	 */
	FixedPointNumber getBalance();

	/**
	 * same as getBalanceRecursive(new Date()).<br/>
	 * ignores transactions after the current date+time<br/>
	 * Be aware that the result is in the currency of this account!
	 *
	 * @return the balance including sub-accounts
	 */
	FixedPointNumber getBalanceRecursive();

	/**
	 * @return true if ${@link #hasTransactions()} is true for this or any
	 * sub-accounts
	 */
	boolean hasTransactionsRecursive();

	/**
	 * @return true if ${@link #getTransactionSplits()}.size()>0
	 */
	boolean hasTransactions();

	/**
	 * Ignores accounts for wich this conversion is not possible.
	 *
	 * @param date     ignores transactions after the given date
	 * @param currency the currency the result shall be in
	 * @return Gets the balance including all sub-accounts.
	 * @see GnucashAccount#getBalanceRecursive(LocalDate)
	 */
	FixedPointNumber getBalanceRecursive(final LocalDate date, final Currency currency);

	/**
	 * same as getBalanceRecursive(new Date()). ignores transactions after the
	 * current date+time
	 *
	 * @return the balance including sub-accounts formatted using the current locale
	 */
	String getBalanceRecursiveFormatted();

	/**
	 * same as getBalance(new Date()). ignores transactions after the current
	 * date+time
	 *
	 * @return the balance formatted using the current locale
	 */
	String getBalanceFormatted();

	/**
	 * same as getBalance(new Date()). ignores transactions after the current
	 * date+time
	 *
	 * @param locale the locale to use (does not affect the currency)
	 * @return the balance formatted using the given locale
	 */
	String getBalanceFormatted(Locale locale);

	/**
	 * Be aware that the result is in the currency of this account!
	 *
	 * @param date if non-null transactions after this date are ignored in the
	 *             calculation
	 * @return the balance formatted using the current locale
	 */
	FixedPointNumber getBalance(LocalDate date);

	/**
	 * Be aware that the result is in the currency of this account!
	 *
	 * @param date  if non-null transactions after this date are ignored in the
	 *              calculation
	 * @param after splits that are after date are added here.
	 * @return the balance formatted using the current locale
	 */
	FixedPointNumber getBalance(final LocalDate date, final Collection<GnucashTransactionSplit> after);

	/**
	 * Gets the balance including all sub-accounts.
	 *
	 * @param date if non-null transactions after this date are ignored in the
	 *             calculation
	 * @return the balance including all sub-accounts
	 */
	FixedPointNumber getBalanceRecursive(LocalDate date);

	/**
	 * Gets the last transaction-split before the given date.
	 *
	 * @param date if null, the last split of all time is returned
	 * @return the last transaction-split before the given date
	 */
	GnucashTransactionSplit getLastSplitBeforeRecursive(LocalDate date);

	/**
	 * Gets the balance including all sub-accounts.
	 *
	 * @param date if non-null transactions after this date are ignored in the
	 *             calculation
	 * @return the balance including all sub-accounts
	 */
	String getBalanceRecursiveFormatted(LocalDate date);

	/**
	 * @param lastIncludesSplit last split to be included
	 * @return the balance up to and including the given split
	 */
	FixedPointNumber getBalance(GnucashTransactionSplit lastIncludesSplit);

	/**
	 * @param id the split-id to look for
	 * @return the identified split or null
	 */
	GnucashTransactionSplit getTransactionSplitByID(String id);

	/**
	 * @param account the account to test
	 * @return true if this is a child of us or any child's or us.
	 */
	boolean isChildAccountRecursive(GnucashAccount account);

	/**
	 * Ignores accounts for wich this conversion is not possible.
	 *
	 * @param date              ignores transactions after the given date
	 * @param currencyNameSpace the currency the result shall be in
	 * @param currencyName      the currency the result shall be in
	 * @return Gets the balance including all sub-accounts.
	 * @see GnucashAccount#getBalanceRecursive(Date, Currency)
	 */
	FixedPointNumber getBalanceRecursive(LocalDate date, String currencyNameSpace, String currencyName);

	/**
	 * Examples: The user-defined-attribute "hidden"="true"/"false" was introduced
	 * in gnucash2.0 to hide accounts.
	 *
	 * @param name the name of the user-defined attribute
	 * @return the value or null if not set
	 */
	String getUserDefinedAttribute(final String name);

	/**
	 * @return all keys that can be used with
	 * ${@link #getUserDefinedAttribute(String)}}.
	 */
	Collection<String> getUserDefinedAttributeKeys();
}
