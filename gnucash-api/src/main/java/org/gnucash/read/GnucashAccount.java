/**
 * GnucashAccount.java
 * License: GPLv3 or later
 * Created on 05.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 05.05.2005 - initial version
 * ...
 */
package org.gnucash.read;

import org.gnucash.numbers.FixedPointNumber;

import java.time.LocalDate;
import java.util.*;

/**
 * <br>
 * created: 05.05.2005
 * <p>
 * An account is a collection of transactions that start or end there. <br>
 * You can compare it's functionality to an abstracted bank account. <br>
 * It has a balance, may have a parent-account(@see #getParentAccount()) and child-accounts(@see #getSubAccounts()) to form
 * a tree. <br>
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 * @see #getParentAccount()
 */
public interface GnucashAccount extends Comparable {

	/**
	 * @return the unique id for that account (not meaningfull to human users)
	 */
	String getId();

	/**
	 * @return a user-defined description to acompany the name of the account. Can encompass many lines.
	 */
	String getDescription();

	/**
	 * @return the account-number
	 */
	String getAccountCode();

	/**
	 * @return user-readable name of this account. Does not contain the name of parent-accounts
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
	 * @return the parent-account we are a child of or null
	 * if we are a top-level account
	 */
	GnucashAccount getParentAccount();

	/**
	 * The returned collection is never null
	 * and is sorted by Account-Name.
	 *
	 * @return all child-accounts
	 * @see #getChildren()
	 */
	Collection getSubAccounts();

	/**
	 * The returned collection is never null
	 * and is sorted by Account-Name.
	 *
	 * @return all child-accounts
	 */
	Collection<GnucashAccount> getChildren();
	
	// ----------------------------

	/**
	 * e.g. "Umsatzsteuer 19%"
	 */
	String ACCOUNTTYPE_LIABILITY = "LIABILITY";

	/**
	 * e.g. "Umsatzerloese 19% USt"
	 */
	String ACCOUNTTYPE_BANK = "BANK";

	/**
	 * e.g. "Umsatzerloese 16% USt"
	 */
	String ACCOUNTTYPE_INCOME = "INCOME";

    /**
     * e.g. "Verbindlichkeiten ggueber Lieferanten"
     */
    String ACCOUNTTYPE_PAYABLE = "PAYABLE";

	/**
	 * e.g. "Forderungen aus Lieferungen und Leistungen"
	 */
	String ACCOUNTTYPE_RECEIVABLE = "RECEIVABLE";

	/**
	 * e.g. "1. Forderungen aus Lieferungen und Leistungen"
	 */
	String ACCOUNTTYPE_ASSET = "ASSET";
	/**
	 * e.g. "private Ausgaben"
	 */
	String ACCOUNTTYPE_EXPENSE = "EXPENSE";

	/**
	 * e.g. "Visa"
	 */
	String ACCOUNTTYPE_CREDIT = "CREDIT";

	/**
	 * e.g. "Anfangsbestand"
	 */
	String ACCOUNTTYPE_EQUITY = "EQUITY";

	/**
	 * e.g. "stock"
	 */
	String ACCOUNTTYPE_CASH = "CASH";

	/**
	 * e.g. "Cash in Wallet"
	 */
	String ACCOUNTTYPE_STOCK = "STOCK";

	/**
	 * e.g. "Lesezeichen"
	 */
	String ACCOUNTTYPE_MUTUAL = "MUTUAL";

    // ----------------------------

	/**
	 * @return the type-string for this account.
	 * @see #ACCOUNTTYPE_ASSET
	 * @see #ACCOUNTTYPE_INCOME
	 * @see #ACCOUNTTYPE_LIABILITY
     * @see #ACCOUNTTYPE_PAYABLE
     * @see #ACCOUNTTYPE_RECEIVABLE
	 * there are other types too
	 */
	String getType();

