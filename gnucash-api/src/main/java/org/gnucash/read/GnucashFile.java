package org.gnucash.read;

import java.io.File;
import java.util.Collection;

import org.gnucash.basetypes.GCshCmdtyCurrID;
import org.gnucash.basetypes.GCshCmdtyCurrNameSpace;
import org.gnucash.basetypes.InvalidCmdtyCurrIDException;
import org.gnucash.basetypes.InvalidCmdtyCurrTypeException;
import org.gnucash.currency.ComplexCurrencyTable;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshBillTerms;
import org.gnucash.read.aux.GCshPrice;
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
     * Comodity-stace is fixed as "CURRENCY" .
     * 
     * @return the default-currencyID to use.
     */
    String getDefaultCurrencyID();

    // ---------------------------------------------------------------

    /**
     * @param id id of a tax table
     * @return the identified tax table or null
     */
    GCshTaxTable getTaxTableByID(String id);

    /**
     * @param id name of a tax table
     * @return the identified tax table or null
     */
    GCshTaxTable getTaxTableByName(String name);

    /**
     * @return all TaxTables defined in the book
     * @link GnucashTaxTable
     */
    Collection<GCshTaxTable> getTaxTables();

    // ---------------------------------------------------------------

    /**
     * @param id id of a tax table
     * @return the identified tax table or null
     */
    GCshBillTerms getBillTermsByID(String id);

    /**
     * @param id name of a tax table
     * @return the identified tax table or null
     */
    GCshBillTerms getBillTermsByName(String name);

    /**
     * @return all TaxTables defined in the book
     * @link GnucashTaxTable
     */
    Collection<GCshBillTerms> getBillTerms();


    // ---------------------------------------------------------------

    /**
     * @param id id of a price
     * @return the identified price or null
     */
    GCshPrice getPriceByID(String id);

    /**
     * @return all prices defined in the book
     * @link GCshPrice
     */
    Collection<GCshPrice> getPrices();

    /**
     * @param pCmdtySpace the namespace for pCmdtyId
     * @param pCmdtyId    the currency-name
     * @return the latest price-quote in the gnucash-file in EURO
     */
    FixedPointNumber getLatestPrice(final String pCmdtySpace, final String pCmdtyId);

    // ---------------------------------------------------------------

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

    Collection<GnucashGenerJob> getGenerJobsByName(String expr);

    Collection<GnucashGenerJob> getGenerJobsByName(String expr, boolean relaxed);

    GnucashGenerJob getGenerJobByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

    /**
     * @return a (possibly read-only) collection of all jobs Do not modify the
     *         returned collection!
     */
    Collection<GnucashGenerJob> getGenerJobs();

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
    Collection<GnucashCustomerInvoice> getInvoicesForCustomer_direct(GnucashCustomer cust)
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
    Collection<GnucashJobInvoice>      getInvoicesForCustomer_viaAllJobs(GnucashCustomer cust)
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
    Collection<GnucashCustomerInvoice> getPaidInvoicesForCustomer_direct(GnucashCustomer cust) throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getPaidInvoicesForCustomer_viaAllJobs(GnucashCustomer cust) throws WrongInvoiceTypeException;

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
    Collection<GnucashCustomerInvoice> getUnpaidInvoicesForCustomer_direct(GnucashCustomer cust)throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getUnpaidInvoicesForCustomer_viaAllJobs(GnucashCustomer cust)throws WrongInvoiceTypeException;

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
    Collection<GnucashVendorBill>      getBillsForVendor_direct(GnucashVendor vend) throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getBillsForVendor_viaAllJobs(GnucashVendor vend) throws WrongInvoiceTypeException;

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
    Collection<GnucashVendorBill>      getPaidBillsForVendor_direct(GnucashVendor vend) throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getPaidBillsForVendor_viaAllJobs(GnucashVendor vend) throws WrongInvoiceTypeException;

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
    Collection<GnucashVendorBill>      getUnpaidBillsForVendor_direct(GnucashVendor vend) throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getUnpaidBillsForVendor_viaAllJobs(GnucashVendor vend) throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getInvoicesForJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getPaidInvoicesForJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

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
    Collection<GnucashJobInvoice>      getUnpaidInvoicesForJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

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
    Collection<GnucashAccount> getAccountsByName(String expr);

    Collection<GnucashAccount> getAccountsByName(String expr, boolean qualif, boolean relaxed);

    GnucashAccount getAccountByNameUniq(String expr, boolean qualif) throws NoEntryFoundException, TooManyEntriesFoundException;

    /**
     * warning: this function has to traverse all accounts. If it much faster to try
     * getAccountByID first and only call this method if the returned account does
     * not have the right name.
     *
     * @param name the regular expression of the name to look for
     * @return null if not found
     * @throws TooManyEntriesFoundException 
     * @throws NoEntryFoundException 
     * @see #getAccountByID(String)
     * @see #getAccountsByName(String)
     */
    GnucashAccount getAccountByNameEx(String name) throws NoEntryFoundException, TooManyEntriesFoundException;

    /**
     * First try to fetch the account by id, then fall back to traversing all
     * accounts to get if by it's name.
     *
     * @param id   the id to look for
     * @param name the name to look for if nothing is found for the id
     * @return null if not found
     * @throws TooManyEntriesFoundException 
     * @throws NoEntryFoundException 
     * @see #getAccountByID(String)
     * @see #getAccountsByName(String)
     */
    GnucashAccount getAccountByIDorName(String id, String name) throws NoEntryFoundException, TooManyEntriesFoundException;

    /**
     * First try to fetch the account by id, then fall back to traversing all
     * accounts to get if by it's name.
     *
     * @param id   the id to look for
     * @param name the regular expression of the name to look for if nothing is
     *             found for the id
     * @return null if not found
     * @throws TooManyEntriesFoundException 
     * @throws NoEntryFoundException 
     * @see #getAccountByID(String)
     * @see #getAccountsByName(String)
     */
    GnucashAccount getAccountByIDorNameEx(String id, String name) throws NoEntryFoundException, TooManyEntriesFoundException;

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
    Collection<GnucashCustomer> getCustomersByName(String expr);

    Collection<GnucashCustomer> getCustomersByName(String expr, boolean relaxed);

    GnucashCustomer getCustomerByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

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
    Collection<GnucashVendor> getVendorsByName(String expr);

    Collection<GnucashVendor> getVendorsByName(String expr, boolean relaxed);

    GnucashVendor getVendorByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

    /**
     * @return a (possibly read-only) collection of all vendors Do not modify the
     *         returned collection!
     */
    Collection<GnucashVendor> getVendors();

    // ----------------------------

    /**
     * @param id the unique id of the currency/security/commodity to look for
     * @return the currency/security/commodity or null if it's not found
     */
    GnucashCommodity getCommodityByQualifID(GCshCmdtyCurrID cmdtyCurrID);

    GnucashCommodity getCommodityByQualifID(String nameSpace, String id);

    GnucashCommodity getCommodityByQualifID(GCshCmdtyCurrNameSpace.Exchange exchange, String id);

    GnucashCommodity getCommodityByQualifID(GCshCmdtyCurrNameSpace.MIC mic, String id);

    GnucashCommodity getCommodityByQualifID(GCshCmdtyCurrNameSpace.SecIdType secIdType, String id);

    /**
     * @param id the unique id of the currency/security/commodity to look for
     * @return the currency/security/commodity or null if it's not found
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    GnucashCommodity getCommodityByQualifID(String qualifID);

    /**
     * @param id the unique id of the currency/security/commodity to look for
     * @return the currency/security/commodity or null if it's not found
     */
    GnucashCommodity getCommodityByXCode(String xCode);

    /**
     * warning: this function has to traverse all currencies/securities/commodities. If it much faster to try
     * getCommodityByID first and only call this method if the returned account does
     * not have the right name.
     *
     * @param name the name to look for
     * @return null if not found
     * @see #getCommodityByID(String)
     */
    Collection<GnucashCommodity> getCommoditiesByName(String expr);

    Collection<GnucashCommodity> getCommoditiesByName(String expr, boolean relaxed);

    GnucashCommodity getCommodityByNameUniq(String expr) throws NoEntryFoundException, TooManyEntriesFoundException;

    /**
     * @return a (possibly read-only) collection of all currencies/securities/commodities Do not modify the
     *         returned collection!
     */
    Collection<GnucashCommodity> getCommodities();

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

    public int getNofEntriesCommodityMap();

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
