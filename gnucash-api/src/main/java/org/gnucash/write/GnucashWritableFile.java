package org.gnucash.write;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.aux.WrongOwnerTypeException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.spec.GnucashWritableCustomerInvoice;
import org.gnucash.write.spec.GnucashWritableCustomerJob;
import org.gnucash.write.spec.GnucashWritableJobInvoice;
import org.gnucash.write.spec.GnucashWritableVendorBill;
import org.gnucash.write.spec.GnucashWritableVendorJob;

/**
 * Extension of GnucashFile that allows writing. <br/>
 * All the instances for accounts,... it returns can be assumed
 * to implement the respetive *Writable-interfaces.
 *
 * @see GnucashFile
 * @see org.gnucash.write.impl.GnucashWritableFileImpl
 */
public interface GnucashWritableFile extends GnucashFile, 
                                             GnucashWritableObject 
{

    /**
     * @return true if this file has been modified.
     */
    boolean isModified();

    /**
     * The value is guaranteed not to be bigger then the maximum of the current
     * system-time and the modification-time in the file at the time of the last
     * (full) read or sucessfull write.<br/ It is thus suitable to detect if the
     * file has been modified outside of this library
     * 
     * @return the time in ms (compatible with File.lastModified) of the last
     *         write-operation
     */
    long getLastWriteTime();

    /**
     * @param pB true if this file has been modified.
     * @see {@link #isModified()}
     */
    void setModified(boolean pB);

    /**
     * Write the data to the given file. That file becomes the new file returned by
     * {@link GnucashFile#getGnucashFile()}
     * 
     * @param file the file to write to
     * @throws IOException kn io-poblems
     */
    void writeFile(File file) throws IOException;

    /**
     * @return the underlying JAXB-element
     */
    @SuppressWarnings("exports")
    GncV2 getRootElement();

    /**
     * @param id the unique id of the customer to look for
     * @return the customer or null if it's not found
     */
    GnucashWritableCustomer getCustomerByID(String id);

    /**
     *
     * @return a read-only collection of all accounts that have no parent
     */
    Collection<? extends GnucashWritableAccount> getWritableRootAccounts();

    /**
     *
     * @return a read-only collection of all accounts
     */
    Collection<? extends GnucashWritableAccount> getWritableAccounts();

    /**
     * @see GnucashFile#getTransactionByID(String)
     * @return A changable version of the transaction.
     */
    GnucashWritableTransaction getTransactionByID(String id);

    public Collection<GnucashWritableGenerInvoice> getWritableGenerInvoices();
    
    /**
     * @see GnucashFile#getGenerInvoiceByID(String)
     * @param id the id to look for
     * @return A changable version of the invoice.
     */
    GnucashWritableGenerInvoice getGenerInvoiceByID(String id);

    /**
     * @see GnucashFile#getAccountByName(String)
     * @param name the name to look for
     * @return A changable version of the account.
     */
    GnucashWritableAccount getAccountByName(String name);

    /**
     * @param type the type to look for
     * @return A changable version of all accounts of that type.
     */
    Collection<GnucashWritableAccount> getAccountsByType(String type);

    /**
     * @see GnucashFile#getAccountByID(String)
     * @param id the id of the account to fetch
     * @return A changable version of the account or null of not found.
     */
    GnucashWritableAccount getAccountByID(String id);

    /**
     * @see GnucashFile#getGenerJobByID(String)
     * @param jobID the id of the job to fetch
     * @return A changable version of the job or null of not found.
     */
    GnucashWritableGenerJob getGenerJobByID(String jobID);

    /**
     * @param jnr the job-number to look for.
     * @return the (first) jobs that have this number or null if not found
     */
    GnucashWritableGenerJob getGenerJobByNumber(final String jnr);

    /**
     * @return all jobs as writable versions.
     */
    Collection<GnucashWritableGenerJob> getWritableGenerJobs();

    /**
     * Add a new currency.<br/>
     * If the currency already exists, add a new price-quote for it.
     * 
     * @param pCmdtySpace        the namespace (e.g. "GOODS" or "CURRENCY")
     * @param pCmdtyId           the currency-name
     * @param conversionFactor   the conversion-factor from the base-currency (EUR).
     * @param pCmdtyNameFraction number of decimal-places after the comma
     * @param pCmdtyName         common name of the new currency
     */
    public void addCurrency(final String pCmdtySpace, final String pCmdtyId, final FixedPointNumber conversionFactor,
	    final int pCmdtyNameFraction, final String pCmdtyName);

    /**
     * @see GnucashFile#getTransactions()
     * @return writable versions of all transactions in the book.
     */
    public Collection<? extends GnucashWritableTransaction> getWritableTransactions();

    // public GnucashWritableTransaction getWritableTransactionByID(final String trxId) throws TransactionNotFoundException;
    
    /**
     * @return a new transaction with no splits that is already added to this file
     */
    GnucashWritableTransaction createWritableTransaction();

    /**
     *
     * @param impl the transaction to remove.
     */
    void removeTransaction(GnucashWritableTransaction impl);

    // ---------------------------------------------------------------

    /**
     * @return a new customer with no values that is already added to this file
     */
    GnucashWritableCustomer createWritableCustomer();

    /**
     * @return a new customer with no values that is already added to this file
     */
    GnucashWritableVendor createWritableVendor();
    
    // ---------------------------------------------------------------

    /**
     * @return a new customer job with no values that is already added to this file
     */
    GnucashWritableCustomerJob createWritableCustomerJob(
	    final GnucashCustomer cust, 
	    final String number, 
	    final String name);

    /**
     * @return a new vendor job with no values that is already added to this file
     */
    GnucashWritableVendorJob createWritableVendorJob(
	    final GnucashVendor vend, 
	    final String number, 
	    final String name);

    // ---------------------------------------------------------------

    /**
     * @return a new account that is already added to this file as a top-level
     *         account
     */
    GnucashWritableAccount createWritableAccount();

    // -----------------------------------------------------------

    /**
     * FOR USE BY EXTENSIONS ONLY
     * 
     * @return a new invoice with no entries that is already added to this file
     * @throws WrongOwnerTypeException 
     */
    GnucashWritableCustomerInvoice createWritableCustomerInvoice(
	    final String invoiceNumber, 
	    final GnucashCustomer cust,
	    final GnucashAccount incomeAcct,
	    final GnucashAccount receivableAcct,
	    final LocalDate openedDate,
	    final LocalDate postDate,
	    final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException;

    /**
     * FOR USE BY EXTENSIONS ONLY
     * 
     * @return a new invoice with no entries that is already added to this file
     */
    GnucashWritableVendorBill createWritableVendorBill(
	    final String invoiceNumber, 
	    final GnucashVendor vend,
	    final GnucashAccount expensesAcct,
	    final GnucashAccount payableAcct,
	    final LocalDate openedDate,
	    final LocalDate postDate,
	    final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException;

    /**
     * FOR USE BY EXTENSIONS ONLY
     * 
     * @return a new invoice with no entries that is already added to this file
     */
    GnucashWritableJobInvoice createWritableJobInvoice(
	    final String invoiceNumber, 
	    final GnucashGenerJob job,
	    final GnucashAccount incExpAcct,
	    final GnucashAccount recvblPayblAcct,
	    final LocalDate openedDate,
	    final LocalDate postDate,
	    final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException;

    // -----------------------------------------------------------

    /**
     * @param impl the account to remove
     */
    void removeAccount(GnucashWritableAccount impl);

}
