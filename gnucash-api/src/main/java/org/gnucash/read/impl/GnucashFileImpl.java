package org.gnucash.read.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.gnucash.Const;
import org.gnucash.basetypes.GCshCmdtyCurrID;
import org.gnucash.basetypes.GCshCmdtyCurrNameSpace;
import org.gnucash.basetypes.InvalidCmdtyCurrTypeException;
import org.gnucash.currency.ComplexCurrencyTable;
import org.gnucash.generated.GncAccount;
import org.gnucash.generated.GncBudget;
import org.gnucash.generated.GncCountData;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.NoEntryFoundException;
import org.gnucash.read.TooManyEntriesFoundException;
import org.gnucash.read.aux.GCshBillTerms;
import org.gnucash.read.aux.GCshPrice;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.aux.GCshBillTermsImpl;
import org.gnucash.read.impl.aux.GCshPriceImpl;
import org.gnucash.read.impl.aux.GCshTaxTableImpl;
import org.gnucash.read.impl.spec.GnucashCustomerInvoiceImpl;
import org.gnucash.read.impl.spec.GnucashCustomerJobImpl;
import org.gnucash.read.impl.spec.GnucashJobInvoiceImpl;
import org.gnucash.read.impl.spec.GnucashVendorBillImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Implementation of GnucashFile that can only
 * read but not modify Gnucash-Files. <br/>
 * @see GnucashFile
 */
public class GnucashFileImpl implements GnucashFile {

    protected static final Logger LOGGER = LoggerFactory.getLogger(GnucashFileImpl.class);

    /**
     * my CurrencyTable.
     */
    private final ComplexCurrencyTable currencyTable = new ComplexCurrencyTable();

    private static final String PADDING_TEMPLATE = "000000";

    // ---------------------------------------------------------------

    /**
     * @param pFile the file to load and initialize from
     * @throws IOException on low level reading-errors (FileNotFoundException if not
     *                     found)
     * @see #loadFile(File)
     */
    public GnucashFileImpl(final File pFile) throws IOException {
	super();
	loadFile(pFile);
    }

    /**
     * @param pFile the file to load and initialize from
     * @throws IOException on low level reading-errors (FileNotFoundException if not
     *                     found)
     * @see #loadFile(File)
     */
    public GnucashFileImpl(final InputStream is) throws IOException {
	super();
	loadInputStream(is);
    }

    // ---------------------------------------------------------------

    /**
     * @return Returns the currencyTable.
     * @link #currencyTable
     */
    public ComplexCurrencyTable getCurrencyTable() {
	return currencyTable;
    }

