/**
 * GnucashFile.java .
 * License: GPLv3 or later
 * Created on 13.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 13.05.2005 - initial version
 * 11.11.2008 - added getDefaultCurrencyID()
 */
package org.gnucash.read;

import java.io.File;
import java.util.Collection;

import org.gnucash.currency.ComplexCurrencyTable;
import org.gnucash.numbers.FixedPointNumber;

/**
 * created: 13.05.2005<br/>
 * <br/>
 * Interface of a top-level class<br/>
 * that gives access to a gnucash-file <br/>
 * with all it's transactions and accounts,... <br/>
 * <br/>
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashFile extends GnucashObject {

	/**
	 *
	 * @return the file on disk we are managing
	 */
	File getFile();

	/**
	 * The Currency-Table gets initialized with the latest prices
	 * found in the gnucash-file.
	 * @return Returns the currencyTable.
	 */
	ComplexCurrencyTable getCurrencyTable();

	/**
	 * Use a heuristic to determine the  defaultcurrency-id.
	 * If we cannot find one, we default to EUR.<br/>
	 * Comodity-stace is fixed as "ISO4217" .
	 * @return the default-currencyID to use.
	 */
	String getDefaultCurrencyID();

	/**
	 * @param id id of a taxtable
	 * @return the identified taxtable or null
	 */
	GnucashTaxTable getTaxTableByID(String id);

	/**
	 * @return all TaxTables defined in the book
	 * @link GnucashTaxTable
	 */
	Collection<GnucashTaxTable> getTaxTables();

	/**
	 * @param pCmdtySpace the namespace for pCmdtyId
	 * @param pCmdtyId the currency-name
	 * @return the latest price-quote in the gnucash-file in EURO
	 */
	FixedPointNumber getLatestPrice(final String pCmdtySpace,
			final String pCmdtyId);

	//public abstract void setFile(File file);

	//public abstract void loadFile(File file) throws Exception;

	/**
	 * @param id the unique id of the account to look for
	 * @return the account or null if it's not found
	 */
	GnucashAccount getAccountByID(String id);

	/**
	 * @return a read-only collection of all accounts that have no parent
	 */
	Collection<? extends GnucashAccount> getRootAccounts();

	/**
	 * @param id the unique id of the job to look for
	 * @return the job or null if it's not found
	 */
	GnucashJob getJobByID(String id);

	/**
	 * @return a (possibly read-only) collection of all jobs
	 * Do not modify the returned collection!
	 */
	Collection<GnucashJob> getJobs();

	/**
	 * @param id the unique id of the transaction to look for
	 * @return the transaction or null if it's not found
	 */
	GnucashTransaction getTransactionByID(String id);

	/**
	 * @return a (possibly read-only) collection of all transactions
	 * Do not modify the returned collection!
	 */
	Collection<? extends GnucashTransaction> getTransactions();

	/**
	 * @return all accounts
	 */
	Collection<GnucashAccount> getAccounts();

	/**
	 *
	 * @param id if null, gives all account that have no parent
	 * @return all accounts with that parent in no particular order
	 */
	Collection<GnucashAccount> getAccountsByParentID(String id);

	/**
	 * @param id the unique id of the invoice to look for
	 * @return the invoice or null if it's not found
	 * @see #getUnpayedInvoices()
	 * @see #getPayedInvoices()
	 * @see #getInvoiceByID(String)
	 * @see #getUnpayedInvoicesForCustomer(GnucashCustomer)
	 */
	GnucashInvoice getInvoiceByID(String id);

	/**
	 * @return a (possibly read-only) collection of all invoices
	 * Do not modify the returned collection!
	 * @see #getUnpayedInvoices()
	 * @see #getPayedInvoices()
	 * @see #getInvoiceByID(String)
	 * @see #getUnpayedInvoicesForCustomer(GnucashCustomer)
	 */
	Collection<GnucashInvoice> getInvoices();

	/**
	 * @return a (possibly read-only) collection of all invoices that are fully payed
	 * Do not modify the returned collection!
	 * @see #getUnpayedInvoices()
	 * @see #getInvoices()
	 * @see #getInvoiceByID(String)
	 * @see #getUnpayedInvoicesForCustomer(GnucashCustomer)
	 */
	Collection<GnucashInvoice> getPayedInvoices();

	/**
	 * @return a (possibly read-only) collection of all invoices that are not fully payed
	 * Do not modify the returned collection!
	 * @see #getPayedInvoices()
	 * @see #getInvoices()
	 * @see #getInvoiceByID(String)
	 * @see #getUnpayedInvoicesForCustomer(GnucashCustomer)
	 */
	Collection<GnucashInvoice> getUnpayedInvoices();

	/**
	 * @param customer the customer to look for (not null)
	 * @return a (possibly read-only) collection of all invoices that are not fully payed and are from the given customer
	 * Do not modify the returned collection!
	 * @see #getPayedInvoices()
	 * @see #getInvoices()
	 * @see #getInvoiceByID(String)
	 * @see #getUnpayedInvoicesForCustomer(GnucashCustomer)
	 */
	Collection<GnucashInvoice> getUnpayedInvoicesForCustomer(GnucashCustomer customer);

	/**
	 * warning: this function has to traverse all
	 * accounts. If it much faster to try
	 * getAccountByID first and only call this method
	 * if the returned account does not have the right name.
	 *
	 * @param name the UNQUaLIFIED name to look for
	 * @return null if not found
	 * @see #getAccountByID(String)
	 */
	GnucashAccount getAccountByName(String name);

	/**
	 * warning: this function has to traverse all
	 * accounts. If it much faster to try
	 * getAccountByID first and only call this method
	 * if the returned account does not have the right name.
	 *
	 * @param name the regular expression of the name to look for
	 * @return null if not found
	 * @see #getAccountByID(String)
	 * @see #getAccountByName(String)
	 */
	GnucashAccount getAccountByNameEx(String name);

	/**
	 * First try to fetch the account by id, then
	 * fall back to traversing all accounts to get
	 * if by it's name.
	 *
	 * @param id the id to look for
	 * @param name the name to look for if nothing is found for the id
	 * @return null if not found
	 * @see #getAccountByID(String)
	 * @see #getAccountByName(String)
	 */
	GnucashAccount getAccountByIDorName(String id, String name);

	/**
	 * First try to fetch the account by id, then
	 * fall back to traversing all accounts to get
	 * if by it's name.
	 *
	 * @param id the id to look for
	 * @param name the regular expression of the name to look for
	 *        if nothing is found for the id
	 * @return null if not found
	 * @see #getAccountByID(String)
	 * @see #getAccountByName(String)
	 */
	GnucashAccount getAccountByIDorNameEx(String id, String name);

	/**
	 * @param id the unique id of the customer to look for
	 * @return the customer or null if it's not found
	 */
	GnucashCustomer getCustomerByID(String id);

	/**
	 * warning: this function has to traverse all
	 * customers. If it much faster to try
	 * getCustomerByID first and only call this method
	 * if the returned account does not have the right name.
	 *
	 * @param name the name to look for
	 * @return null if not found
	 * @see #getCustomerByID(String)
	 */
	GnucashCustomer getCustomerByName(String name);

	/**
	 * @return a (possibly read-only) collection of all customers
	 * Do not modify the returned collection!
	 */
	Collection<GnucashCustomer> getCustomers();

}
