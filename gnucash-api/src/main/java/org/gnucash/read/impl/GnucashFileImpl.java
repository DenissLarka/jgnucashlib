/**
 * GnucashFileImpl.java
 * License: GPLv3 or later
 * Created on 13.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 13.05.2005 - initial version
 * 11.11.2008 - using defaultCurrency from Gnucash-file
 * 03.01.2010 - support GNCVendor
 * ...
 */
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

import org.gnucash.currency.ComplexCurrencyTable;
import org.gnucash.generated.GncAccount;
import org.gnucash.generated.GncBudget;
import org.gnucash.generated.GncCountData;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustVendInvoiceEntry;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.GnucashTaxTable;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.spec.GnucashCustomerInvoiceImpl;
import org.gnucash.read.impl.spec.GnucashCustomerJobImpl;
import org.gnucash.read.impl.spec.GnucashVendorBillImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerJob;
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
 * created: 13.05.2005<br/>
 * <br/>
 * Implementation of GnucashFile that can only
 * read but not modify Gnucash-Files. <br/>
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 * @see GnucashFile
 */
public class GnucashFileImpl implements GnucashFile {

  /**
	 * Our logger for debug- and error-ourput.
	 */
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
	protected Map<String, GnucashTaxTable> taxTablesById = null;

	/**
	 * @param id id of a taxtable
	 * @return the identified taxtable or null
	 */
	public GnucashTaxTable getTaxTableByID(final String id) {
		if (taxTablesById == null) {
			getTaxTables();
		}
		return taxTablesById.get(id);
	}

	/**
	 * @return all TaxTables defined in the book
	 * @link GnucashTaxTable
	 */
	@SuppressWarnings("unchecked")
	public Collection<GnucashTaxTable> getTaxTables() {
		if (taxTablesById == null) {

			taxTablesById = new HashMap<String, GnucashTaxTable>();

			List bookElements = this.getRootElement().getGncBook().getBookElements();
			for (Object bookElement : bookElements) {
				if (!(bookElement instanceof GncV2.GncBook.GncGncTaxTable)) {
					continue;
				}
				GncV2.GncBook.GncGncTaxTable jwsdpPeer = (GncV2.GncBook.GncGncTaxTable) bookElement;
				GnucashTaxTableImpl gnucashTaxTable = new GnucashTaxTableImpl(jwsdpPeer, this);
				taxTablesById.put(gnucashTaxTable.getId(), gnucashTaxTable);
			}
		}

		return taxTablesById.values();
	}

    // ----------------------------

//    /**
//     * Filles lazy in getVendorTerms() .
//     *
//     * @see #getVendorTerms()
//     */
//    protected Map<String, GnucashVendorTerms> vendorTermsByID = null;
//
//    /**
//     * @param id id of a vendor terms item
//     * @return the identified vendor terms item or null
//     */
//    public GnucashVendorTerms getVendorTermsByID(final String id) {
//        if (vendorTermsByVendID == null) {
//            getVendorTerms();
//        }
//        
//        return vendorTermsByVendID.get(id);
//    }
//
//    /**
//     * @return all TaxTables defined in the book
//     * @link GnucashTaxTable
//     */
//    @SuppressWarnings("unchecked")
//    public Collection<GnucashVendorTerms> getVendorTerms() {
//        if (vendorTermsByVendID == null) {
//
//          vendorTermsByVendID = new HashMap<String, GnucashVendorTerms>();
//
//            List bookElements = this.getRootElement().getGncBook().getBookElements();
//            for (Object bookElement : bookElements) {
//                if (!(bookElement instanceof GncV2.GncBook.GncGncVendor.VendorTerms)) {
//                    continue;
//                }
//                GncV2.GncBook.GncGncVendor.VendorTerms jwsdpPeer = (GncV2.GncBook.GncGncVendor.VendorTerms) bookElement;
//                GnucashVendorTermsImpl gnucashVendorTerms = new GnucashVendorTermsImpl(jwsdpPeer, this);
//                vendorTermsByVendID.put(gnucashVendorTerms.get, gnucashVendorTerms);
//            }
//        }
//
//        return vendorTermsByVendID.values();
//    }

    // ---------------------------------------------------------------

	/**
	 * @return a read-only collection of all accounts that have no parent (the
	 * result is sorted)
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
		}
		catch (RuntimeException e) {
			LOGGER.error("Problem getting all root-account", e);
			throw e;
		}
		catch (Throwable e) {
			LOGGER.error("SERIOUS Problem getting all root-account", e);
			return new LinkedList<GnucashAccount>();
		}
	}

	/**
	 * @param id if null, gives all account that have no parent
	 * @return the sorted collection of children of that account
	 */
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

	/**
	 * @see GnucashFile#getAccountByName(java.lang.String)
	 */
	public GnucashAccount getAccountByName(final String name) {

		if (accountID2account == null) {
			throw new IllegalStateException("no root-element loaded");
		}

		for (GnucashAccount account : accountID2account.values()) {
			if (account.getName().equals(name)) {
				return account;
			}
			if (account.getQualifiedName().equals(name)) {
				return account;
			}
		}
		
		return null;
	}