    /**
     * @return a read-only collection of all accounts
     */
    public Collection<GnucashAccount> getAccounts() {
	if (accountID2account == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	return Collections.unmodifiableCollection(new TreeSet<>(accountID2account.values()));
    }

    // ---------------------------------------------------------------

    /**
     * Filles lazy in getTaxTables() .
     *
     * @see #getTaxTables()
     */
    protected Map<String, GCshTaxTable> taxTablesById = null;

    /**
     * @param id ID of a tax table
     * @return the identified tax table or null
     */
    @Override
    public GCshTaxTable getTaxTableByID(final String id) {
	if (taxTablesById == null) {
	    getTaxTables();
	}
	
	return taxTablesById.get(id);
    }

    /**
     * @param name Name of a tax table
     * @return the identified tax table or null
     */
    @Override
    public GCshTaxTable getTaxTableByName(final String name) {
	if (taxTablesById == null) {
	    getTaxTables();
	}
	
	for (GCshTaxTable taxTab : taxTablesById.values()) {
	    if (taxTab.getName().equals(name)) {
		return taxTab;
	    }
	}

	return null;
    }

    /**
     * @return all TaxTables defined in the book
     * @link GnucashTaxTable
     */
    @Override
    public Collection<GCshTaxTable> getTaxTables() {
	if (taxTablesById == null) {
	    taxTablesById = new HashMap<String, GCshTaxTable>();

	    List<Object> bookElements = this.getRootElement().getGncBook().getBookElements();
	    for (Object bookElement : bookElements) {
		if (!(bookElement instanceof GncV2.GncBook.GncGncTaxTable)) {
		    continue;
		}
		GncV2.GncBook.GncGncTaxTable jwsdpPeer = (GncV2.GncBook.GncGncTaxTable) bookElement;
		GCshTaxTableImpl taxTab = new GCshTaxTableImpl(jwsdpPeer, this);
		taxTablesById.put(taxTab.getId(), taxTab);
	    }
	}

	return taxTablesById.values();
    }

    // ----------------------------

    /**
     * Filles lazy in getBillTerms() .
     *
     * @see #getVendorTerms()
     */
    protected Map<String, GCshBillTerms> billTermsByID = null;

    /**
     * @param id ID of a bill terms item
     * @return the identified bill terms item or null
     */
    @Override
    public GCshBillTerms getBillTermsByID(final String id) {
        if (billTermsByID == null) {
            getBillTerms();
        }
        
        return billTermsByID.get(id);
    }

    /**
     * @param name Name of a bill terms item
     * @return the identified bill-terms item or null
     */
    @Override
    public GCshBillTerms getBillTermsByName(final String name) {
	if (billTermsByID == null) {
	    getBillTerms();
	}
	
	for (GCshBillTerms billTerms : billTermsByID.values()) {
	    if (billTerms.getName().equals(name)) {
		return billTerms;
	    }
	}

	return null;
    }

    /**
     * @return all TaxTables defined in the book
     * @link GnucashTaxTable
     */
    public Collection<GCshBillTerms> getBillTerms() {
        if (billTermsByID == null) {
            billTermsByID = new HashMap<String, GCshBillTerms>();

            List<Object> bookElements = this.getRootElement().getGncBook().getBookElements();
            for (Object bookElement : bookElements) {
                if (!(bookElement instanceof GncV2.GncBook.GncGncBillTerm)) {
                    continue;
                }
                GncV2.GncBook.GncGncBillTerm jwsdpPeer = (GncV2.GncBook.GncGncBillTerm) bookElement;
                GCshBillTermsImpl billTerms = new GCshBillTermsImpl(jwsdpPeer);
                billTermsByID.put(billTerms.getId(), billTerms);
            }
        }

        return billTermsByID.values();
    }

    // ---------------------------------------------------------------

    /**
     * @return a read-only collection of all accounts that have no parent (the
     *         result is sorted)
     */
    public Collection<? extends GnucashAccount> getRootAccounts() {
	try {
	    Collection<GnucashAccount> retval = new TreeSet<GnucashAccount>();

	    for (GnucashAccount account : getAccounts()) {
		if (account.getParentAccountId() == null) {
		    retval.add(account);
		}

	    }

	    return retval;
	} catch (RuntimeException e) {
	    LOGGER.error("Problem getting all root-account", e);
	    throw e;
	} catch (Throwable e) {
	    LOGGER.error("SERIOUS Problem getting all root-account", e);
	    return new LinkedList<GnucashAccount>();
	}
    }

    /**
     * @param id if null, gives all account that have no parent
     * @return the sorted collection of children of that account
     */
    @Override
    public Collection<GnucashAccount> getAccountsByParentID(final String id) {
	if (accountID2account == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	SortedSet<GnucashAccount> retval = new TreeSet<GnucashAccount>();

	for (Object element : accountID2account.values()) {
	    GnucashAccount account = (GnucashAccount) element;

	    String parent = account.getParentAccountId();
	    if (parent == null) {
		if (id == null) {
		    retval.add((GnucashAccount) account);
		}
	    } else {
		if (parent.equals(id)) {
		    retval.add((GnucashAccount) account);
		}
	    }
	}

	return retval;
    }

    @Override
    public Collection<GnucashAccount> getAccountsByName(final String name) {
	return getAccountsByName(name, true, true);
    }
    
    /**
     * @see GnucashFile#getAccountsByName(java.lang.String)
     */
    @Override
    public Collection<GnucashAccount> getAccountsByName(final String expr, boolean qualif, boolean relaxed) {

	if (accountID2account == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	Collection<GnucashAccount> result = new ArrayList<GnucashAccount>();
	
	for ( GnucashAccount acct : accountID2account.values() ) {
	    if ( relaxed ) {
		if ( qualif ) {
		    if ( acct.getQualifiedName().trim().toLowerCase().
			    contains(expr.trim().toLowerCase()) ) {
			result.add(acct);
		    }
		} else {
		    if ( acct.getName().trim().toLowerCase().
			    contains(expr.trim().toLowerCase()) ) {
			result.add(acct);
		    }
		}
	    } else {
		if ( qualif ) {
		    if ( acct.getQualifiedName().equals(expr) ) {
			result.add(acct);
		    }
		} else {
		    if ( acct.getName().equals(expr) ) {
			result.add(acct);
		    }
		}
	    }
	}

	return result;
    }

    @Override
    public GnucashAccount getAccountByNameUniq(final String name, final boolean qualif) throws NoEntryFoundException, TooManyEntriesFoundException {
	Collection<GnucashAccount> acctList = getAccountsByName(name, qualif, false);
	if ( acctList.size() == 0 )
	    throw new NoEntryFoundException();
	else if ( acctList.size() > 1 )
	    throw new TooManyEntriesFoundException();
	else
	    return acctList.iterator().next();
    }
    
    /**
     * warning: this function has to traverse all accounts. If it much faster to try
     * getAccountByID first and only call this method if the returned account does
     * not have the right name.
     *
     * @param nameRegEx the regular expression of the name to look for
     * @return null if not found
     * @throws TooManyEntriesFoundException 
     * @throws NoEntryFoundException 
     * @see #getAccountByID(String)
     * @see #getAccountsByName(String)
     */
    @Override
    public GnucashAccount getAccountByNameEx(final String nameRegEx) throws NoEntryFoundException, TooManyEntriesFoundException {

	if (accountID2account == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashAccount foundAccount = getAccountByNameUniq(nameRegEx, true);
	if (foundAccount != null) {
	    return foundAccount;
	}
	Pattern pattern = Pattern.compile(nameRegEx);

	for (GnucashAccount account : accountID2account.values()) {
	    Matcher matcher = pattern.matcher(account.getName());
	    if (matcher.matches()) {
		return account;
	    }
	}

	return null;
    }

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
    @Override
    public GnucashAccount getAccountByIDorName(final String id, final String name) throws NoEntryFoundException, TooManyEntriesFoundException {
	GnucashAccount retval = getAccountByID(id);
	if (retval == null) {
	    retval = getAccountByNameUniq(name, true);
	}

	return retval;
    }

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
    @Override
    public GnucashAccount getAccountByIDorNameEx(final String id, final String name) throws NoEntryFoundException, TooManyEntriesFoundException {
	GnucashAccount retval = getAccountByID(id);
	if (retval == null) {
	    retval = getAccountByNameEx(name);
	}

	return retval;
    }

    /**
     * @see GnucashFile#getGenerInvoiceByID(java.lang.String)
     */
    @Override
    public GnucashGenerInvoice getGenerInvoiceByID(final String id) {
	if (invoiceID2invoice == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashGenerInvoice retval = invoiceID2invoice.get(id);
	if (retval == null) {
	    LOGGER.error("No (generic) Invoice with id '" + id + "'. " + 
	                 "We know " + invoiceID2invoice.size() + " accounts.");
	}

	return retval;
    }

    /**
     * @see GnucashFile#getGenerInvoices()
     */
    @Override
    public Collection<GnucashGenerInvoice> getGenerInvoices() {

	Collection<GnucashGenerInvoice> c = invoiceID2invoice.values();

	ArrayList<GnucashGenerInvoice> retval = new ArrayList<GnucashGenerInvoice>(c);
	Collections.sort(retval);

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getPaidGenerInvoices()
     */
    @Override
    public Collection<GnucashGenerInvoice> getPaidGenerInvoices() {
	Collection<GnucashGenerInvoice> retval = new LinkedList<GnucashGenerInvoice>();
	for (GnucashGenerInvoice invc : getGenerInvoices()) {
	    // ::TODO use methods is[Invc|Bill]FullyPaid
//            if (!invoice.getInvcAmountUnpaidWithTaxes().isPositive()) {
//                retval.add(invoice);
//            }
	    if (invc.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
		try {
		    if (invc.isInvcFullyPaid()) {
			retval.add(invc);
		    }
		} catch (WrongInvoiceTypeException e) {
		    // This should not happen
		    LOGGER.error("getPaidInvoices: Serious error");
		}
	    } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
		try {
		    if (invc.isBillFullyPaid()) {
			retval.add(invc);
		    }
		} catch (WrongInvoiceTypeException e) {
		    // This should not happen
		    LOGGER.error("getPaidInvoices: Serious error");
		}
	    } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
		try {
		    if (invc.isJobFullyPaid()) {
			retval.add(invc);
		    }
		} catch (WrongInvoiceTypeException e) {
		    // This should not happen
		    LOGGER.error("getPaidInvoices: Serious error");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidGenerInvoices()
     */
    @Override
    public Collection<GnucashGenerInvoice> getUnpaidGenerInvoices() {
	Collection<GnucashGenerInvoice> retval = new LinkedList<GnucashGenerInvoice>();
	for (GnucashGenerInvoice invc : getGenerInvoices()) {
	    // ::TODO use methods is[Invc|Bill]NotFullyPaid
//			if (invoice.getInvcAmountUnpaidWithTaxes().isPositive()) {
//				retval.add(invoice);
//			}
	    if (invc.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
		try {
		    if (invc.isNotInvcFullyPaid()) {
			retval.add(invc);
		    }
		} catch (WrongInvoiceTypeException e) {
		    // This should not happen
		    LOGGER.error("getUnpaidInvoices: Serious error");
		}
	    } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
		try {
		    if (invc.isNotBillFullyPaid()) {
			retval.add(invc);
		    }
		} catch (WrongInvoiceTypeException e) {
		    // This should not happen
		    LOGGER.error("getUnpaidInvoices: Serious error");
		}
	    } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
		try {
		    if (invc.isNotJobFullyPaid()) {
			retval.add(invc);
		    }
		} catch (WrongInvoiceTypeException e) {
		    // This should not happen
		    LOGGER.error("getUnpaidInvoices: Serious error");
		}
	    }
	}

	return retval;
    }

    // ----------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashCustomerInvoice> getInvoicesForCustomer_direct(final GnucashCustomer cust)
	    throws WrongInvoiceTypeException {
	Collection<GnucashCustomerInvoice> retval = new LinkedList<GnucashCustomerInvoice>();

	for (GnucashGenerInvoice invc : getGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(cust.getId())) {
		try {
		    retval.add(new GnucashCustomerInvoiceImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getInvoicesForCustomer: Cannot instantiate GnucashCustomerInvoiceImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getInvoicesForCustomer_viaAllJobs(final GnucashCustomer cust)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for ( GnucashCustomerJob job : cust.getJobs() ) {
	    for ( GnucashJobInvoice jobInvc : job.getInvoices() ) {
		retval.add(jobInvc);
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashCustomerInvoice> getPaidInvoicesForCustomer_direct(final GnucashCustomer cust)
	    throws WrongInvoiceTypeException {
	Collection<GnucashCustomerInvoice> retval = new LinkedList<GnucashCustomerInvoice>();

	for (GnucashGenerInvoice invc : getPaidGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(cust.getId())) {
		try {
		    retval.add(new GnucashCustomerInvoiceImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getPaidInvoicesForCustomer_direct: Cannot instantiate GnucashCustomerInvoiceImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getPaidInvoicesForCustomer_viaAllJobs(final GnucashCustomer cust)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for ( GnucashCustomerJob job : cust.getJobs() ) {
	    for ( GnucashJobInvoice jobInvc : job.getPaidInvoices() ) {
		retval.add(jobInvc);
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashCustomerInvoice> getUnpaidInvoicesForCustomer_direct(final GnucashCustomer cust)
	    throws WrongInvoiceTypeException {
	Collection<GnucashCustomerInvoice> retval = new LinkedList<GnucashCustomerInvoice>();

	for (GnucashGenerInvoice invc : getUnpaidGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(cust.getId())) {
		try {
		    retval.add(new GnucashCustomerInvoiceImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getUnpaidInvoicesForCustomer_direct: Cannot instantiate GnucashCustomerInvoiceImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getUnpaidInvoicesForCustomer_viaAllJobs(final GnucashCustomer cust)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for ( GnucashCustomerJob job : cust.getJobs() ) {
	    for ( GnucashJobInvoice jobInvc : job.getUnpaidInvoices() ) {
		retval.add(jobInvc);
	    }
	}

	return retval;
    }

    // ----------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    @Override
    public Collection<GnucashVendorBill> getBillsForVendor_direct(final GnucashVendor vend)
	    throws WrongInvoiceTypeException {
	Collection<GnucashVendorBill> retval = new LinkedList<GnucashVendorBill>();

	for (GnucashGenerInvoice invc : getGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(vend.getId())) {
		try {
		    retval.add(new GnucashVendorBillImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getBillsForVendor: Cannot instantiate GnucashVendorBillImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getBillsForVendor_viaAllJobs(final GnucashVendor vend)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for ( GnucashVendorJob job : vend.getJobs() ) {
	    for ( GnucashJobInvoice jobInvc : job.getInvoices() ) {
		retval.add(jobInvc);
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    @Override
    public Collection<GnucashVendorBill> getPaidBillsForVendor_direct(final GnucashVendor vend)
	    throws WrongInvoiceTypeException {
	Collection<GnucashVendorBill> retval = new LinkedList<GnucashVendorBill>();

	for (GnucashGenerInvoice invc : getPaidGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(vend.getId())) {
		try {
		    retval.add(new GnucashVendorBillImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getPaidBillsForVendor_direct: Cannot instantiate GnucashVendorBillImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getPaidBillsForVendor_viaAllJobs(final GnucashVendor vend)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for ( GnucashVendorJob job : vend.getJobs() ) {
	    for ( GnucashJobInvoice jobInvc : job.getPaidInvoices() ) {
		retval.add(jobInvc);
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidBillsForVendor_viaJob(GnucashVendor)
     */
    @Override
    public Collection<GnucashVendorBill> getUnpaidBillsForVendor_direct(final GnucashVendor vend)
	    throws WrongInvoiceTypeException {
	Collection<GnucashVendorBill> retval = new LinkedList<GnucashVendorBill>();

	for (GnucashGenerInvoice invc : getUnpaidGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(vend.getId())) {
		try {
		    retval.add(new GnucashVendorBillImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getUnpaidBillsForVendor_direct: Cannot instantiate GnucashVendorBillImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getUnpaidBillsForVendor_viaAllJobs(final GnucashVendor vend)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for ( GnucashVendorJob job : vend.getJobs() ) {
	    for ( GnucashJobInvoice jobInvc : job.getUnpaidInvoices() ) {
		retval.add(jobInvc);
	    }
	}

	return retval;
    }

    // ----------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getInvoicesForJob(final GnucashGenerJob job)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for (GnucashGenerInvoice invc : getGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(job.getId())) {
		try {
		    retval.add(new GnucashJobInvoiceImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getInvoicesForJob: Cannot instantiate GnucashJobInvoiceImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getPaidInvoicesForJob(final GnucashGenerJob job)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for (GnucashGenerInvoice invc : getPaidGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(job.getId())) {
		try {
		    retval.add(new GnucashJobInvoiceImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getPaidInvoicesForJob: Cannot instantiate GnucashJobInvoiceImpl");
		}
	    }
	}

	return retval;
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
     */
    @Override
    public Collection<GnucashJobInvoice> getUnpaidInvoicesForJob(final GnucashGenerJob job)
	    throws WrongInvoiceTypeException {
	Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

	for (GnucashGenerInvoice invc : getUnpaidGenerInvoices()) {
	    if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(job.getId())) {
		try {
		    retval.add(new GnucashJobInvoiceImpl(invc));
		} catch (WrongInvoiceTypeException e) {
		    // This really should not happen, one can almost
		    // throw a fatal log here.
		    LOGGER.error("getUnpaidInvoicesForJob: Cannot instantiate GnucashJobInvoiceImpl");
		}
	    }
	}

	return retval;
    }

    // ---------------------------------------------------------------

    /**
     * @see GnucashFile#getGenerInvoiceByID(java.lang.String)
     */
    @Override
    public GnucashGenerInvoiceEntry getGenerInvoiceEntryByID(final String id) {
	if (invoiceEntryID2invoiceEntry == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashGenerInvoiceEntry retval = invoiceEntryID2invoiceEntry.get(id);
	if (retval == null) {
	    LOGGER.error("No (generic) Invoice-Entry with id '" + id + "'. " + 
	                 "We know " + invoiceEntryID2invoiceEntry.size() + " accounts.");
	}

	return retval;
    }

    /**
     * @see GnucashFile#getGenerInvoices()
     */
    public Collection<GnucashGenerInvoiceEntry> getInvoiceEntries() {

	Collection<GnucashGenerInvoiceEntry> c = invoiceEntryID2invoiceEntry.values();

	ArrayList<GnucashGenerInvoiceEntry> retval = new ArrayList<GnucashGenerInvoiceEntry>(c);
	Collections.sort(retval);

	return retval;
    }

    /**
     * @see #getGnucashFile()
     */
    private File file;
    
    // ---------------------------------------------------------------

    /**
     * Filles lazy in getTaxTables() .
     *
     * @see #getTaxTables()
     */
    protected Map<String, GCshPrice> priceById = null;

    /**
     * {@inheritDoc}
     */
    public GCshPrice getPriceByID(String id) {
        if (priceById == null) {
            getPrices();
        }
        
        return priceById.get(id);
    }

    protected GncV2.GncBook.GncPricedb getPriceDB() {
	List<Object> bookElements = this.getRootElement().getGncBook().getBookElements();
	for ( Object bookElement : bookElements ) {
	    if ( bookElement instanceof GncV2.GncBook.GncPricedb ) {
		return (GncV2.GncBook.GncPricedb) bookElement;
	    } 
	}
	
	return null; // Compiler happy
    }

    /**
     * {@inheritDoc}
     */
    public Collection<GCshPrice> getPrices() {
        if (priceById == null) {
            priceById = new HashMap<String, GCshPrice>();

            GncV2.GncBook.GncPricedb priceDB = getPriceDB();
            List<GncV2.GncBook.GncPricedb.Price> prices = priceDB.getPrice();
            for ( GncV2.GncBook.GncPricedb.Price jwsdpPeer : prices ) {
        	GCshPriceImpl price = new GCshPriceImpl(jwsdpPeer, this);
        	priceById.put(price.getId(), price);
            }
        } 

        return priceById.values();
    }

    /**
     * {@inheritDoc}
     */
    public FixedPointNumber getLatestPrice(final String pCmdtySpace, final String pCmdtyId) {
	return getLatestPrice(pCmdtySpace, pCmdtyId, 0);
    }

    // ---------------------------------------------------------------

    /**
     * the top-level Element of the gnucash-files parsed and checked for validity by
     * JAXB.
     */
    private GncV2 rootElement;

    /**
     * All accounts indexed by their unique id-String.
     *
     * @see GnucashAccount
     * @see GnucashAccountImpl
     */
    protected Map<String, GnucashAccount> accountID2account;

    /**
     * All transactions indexed by their unique id-String.
     *
     * @see GnucashTransaction
     * @see GnucashTransactionImpl
     */
    protected Map<String, GnucashTransaction> transactionID2transaction;

    /**
     * All transaction-splits indexed by their unique id-String.
     *
     * @see GnucashTransactionSplit
     * @see GnucashTransactionSplitImpl
     */
    protected Map<String, GnucashTransactionSplit> transactionSplitID2transactionSplit;

    /**
     * All customer/vendor invoices indexed by their unique id-String.
     *
     * @see GnucashGenerInvoice
     * @see GnucashGenerInvoiceImpl
     */
    protected Map<String, GnucashGenerInvoice> invoiceID2invoice;

    /**
     * All customer/vendor invoice entries indexed by their unique id-String.
     *
     * @see GnucashGenerInvoiceEnctry
     * @see GnucashGenerInvoiceEntryImpl
     */
    protected Map<String, GnucashGenerInvoiceEntry> invoiceEntryID2invoiceEntry;

    /**
     * All jobs indexed by their unique id-String.
     *
     * @see GnucashGenerJob
     * @see GnucashCustomerJobImpl
     */
    protected Map<String, GnucashGenerJob> jobID2job;

    /**
     * All customers indexed by their unique id-String.
     *
     * @see GnucashCustomer
     * @see GnucashCustomerImpl
     */
    protected Map<String, GnucashCustomer> customerID2customer;

    /**
     * All vendors indexed by their unique id-String.
     *
     * @see GnucashVendor
     * @see GnucashVendorImpl
     */
    protected Map<String, GnucashVendor> vendorID2vendor;

    /**
     * All vendors indexed by their unique id-String.
     *
     * @see GnucashVendor
     * @see GnucashVendorImpl
     */
    protected Map<String, GnucashCommodity> cmdtyQualifID2Cmdty; // Keys: Sic String not CmdtyCurrID
                                                                 // else subtle problems ensue
    protected Map<String, String>           cmdtyXCode2QualifID; // Values: String not CmdtyCurrID,
                                                                 // dto.

    /**
     * Helper to implement the {@link GnucashObject}-interface without having the
     * same code twice.
     */
    private GnucashObjectImpl myGnucashObject;

    /**
     * @return the underlying JAXB-element
     */
    protected GncV2 getRootElement() {
	return rootElement;
    }

    /**
     * Set the new root-element and load all accounts, transactions,... from it.
     *
     * @param pRootElement the new root-element
     */
    protected void setRootElement(final GncV2 pRootElement) {
	if (pRootElement == null) {
	    throw new IllegalArgumentException("null not allowed for field this.rootElement");
	}
	rootElement = pRootElement;

	// fill prices

	loadPriceDatabase(pRootElement);
	if (pRootElement.getGncBook().getBookSlots() == null) {
	    pRootElement.getGncBook().setBookSlots((new ObjectFactory()).createSlotsType());
	}
	myGnucashObject = new GnucashObjectImpl(pRootElement.getGncBook().getBookSlots(), this);

	// fill maps
	initAccountMap(pRootElement);

	initGenerInvoiceMap(pRootElement);

	// invoiceEntries refer to invoices, therefore they must be loaded after
	// them
	initGenerInvoiceEntryMap(pRootElement);

	// transactions refer to invoices, therefore they must be loaded after
	// them
	initTransactionMap(pRootElement);

	initCustomerMap(pRootElement);

	initVendorMap(pRootElement);

	initCommodityMap(pRootElement);

	initJobMap(pRootElement);

	// check for unknown book-elements
	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (bookElement instanceof GncTransaction) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncSchedxaction) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncTemplateTransactions) {
		continue;
	    }
	    if (bookElement instanceof GncAccount) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncInvoice) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncEntry) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncJob) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncCustomer) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncVendor) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncCommodity) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncPricedb) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncTaxTable) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncVendor.VendorTerms) {
		continue;
	    }
	    if (bookElement instanceof GncBudget) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncBillTerm) {
		continue;
	    }
	    if (bookElement instanceof GncV2.GncBook.GncGncEmployee) {
		continue; // TODO: create a Java-Class for employees like we have for customers
	    }
	    throw new IllegalArgumentException(
		    "<gnc:book> contains unknown element [" + bookElement.getClass().getName() + "]");
	}
    }

    private void initAccountMap(final GncV2 pRootElement) {
	accountID2account = new HashMap<>();

	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncAccount)) {
		continue;
	    }
	    GncAccount jwsdpAcct = (GncAccount) bookElement;

	    try {
		GnucashAccount acct = createAccount(jwsdpAcct);
		accountID2account.put(acct.getId(), acct);
	    } catch (RuntimeException e) {
		LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initAccountMap: "
			+ "ignoring illegal Account-Entry with id=" + jwsdpAcct.getActId().getValue(), e);
	    }
	} // for

	LOGGER.debug("No. of entries in account map: " + accountID2account.size());
    }

    private void initGenerInvoiceMap(final GncV2 pRootElement) {
	invoiceID2invoice = new HashMap<>();

	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncV2.GncBook.GncGncInvoice)) {
		continue;
	    }
	    GncV2.GncBook.GncGncInvoice jwsdpInvc = (GncV2.GncBook.GncGncInvoice) bookElement;

	    try {
		GnucashGenerInvoice invc = createGenerInvoice(jwsdpInvc);
		invoiceID2invoice.put(invc.getId(), invc);
	    } catch (RuntimeException e) {
		LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initInvoiceMap: "
			+ "ignoring illegal (generic) Invoice-Entry with id=" + jwsdpInvc.getInvoiceId(), e);
	    }
	} // for

	LOGGER.debug("No. of entries in (generic) invoice map: " + invoiceID2invoice.size());
    }

    private void initGenerInvoiceEntryMap(final GncV2 pRootElement) {
	invoiceEntryID2invoiceEntry = new HashMap<>();

	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncV2.GncBook.GncGncEntry)) {
		continue;
	    }
	    GncV2.GncBook.GncGncEntry jwsdpInvcEntr = (GncV2.GncBook.GncGncEntry) bookElement;

	    try {
		GnucashGenerInvoiceEntry invcEntr = createGenerInvoiceEntry(jwsdpInvcEntr);
		invoiceEntryID2invoiceEntry.put(invcEntr.getId(), invcEntr);
	    } catch (RuntimeException e) {
		LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initInvoiceEntryMap: "
			+ "ignoring illegal (generic) Invoice-Entry-Entry with id="
			+ jwsdpInvcEntr.getEntryGuid().getValue(), e);
	    }
	} // for

	LOGGER.debug("No. of entries in (generic) invoice-entry map: " + invoiceEntryID2invoiceEntry.size());
    }

    private void initTransactionMap(final GncV2 pRootElement) {
	transactionID2transaction = new HashMap<>();
	transactionSplitID2transactionSplit = new HashMap<>();

	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncTransaction)) {
		continue;
	    }
	    GncTransaction jwsdpTrx = (GncTransaction) bookElement;

	    try {
		GnucashTransactionImpl trx = createTransaction(jwsdpTrx);
		transactionID2transaction.put(trx.getId(), trx);
		for (GnucashTransactionSplit splt : trx.getSplits()) {
		    transactionSplitID2transactionSplit.put(splt.getId(), splt);
		}
	    } catch (RuntimeException e) {
		LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initTransactionMap: "
			+ "ignoring illegal Transaction-Entry with id=" + jwsdpTrx.getTrnId().getValue(), e);
	    }
	} // for

	LOGGER.debug("No. of entries in transaction map: " + transactionID2transaction.size());
    }

    private void initCustomerMap(final GncV2 pRootElement) {
	customerID2customer = new HashMap<>();

	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncV2.GncBook.GncGncCustomer)) {
		continue;
	    }
	    GncV2.GncBook.GncGncCustomer jwsdpCust = (GncV2.GncBook.GncGncCustomer) bookElement;

	    try {
		GnucashCustomerImpl cust = createCustomer(jwsdpCust);
		customerID2customer.put(cust.getId(), cust);
	    } catch (RuntimeException e) {
		LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initCustomerMap: "
			+ "ignoring illegal Customer-Entry with id=" + jwsdpCust.getCustId(), e);
	    }
	} // for

	LOGGER.debug("No. of entries in customer map: " + customerID2customer.size());
    }

    private void initVendorMap(final GncV2 pRootElement) {
	vendorID2vendor = new HashMap<>();

	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncV2.GncBook.GncGncVendor)) {
		continue;
	    }
	    GncV2.GncBook.GncGncVendor jwsdpVend = (GncV2.GncBook.GncGncVendor) bookElement;

	    try {
		GnucashVendorImpl vend = createVendor(jwsdpVend);
		vendorID2vendor.put(vend.getId(), vend);
	    } catch (RuntimeException e) {
		LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initVendorMap: "
			+ "ignoring illegal Vendor-Entry with id=" + jwsdpVend.getVendorId(), e);
	    }
	} // for

	LOGGER.debug("No. of entries in vendor map: " + vendorID2vendor.size());
    }

    private void initCommodityMap(final GncV2 pRootElement) {
	initCommodityMap1(pRootElement);
	initCommodityMap2(pRootElement);
    }
    
    private void initCommodityMap1(final GncV2 pRootElement) {
    cmdtyQualifID2Cmdty = new HashMap<String, GnucashCommodity>();

    for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
        Object bookElement = iter.next();
        if (!(bookElement instanceof GncV2.GncBook.GncCommodity)) {
        continue;
        }
        GncV2.GncBook.GncCommodity jwsdpCmdty = (GncV2.GncBook.GncCommodity) bookElement;

        try {
            GnucashCommodityImpl cmdty = createCommodity(jwsdpCmdty);
            try {
        	cmdtyQualifID2Cmdty.put(cmdty.getQualifId().toString(), cmdty);
            } catch (InvalidCmdtyCurrTypeException e) {
        	LOGGER.error("initCommodityMap1: Could not add Commodity to map: " + cmdty.toString());
            }
        } catch (RuntimeException e) {
            LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initCommodityMap: "
        	    + "ignoring illegal Commodity entry with id=" + jwsdpCmdty.getCmdtyId(), e);
        }
    } // for