	/**
	 * Values for the currency-namspace ISO4217 .
	 *
	 * @see {@link #getCurrencyNameSpace()}
	 */
	String CURRENCYNAMESPACE_CURRENCY = "ISO4217";

	/**
	 * @see {@link #getCurrencyNameSpace()}
	 */
	String CURRENCYNAMESPACE_FUND = "FUND";

	/**
	 * @see {@link #getCurrencyNameSpace()}
	 */
	String CURRENCYNAMESPACE_AMEX = "AMEX";

	/**
	 * @see {@link #getCurrencyNameSpace()}
	 */
	String CURRENCYNAMESPACE_EUREX = "EUREX";

	/**
	 * @see {@link #getCurrencyNameSpace()}
	 */
	String CURRENCYNAMESPACE_NASDAQ = "NASDAQ";

	/**
	 * @see {@link #getCurrencyNameSpace()}
	 */
	String CURRENCYNAMESPACE_NYSE = "NYSE";

	/**
	 * @return "ISO4217" for a currency "FUND" or a fond,...
	 * @see {@link #CURRENCYNAMESPACE_CURRENCY}
	 * @see {@link #CURRENCYNAMESPACE_FUND}
	 */
	String getCurrencyNameSpace();

	/**
	 * The name of the currency in the given namespace
	 * e.g. "EUR" for euro in namespace "ISO4217"= {@link #CURRENCYNAMESPACE_CURRENCY}
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
	 * Be aware that the result is in the currency of this
	 * account!
	 *
	 * @return the balance
	 */
	FixedPointNumber getBalance();

	/**
	 * same as getBalanceRecursive(new Date()).<br/>
	 * ignores transactions after the current date+time<br/>
	 * Be aware that the result is in the currency of this
	 * account!
	 *
	 * @return the balance including sub-accounts
	 */
	FixedPointNumber getBalanceRecursive();

	/**
	 * @return true if ${@link #hasTransactions()} is true for this
	 * or any sub-accounts
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
	 * same as getBalanceRecursive(new Date()).
	 * ignores transactions after the current date+time
	 *
	 * @return the balance including sub-accounts formated using the current locale
	 */
	String getBalanceRecursiveFormated();

	/**
	 * same as getBalance(new Date()).
	 * ignores transactions after the current date+time
	 *
	 * @return the balance formated using the current locale
	 */
	String getBalanceFormated();

	/**
	 * same as getBalance(new Date()).
	 * ignores transactions after the current date+time
	 *
	 * @param locale the locale to use (does not affect the currency)
	 * @return the balance formated using the given locale
	 */
	String getBalanceFormated(Locale locale);

	/**
	 * Be aware that the result is in the currency of this
	 * account!
	 *
	 * @param date if non-null transactions after this date are ignored in the calculation
	 * @return the balance formated using the current locale
	 */
	FixedPointNumber getBalance(LocalDate date);

	/**
	 * Be aware that the result is in the currency of this
	 * account!
	 *
	 * @param date  if non-null transactions after this date are ignored in the calculation
	 * @param after splits that are after date are added here.
	 * @return the balance formated using the current locale
	 */
	FixedPointNumber getBalance(final LocalDate date, final Collection<GnucashTransactionSplit> after);

	/**
	 * Gets the balance including all sub-accounts.
	 *
	 * @param date if non-null transactions after this date are ignored in the calculation
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
	 * @param date if non-null transactions after this date are ignored in the calculation
	 * @return the balance including all sub-accounts
	 */
	String getBalanceRecursiveFormated(LocalDate date);

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
	 * Examples:
	 * The user-defined-attribute "hidden"="true"/"false"
	 * was introduced in gnucash2.0 to hide accounts.
	 *
	 * @param name the name of the user-defined attribute
	 * @return the value or null if not set
	 */
	String getUserDefinedAttribute(final String name);

	/**
	 * @return all keys that can be used with ${@link #getUserDefinedAttribute(String)}}.
	 */
	Collection<String> getUserDefinedAttributeKeys();
}