	/**
	 * warning: this function has to traverse all
	 * accounts. If it much faster to try
	 * getAccountByID first and only call this method
	 * if the returned account does not have the right name.
	 *
	 * @param nameRegEx the regular expression of the name to look for
	 * @return null if not found
	 * @see #getAccountByID(String)
	 * @see #getAccountByName(String)
	 */
	public GnucashAccount getAccountByNameEx(final String nameRegEx) {

		if (accountID2account == null) {
			throw new IllegalStateException("no root-element loaded");
		}

		GnucashAccount foundAccount = getAccountByName(nameRegEx);
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
	 * First try to fetch the account by id, then
	 * fall back to traversing all accounts to get
	 * if by it's name.
	 *
	 * @param id   the id to look for
	 * @param name the name to look for if nothing is found for the id
	 * @return null if not found
	 * @see #getAccountByID(String)
	 * @see #getAccountByName(String)
	 */
	public GnucashAccount getAccountByIDorName(final String id, final String name) {
		GnucashAccount retval = getAccountByID(id);
		if (retval == null) {
			retval = getAccountByName(name);
		}
		
		return retval;
	}

	/**
	 * First try to fetch the account by id, then
	 * fall back to traversing all accounts to get
	 * if by it's name.
	 *
	 * @param id   the id to look for
	 * @param name the regular expression of the name to look for
	 *             if nothing is found for the id
	 * @return null if not found
	 * @see #getAccountByID(String)
	 * @see #getAccountByName(String)
	 */
	public GnucashAccount getAccountByIDorNameEx(final String id, final String name) {
		GnucashAccount retval = getAccountByID(id);
		if (retval == null) {
			retval = getAccountByNameEx(name);
		}
		
		return retval;
	}

	/**
	 * @see GnucashFile#getCustVendInvoiceByID(java.lang.String)
	 */
	public GnucashCustVendInvoice getCustVendInvoiceByID(final String id) {
		return invoiceID2invoice.get(id);
	}

	/**
	 * @see GnucashFile#getInvoices()
	 */
	@SuppressWarnings("unchecked")
	public Collection<GnucashCustVendInvoice> getInvoices() {

		Collection<GnucashCustVendInvoice> c = invoiceID2invoice.values();

		ArrayList<GnucashCustVendInvoice> retval = new ArrayList<GnucashCustVendInvoice>(c);
		Collections.sort(retval);

		return retval;
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @see GnucashFile#getUnpayedInvoices()
	 */
	public Collection<GnucashCustVendInvoice> getUnpayedInvoices() throws WrongInvoiceTypeException {
		Collection<GnucashCustVendInvoice> retval = new LinkedList<GnucashCustVendInvoice>();
		for (GnucashCustVendInvoice invoice : getInvoices()) {
			if (invoice.getInvcAmmountUnPayedWithTaxes().isPositive()) {
				retval.add(invoice);
			}
		}
		
		return retval;
	}

    /**
     * @throws WrongInvoiceTypeException 
     * @see GnucashFile#getUnpayedInvoicesForCustomer_direct(GnucashCustomer)
     */
    public Collection<GnucashCustomerInvoice> getUnpayedInvoicesForCustomer_direct(final GnucashCustomer customer) throws WrongInvoiceTypeException {
        Collection<GnucashCustomerInvoice> retval = new LinkedList<GnucashCustomerInvoice>();
        for (GnucashCustVendInvoice invoice : getUnpayedInvoices()) {
            if (invoice.getOwnerId(GnucashCustVendInvoice.ReadVariant.DIRECT).equals(customer.getId())) {
                try {
                  retval.add(new GnucashCustomerInvoiceImpl(invoice));
                }
                catch (WrongInvoiceTypeException e) {
                  // This really should not happen, one can almost
                  // throw a fatal log here.
                  LOGGER.error("getUnpayedInvoicesForCustomer_direct: Cannot instantiate GnucashCustomerInvoiceImpl");
                }
            }
        }
        
        return retval;
    }

	/**
	 * @throws WrongInvoiceTypeException 
	 * @see GnucashFile#getUnpayedInvoicesForCustomer_viaJob(GnucashCustomer)
	 */
	public Collection<GnucashCustomerInvoice> getUnpayedInvoicesForCustomer_viaJob(final GnucashCustomer customer) throws WrongInvoiceTypeException {
		Collection<GnucashCustomerInvoice> retval = new LinkedList<GnucashCustomerInvoice>();
        for (GnucashCustVendInvoice invoice : getUnpayedInvoices())
        {
          if (invoice.getJob().getOwnerId().equals(customer.getId())) {
            try {
              retval.add(new GnucashCustomerInvoiceImpl(invoice));
            }
            catch (WrongInvoiceTypeException e) {
              // This really should not happen, one can almost
              // throw a fatal log here.
              LOGGER.error("getUnpayedInvoicesForCustomer_viaJob: Cannot instantiate GnucashCustomerInvoiceImpl");
            }
          }
        }
		
		return retval;
	}

    /**
     * @throws WrongInvoiceTypeException 
     * @see GnucashFile#getUnpayedInvoicesForVendor_viaJob(GnucashVendor)
     */
    public Collection<GnucashVendorBill> getUnpayedInvoicesForVendor_direct(final GnucashVendor vendor) throws WrongInvoiceTypeException {
        Collection<GnucashVendorBill> retval = new LinkedList<GnucashVendorBill>();
        for (GnucashCustVendInvoice invoice : getUnpayedInvoices()) {
            if (invoice.getOwnerId(GnucashCustVendInvoice.ReadVariant.DIRECT).equals(vendor.getId())) {
                try {
                  retval.add(new GnucashVendorBillImpl(invoice));
                }
                catch (WrongInvoiceTypeException e) {
                  // This really should not happen, one can almost
                  // throw a fatal log here.
                  LOGGER.error("getUnpayedInvoicesForVendor_direct: Cannot instantiate GnucashCustomerInvoiceImpl");
                }
            }
        }
        
        return retval;
    }

    /**
     * @throws WrongInvoiceTypeException 
     * @see GnucashFile#getUnpayedInvoicesForVendor_viaJob(GnucashVendor)
     */
    public Collection<GnucashVendorBill> getUnpayedInvoicesForVendor_viaJob(final GnucashVendor vendor) throws WrongInvoiceTypeException {
        Collection<GnucashVendorBill> retval = new LinkedList<GnucashVendorBill>();
        for (GnucashCustVendInvoice invoice : getUnpayedInvoices()) {
            if (invoice.getJob().getOwnerId().equals(vendor.getId())) {
                try {
                  retval.add(new GnucashVendorBillImpl(invoice));
                }
                catch (WrongInvoiceTypeException e) {
                  // This really should not happen, one can almost
                  // throw a fatal log here.
                  LOGGER.error("getUnpayedInvoicesForVendor_viaJob: Cannot instantiate GnucashCustomerInvoiceImpl");
                }
            }
        }
        
        return retval;
    }

	/**
	 * @throws WrongInvoiceTypeException 
	 * @see GnucashFile#getPayedInvoices()
	 */
	public Collection<GnucashCustVendInvoice> getPayedInvoices() throws WrongInvoiceTypeException {
		Collection<GnucashCustVendInvoice> retval = new LinkedList<GnucashCustVendInvoice>();
		for (GnucashCustVendInvoice invoice : getInvoices()) {
			if (!invoice.getInvcAmmountUnPayedWithTaxes().isPositive()) {
				retval.add(invoice);
			}
		}
		
		return retval;
	}

    /**
     * @see GnucashFile#getCustVendInvoiceByID(java.lang.String)
     */
    public GnucashCustVendInvoiceEntry getInvoiceEntryByID(final String id) {
        return invoiceEntryID2invoiceEntry.get(id);
    }

    /**
     * @see GnucashFile#getInvoices()
     */
    @SuppressWarnings("unchecked")
    public Collection<GnucashCustVendInvoiceEntry> getInvoiceEntries() {

        Collection<GnucashCustVendInvoiceEntry> c = invoiceEntryID2invoiceEntry.values();

        ArrayList<GnucashCustVendInvoiceEntry> retval = new ArrayList<GnucashCustVendInvoiceEntry>(c);
        Collections.sort(retval);

        return retval;
    }

	/**
	 * @see #getGnucashFile()
	 */
	private File file;

	/**
	 * @param pCmdtySpace the namespace for pCmdtyId
	 * @param pCmdtyId    the currency-name
	 * @return the latest price-quote in the gnucash-file in EURO
	 * @see {@link GnucashFile#getLatestPrice(String, String)}
	 */
	public FixedPointNumber getLatestPrice(final String pCmdtySpace, final String pCmdtyId) {
		return getLatestPrice(pCmdtySpace, pCmdtyId, 0);
	}

	/**
	 * the top-level Element of the gnucash-files parsed and checked for
	 * validity by JAXB.
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
	 * All customer/vendor invoices indexed by their unique id-String.
	 *
	 * @see GnucashCustVendInvoice
	 * @see GnucashCustVendInvoiceImpl
	 */
	protected Map<String, GnucashCustVendInvoice> invoiceID2invoice;

    /**
     * All customer/vendor invoice entries indexed by their unique id-String.
     *
     * @see GnucashCustVendInvoiceEnctry
     * @see GnucashCustVendInvoiceEntryImpl
     */
    protected Map<String, GnucashCustVendInvoiceEntry> invoiceEntryID2invoiceEntry;

	/**
	 * All jobs indexed by their unique id-String.
	 *
	 * @see GnucashJob
	 * @see GnucashCustomerJobImpl
	 */
	protected Map<String, GnucashJob> jobid2job;

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
	 * Helper to implement the {@link GnucashObject}-interface
	 * without having the same code twice.
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
	@SuppressWarnings("unchecked")
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

		initInvoiceMap(pRootElement);
		
		// invoiceEntries refer to invoices, therefore they must be loaded after
		// them
		initInvoiceEntryMap(pRootElement);

		// transactions refer to invoices, therefore they must be loaded after
		// them
		initTransactionMap(pRootElement);

		initCustomerMap(pRootElement);

        initVendorMap(pRootElement);

		initJobMap(pRootElement);

		// check for unknown book-elements
		for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
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
				continue; //TODO: create a Java-Class for employees like we have for customers
			}
			throw new IllegalArgumentException("<gnc:book> contains unknown element [" + bookElement.getClass().getName() + "]");
		}
	}

  private void initAccountMap(final GncV2 pRootElement)
  {
    accountID2account = new HashMap<>();
    
    for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
      Object bookElement = iter.next();
      if (!(bookElement instanceof GncAccount)) {
        continue;
      }
      GncAccount jwsdpAccount = (GncAccount) bookElement;
      
      try {
        GnucashAccount account = createAccount(jwsdpAccount);
        accountID2account.put(account.getId(), account);
      }
      catch (RuntimeException e) {
        LOGGER.error("[RuntimeException] Problem in "
            + getClass().getName()
            + " ignoring illegal Account-Entry with id="
            + jwsdpAccount.getActId().getValue(),
            e);
      }
    }
  }

  private void initInvoiceMap(final GncV2 pRootElement)
  {
    invoiceID2invoice = new HashMap<>();
    
    for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
      Object bookElement = iter.next();
      if (!(bookElement instanceof GncV2.GncBook.GncGncInvoice)) {
        continue;
      }
      GncV2.GncBook.GncGncInvoice jwsdpInvoice = (GncV2.GncBook.GncGncInvoice) bookElement;
      
      try {
        GnucashCustVendInvoice invoice = createCustVendInvoice(jwsdpInvoice);
        invoiceID2invoice.put(invoice.getId(), invoice);
      }
      catch (RuntimeException e) {
        LOGGER.error("[RuntimeException] Problem in "
            + getClass().getName()
            + " ignoring illegal Customer/Vendor-Invoice-Entry with id="
            + jwsdpInvoice.getInvoiceId(),
            e);
      }
    }
  }

  private void initInvoiceEntryMap(final GncV2 pRootElement)
  {
    invoiceEntryID2invoiceEntry = new HashMap<>();
    
    for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
      Object bookElement = iter.next();
      if (!(bookElement instanceof GncV2.GncBook.GncGncEntry)) {
        continue;
      }
      GncV2.GncBook.GncGncEntry jwsdpInvcEntr = (GncV2.GncBook.GncGncEntry) bookElement;
      
      try {
        GnucashCustVendInvoiceEntry invcEntr = createCustVendInvoiceEntry(jwsdpInvcEntr);
        invoiceEntryID2invoiceEntry.put(invcEntr.getId(), invcEntr);
      }
      catch (RuntimeException e) {
        LOGGER.error("[RuntimeException] Problem in "
            + getClass().getName()
            + " ignoring illegal Customer/Vendor-Invoice-Entry-Entry with id="
            + jwsdpInvcEntr.getEntryGuid().getValue(), e);
      }
    }
  }

  private void initTransactionMap(final GncV2 pRootElement)
  {
    transactionID2transaction = new HashMap<>();
    
    for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
      Object bookElement = iter.next();
      if (!(bookElement instanceof GncTransaction)) {
        continue;
      }
      GncTransaction jwsdpTransaction = (GncTransaction) bookElement;

      try {
        GnucashTransactionImpl account = createTransaction(jwsdpTransaction);
        transactionID2transaction.put(account.getId(), account);
        for (GnucashTransactionSplit split : account.getSplits()) {
          /*String accountID = */
          split.getAccountID();
        }
      }
      catch (RuntimeException e) {
        LOGGER.error("[RuntimeException] Problem in "
            + getClass().getName()
            + " ignoring illegal Transaction-Entry with id="
            + jwsdpTransaction.getTrnId().getValue(),
            e);
      }
    }
  }

  private void initCustomerMap(final GncV2 pRootElement)
  {
    customerID2customer = new HashMap<>();
    
    for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
      Object bookElement = iter.next();
      if (!(bookElement instanceof GncV2.GncBook.GncGncCustomer)) {
        continue;
      }
      GncV2.GncBook.GncGncCustomer jwsdpCust = (GncV2.GncBook.GncGncCustomer) bookElement;
      
      try {
        GnucashCustomerImpl cust = createCustomer(jwsdpCust);
        customerID2customer.put(cust.getId(), cust);
      }
      catch (RuntimeException e) {
        LOGGER.error("[RuntimeException] Problem in "
            + getClass().getName()
            + " ignoring illegal Customer-Entry with id="
            + jwsdpCust.getCustId(),
            e);
      }
    }
  }

  private void initVendorMap(final GncV2 pRootElement)
  {
    vendorID2vendor = new HashMap<>();
    
    for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
      Object bookElement = iter.next();
      if (!(bookElement instanceof GncV2.GncBook.GncGncVendor)) {
        continue;
      }
      GncV2.GncBook.GncGncVendor jwsdpVend = (GncV2.GncBook.GncGncVendor) bookElement;

      try {
        GnucashVendorImpl vend = createVendor(jwsdpVend);
        vendorID2vendor.put(vend.getId(), vend);
      }
      catch (RuntimeException e) {
        LOGGER.error("[RuntimeException] Problem in "
            + getClass().getName()
            + " ignoring illegal Vendor-Entry with id="
            + jwsdpVend.getVendorId(),
            e);
      }
    }
  }
	
  private void initJobMap(final GncV2 pRootElement)
  {
    jobid2job = new HashMap<String, GnucashJob>();
    
    for (Iterator iter = pRootElement.getGncBook().getBookElements().iterator(); iter.hasNext(); ) {
      Object bookElement = iter.next();
      if (!(bookElement instanceof GncV2.GncBook.GncGncJob)) {
        continue;
      }
      GncV2.GncBook.GncGncJob jwsdpJob = (GncV2.GncBook.GncGncJob) bookElement;
      
      try {
        GnucashJobImpl job = createCustVendJob(jwsdpJob);
        String jobID = job.getId();
        if (jobID == null) {
          LOGGER.error("File contains a customer/vendor job w/o an ID. indexing it with the ID ''");
          jobID = "";
        }
        jobid2job.put(job.getId(), job);
      }
      catch (RuntimeException e) {
        LOGGER.error("[RuntimeException] Problem in "
            + getClass().getName()
            + " ignoring illegal Customer/Vendor-Job-Entry with id="
            + jwsdpJob.getJobId(),
            e);
      }
    }
  }

  // ---------------------------------------------------------------

  /**
	 * Use a heuristic to determine the  defaultcurrency-id.
	 * If we cannot find one, we default to EUR.<br/>
	 * Comodity-stace is fixed as "ISO4217" .
	 *
	 * @return the default-currencyID to use.
	 */
	@SuppressWarnings("unchecked")
	public String getDefaultCurrencyID() {
		GncV2 root = getRootElement();
		if (root == null) {
			return "EUR";
		}
		for (Iterator iter = getRootElement().getGncBook().getBookElements()
				.iterator(); iter.hasNext(); ) {
			Object bookElement = iter.next();
			if (!(bookElement instanceof GncAccount)) {
				continue;
			}
			GncAccount jwsdpAccount = (GncAccount) bookElement;
			if (jwsdpAccount.getActCommodity() != null
					&& jwsdpAccount.getActCommodity().getCmdtySpace().equals("ISO4217")) {
				return jwsdpAccount.getActCommodity().getCmdtyId();
			}
		}
		return "EUR";
	}

	/**
	 * @param pRootElement the root-element of the Gnucash-file
	 */
	@SuppressWarnings("unchecked")
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

				LOGGER.warn("We know only the format of the price-db 1, "
						+ "the file has version "
						+ priceDB.getVersion()
						+ " prices will not be loaded!");
			} else {
				getCurrencyTable().clear();
				getCurrencyTable().setConversionFactor("ISO4217",
						getDefaultCurrencyID(),
						new FixedPointNumber(1));

				for (Iterator<GncV2.GncBook.GncPricedb.Price> iter = priceDB.getPrice().iterator(); iter.hasNext(); ) {
					GncV2.GncBook.GncPricedb.Price price = iter.next();
					GncV2.GncBook.GncPricedb.Price.PriceCommodity comodity = price.getPriceCommodity();

					// check if we already have a latest price for this comodity
					// (=currency, fund, ...)
					if (getCurrencyTable().getConversionFactor(comodity.getCmdtySpace(), comodity.getCmdtyId()) != null) {
						continue;
					}

					String baseCurrency = getDefaultCurrencyID();
					if (comodity.getCmdtySpace().equals("ISO4217")
							&&
							comodity.getCmdtyId().equals(baseCurrency)) {
						LOGGER.warn("Ignoring price-quote for "
								+ baseCurrency + " because "
								+ baseCurrency + " is"
								+ "our base-currency.");
						continue;
					}

					// get the latest price in the file and insert it into
					// our currency table
					FixedPointNumber factor = getLatestPrice(
							comodity.getCmdtySpace(),
							comodity.getCmdtyId());

					if (factor != null) {
						getCurrencyTable().setConversionFactor(comodity.getCmdtySpace(),
								comodity.getCmdtyId(),
								factor);
					} else {
						LOGGER.warn("The gnucash-file defines a factor for a comodity '"
								+ comodity.getCmdtySpace()
								+ "' - '"
								+ comodity.getCmdtyId()
								+ "' but has no comodity for it");
					}
				}
			}
		}

		if (noPriceDB) {
			//case: no priceDB in file
			getCurrencyTable().clear();
		}
	}

	/**
	 * @see {@link #getLatestPrice(String, String)}
	 */
	protected static final DateFormat PRICE_QUOTE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

	/**
	 * @param pCmdtySpace the namespace for pCmdtyId
	 * @param pCmdtyId    the currency-name
	 * @param depth       used for recursion. Allways call with '0'
	 *                    for aborting recursive quotes (quotes to other then the base-
	 *                    currency) we abort if the depth reached 6.
	 * @return the latest price-quote in the gnucash-file in the default-currency
	 * @see {@link GnucashFile#getLatestPrice(String, String)}
	 * @see #getDefaultCurrencyID()
	 */
	@SuppressWarnings("unchecked")
	private FixedPointNumber getLatestPrice(final String pCmdtySpace,
			final String pCmdtyId,
			final int depth) {
		if (pCmdtySpace == null) {
			throw new IllegalArgumentException("null parameter 'pCmdtySpace' "
					+ "given");
		}
		if (pCmdtyId == null) {
			throw new IllegalArgumentException("null parameter 'pCmdtyId' "
					+ "given");
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
						LOGGER.warn("gnucash-file contains null price-quotes"
								+ " there may be a problem with JWSDP");
						continue;
					}
					if (priceQuote.getPriceCurrency() == null) {
						LOGGER.warn("gnucash-file contains price-quotes"
								+ " with no currency id='"
								+ priceQuote.getPriceId().getValue()
								+ "'");
						continue;
					}
					if (priceQuote.getPriceCurrency().getCmdtyId() == null) {
						LOGGER.warn("gnucash-file contains price-quotes"
								+ " with no currency-id id='"
								+ priceQuote.getPriceId().getValue()
								+ "'");
						continue;
					}
					if (priceQuote.getPriceCurrency().getCmdtySpace() == null) {
						LOGGER.warn("gnucash-file contains price-quotes"
								+ " with no currency-namespace id='"
								+ priceQuote.getPriceId().getValue()
								+ "'");
						continue;
					}
					if (priceQuote.getPriceTime() == null) {
						LOGGER.warn("gnucash-file contains price-quotes"
								+ " with no timestamp id='"
								+ priceQuote.getPriceId().getValue()
								+ "'");
						continue;
					}
					if (priceQuote.getPriceValue() == null) {
						LOGGER.warn("gnucash-file contains price-quotes"
								+ " with no value id='"
								+ priceQuote.getPriceId().getValue()
								+ "'");
						continue;
					}
					/*if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND")
							&&
                        priceQuote.getPriceType() == null) {
                        LOGGER.warn("gnucash-file contains FUND-price-quotes"
                                + " with no type id='"
                                + priceQuote.getPriceId().getValue()
                                + "'");
                        continue;
                    }*/
					if (!priceQuote.getPriceCommodity().getCmdtySpace().equals(pCmdtySpace)) {
						continue;
					}
					if (!priceQuote.getPriceCommodity().getCmdtyId().equals(pCmdtyId)) {
						continue;
					}
					/*if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND")
							&&
                        (priceQuote.getPriceType() == null
                            ||
                         !priceQuote.getPriceType().equals("last")
                       )) {
                        LOGGER.warn("ignoring FUND-price-quote of unknown type '"
                                  + priceQuote.getPriceType()
                                  + "' expecting 'last' ");
                        continue;
                    }*/

					if (!priceQuote.getPriceCurrency()
							.getCmdtySpace().equals("ISO4217")) {
						if (depth > maxRecursionDepth) {
							LOGGER.warn("ignoring price-quote that is not in an"
									+ " ISO4217 -currency but in '"
									+ priceQuote.getPriceCurrency().getCmdtyId());
							continue;
						}
						factor = getLatestPrice(priceQuote.getPriceCurrency()
								.getCmdtySpace(), priceQuote.getPriceCurrency()
								.getCmdtyId(), depth + 1);
					} else {
						if (!priceQuote.getPriceCurrency()
								.getCmdtyId().equals(getDefaultCurrencyID())) {
							if (depth > maxRecursionDepth) {
								LOGGER.warn("ignoring price-quote that is not in "
										+ getDefaultCurrencyID() + " "
										+ "but in  '"
										+ priceQuote.getPriceCurrency().getCmdtyId());
								continue;
							}
							factor = getLatestPrice(priceQuote.getPriceCurrency()
									.getCmdtySpace(), priceQuote.getPriceCurrency()
									.getCmdtyId(), depth + 1);
						}
					}

					Date date = PRICE_QUOTE_DATE_FORMAT.parse(
							priceQuote.getPriceTime().getTsDate());

					if (latestDate == null || latestDate.before(date)) {
						latestDate = date;
						latestQuote = new FixedPointNumber(
								priceQuote.getPriceValue());
						LOGGER.debug("getLatestPrice(pCmdtySpace='"
								+ pCmdtySpace
								+ "', String pCmdtyId='"
								+ pCmdtyId
								+ "') converted " + latestQuote
								+ " <= "
								+ priceQuote.getPriceValue());
					}

				}
				catch (NumberFormatException e) {
					LOGGER.error("[NumberFormatException] Problem in "
									+ getClass().getName()
									+ ".getLatestPrice(pCmdtySpace='"
									+ pCmdtySpace
									+ "', String pCmdtyId='"
									+ pCmdtyId
									+ "')! Ignoring a bad price-quote '"
									+ priceQuote
									+ "'",
							e);
				}
				catch (ParseException e) {
					LOGGER.error("[ParseException] Problem in "
									+ getClass().getName()
									+ ".getLatestPrice(pCmdtySpace='"
									+ pCmdtySpace
									+ "', String pCmdtyId='"
									+ pCmdtyId
									+ "')! Ignoring a bad price-quote '"
									+ priceQuote
									+ "'",
							e);
				}
				catch (NullPointerException e) {
					LOGGER.error("[NullPointerException] Problem in "
									+ getClass().getName()
									+ ".getLatestPrice(pCmdtySpace='"
									+ pCmdtySpace
									+ "', String pCmdtyId='"
									+ pCmdtyId
									+ "')! Ignoring a bad price-quote '"
									+ priceQuote
									+ "'",
							e);
				}
				catch (ArithmeticException e) {
					LOGGER.error("[ArithmeticException] Problem in "
									+ getClass().getName()
									+ ".getLatestPrice(pCmdtySpace='"
									+ pCmdtySpace
									+ "', String pCmdtyId='"
									+ pCmdtyId
									+ "')! Ignoring a bad price-quote '"
									+ priceQuote
									+ "'",
							e);
				}

			}
		}

		LOGGER.debug(getClass().getName()
				+ ".getLatestPrice(pCmdtySpace='"
				+ pCmdtySpace
				+ "', String pCmdtyId='"
				+ pCmdtyId
				+ "')= " + latestQuote
				+ " from "
				+ latestDate);

		if (latestQuote == null) {
			return null;
		}

		if ( factor == null ) {
		  factor = new FixedPointNumber(1);
		}

		return factor.multiply(latestQuote);
	}

    // ----------------------------

	/**
	 * @param jwsdpAccount the JWSDP-peer (parsed xml-element) to fill our object with
	 * @return the new GnucashAccount to wrap the given jaxb-object.
	 */
	protected GnucashAccount createAccount(final GncAccount jwsdpAccount) {
		GnucashAccount account = new GnucashAccountImpl(jwsdpAccount, this);
		return account;
	}

	/**
	 * @param jwsdpInvoice the JWSDP-peer (parsed xml-element) to fill our object with
	 * @return the new GnucashInvoice to wrap the given jaxb-object.
	 */
	protected GnucashCustVendInvoice createCustVendInvoice(
			final GncV2.GncBook.GncGncInvoice jwsdpInvoice) {
		GnucashCustVendInvoice invoice = new GnucashCustVendInvoiceImpl(jwsdpInvoice, this);
		return invoice;
	}

	/**
	 * @param jwsdpInvoiceEntry the JWSDP-peer (parsed xml-element) to fill our object with
	 * @return the new GnucashInvoiceEntry to wrap the given jaxb-object.
	 */
	protected GnucashCustVendInvoiceEntry createCustVendInvoiceEntry(
			final GncV2.GncBook.GncGncEntry jwsdpInvoiceEntry) {
		GnucashCustVendInvoiceEntry entry = new GnucashCustVendInvoiceEntryImpl(jwsdpInvoiceEntry, this);
		return entry;
	}

	/**
	 * @param jwsdpjob the JWSDP-peer (parsed xml-element) to fill our object with
	 * @return the new GnucashJob to wrap the given jaxb-object.
	 */
	protected GnucashJobImpl createCustVendJob(final GncV2.GncBook.GncGncJob jwsdpjob) {

		GnucashJobImpl job = new GnucashJobImpl(jwsdpjob, this);
		return job;
	}

	/**
	 * @param jwsdpCustomer the JWSDP-peer (parsed xml-element) to fill our object with
	 * @return the new GnucashCustomer to wrap the given JAXB object.
	 */
	protected GnucashCustomerImpl createCustomer(final GncV2.GncBook.GncGncCustomer jwsdpCustomer) {
		GnucashCustomerImpl customer = new GnucashCustomerImpl(jwsdpCustomer, this);
		return customer;
	}

    /**
     * @param jwsdpVendor the JWSDP-peer (parsed xml-element) to fill our object with
     * @return the new GnucashVendor to wrap the given JAXB object.
     */
    protected GnucashVendorImpl createVendor(final GncV2.GncBook.GncGncVendor jwsdpVendor) {
        GnucashVendorImpl vendor = new GnucashVendorImpl(jwsdpVendor, this);
        return vendor;
    }

	/**
	 * @param jwsdpTransaction the JWSDP-peer (parsed xml-element) to fill our object with
	 * @return the new GnucashTransaction to wrap the given jaxb-object.
	 */
	protected GnucashTransactionImpl createTransaction(final GncTransaction jwsdpTransaction) {
		GnucashTransactionImpl account = new GnucashTransactionImpl(jwsdpTransaction, this);
		return account;
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
			throw new IllegalArgumentException(
					"null not allowed for field this.file");
		}

		if (!pFile.exists()) {
			throw new IllegalArgumentException("Given file '"
					+ pFile.getAbsolutePath() + "' does not exist!");
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

  private void loadInputStream(InputStream in)
      throws UnsupportedEncodingException, IOException
  {
    long start = System.currentTimeMillis();

    NamespaceRemovererReader reader = new NamespaceRemovererReader(new InputStreamReader(in, "utf-8"));
		try {

			JAXBContext myContext = getJAXBContext();
			Unmarshaller unmarshaller = myContext.createUnmarshaller();

			GncV2 o = (GncV2) unmarshaller.unmarshal(new InputSource(new BufferedReader(reader)));
			long start2 = System.currentTimeMillis();
			setRootElement(o);
			long end = System.currentTimeMillis();
			LOGGER.info("GnucashFileImpl.loadFileInputStream took "
					+ (end - start) + " ms (total) "
					+ (start2 - start) + " ms (jaxb-loading)"
					+ (end - start2) + " ms (building facades)"
			);

		}
		catch (JAXBException e) {
			LOGGER.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
		finally {
			reader.close();
		}
  }

	/**
	 * @see #getObjectFactory()
	 */
	private volatile ObjectFactory myJAXBFactory;

	/**
	 * @return the jaxb object-factory used to create new peer-objects to extend
	 * this
	 */
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
			}
			catch (JAXBException e) {
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
		for (Iterator iter = getRootElement().getGncBook().getGncCountData()
				.iterator(); iter.hasNext(); ) {
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
			System.err.println("No Account with id '" + id + "'. We know "
					+ accountID2account.size() + " accounts.");
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
			LOGGER.warn("No Customer with id '" + id + "'. We know "
					+ customerID2customer.size() + " customers.");
		}
		return retval;
	}

	/**
	 * @see GnucashFile#getCustomerByName(java.lang.String)
	 */
	public GnucashCustomer getCustomerByName(final String name) {

		if (customerID2customer == null) {
			throw new IllegalStateException("no root-element loaded");
		}

		for (GnucashCustomer customer : getCustomers()) {
			if (customer.getName().equals(name)) {
				return customer;
			}
		}
		return null;
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
        LOGGER.warn("No Vendor with id '" + id + "'. We know "
                + vendorID2vendor.size() + " vendors.");
      }
      return retval;
	}

	@Override
	public GnucashVendor getVendorByName(String name) {
      if (vendorID2vendor == null) {
        throw new IllegalStateException("no root-element loaded");
      }

      for (GnucashVendor vendor : getVendors()) {
        if (vendor.getName().equals(name)) {
            return vendor;
        }
      }
      return null;
	}

	@Override
	public Collection<GnucashVendor> getVendors()
	{
      return vendorID2vendor.values();
	}

    // ---------------------------------------------------------------
    
	/**
	 * @param customer the customer to look for.
	 * @return all jobs that have this customer, never null
	 */
	public Collection<GnucashCustomerJob> getJobsByCustomer(final GnucashCustomer customer) {
		if (jobid2job == null) {
			throw new IllegalStateException("no root-element loaded");
		}

		Collection<GnucashCustomerJob> retval = new LinkedList<GnucashCustomerJob>();

		for (Object element : jobid2job.values()) {
			GnucashJob job = (GnucashJob) element;
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
        if (jobid2job == null) {
            throw new IllegalStateException("no root-element loaded");
        }

        Collection<GnucashVendorJob> retval = new LinkedList<GnucashVendorJob>();

        for (Object element : jobid2job.values()) {
            GnucashJob job = (GnucashJob) element;
            if (job.getOwnerId().equals(vendor.getId())) {
              retval.add((GnucashVendorJob) job);
            }
        }
        return retval;
    }

	/**
	 * @see GnucashFile#getJobByID(java.lang.String)
	 */
	public GnucashJob getJobByID(final String id) {
		if (jobid2job == null) {
			throw new IllegalStateException("no root-element loaded");
		}

		GnucashJob retval = jobid2job.get(id);
		if (retval == null) {
			LOGGER.warn("No Job with id '"
					+ id
					+ "'. We know "
					+ jobid2job.size()
					+ " jobs.");
		}

		return retval;
	}

	/**
	 * @see GnucashFile#getJobs()
	 */
	public Collection<GnucashJob> getJobs() {
		if (jobid2job == null) {
			throw new IllegalStateException("no root-element loaded");
		}
		return jobid2job.values();
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
			LOGGER.warn("No Transaction with id '" + id + "'. We know "
					+ transactionID2transaction.size() + " transactions.");
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
	 * created: 13.05.2005 <br/>
	 * replaces ':' in tag-names and attribute-names by '_' .
	 *
	 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
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
				throw new IllegalArgumentException(
						"null not allowed for field this.input");
			}

			input = newInput;
		}

		/**
		 * What to read from.
		 */
		private Reader input;

		/**
		 * true if we are in a quotation and thus
		 * shall not remove any namespaces.
		 */
		private boolean isInQuotation = false;

		/**
		 * true if we are in a quotation and thus
		 * shall remove any namespaces.
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
		private void logReatBytes(final char[] cbuf,
				final int off,
				final int reat) {
			debugLastReatLength = Math.min(debugLastTeat.length, reat);
			try {
				System.arraycopy(cbuf, off, debugLastTeat, 0,
						debugLastTeat.length);
			}
			catch (Exception e) {
				e.printStackTrace();
				LOGGER.debug("debugLastReatLength=" + debugLastReatLength
						+ "\n" + "off=" + off + "\n" + "reat=" + reat + "\n"
						+ "cbuf.length=" + cbuf.length + "\n"
						+ "debugLastTeat.length=" + debugLastTeat.length
						+ "\n");
			}
		}

		/**
		 * @see java.io.Reader#read(char[], int, int)
		 */
		@Override
		public int read(final char[] cbuf,
				final int off,
				final int len) throws IOException {

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
	 * created: 13.05.2005 <br/>
	 * replaces &#164; by the euro-sign .
	 *
	 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
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
				throw new IllegalArgumentException(
						"null not allowed for field this.input");
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
		public int read(final char[] cbuf,
				final int off,
				final int len) throws IOException {

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
								System.arraycopy(cbuf, (i + 1), cbuf,
										(i - (REPLACESTRINGLENGTH - 1)),
										(reat - i - 1));
							}
							int reat2 = input
									.read(cbuf, reat - REPLACESTRINGLENGTH,
											REPLACESTRINGLENGTH);
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
	// In this section, we assume that all customer and vendor numbers
	// (internally, the IDs, not the GUIDs) are purely numeric (as 
	// automatically generated by default).

	/**
	 * Assuming that all customer numbers (manually set IDs, not 
	 * GUIDs) are numeric as generated by default.
	 * 
	 * @param gcshFile
	 * @return
	 */
    public int getHighestCustomerNumber()
    {
      int highest = -1;
      
      for ( GnucashCustomer cust : customerID2customer.values() )
      {
        try {
          int newNum = Integer.parseInt(cust.getNumber());
          if ( newNum > highest )
            highest = newNum;
        } catch ( Exception exc )
        {
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
     * Assuming that all customer numbers (manually set IDs, not 
     * GUIDs) are numeric as generated by default.
     * 
     * @param gcshFile
     * @return
     */
    public int getHighestVendorNumber()
    {
      int highest = -1;
      
      for ( GnucashVendor vend : vendorID2vendor.values() )
      {
        try {
          int newNum = Integer.parseInt(vend.getNumber());
          if ( newNum > highest )
            highest = newNum;
        } catch ( Exception exc )
        {
          // Cf. .getHighestCustomerNumber() above.
          // ==> ::TODO Adapt how a vendor object is created.
          LOGGER.warn("getHighestCustomerNumber: Found vendor with non-numerical number");
        }
      }
      
      return highest;
    }
    
    // ----------------------------

    /**
     * Assuming that all customer numbers (manually set IDs, not 
     * GUIDs) are numeric as generated by default.
     * 
     * @param gcshFile
     * @return
     */
    public String getNewCustomerNumber()
    {
      int newNo = getHighestCustomerNumber() + 1;
      String newNoStr =  Integer.toString(newNo);
      String newNoStrPadded = PADDING_TEMPLATE + newNoStr; 
      // 10 zeroes if you need a string of length 10 in the end
      newNoStrPadded = newNoStrPadded.substring(newNoStr.length());
      
      return newNoStrPadded;
    }

    /**
     * Assuming that all customer numbers (manually set IDs, not 
     * GUIDs) are numeric as generated by default.
     * 
     * @param gcshFile
     * @return
     */
    public String getNewVendorNumber()
    {
      int newNo = getHighestVendorNumber() + 1;
      String newNoStr =  Integer.toString(newNo);
      String newNoStrPadded = PADDING_TEMPLATE + newNoStr; 
      // 10 zeroes if you need a string of length 10 in the end
      newNoStrPadded = newNoStrPadded.substring(newNoStr.length());
      
      return newNoStrPadded;
    }
    
}