    LOGGER.debug("No. of entries in Commodity map (1): " + cmdtyQualifID2Cmdty.size());
    }

    private void initCommodityMap2(final GncV2 pRootElement) {
    cmdtyXCode2QualifID = new HashMap<String, String>();

    for ( String qualifID : cmdtyQualifID2Cmdty.keySet() ) {
	GnucashCommodity cmdty = cmdtyQualifID2Cmdty.get(qualifID);
	try {
	    cmdtyXCode2QualifID.put(cmdty.getXCode(), cmdty.getQualifId().toString());
	} catch (InvalidCmdtyCurrTypeException e) {
	    LOGGER.error("initCommodityMap2: Could not add element to map: " + cmdty.getXCode());
	}
    } 

    LOGGER.debug("No. of entries in Commodity map (2): " + cmdtyXCode2QualifID.size());
    }

    private void initJobMap(final GncV2 pRootElement) {
	jobID2job = new HashMap<String, GnucashGenerJob>();

	for (Iterator<Object> iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncV2.GncBook.GncGncJob)) {
		continue;
	    }
	    GncV2.GncBook.GncGncJob jwsdpJob = (GncV2.GncBook.GncGncJob) bookElement;

	    try {
		GnucashGenerJobImpl job = createGenerJob(jwsdpJob);
		String jobID = job.getId();
		if (jobID == null) {
		    LOGGER.error("File contains a (generic) Job w/o an ID. indexing it with the ID ''");
		    jobID = "";
		}
		jobID2job.put(job.getId(), job);
	    } catch (RuntimeException e) {
		LOGGER.error("[RuntimeException] Problem in " + getClass().getName() + ".initJobMap: "
			+ "ignoring illegal (generic) Job entry with id=" + jwsdpJob.getJobId(), e);
	    }
	} // for

	LOGGER.debug("No. of entries in (generic) Job map: " + jobID2job.size());
    }

    /**
     * Use a heuristic to determine the defaultcurrency-id. If we cannot find one,
     * we default to EUR.<br/>
     * Comodity-stace is fixed as "CURRENCY" .
     *
     * @return the default-currencyID to use.
     */
    public String getDefaultCurrencyID() {
	GncV2 root = getRootElement();
	if (root == null) {
	    return "EUR";
	}
	
	for (Iterator<Object> iter = getRootElement().getGncBook().getBookElements().iterator(); iter.hasNext();) {
	    Object bookElement = iter.next();
	    if (!(bookElement instanceof GncAccount)) {
		continue;
	    }
	    
	    GncAccount jwsdpAccount = (GncAccount) bookElement;
	    if ( jwsdpAccount.getActCommodity() != null ) {
		 if ( jwsdpAccount.getActCommodity().getCmdtySpace().equals(GCshCmdtyCurrNameSpace.CURRENCY) ) {
		     return jwsdpAccount.getActCommodity().getCmdtyId();
		 }
	    }
	}
	
	return "EUR";
    }

    /**
     * @param pRootElement the root-element of the Gnucash-file
     */
    private void loadPriceDatabase(final GncV2 pRootElement) {
	boolean noPriceDB = true;
	List<Object> bookElements = pRootElement.getGncBook().getBookElements();
	for (Object bookElement : bookElements) {
	    if (!(bookElement instanceof GncV2.GncBook.GncPricedb)) {
		continue;
	    }
	    noPriceDB = false;
	    GncV2.GncBook.GncPricedb priceDB = (GncV2.GncBook.GncPricedb) bookElement;

	    if (priceDB.getVersion() != 1) {

		LOGGER.warn("We know only the format of the price-db 1, " + "the file has version "
			+ priceDB.getVersion() + " prices will not be loaded!");
	    } else {
		getCurrencyTable().clear();
		getCurrencyTable().setConversionFactor(GCshCmdtyCurrNameSpace.CURRENCY, 
			                               getDefaultCurrencyID(), 
			                               new FixedPointNumber(1));

		for (Iterator<GncV2.GncBook.GncPricedb.Price> iter = priceDB.getPrice().iterator(); iter.hasNext();) {
		    GncV2.GncBook.GncPricedb.Price price = iter.next();
		    GncV2.GncBook.GncPricedb.Price.PriceCommodity comodity = price.getPriceCommodity();

		    // check if we already have a latest price for this comodity
		    // (=currency, fund, ...)
		    if (getCurrencyTable().getConversionFactor(comodity.getCmdtySpace(),
			    comodity.getCmdtyId()) != null) {
			continue;
		    }

		    String baseCurrency = getDefaultCurrencyID();
		    if ( comodity.getCmdtySpace().equals(GCshCmdtyCurrNameSpace.CURRENCY) && 
			 comodity.getCmdtyId().equals(baseCurrency) ) {
			LOGGER.warn("Ignoring price-quote for " + baseCurrency + " because " + baseCurrency + " is"
				+ "our base-currency.");
			continue;
		    }

		    // get the latest price in the file and insert it into
		    // our currency table
		    FixedPointNumber factor = getLatestPrice(comodity.getCmdtySpace(), comodity.getCmdtyId());

		    if (factor != null) {
			getCurrencyTable().setConversionFactor(comodity.getCmdtySpace(), comodity.getCmdtyId(), factor);
		    } else {
			LOGGER.warn("The gnucash-file defines a factor for a comodity '" + comodity.getCmdtySpace()
				+ "' - '" + comodity.getCmdtyId() + "' but has no comodity for it");
		    }
		}
	    }
	}

	if (noPriceDB) {
	    // case: no priceDB in file
	    getCurrencyTable().clear();
	}
    }

    /**
     * @see {@link #getLatestPrice(String, String)}
     */
    protected static final DateFormat PRICE_QUOTE_DATE_FORMAT = new SimpleDateFormat(Const.STANDARD_DATE_FORMAT);

    /**
     * @param pCmdtySpace the namespace for pCmdtyId
     * @param pCmdtyId    the currency-name
     * @param depth       used for recursion. Allways call with '0' for aborting
     *                    recursive quotes (quotes to other then the base- currency)
     *                    we abort if the depth reached 6.
     * @return the latest price-quote in the gnucash-file in the default-currency
     * @see {@link GnucashFile#getLatestPrice(String, String)}
     * @see #getDefaultCurrencyID()
     */
    private FixedPointNumber getLatestPrice(final String pCmdtySpace, final String pCmdtyId, final int depth) {
	if (pCmdtySpace == null) {
	    throw new IllegalArgumentException("null parameter 'pCmdtySpace' " + "given");
	}
	if (pCmdtyId == null) {
	    throw new IllegalArgumentException("null parameter 'pCmdtyId' " + "given");
	}

	Date latestDate = null;
	FixedPointNumber latestQuote = null;
	FixedPointNumber factor = new FixedPointNumber(1); // factor is used if the quote is not to our base-currency
	final int maxRecursionDepth = 5;

	for (Object bookElement : getRootElement().getGncBook().getBookElements()) {
	    if (!(bookElement instanceof GncV2.GncBook.GncPricedb)) {
		continue;
	    }
	    GncV2.GncBook.GncPricedb priceDB = (GncV2.GncBook.GncPricedb) bookElement;
	    for (GncV2.GncBook.GncPricedb.Price priceQuote : (List<GncV2.GncBook.GncPricedb.Price>) priceDB.getPrice()) {

		try {
		    if (priceQuote == null) {
			LOGGER.warn("gnucash-file contains null price-quotes" + " there may be a problem with JWSDP");
			continue;
		    }
		    if (priceQuote.getPriceCurrency() == null) {
			LOGGER.warn("gnucash-file contains price-quotes" + " with no currency id='"
				+ priceQuote.getPriceId().getValue() + "'");
			continue;
		    }
		    if (priceQuote.getPriceCurrency().getCmdtyId() == null) {
			LOGGER.warn("gnucash-file contains price-quotes" + " with no currency-id id='"
				+ priceQuote.getPriceId().getValue() + "'");
			continue;
		    }
		    if (priceQuote.getPriceCurrency().getCmdtySpace() == null) {
			LOGGER.warn("gnucash-file contains price-quotes" + " with no currency-namespace id='"
				+ priceQuote.getPriceId().getValue() + "'");
			continue;
		    }
		    if (priceQuote.getPriceTime() == null) {
			LOGGER.warn("gnucash-file contains price-quotes" + " with no timestamp id='"
				+ priceQuote.getPriceId().getValue() + "'");
			continue;
		    }
		    if (priceQuote.getPriceValue() == null) {
			LOGGER.warn("gnucash-file contains price-quotes" + " with no value id='"
				+ priceQuote.getPriceId().getValue() + "'");
			continue;
		    }
		    /*
		     * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND") &&
		     * priceQuote.getPriceType() == null) {
		     * LOGGER.warn("gnucash-file contains FUND-price-quotes" + " with no type id='"
		     * + priceQuote.getPriceId().getValue() + "'"); continue; }
		     */
		    if (!priceQuote.getPriceCommodity().getCmdtySpace().equals(pCmdtySpace)) {
			continue;
		    }
		    if (!priceQuote.getPriceCommodity().getCmdtyId().equals(pCmdtyId)) {
			continue;
		    }
		    /*
		     * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND") &&
		     * (priceQuote.getPriceType() == null ||
		     * !priceQuote.getPriceType().equals("last") )) {
		     * LOGGER.warn("ignoring FUND-price-quote of unknown type '" +
		     * priceQuote.getPriceType() + "' expecting 'last' "); continue; }
		     */

		    if (!priceQuote.getPriceCurrency().getCmdtySpace().equals(GCshCmdtyCurrNameSpace.CURRENCY)) {
			if (depth > maxRecursionDepth) {
			    LOGGER.warn("ignoring price-quote that is not in an ISO4217-currency but in '"
				    + priceQuote.getPriceCurrency().getCmdtyId());
			    continue;
			}
			factor = getLatestPrice(priceQuote.getPriceCurrency().getCmdtySpace(),
				priceQuote.getPriceCurrency().getCmdtyId(), depth + 1);
		    } else {
			if (!priceQuote.getPriceCurrency().getCmdtyId().equals(getDefaultCurrencyID())) {
			    if (depth > maxRecursionDepth) {
				LOGGER.warn("ignoring price-quote that is not in " + getDefaultCurrencyID() + " "
					+ "but in  '" + priceQuote.getPriceCurrency().getCmdtyId());
				continue;
			    }
			    factor = getLatestPrice(priceQuote.getPriceCurrency().getCmdtySpace(),
				    priceQuote.getPriceCurrency().getCmdtyId(), depth + 1);
			}
		    }

		    Date date = PRICE_QUOTE_DATE_FORMAT.parse(priceQuote.getPriceTime().getTsDate());

		    if (latestDate == null || latestDate.before(date)) {
			latestDate = date;
			latestQuote = new FixedPointNumber(priceQuote.getPriceValue());
			LOGGER.debug("getLatestPrice(pCmdtySpace='" + pCmdtySpace + "', String pCmdtyId='" + pCmdtyId
				+ "') converted " + latestQuote + " <= " + priceQuote.getPriceValue());
		    }

		} catch (NumberFormatException e) {
		    LOGGER.error("[NumberFormatException] Problem in " + getClass().getName()
			    + ".getLatestPrice(pCmdtySpace='" + pCmdtySpace + "', String pCmdtyId='" + pCmdtyId
			    + "')! Ignoring a bad price-quote '" + priceQuote + "'", e);
		} catch (ParseException e) {
		    LOGGER.error("[ParseException] Problem in " + getClass().getName() + ".getLatestPrice(pCmdtySpace='"
			    + pCmdtySpace + "', String pCmdtyId='" + pCmdtyId + "')! Ignoring a bad price-quote '"
			    + priceQuote + "'", e);
		} catch (NullPointerException e) {
		    LOGGER.error("[NullPointerException] Problem in " + getClass().getName()
			    + ".getLatestPrice(pCmdtySpace='" + pCmdtySpace + "', String pCmdtyId='" + pCmdtyId
			    + "')! Ignoring a bad price-quote '" + priceQuote + "'", e);
		} catch (ArithmeticException e) {
		    LOGGER.error("[ArithmeticException] Problem in " + getClass().getName()
			    + ".getLatestPrice(pCmdtySpace='" + pCmdtySpace + "', String pCmdtyId='" + pCmdtyId
			    + "')! Ignoring a bad price-quote '" + priceQuote + "'", e);
		}

	    }
	}

	LOGGER.debug(getClass().getName() + ".getLatestPrice(pCmdtySpace='" + pCmdtySpace + "', String pCmdtyId='"
		+ pCmdtyId + "')= " + latestQuote + " from " + latestDate);

	if (latestQuote == null) {
	    return null;
	}

	if (factor == null) {
	    factor = new FixedPointNumber(1);
	}

	return factor.multiply(latestQuote);
    }

    // ---------------------------------------------------------------

    /**
     * @param jwsdpAcct the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashAccount to wrap the given jaxb-object.
     */
    protected GnucashAccount createAccount(final GncAccount jwsdpAcct) {
	GnucashAccount acct = new GnucashAccountImpl(jwsdpAcct, this);
	return acct;
    }

    /**
     * @param jwsdpInvc the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashInvoice to wrap the given jaxb-object.
     */
    protected GnucashGenerInvoice createGenerInvoice(final GncV2.GncBook.GncGncInvoice jwsdpInvc) {
	GnucashGenerInvoice invc = new GnucashGenerInvoiceImpl(jwsdpInvc, this);
	return invc;
    }

    /**
     * @param jwsdpInvcEntr the JWSDP-peer (parsed xml-element) to fill our object
     *                      with
     * @return the new GnucashInvoiceEntry to wrap the given jaxb-object.
     */
    protected GnucashGenerInvoiceEntry createGenerInvoiceEntry(final GncV2.GncBook.GncGncEntry jwsdpInvcEntr) {
	GnucashGenerInvoiceEntry entr = new GnucashGenerInvoiceEntryImpl(jwsdpInvcEntr, this);
	return entr;
    }

    /**
     * @param jwsdpJob the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashJob to wrap the given jaxb-object.
     */
    protected GnucashGenerJobImpl createGenerJob(final GncV2.GncBook.GncGncJob jwsdpJob) {

	GnucashGenerJobImpl job = new GnucashGenerJobImpl(jwsdpJob, this);
	return job;
    }

    /**
     * @param jwsdpCust the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashCustomer to wrap the given JAXB object.
     */
    protected GnucashCustomerImpl createCustomer(final GncV2.GncBook.GncGncCustomer jwsdpCust) {
	GnucashCustomerImpl cust = new GnucashCustomerImpl(jwsdpCust, this);
	return cust;
    }

    /**
     * @param jwsdpVend the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashVendor to wrap the given JAXB object.
     */
    protected GnucashVendorImpl createVendor(final GncV2.GncBook.GncGncVendor jwsdpVend) {
	GnucashVendorImpl vend = new GnucashVendorImpl(jwsdpVend, this);
	return vend;
    }

    /**
     * @param jwsdpCmdty the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashCommodity to wrap the given JAXB object.
     */
    protected GnucashCommodityImpl createCommodity(final GncV2.GncBook.GncCommodity jwsdpCmdty) {
      GnucashCommodityImpl cmdty = new GnucashCommodityImpl(jwsdpCmdty, this);
    return cmdty;
    }

    /**
     * @param jwsdpTrx the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashTransaction to wrap the given jaxb-object.
     */
    protected GnucashTransactionImpl createTransaction(final GncTransaction jwsdpTrx) {
	GnucashTransactionImpl trx = new GnucashTransactionImpl(jwsdpTrx, this);
	return trx;
    }

    // ----------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() {
	return file;
    }

    /**
     * Internal method, just sets this.file .
     *
     * @param pFile the file loaded
     */
    protected void setFile(final File pFile) {
	if (pFile == null) {
	    throw new IllegalArgumentException("null not allowed for field this.file");
	}
	file = pFile;
    }

    /**
     * loads the file and calls setRootElement.
     *
     * @param pFile the file to read
     * @throws IOException on low level reading-errors (FileNotFoundException if not
     *                     found)
     * @see #setRootElement(GncV2)
     */
    protected void loadFile(final File pFile) throws IOException {

	long start = System.currentTimeMillis();

	if (pFile == null) {
	    throw new IllegalArgumentException("null not allowed for field this.file");
	}

	if (!pFile.exists()) {
	    throw new IllegalArgumentException("Given file '" + pFile.getAbsolutePath() + "' does not exist!");
	}

	setFile(pFile);

	InputStream in = new FileInputStream(pFile);
	if (pFile.getName().endsWith(".gz")) {
	    in = new BufferedInputStream(in);
	    in = new GZIPInputStream(in);
	} else {
	    // determine if it's gzipped by the magic bytes
	    byte[] magic = new byte[2];
	    in.read(magic);
	    in.close();

	    in = new FileInputStream(pFile);
	    in = new BufferedInputStream(in);
	    if (magic[0] == 31 && magic[1] == -117) {
		in = new GZIPInputStream(in);
	    }
	}

	loadInputStream(in);

	long end = System.currentTimeMillis();
	LOGGER.info("GnucashFileImpl.loadFile took " + (end - start) + " ms (total) ");

    }

    protected void loadInputStream(InputStream in) throws UnsupportedEncodingException, IOException {
	long start = System.currentTimeMillis();

	NamespaceRemovererReader reader = new NamespaceRemovererReader(new InputStreamReader(in, "utf-8"));
	try {

	    JAXBContext myContext = getJAXBContext();
	    Unmarshaller unmarshaller = myContext.createUnmarshaller();

	    GncV2 o = (GncV2) unmarshaller.unmarshal(new InputSource(new BufferedReader(reader)));
	    long start2 = System.currentTimeMillis();
	    setRootElement(o);
	    long end = System.currentTimeMillis();
	    LOGGER.info("GnucashFileImpl.loadFileInputStream took " + (end - start) + " ms (total) " + (start2 - start)
		    + " ms (jaxb-loading)" + (end - start2) + " ms (building facades)");

	} catch (JAXBException e) {
	    LOGGER.error(e.getMessage(), e);
	    throw new IllegalStateException(e);
	} finally {
	    reader.close();
	}
    }

    /**
     * @see #getObjectFactory()
     */
    private volatile ObjectFactory myJAXBFactory;

    /**
     * @return the jaxb object-factory used to create new peer-objects to extend
     *         this
     */
    @SuppressWarnings("exports")
    public ObjectFactory getObjectFactory() {
	if (myJAXBFactory == null) {
	    myJAXBFactory = new ObjectFactory();
	}
	return myJAXBFactory;
    }

    /**
     * @see #getJAXBContext()
     */
    private volatile JAXBContext myJAXBContext;

    /**
     * @return the JAXB-context
     */
    protected JAXBContext getJAXBContext() {
	if (myJAXBContext == null) {
	    try {
		myJAXBContext = JAXBContext.newInstance("org.gnucash.generated", this.getClass().getClassLoader());
	    } catch (JAXBException e) {
		LOGGER.error(e.getMessage(), e);
	    }
	}
	return myJAXBContext;
    }

    /**
     * @param type the type-string to look for
     * @return the count-data saved in the xml-file
     */
    protected GncCountData findCountDataByType(final String type) {
	for (Iterator<GncCountData> iter = getRootElement().getGncBook().getGncCountData().iterator(); iter.hasNext();) {
	    GncCountData count = (GncCountData) iter.next();
	    if (count.getCdType().equals(type)) {
		return count;
	    }
	}
	return null;
    }

    /**
     * @return the number of transactions
     */
    protected int getTransactionCount() {
	GncCountData count = findCountDataByType("transaction");
	return count.getValue();
    }

    /**
     * @see GnucashFile#getAccountByID(java.lang.String)
     */
    public GnucashAccount getAccountByID(final String id) {
	if (accountID2account == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashAccount retval = accountID2account.get(id);
	if (retval == null) {
	    LOGGER.error("No Account with id '" + id + "'. " + 
	                 "We know " + accountID2account.size() + " accounts.");
	}
	return retval;
    }

    // ---------------------------------------------------------------

    /**
     * @see GnucashFile#getCustomerByID(java.lang.String)
     */
    public GnucashCustomer getCustomerByID(final String id) {
	if (customerID2customer == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashCustomer retval = customerID2customer.get(id);
	if (retval == null) {
	    LOGGER.warn("No Customer with id '" + id + "'. We know " + customerID2customer.size() + " customers.");
	}
	return retval;
    }

    /**
     * @see GnucashFile#getCustomersByName(java.lang.String)
     */
    @Override
    public Collection<GnucashCustomer> getCustomersByName(final String name) {
	return getCustomersByName(name, true);
    }

    /**
     * @see GnucashFile#getCustomersByName(java.lang.String)
     */
    @Override
    public Collection<GnucashCustomer> getCustomersByName(final String expr, boolean relaxed) {

	if (customerID2customer == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	Collection<GnucashCustomer> result = new ArrayList<GnucashCustomer>();

	for ( GnucashCustomer cust : getCustomers() ) {
	    if ( relaxed ) {
		if ( cust.getName().trim().toLowerCase().
			contains(expr.trim().toLowerCase()) ) {
		    result.add(cust);
		}
	    } else {
		if ( cust.getName().equals(expr) ) {
		    result.add(cust);
		}
	    }
	}
	
	return result;
    }

    @Override
    public GnucashCustomer getCustomerByNameUniq(final String name) throws NoEntryFoundException, TooManyEntriesFoundException {
	Collection<GnucashCustomer> custList = getCustomersByName(name);
	if ( custList.size() == 0 )
	    throw new NoEntryFoundException();
	else if ( custList.size() > 1 )
	    throw new TooManyEntriesFoundException();
	else
	    return custList.iterator().next();
    }
    
    /**
     * @see GnucashFile#getCustomers()
     */
    public Collection<GnucashCustomer> getCustomers() {
	return customerID2customer.values();
    }

    // ---------------------------------------------------------------

    @Override
    public GnucashVendor getVendorByID(String id) {
	if (vendorID2vendor == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashVendor retval = vendorID2vendor.get(id);
	if (retval == null) {
	    LOGGER.warn("No Vendor with id '" + id + "'. We know " + vendorID2vendor.size() + " vendors.");
	}
	return retval;
    }

    @Override
    public Collection<GnucashVendor> getVendorsByName(final String name) {
	return getVendorsByName(name, true);
    }

    @Override
    public Collection<GnucashVendor> getVendorsByName(final String expr, final boolean relaxed) {
	if (vendorID2vendor == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	Collection<GnucashVendor> result = new ArrayList<GnucashVendor>();
	
	for ( GnucashVendor vend : getVendors() ) {
	    if ( relaxed ) {
		if ( vend.getName().trim().toLowerCase().
			contains(expr.trim().toLowerCase()) ) {
		    result.add(vend);
		}
	    } else {
		if ( vend.getName().equals(expr) ) {
		    result.add(vend);
		}
	    }
	}
	
	return result;
    }

    @Override
    public GnucashVendor getVendorByNameUniq(final String name) throws NoEntryFoundException, TooManyEntriesFoundException {
	Collection<GnucashVendor> vendList = getVendorsByName(name);
	if ( vendList.size() == 0 )
	    throw new NoEntryFoundException();
	else if ( vendList.size() > 1 )
	    throw new TooManyEntriesFoundException();
	else
	    return vendList.iterator().next();
    }
    
    @Override
    public Collection<GnucashVendor> getVendors() {
	if (vendorID2vendor == null) {
	    throw new IllegalStateException("no root-element loaded");
	}
	
	return vendorID2vendor.values();
    }

    // ---------------------------------------------------------------

    @Override
    public GnucashCommodity getCommodityByQualifID(final GCshCmdtyCurrID qualifID) {
	return getCommodityByQualifID(qualifID.toString());
    }

    @Override
    public GnucashCommodity getCommodityByQualifID(final String nameSpace, final String id) {
	return getCommodityByQualifID(nameSpace + GCshCmdtyCurrID.SEPARATOR + id);
    }

    @Override
    public GnucashCommodity getCommodityByQualifID(final GCshCmdtyCurrNameSpace.Exchange exchange, String id) {
	return getCommodityByQualifID(exchange.toString() + GCshCmdtyCurrID.SEPARATOR + id);
    }

    @Override
    public GnucashCommodity getCommodityByQualifID(final String qualifID) {
	if (qualifID == null) {
	    throw new IllegalStateException("null string given");
	}

	if (qualifID.trim().equals("")) {
	    throw new IllegalStateException("Search string is empty");
	}

	if (cmdtyQualifID2Cmdty == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashCommodity retval = cmdtyQualifID2Cmdty.get(qualifID.trim());
	if (retval == null) {
	    LOGGER.warn("No Commodity with qualified id '" + qualifID + "'. We know " + cmdtyQualifID2Cmdty.size()
		    + " commodities.");
	}
	
	return retval;
    }

    @Override
    public GnucashCommodity getCommodityByXCode(final String xCode) {
	if ( cmdtyQualifID2Cmdty == null ||
             cmdtyXCode2QualifID == null ) {
	    throw new IllegalStateException("no root-element(s) loaded");
	}

	if ( cmdtyQualifID2Cmdty.size() != cmdtyXCode2QualifID.size() ) {
	    // CAUTION: Don't throw an exception, at least not in all cases,
	    // because this is not necessarily an error: Only if the GnuCash
	    // file does not contain quotes for foreign currencies (i.e. currency-
	    // commodities but only security-commodities is this an error.
	    // throw new IllegalStateException("Sizes of root elements are not equal");
	    LOGGER.debug("getCommodityByXCode: Sizes of root elements are not equal.");
	}
	
	String qualifIDStr = cmdtyXCode2QualifID.get(xCode);
	if (qualifIDStr == null) {
	    LOGGER.warn("No Commodity with X-Code '" + xCode + "'. We know " + cmdtyXCode2QualifID.size() + " commodities in map 2.");
	}
	
	GnucashCommodity retval = cmdtyQualifID2Cmdty.get(qualifIDStr);
	if (retval == null) {
	    LOGGER.warn("No Commodity with qualified ID '" + qualifIDStr + "'. We know " + cmdtyQualifID2Cmdty.size() + " commodities in map 1.");
	}
	
	return retval;
    }

    @Override
    public Collection<GnucashCommodity> getCommoditiesByName(final String expr) {
	return getCommoditiesByName(expr, true);
    }
    
    @Override
    public Collection<GnucashCommodity> getCommoditiesByName(final String expr, final boolean relaxed) {
	if (cmdtyQualifID2Cmdty == null) {
	    throw new IllegalStateException("no root-element loaded");
	}
	
	Collection<GnucashCommodity> result = new ArrayList<GnucashCommodity>();

	for ( GnucashCommodity cmdty : getCommodities() ) {
	    if ( cmdty.getName() != null ) // yes, that can actually happen! 
	    {
		if ( relaxed ) {
		    if ( cmdty.getName().trim().toLowerCase().
			    contains(expr.trim().toLowerCase()) ) {
			result.add(cmdty);
		    }
		} else {
		    if ( cmdty.getName().equals(expr) ) {
			result.add(cmdty);
		    }
		}
	    }
	}
	
	return result;
    }

    @Override
    public GnucashCommodity getCommodityByNameUniq(final String expr) throws NoEntryFoundException, TooManyEntriesFoundException {
	Collection<GnucashCommodity> cmdtyList = getCommoditiesByName(expr, false);
	if ( cmdtyList.size() == 0 )
	    throw new NoEntryFoundException();
	else if ( cmdtyList.size() > 1 )
	    throw new TooManyEntriesFoundException();
	else
	    return cmdtyList.iterator().next();
    }

    @Override
    public Collection<GnucashCommodity> getCommodities() {
	if (cmdtyQualifID2Cmdty == null) {
	    throw new IllegalStateException("no root-element loaded");
	}
	
	return cmdtyQualifID2Cmdty.values();
    }

    // ---------------------------------------------------------------

    /**
     * @param customer the customer to look for.
     * @return all jobs that have this customer, never null
     */
    public Collection<GnucashCustomerJob> getJobsByCustomer(final GnucashCustomer customer) {
	if (jobID2job == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	Collection<GnucashCustomerJob> retval = new LinkedList<GnucashCustomerJob>();

	for (Object element : jobID2job.values()) {
	    GnucashGenerJob job = (GnucashGenerJob) element;
	    if (job.getOwnerId().equals(customer.getId())) {
		retval.add((GnucashCustomerJob) job);
	    }
	}
	return retval;
    }

    /**
     * @param vendor the customer to look for.
     * @return all jobs that have this customer, never null
     */
    public Collection<GnucashVendorJob> getJobsByVendor(final GnucashVendor vendor) {
	if (jobID2job == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	Collection<GnucashVendorJob> retval = new LinkedList<GnucashVendorJob>();

	for (Object element : jobID2job.values()) {
	    GnucashGenerJob job = (GnucashGenerJob) element;
	    if (job.getOwnerId().equals(vendor.getId())) {
		retval.add((GnucashVendorJob) job);
	    }
	}
	return retval;
    }

    /**
     * @see GnucashFile#getGenerJobByID(java.lang.String)
     */
    @Override
    public GnucashGenerJob getGenerJobByID(final String id) {
	if (jobID2job == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashGenerJob retval = jobID2job.get(id);
	if (retval == null) {
	    LOGGER.warn("No Job with id '" + id + "'. We know " + jobID2job.size() + " jobs.");
	}

	return retval;
    }

    @Override
    public Collection<GnucashGenerJob> getGenerJobsByName(String name) {
	return getGenerJobsByName(name, true);
    }
    
    @Override
    public Collection<GnucashGenerJob> getGenerJobsByName(final String expr, final boolean relaxed) {
	if (jobID2job == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	Collection<GnucashGenerJob> result = new ArrayList<GnucashGenerJob>();
	
	for ( GnucashGenerJob job : jobID2job.values() ) {
	    if ( relaxed ) {
		if ( job.getName().trim().toLowerCase().
			contains(expr.trim().toLowerCase()) ) {
		    result.add(job);
		}
	    } else {
		if ( job.getName().equals(expr) ) {
		    result.add(job);
		}
	    }
	}

	return result;
    }
    
    @Override
    public GnucashGenerJob getGenerJobByNameUniq(final String name) throws NoEntryFoundException, TooManyEntriesFoundException {
	Collection<GnucashGenerJob> jobList = getGenerJobsByName(name, false);
	if ( jobList.size() == 0 )
	    throw new NoEntryFoundException();
	else if ( jobList.size() > 1 )
	    throw new TooManyEntriesFoundException();
	else
	    return jobList.iterator().next();
    }
    
    /**
     * @see GnucashFile#getGenerJobs()
     */
    public Collection<GnucashGenerJob> getGenerJobs() {
	if (jobID2job == null) {
	    throw new IllegalStateException("no root-element loaded");
	}
	return jobID2job.values();
    }

    /**
     * @see GnucashFile#getTransactionByID(java.lang.String)
     */
    public GnucashTransaction getTransactionByID(final String id) {
	if (transactionID2transaction == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashTransaction retval = transactionID2transaction.get(id);
	if (retval == null) {
	    LOGGER.warn("No Transaction with id '" + id + "'. We know " + transactionID2transaction.size()
		    + " transactions.");
	}
	return retval;
    }

    /**
     * @see GnucashFile#getTransactionByID(java.lang.String)
     */
    public GnucashTransactionSplit getTransactionSplitByID(final String id) {
	if (transactionSplitID2transactionSplit == null) {
	    throw new IllegalStateException("no root-element loaded");
	}

	GnucashTransactionSplit retval = transactionSplitID2transactionSplit.get(id);
	if (retval == null) {
	    LOGGER.warn("No Transaction-Split with id '" + id + "'. We know "
		    + transactionSplitID2transactionSplit.size() + " transactions.");
	}
	return retval;
    }

    /**
     * @see GnucashFile#getTransactions()
     */
    public Collection<? extends GnucashTransaction> getTransactions() {
	if (transactionID2transaction == null) {
	    throw new IllegalStateException("no root-element loaded");
	}
	return Collections.unmodifiableCollection(transactionID2transaction.values());
    }

    /**
     * replaces ':' in tag-names and attribute-names by '_' .
     */
    public static class NamespaceRemovererReader extends Reader {

	/**
	 * How much we have reat.
	 */
	private long position = 0;

	/**
	 * @return How much we have reat.
	 */
	public long getPosition() {
	    return position;
	}

	/**
	 * @param pInput what to read from.
	 */
	public NamespaceRemovererReader(final Reader pInput) {
	    super();
	    input = pInput;
	}

	/**
	 * @return What to read from.
	 */
	public Reader getInput() {
	    return input;
	}

	/**
	 * @param newInput What to read from.
	 */
	public void setInput(final Reader newInput) {
	    if (newInput == null) {
		throw new IllegalArgumentException("null not allowed for field this.input");
	    }

	    input = newInput;
	}

	/**
	 * What to read from.
	 */
	private Reader input;

	/**
	 * true if we are in a quotation and thus shall not remove any namespaces.
	 */
	private boolean isInQuotation = false;

	/**
	 * true if we are in a quotation and thus shall remove any namespaces.
	 */
	private boolean isInTag = false;

	/**
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
	    input.close();
	}

	/**
	 * For debugging.
	 */
	public char[] debugLastTeat = new char[255];

	/**
	 * For debugging.
	 */
	public int debugLastReatLength = -1;

	/**
	 * Log the last chunk of bytes reat for debugging-purposes.
	 *
	 * @param cbuf the data
	 * @param off  where to start in cbuf
	 * @param reat how much
	 */
	private void logReatBytes(final char[] cbuf, final int off, final int reat) {
	    debugLastReatLength = Math.min(debugLastTeat.length, reat);
	    try {
		System.arraycopy(cbuf, off, debugLastTeat, 0, debugLastTeat.length);
	    } catch (Exception e) {
		e.printStackTrace();
		LOGGER.debug("debugLastReatLength=" + debugLastReatLength + "\n" + "off=" + off + "\n" + "reat=" + reat
			+ "\n" + "cbuf.length=" + cbuf.length + "\n" + "debugLastTeat.length=" + debugLastTeat.length
			+ "\n");
	    }
	}

	/**
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {

	    int reat = input.read(cbuf, off, len);

	    logReatBytes(cbuf, off, reat);

	    for (int i = off; i < off + reat; i++) {
		position++;

		if (isInTag && (cbuf[i] == '"' || cbuf[i] == '\'')) {
		    toggleIsInQuotation();
		} else if (cbuf[i] == '<' && !isInQuotation) {
		    isInTag = true;
		} else if (cbuf[i] == '>' && !isInQuotation) {
		    isInTag = false;
		} else if (cbuf[i] == ':' && isInTag && !isInQuotation) {
		    cbuf[i] = '_';
		}

	    }

	    return reat;
	}

	/**
	 *
	 */
	private void toggleIsInQuotation() {
	    if (isInQuotation) {
		isInQuotation = false;
	    } else {
		isInQuotation = true;
	    }
	}
    }

    /**
     * replaces &#164; by the euro-sign .
     */
    public static class EuroConverterReader extends Reader {

	/**
	 * This is "&#164;".length .
	 */
	private static final int REPLACESTRINGLENGTH = 5;

	/**
	 * @param pInput Where to read from.
	 */
	public EuroConverterReader(final Reader pInput) {
	    super();
	    input = pInput;
	}

	/**
	 * @return Where to read from.
	 */
	public Reader getInput() {
	    return input;
	}

	/**
	 * @param newInput Where to read from.
	 */
	public void setInput(Reader newInput) {
	    if (newInput == null) {
		throw new IllegalArgumentException("null not allowed for field this.input");
	    }

	    input = newInput;
	}

	/**
	 * Where to read from.
	 */
	private Reader input;

	/**
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
	    input.close();

	}

	/**
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {

	    int reat = input.read(cbuf, off, len);

	    // this does not work if the euro-sign is wrapped around the
	    // edge of 2 read-call buffers

	    int state = 0;

	    for (int i = off; i < off + reat; i++) {

		switch (state) {

		case 0: {
		    if (cbuf[i] == '&') {
			state++;
		    }
		    break;
		}

		case 1: {
		    if (cbuf[i] == '#') {
			state++;
		    } else {
			state = 0;
		    }
		    break;
		}

		case 2: {
		    if (cbuf[i] == '1') {
			state++;
		    } else {
			state = 0;
		    }
		    break;
		}

		case REPLACESTRINGLENGTH - 2: {
		    if (cbuf[i] == '6') {
			state++;
		    } else {
			state = 0;
		    }
		    break;
		}

		case REPLACESTRINGLENGTH - 1: {
		    if (cbuf[i] == '4') {
			state++;
		    } else {
			state = 0;
		    }
		    break;
		}
		case REPLACESTRINGLENGTH: {
		    if (cbuf[i] == ';') {
			// found it!!!
			cbuf[i - REPLACESTRINGLENGTH] = '�';
			if (i != reat - 1) {
			    System.arraycopy(cbuf, (i + 1), cbuf, (i - (REPLACESTRINGLENGTH - 1)), (reat - i - 1));
			}
			int reat2 = input.read(cbuf, reat - REPLACESTRINGLENGTH, REPLACESTRINGLENGTH);
			if (reat2 != REPLACESTRINGLENGTH) {
			    reat -= (REPLACESTRINGLENGTH - reat2);
			}
			i -= (REPLACESTRINGLENGTH - 1);
			state = 0;
		    } else {
			state = 0;
		    }
		    break;
		}

		default:
		}

	    }
	    return reat;
	}

	;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GnucashFile getGnucashFile() {
	return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserDefinedAttribute(final String aName) {
	return myGnucashObject.getUserDefinedAttribute(aName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getUserDefinedAttributeKeys() {
	return myGnucashObject.getUserDefinedAttributeKeys();
    }

    // ---------------------------------------------------------------
    // Statistics (for test purposes)

    @Override
    public int getNofEntriesAccountMap() {
	return accountID2account.size();
    }

    @Override
    public int getNofEntriesTransactionMap() {
	return transactionID2transaction.size();
    }

    @Override
    public int getNofEntriesTransactionSplitsMap() {
	return transactionSplitID2transactionSplit.size();
    }

    @Override
    public int getNofEntriesGenerInvoiceMap() {
	return invoiceID2invoice.size();
    }

    @Override
    public int getNofEntriesGenerInvoiceEntriesMap() {
	return invoiceEntryID2invoiceEntry.size();
    }

    @Override
    public int getNofEntriesGenerJobMap() {
	return jobID2job.size();
    }

    @Override
    public int getNofEntriesCustomerMap() {
	return customerID2customer.size();
    }

    @Override
    public int getNofEntriesVendorMap() {
	return vendorID2vendor.size();
    }

    @Override
    public int getNofEntriesCommodityMap() {
    return cmdtyQualifID2Cmdty.size();
    }

    // ---------------------------------------------------------------
    // In this section, we assume that all customer, vendor and job numbers
    // (internally, the IDs, not the GUIDs) are purely numeric, resp. (as
    // automatically generated by default).
    // CAUTION:
    // For customers and vendors, this may typically be usual and effective.
    // For jobs, however, things are typically different, so think twice
    // before using the job-methods!

    /**
     * Assuming that all customer numbers (manually set IDs, not GUIDs) are numeric
     * as generated by default.
     * 
     * @param gcshFile
     * @return
     */
    public int getHighestCustomerNumber() {
	int highest = -1;

	for (GnucashCustomer cust : customerID2customer.values()) {
	    try {
		int newNum = Integer.parseInt(cust.getNumber());
		if (newNum > highest)
		    highest = newNum;
	    } catch (Exception exc) {
		// We run into this exception even when we stick to the
		// automatically generated numbers, because this API's
		// createWritableCustomer() method at first generates
		// an object whose number is equal to its GUID.
		// ==> ::TODO Adapt how a customer object is created.
		LOGGER.warn("getHighestCustomerNumber: Found customer with non-numerical number");
	    }
	}

	return highest;
    }

    /**
     * Assuming that all vendor numbers (manually set IDs, not GUIDs) are numeric as
     * generated by default.
     * 
     * @param gcshFile
     * @return
     */
    public int getHighestVendorNumber() {
	int highest = -1;

	for (GnucashVendor vend : vendorID2vendor.values()) {
	    try {
		int newNum = Integer.parseInt(vend.getNumber());
		if (newNum > highest)
		    highest = newNum;
	    } catch (Exception exc) {
		// Cf. .getHighestCustomerNumber() above.
		// ==> ::TODO Adapt how a vendor object is created.
		LOGGER.warn("getHighestVendorNumber: Found vendor with non-numerical number");
	    }
	}

	return highest;
    }

    /**
     * Assuming that all job numbers (manually set IDs, not GUIDs) are numeric as
     * generated by default.
     * 
     * CAUTION: As opposed to customers and vendors, it may not be a good idea to
     * actually have the job numbers generated automatically.
     * 
     * @param gcshFile
     * @return
     */
    public int getHighestJobNumber() {
	int highest = -1;

	for (GnucashGenerJob job : jobID2job.values()) {
	    try {
		int newNum = Integer.parseInt(job.getNumber());
		if (newNum > highest)
		    highest = newNum;
	    } catch (Exception exc) {
		// We run into this exception even when we stick to the
		// automatically generated numbers, because this API's
		// createWritableCustomer() method at first generates
		// an object whose number is equal to its GUID.
		// ==> ::TODO Adapt how a customer object is created.
		LOGGER.warn("getHighestJobNumber: Found job with non-numerical number");
	    }
	}

	return highest;
    }

    // ----------------------------

    /**
     * Assuming that all customer numbers (manually set IDs, not GUIDs) are numeric
     * as generated by default.
     * 
     * @param gcshFile
     * @return
     */
    public String getNewCustomerNumber() {
	int newNo = getHighestCustomerNumber() + 1;
	String newNoStr = Integer.toString(newNo);
	String newNoStrPadded = PADDING_TEMPLATE + newNoStr;
	// 10 zeroes if you need a string of length 10 in the end
	newNoStrPadded = newNoStrPadded.substring(newNoStr.length());

	return newNoStrPadded;
    }

    /**
     * Assuming that all customer numbers (manually set IDs, not GUIDs) are numeric
     * as generated by default.
     * 
     * @param gcshFile
     * @return
     */
    public String getNewVendorNumber() {
	int newNo = getHighestVendorNumber() + 1;
	String newNoStr = Integer.toString(newNo);
	String newNoStrPadded = PADDING_TEMPLATE + newNoStr;
	// 10 zeroes if you need a string of length 10 in the end
	newNoStrPadded = newNoStrPadded.substring(newNoStr.length());

	return newNoStrPadded;
    }

    /**
     * Assuming that all job numbers (manually set IDs, not GUIDs) are numeric as
     * generated by default.
     * 
     * CAUTION: As opposed to customers and vendors, it may not be a good idea to
     * actually have the job numbers generated automatically.
     * 
     * @param gcshFile
     * @return
     */
    public String getNewJobNumber() {
	int newNo = getHighestJobNumber() + 1;
	String newNoStr = Integer.toString(newNo);
	String newNoStrPadded = PADDING_TEMPLATE + newNoStr;
	// 10 zeroes if you need a string of length 10 in the end
	newNoStrPadded = newNoStrPadded.substring(newNoStr.length());

	return newNoStrPadded;
    }

}
