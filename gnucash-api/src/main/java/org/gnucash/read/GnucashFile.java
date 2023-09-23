package org.gnucash.read;

import java.io.File;
import java.util.Collection;

import org.gnucash.currency.ComplexCurrencyTable;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Interface of a top-level class<br/>
 * that gives access to a gnucash-file <br/>
 * with all it's transactions and accounts,... <br/>
 * <br/>
 */
public interface GnucashFile extends GnucashObject {

    /**
     *
     * @return the file on disk we are managing
     */
    File getFile();

    /**
     * The Currency-Table gets initialized with the latest prices found in the
     * gnucash-file.
     * 
     * @return Returns the currencyTable.
     */
    ComplexCurrencyTable getCurrencyTable();

    /**
     * Use a heuristic to determine the defaultcurrency-id. If we cannot find one,
     * we default to EUR.<br/>
     * Comodity-stace is fixed as "ISO4217" .
     * 
     * @return the default-currencyID to use.
     */
    String getDefaultCurrencyID();

    // ---------------------------------------------------------------

    /**
     * @param id id of a taxtable
     * @return the identified taxtable or null
     */
    GCshTaxTable getTaxTableByID(String id);

    /**
     * @return all TaxTables defined in the book
     * @link GnucashTaxTable
     */
    Collection<GCshTaxTable> getTaxTables();

    // ----------------------------

//    GnucashVendorTerms getVendorTermsByID(String id);
//
//    Collection<GnucashVendorTerms> getVendorTerms();

    // ---------------------------------------------------------------

    /**
     * @param pCmdtySpace the namespace for pCmdtyId
     * @param pCmdtyId    the currency-name
     * @return the latest price-quote in the gnucash-file in EURO
     */
    FixedPointNumber getLatestPrice(final String pCmdtySpace, final String pCmdtyId);

    // public abstract void setFile(File file);

    // public abstract void loadFile(File file) throws Exception;

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
    GnucashGenerJob getGenerJobByID(String id);

    /**
     * @return a (possibly read-only) collection of all jobs Do not modify the
     *         returned collection!
     */
    Collection<GnucashGenerJob> getJobs();

    /**
     * @param id the unique id of the transaction to look for
     * @return the transaction or null if it's not found
     */
    GnucashTransaction getTransactionByID(String id);

    /**
     * @return a (possibly read-only) collection of all transactions Do not modify
     *         the returned collection!
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
     * @param id the unique id of the (generic) invoice to look for
     * @return the invoice or null if it's not found
     * @see #getUnpaidGenerInvoices()
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    GnucashGenerInvoice getGenerInvoiceByID(String id);

    /**
     * @param id the unique id of the (generic) invoice entry to look for
     * @return the invoice entry or null if it's not found
     * @see #getUnpaidGenerInvoices()
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    GnucashGenerInvoiceEntry getGenerInvoiceEntryByID(String id);

    /**
     * @return a (possibly read-only) collection of all invoices Do not modify the
     *         returned collection!
     * @see #getUnpaidGenerInvoices()
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    Collection<GnucashGenerInvoice> getGenerInvoices();

    /**
     * @return a (possibly read-only) collection of all invoices that are fully Paid
     *         Do not modify the returned collection!
     * @throws WrongInvoiceTypeException
     * @see #getUnpaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    Collection<GnucashGenerInvoice> getPaidGenerInvoices();

    /**
     * @return a (possibly read-only) collection of all invoices that are not fully
     *         Paid Do not modify the returned collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    Collection<GnucashGenerInvoice> getUnpaidGenerInvoices();

    // ----------------------------

    /**
     * @param customer the customer to look for (not null)
     * @return a (possibly read-only) collection of all invoices that have fully
     *         been paid and are from the given customer. Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    Collection<GnucashCustomerInvoice> getInvoicesForCustomer(GnucashCustomer cust)
	    throws WrongInvoiceTypeException;

    /**
     * @param customer the customer to look for (not null)
     * @return a (possibly read-only) collection of all invoices that have fully
     *         been paid and are from the given customer. Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    Collection<GnucashCustomerInvoice> getPaidInvoicesForCustomer(GnucashCustomer cust) throws WrongInvoiceTypeException;

    /**
     * @param customer the customer to look for (not null)
     * @return a (possibly read-only) collection of all invoices that have not fully
     *         been paid and are from the given customer Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidInvoicesForCustomer_viaJob(GnucashCustomer)
     */
    Collection<GnucashCustomerInvoice> getUnpaidInvoicesForCustomer(GnucashCustomer cust)throws WrongInvoiceTypeException;

    // ----------------------------

    /**
     * @param vendor the vendor to look for (not null)
     * @return a (possibly read-only) collection of all bills that have fully been
     *         paid and are from the given vendor Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    Collection<GnucashVendorBill> getBillsForVendor(GnucashVendor vend) throws WrongInvoiceTypeException;

    /**
     * @param vendor the vendor to look for (not null)
     * @return a (possibly read-only) collection of all bills that have fully been
     *         paid and are from the given vendor Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    Collection<GnucashVendorBill> getPaidBillsForVendor(GnucashVendor vend) throws WrongInvoiceTypeException;

    /**
     * @param vendor the vendor to look for (not null)
     * @return a (possibly read-only) collection of all bills that have not fully
     *         been paid and are from the given vendor Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    Collection<GnucashVendorBill> getUnpaidBillsForVendor(GnucashVendor vend) throws WrongInvoiceTypeException;

    // ----------------------------

    /**
     * @param vendor the job to look for (not null)
     * @return a (possibly read-only) collection of all invoices that have fully
     *         been paid and are from the given job Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    Collection<GnucashJobInvoice> getInvoicesForJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

    /**
     * @param vendor the job to look for (not null)
     * @return a (possibly read-only) collection of all invoices that have fully
     *         been paid and are from the given job Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    Collection<GnucashJobInvoice> getPaidInvoicesForJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

    /**
     * @param vendor the job to look for (not null)
     * @return a (possibly read-only) collection of all invoices that have not fully
     *         been paid and are from the given job Do not modify the returned
     *         collection!
     * @throws WrongInvoiceTypeException
     * @see #getPaidGenerInvoices()
     * @see #getGenerInvoices()
     * @see #getGenerInvoiceByID(String)
     * @see #getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    Collection<GnucashJobInvoice> getUnpaidInvoicesForJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

    /**
     * warning: this function has to traverse all accounts. If it much faster to try
     * getAccountByID first and only call this method if the returned account does
     * not have the right name.
     *
     * @param name the UNQUaLIFIED name to look for
     * @return null if not found
     * @see #getAccountByID(String)
     */
    GnucashAccount getAccountByName(String name);

    /**
     * warning: this function has to traverse all accounts. If it much faster to try
     * getAccountByID first and only call this method if the returned account does
     * not have the right name.
     *
     * @param name the regular expression of the name to look for
     * @return null if not found
     * @see #getAccountByID(String)
     * @see #getAccountByName(String)
     */
    GnucashAccount getAccountByNameEx(String name);

    /**
     * First try to fetch the account by id, then fall back to traversing all
     * accounts to get if by it's name.
     *
     * @param id   the id to look for
     * @param name the name to look for if nothing is found for the id
     * @return null if not found
     * @see #getAccountByID(String)
     * @see #getAccountByName(String)
     */
    GnucashAccount getAccountByIDorName(String id, String name);

    /**
     * First try to fetch the account by id, then fall back to traversing all
     * accounts to get if by it's name.
     *
     * @param id   the id to look for
     * @param name the regular expression of the name to look for if nothing is
     *             found for the id
     * @return null if not found
     * @see #getAccountByID(String)
     * @see #getAccountByName(String)
     */
    GnucashAccount getAccountByIDorNameEx(String id, String name);

    // ----------------------------

    /**
     * @param id the unique id of the customer to look for
     * @return the customer or null if it's not found
     */
    GnucashCustomer getCustomerByID(String id);

    /**
     * warning: this function has to traverse all customers. If it much faster to
     * try getCustomerByID first and only call this method if the returned account
     * does not have the right name.
     *
     * @param name the name to look for
     * @return null if not found
     * @see #getCustomerByID(String)
     */
    GnucashCustomer getCustomerByName(String name);

    /**
     * @return a (possibly read-only) collection of all customers Do not modify the
     *         returned collection!
     */
    Collection<GnucashCustomer> getCustomers();

    // ----------------------------

    /**
     * @param id the unique id of the vendor to look for
     * @return the vendor or null if it's not found
     */
    GnucashVendor getVendorByID(String id);

    /**
     * warning: this function has to traverse all vendors. If it much faster to try
     * getVendorByID first and only call this method if the returned account does
     * not have the right name.
     *
     * @param name the name to look for
     * @return null if not found
     * @see #getVendorByID(String)
     */
    GnucashVendor getVendorByName(String name);

    /**
     * @return a (possibly read-only) collection of all vendors Do not modify the
     *         returned collection!
     */
    Collection<GnucashVendor> getVendors();

    // ---------------------------------------------------------------
    // Statistics (for test purposes)

    public int getNofEntriesAccountMap();

    public int getNofEntriesTransactionMap();

    public int getNofEntriesTransactionSplitsMap();

    public int getNofEntriesGenerInvoiceMap();

    public int getNofEntriesGenerInvoiceEntriesMap();

    public int getNofEntriesGenerJobMap();

    public int getNofEntriesCustomerMap();

    public int getNofEntriesVendorMap();

    // ---------------------------------------------------------------
    // In this section, we assume that customer, vendor and job numbers
    // (internally, the IDs, not the GUIDs) are purely numeric, resp. (as
    // automatically generated by default).
    // CAUTION:
    // For customers and vendors, this may typically be usual and effective.
    // For jobs, however, things are typically different, so think twice
    // before using the job-methods!

    public int getHighestCustomerNumber();

    public int getHighestVendorNumber();

    public int getHighestJobNumber();

    public String getNewCustomerNumber();

    public String getNewVendorNumber();

    public String getNewJobNumber();

}
