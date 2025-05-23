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
import org.gnucash.currency.ComplexCurrencyTable;
import org.gnucash.generated.GncAccount;
import org.gnucash.generated.GncBudget;
import org.gnucash.generated.GncCountData;
import org.gnucash.generated.GncPricedb;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.Price;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.auxiliary.GCshBillTerms;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.impl.auxiliary.GCshBillTermsImpl;
import org.gnucash.read.impl.auxiliary.GCshTaxTableImpl;
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
 * Implementation of GnucashFile that can only read but not modify Gnucash-Files. <br/>
 * 
 * @see GnucashFile
 */
public class GnucashFileImpl implements GnucashFile {

  protected static final Logger LOGGER = LoggerFactory.getLogger(GnucashFileImpl.class);
  private ApplicationMessages bundle = ApplicationMessages.getInstance();

  /**
   * my CurrencyTable.
   */
  private final ComplexCurrencyTable currencyTable = new ComplexCurrencyTable();

  private static final String PADDING_TEMPLATE = "000000";

  // ---------------------------------------------------------------

  /**
   * @param pFile the file to load and initialize from
   * @throws IOException on low level reading-errors (FileNotFoundException if not found)
   * @see #loadFile(File)
   */
  public GnucashFileImpl(final File pFile) throws IOException {
    super();
    loadFile(pFile);
  }

  /**
   * @param pFile the file to load and initialize from
   * @throws IOException on low level reading-errors (FileNotFoundException if not found)
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

//    // ----------------------------
//
//    /**
//     * Filles lazy in getVendorTerms() .
//     *
//     * @see #getVendorTerms()
//     */
//    protected Map<String, GCshVendorTerms> vendorTermsByID = null;
//
//    /**
//     * @param id id of a vendor terms item
//     * @return the identified vendor terms item or null
//     */
//    public GCshVendorTerms getVendorTermsByID(final String id) {
//        if (vendorTermsByID == null) {
//            getVendorTerms();
//        }
//        
//        return vendorTermsByID.get(id);
//    }
//
//    /**
//     * @return all TaxTables defined in the book
//     * @link GnucashTaxTable
//     */
//    public Collection<GCshVendorTerms> getVendorTerms() {
//        if (vendorTermsByID == null) {
//            vendorTermsByID = new HashMap<String, GCshVendorTerms>();
//
//            List<Object> bookElements = this.getRootElement().getGncBook().getBookElements();
//            for (Object bookElement : bookElements) {
//                if (!(bookElement instanceof GncV2.GncBook.GncGncVendor.VendorTerms)) {
//                    continue;
//                }
//                GncV2.GncBook.GncGncVendor.VendorTerms jwsdpPeer = (GncV2.GncBook.GncGncVendor.VendorTerms) bookElement;
//                GCshVendorTermsImpl vendTerms = new GCshVendorTermsImpl(jwsdpPeer, this);
//                vendorTermsByID.put(vendTerms.getId(), vendTerms);
//            }
//        }
//
//        return vendorTermsByID.values();
//    }
//
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
   * @return a read-only collection of all accounts that have no parent (the result is sorted)
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
      LOGGER.error(bundle.getMessage("FatNoRootAccounts"));
      throw e;
    } catch (Throwable e) {
      LOGGER.error(bundle.getMessage("FatSerRootAccounts"));
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
   * warning: this function has to traverse all accounts. If it much faster to try getAccountByID first and only call
   * this method if the returned account does not have the right name.
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
   * First try to fetch the account by id, then fall back to traversing all accounts to get if by it's name.
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
   * First try to fetch the account by id, then fall back to traversing all accounts to get if by it's name.
   *
   * @param id   the id to look for
   * @param name the regular expression of the name to look for if nothing is found for the id
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
   * @see GnucashFile#getGenerInvoiceByID(java.lang.String)
   */
  public GnucashGenerInvoice getGenerInvoiceByID(final String id) {
    if (invoiceID2invoice == null) {
      throw new IllegalStateException("no root-element loaded");
    }

    GnucashGenerInvoice retval = invoiceID2invoice.get(id);
    if (retval == null) {
      LOGGER.error(bundle.getMessage("Err_NoGenInvid", id, invoiceID2invoice.size()));
    }

    return retval;
  }

  /**
   * @see GnucashFile#getGenerInvoices()
   */
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
          LOGGER.error(bundle.getMessage("FatSerPaidInv"));
        }
      } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
        try {
          if (invc.isBillFullyPaid()) {
            retval.add(invc);
          }
        } catch (WrongInvoiceTypeException e) {
          // This should not happen
          LOGGER.error(bundle.getMessage("FatSerPaidInv"));
        }
      } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
        try {
          if (invc.isJobFullyPaid()) {
            retval.add(invc);
          }
        } catch (WrongInvoiceTypeException e) {
          // This should not happen
          LOGGER.error(bundle.getMessage("FatSerPaidInv"));
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidGenerInvoices()
   */
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
          LOGGER.error(bundle.getMessage("FatUnpInv"), e);
        }
      } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
        try {
          if (invc.isNotBillFullyPaid()) {
            retval.add(invc);
          }
        } catch (WrongInvoiceTypeException e) {
          // This should not happen
          LOGGER.error(bundle.getMessage("FatUnpInv"), e);
        }
      } else if (invc.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
        try {
          if (invc.isNotJobFullyPaid()) {
            retval.add(invc);
          }
        } catch (WrongInvoiceTypeException e) {
          // This should not happen
          LOGGER.error(bundle.getMessage("FatUnpInv"), e);
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
          LOGGER.error(bundle.getMessage("FatNotInstCustInv", "getInvoicesForCustomer_direct"));
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
  public Collection<GnucashJobInvoice> getInvoicesForCustomer_viaAllJobs(final GnucashCustomer cust)
      throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

    for (GnucashCustomerJob job : cust.getJobs()) {
      for (GnucashJobInvoice jobInvc : job.getInvoices()) {
        retval.add(jobInvc);
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
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
          LOGGER.error(bundle.getMessage("FatPaidInvNoCustInv"), e);
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
  public Collection<GnucashJobInvoice> getPaidInvoicesForCustomer_viaAllJobs(final GnucashCustomer cust)
      throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

    for (GnucashCustomerJob job : cust.getJobs()) {
      for (GnucashJobInvoice jobInvc : job.getPaidInvoices()) {
        retval.add(jobInvc);
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
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
          LOGGER.error(bundle.getMessage("FatNotInstCustInv", "getUnpaidInvoicesForCustomer_direct"));
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
  public Collection<GnucashJobInvoice> getUnpaidInvoicesForCustomer_viaAllJobs(final GnucashCustomer cust)
      throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

    for (GnucashCustomerJob job : cust.getJobs()) {
      for (GnucashJobInvoice jobInvc : job.getUnpaidInvoices()) {
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
          LOGGER.error(bundle.getMessage("FatNotInstVendBill", "getBillsForVendor"), e);
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
  public Collection<GnucashJobInvoice> getBillsForVendor_viaAllJobs(final GnucashVendor vend)
      throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

    for (GnucashVendorJob job : vend.getJobs()) {
      for (GnucashJobInvoice jobInvc : job.getInvoices()) {
        retval.add(jobInvc);
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidBillsForVendor_viaJob(GnucashVendor)
   */
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
          LOGGER.error(bundle.getMessage("FatNotInstVendBill", "getPaidBillsForVendor_direct"), e);
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
  public Collection<GnucashJobInvoice> getPaidBillsForVendor_viaAllJobs(final GnucashVendor vend)
      throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

    for (GnucashVendorJob job : vend.getJobs()) {
      for (GnucashJobInvoice jobInvc : job.getPaidInvoices()) {
        retval.add(jobInvc);
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidBillsForVendor_viaJob(GnucashVendor)
   */
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
          LOGGER.error(bundle.getMessage("getUnpaidBillsForVendor_direct", "getPaidBillsForVendor_direct"), e);
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
  public Collection<GnucashJobInvoice> getUnpaidBillsForVendor_viaAllJobs(final GnucashVendor vend)
      throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

    for (GnucashVendorJob job : vend.getJobs()) {
      for (GnucashJobInvoice jobInvc : job.getUnpaidInvoices()) {
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
  public Collection<GnucashJobInvoice> getInvoicesForJob(final GnucashGenerJob job) throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoice> retval = new LinkedList<GnucashJobInvoice>();

    for (GnucashGenerInvoice invc : getGenerInvoices()) {
      if (invc.getOwnerId(GnucashGenerInvoice.ReadVariant.DIRECT).equals(job.getId())) {
        try {
          retval.add(new GnucashJobInvoiceImpl(invc));
        } catch (WrongInvoiceTypeException e) {
          // This really should not happen, one can almost
          // throw a fatal log here.
          LOGGER.error(bundle.getMessage("FatNotInstJobInv", "getInvoicesForJob"), e);
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
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
          LOGGER.error(bundle.getMessage("FatNotInstJobInv", "getPaidInvoicesForJob"), e);
        }
      }
    }

    return retval;
  }

  /**
   * @throws WrongInvoiceTypeException
   * @see GnucashFile#getUnpaidInvoicesForCustomer_direct(GnucashCustomer)
   */
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
          LOGGER.error(bundle.getMessage("FatNotInstJobInv", "getUnpaidInvoicesForJob"), e);
        }
      }
    }

    return retval;
  }

  // ---------------------------------------------------------------

  /**
   * @see GnucashFile#getGenerInvoiceByID(java.lang.String)
   */
  public GnucashGenerInvoiceEntry getGenerInvoiceEntryByID(final String id) {
    if (invoiceEntryID2invoiceEntry == null) {
      throw new IllegalStateException("no root-element loaded");
    }

    GnucashGenerInvoiceEntry retval = invoiceEntryID2invoiceEntry.get(id);
    if (retval == null) {
      LOGGER.error(bundle.getMessage("Err_NoInvoiceEntry", id, invoiceEntryID2invoiceEntry.size()));
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
   * the top-level Element of the gnucash-files parsed and checked for validity by JAXB.
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
   * Helper to implement the {@link GnucashObject}-interface without having the same code twice.
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
      if (bookElement instanceof GncPricedb) {
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
        LOGGER.error(bundle.getMessage("Err_RuntimeExcpIgn", getClass().getName(), "initAccountMap", "Account-Entry",
            jwsdpAcct.getActId().getValue()));
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
        LOGGER.error(bundle.getMessage("Err_RuntimeExcpIgn", getClass().getName(), "initInvoiceMap", "Invoice-Entry",
            jwsdpInvc.getInvoiceId()));
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
        LOGGER.error(bundle.getMessage("Err_RuntimeExcpIgn", getClass().getName(), "initInvoiceEntryMap",
            "(generic) Invoice-Entry-Entry", jwsdpInvcEntr.getEntryGuid().getValue()));
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
        LOGGER.error(bundle.getMessage("Err_RuntimeExcpIgn", getClass().getName(), "initTransactionMap",
            "Transaction-Entry", jwsdpTrx.getTrnId().getValue()));
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
        LOGGER.error(bundle.getMessage("Err_RuntimeExcpIgn", getClass().getName(), "initCustomerMap", "Customer-Entry",
            jwsdpCust.getCustId()));
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
        LOGGER.error(bundle.getMessage("Err_RuntimeExcpIgn", getClass().getName(), "initVendorMap", "Vendor-Entry",
            jwsdpVend.getVendorId()));
      }
    } // for

    LOGGER.debug("No. of entries in vendor map: " + vendorID2vendor.size());
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
          LOGGER.error(bundle.getMessage("Err_NoVendorId"));
          jobID = "";
        }
        jobID2job.put(job.getId(), job);
      } catch (RuntimeException e) {
        LOGGER.error(bundle.getMessage("Err_RuntimeExcpIgn", getClass().getName(), "initJobMap",
            "Customer/Vendor-Job-Entry", jwsdpJob.getJobId()));
      }
    } // for

    LOGGER.debug("No. of entries in (generic) job map: " + jobID2job.size());
  }

  // ---------------------------------------------------------------

  /**
   * Use a heuristic to determine the defaultcurrency-id. If we cannot find one, we default to EUR.<br/>
   * Comodity-stace is fixed as "ISO4217" .
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
      if (jwsdpAccount.getActCommodity() != null && jwsdpAccount.getActCommodity().getCmdtySpace().equals("ISO4217")) {
        return jwsdpAccount.getActCommodity().getCmdtyId();
      }
    }
    return "EUR";
  }

  /**
   * priceDB Price database
   */
  GncPricedb priceDB;

  /**
   * @param pRootElement the root-element of the Gnucash-file
   */
  private void loadPriceDatabase(final GncV2 pRootElement) {
    boolean noPriceDB = true;
    List<Object> bookElements = pRootElement.getGncBook().getBookElements();
    for (Object bookElement : bookElements) {
      if (!(bookElement instanceof GncPricedb)) {
        continue;
      }
      noPriceDB = false;
      priceDB = (GncPricedb) bookElement;

      if (priceDB.getVersion() != 1) {
        LOGGER.warn(bundle.getMessage("Warn_WrgVersPriceDb", priceDB.getVersion()));
      } else {
        getCurrencyTable().clear();
        getCurrencyTable().setConversionFactor("ISO4217", getDefaultCurrencyID(), new FixedPointNumber(1));

        for (Iterator<Price> iter = priceDB.getPrice().iterator(); iter.hasNext();) {
          Price price = iter.next();
          Price.PriceCommodity comodity = price.getPriceCommodity();

          // check if we already have a latest price for this comodity
          // (=currency, fund, ...)
          if (getCurrencyTable().getConversionFactor(comodity.getCmdtySpace(), comodity.getCmdtyId()) != null) {
            continue;
          }

          String baseCurrency = getDefaultCurrencyID();
          if (comodity.getCmdtySpace().equals("ISO4217") && comodity.getCmdtyId().equals(baseCurrency)) {
            LOGGER.warn(bundle.getMessage("Warn_IgnProiceQuote", baseCurrency, baseCurrency));
            continue;
          }

          // get the latest price in the file and insert it into
          // our currency table
          FixedPointNumber factor = getLatestPrice(comodity.getCmdtySpace(), comodity.getCmdtyId());

          if (factor != null) {
            getCurrencyTable().setConversionFactor(comodity.getCmdtySpace(), comodity.getCmdtyId(), factor);
          } else {
            LOGGER.warn(bundle.getMessage("Warn_NoFactForCommodity", comodity.getCmdtySpace(), comodity.getCmdtyId()));
          }
        }
      }
    }

    if (noPriceDB) {
      // case: no priceDB in file
      getCurrencyTable().clear();
    }
  }

  public int getNofEntriesPricesInDB() {
    return priceDB.getPrice().size();
  }

  public GncPricedb getPriceDB() {
    return priceDB;
  }

  /**
   * @see {@link #getLatestPrice(String, String)}
   */
  protected static final DateFormat PRICE_QUOTE_DATE_FORMAT = new SimpleDateFormat(Const.STANDARD_DATE_FORMAT);

  /**
   * @param pCmdtySpace the namespace for pCmdtyId
   * @param pCmdtyId    the currency-name
   * @param depth       used for recursion. Allways call with '0' for aborting recursive quotes (quotes to other then
   *                    the base- currency) we abort if the depth reached maxRecursionDepth+1.
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
      if (!(bookElement instanceof GncPricedb)) {
        continue;
      }
      GncPricedb priceDB = (GncPricedb) bookElement;
      for (Price priceQuote : (List<Price>) priceDB.getPrice()) {

        try {
          if (priceQuote == null) {
            LOGGER.warn(bundle.getMessage("Warn_NullPriceQuotes"));
            continue;
          }
          if (priceQuote.getPriceCurrency() == null) {
            LOGGER.warn(bundle.getMessage("Warn_PriceQuotesNoId", "currency", priceQuote.getPriceId().getValue()));
            continue;
          }
          if (priceQuote.getPriceCurrency().getCmdtyId() == null) {
            LOGGER.warn(bundle.getMessage("Warn_PriceQuotesNoId", "currency-id", priceQuote.getPriceId().getValue()));
            continue;
          }
          if (priceQuote.getPriceCurrency().getCmdtySpace() == null) {
            LOGGER.warn(
                bundle.getMessage("Warn_PriceQuotesNoId", "currency-namespace", priceQuote.getPriceId().getValue()));
            continue;
          }
          if (priceQuote.getPriceTime() == null) {
            LOGGER.warn(bundle.getMessage("Warn_PriceQuotesNoId", "timestamp", priceQuote.getPriceId().getValue()));
            continue;
          }
          if (priceQuote.getPriceValue() == null) {
            LOGGER.warn(bundle.getMessage("Warn_PriceQuotesNoId", "value", priceQuote.getPriceId().getValue()));
            continue;
          }
          /*
           * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND") && priceQuote.getPriceType() == null) {
           * LOGGER.warn("gnucash-file contains FUND-price-quotes" + " with no type id='" +
           * priceQuote.getPriceId().getValue() + "'"); continue; }
           */
          if (!priceQuote.getPriceCommodity().getCmdtySpace().equals(pCmdtySpace)) {
            continue;
          }
          if (!priceQuote.getPriceCommodity().getCmdtyId().equals(pCmdtyId)) {
            continue;
          }
          /*
           * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND") && (priceQuote.getPriceType() == null ||
           * !priceQuote.getPriceType().equals("last") )) { LOGGER.warn("ignoring FUND-price-quote of unknown type '" +
           * priceQuote.getPriceType() + "' expecting 'last' "); continue; }
           */

          if (!priceQuote.getPriceCurrency().getCmdtySpace().equals("ISO4217")) {
            if (depth > maxRecursionDepth) {
              LOGGER
                  .warn(bundle.getMessage("Err_IgnPriceQuote", "ISO4217", priceQuote.getPriceCurrency().getCmdtyId()));
              continue;
            }
            factor = getLatestPrice(priceQuote.getPriceCurrency().getCmdtySpace(),
                priceQuote.getPriceCurrency().getCmdtyId(), depth + 1);
          } else {
            if (!priceQuote.getPriceCurrency().getCmdtyId().equals(getDefaultCurrencyID())) {
              if (depth > maxRecursionDepth) {
                LOGGER.warn(bundle.getMessage("Err_IgnPriceQuote", getDefaultCurrencyID(),
                    priceQuote.getPriceCurrency().getCmdtyId()));
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
          LOGGER.error(bundle.getMessage("FatExcpIgnPrcQuote", "NumberFormatException", getClass().getName(),
              "getLatestPrice(pCmdtySpace", pCmdtySpace, pCmdtyId));
        } catch (ParseException e) {
          LOGGER.error(bundle.getMessage("FatExcpIgnPrcQuote", "ParseException", getClass().getName(),
              "getLatestPrice(pCmdtySpace", pCmdtySpace, pCmdtyId));
        } catch (NullPointerException e) {
          LOGGER.error(bundle.getMessage("FatExcpIgnPrcQuote", "NullPointerException", getClass().getName(),
              "getLatestPrice(pCmdtySpace", pCmdtySpace, pCmdtyId));
        } catch (ArithmeticException e) {
          LOGGER.error(bundle.getMessage("FatExcpIgnPrcQuote", "ArithmeticException", getClass().getName(),
              "getLatestPrice(pCmdtySpace", pCmdtySpace, pCmdtyId));
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

  // ----------------------------

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
   * @param jwsdpInvcEntr the JWSDP-peer (parsed xml-element) to fill our object with
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
   * @throws IOException on low level reading-errors (FileNotFoundException if not found)
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
    LOGGER.debug("GnucashFileImpl.loadFile took " + (end - start) + " ms (total) ");

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
      LOGGER.debug("GnucashFileImpl.loadFileInputStream took " + (end - start) + " ms (total) " + (start2 - start)
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
   * @return the jaxb object-factory used to create new peer-objects to extend this
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
      LOGGER.error(bundle.getMessage("Err_NoAccountId", id, accountID2account.size()));
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
      LOGGER.warn(bundle.getMessage("Warn_NoObjectWithId", "Customer", id, customerID2customer.size(), "customers"));
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
      LOGGER.warn(bundle.getMessage("Warn_NoObjectWithId", "Vendor", id, vendorID2vendor.size(), "vendors"));
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
  public Collection<GnucashVendor> getVendors() {
    return vendorID2vendor.values();
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
  public GnucashGenerJob getGenerJobByID(final String id) {
    if (jobID2job == null) {
      throw new IllegalStateException("no root-element loaded");
    }

    GnucashGenerJob retval = jobID2job.get(id);
    if (retval == null) {
      LOGGER.warn(bundle.getMessage("Warn_NoObjectWithId", "Job", id, jobID2job.size(), "jobs"));
    }

    return retval;
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
      LOGGER
          .warn("No Transaction with id '" + id + "'. We know " + transactionID2transaction.size() + " transactions.");
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
      LOGGER.warn(bundle.getMessage("Warn_NoObjectWithId", "Transaction-Split", id,
          transactionSplitID2transactionSplit.size(), "transactions"));
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
        LOGGER.debug("debugLastReatLength=" + debugLastReatLength + "\n" + "off=" + off + "\n" + "reat=" + reat + "\n"
            + "cbuf.length=" + cbuf.length + "\n" + "debugLastTeat.length=" + debugLastTeat.length + "\n");
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

  public int getNofEntriesAccountMap() {
    return accountID2account.size();
  }

  public int getNofEntriesTransactionMap() {
    return transactionID2transaction.size();
  }

  public int getNofEntriesTransactionSplitsMap() {
    return transactionSplitID2transactionSplit.size();
  }

  public int getNofEntriesGenerInvoiceMap() {
    return invoiceID2invoice.size();
  }

  public int getNofEntriesGenerInvoiceEntriesMap() {
    return invoiceEntryID2invoiceEntry.size();
  }

  public int getNofEntriesGenerJobMap() {
    return jobID2job.size();
  }

  public int getNofEntriesCustomerMap() {
    return customerID2customer.size();
  }

  public int getNofEntriesVendorMap() {
    return vendorID2vendor.size();
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
   * Assuming that all customer numbers (manually set IDs, not GUIDs) are numeric as generated by default.
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
        LOGGER.warn(bundle.getMessage("Warn_GetHighestObj", "getHighestCustomerNumber"));
      }
    }

    return highest;
  }

  /**
   * Assuming that all vendor numbers (manually set IDs, not GUIDs) are numeric as generated by default.
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
        LOGGER.warn(bundle.getMessage("Warn_GetHighestObj", "getHighestVendorNumber"));
      }
    }

    return highest;
  }

  /**
   * Assuming that all job numbers (manually set IDs, not GUIDs) are numeric as generated by default.
   * 
   * CAUTION: As opposed to customers and vendors, it may not be a good idea to actually have the job numbers generated
   * automatically.
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
        LOGGER.warn(bundle.getMessage("Warn_GetHighestObj", "getHighestJobNumber"));
      }
    }

    return highest;
  }

  // ----------------------------

  /**
   * Assuming that all customer numbers (manually set IDs, not GUIDs) are numeric as generated by default.
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
   * Assuming that all customer numbers (manually set IDs, not GUIDs) are numeric as generated by default.
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
   * Assuming that all job numbers (manually set IDs, not GUIDs) are numeric as generated by default.
   * 
   * CAUTION: As opposed to customers and vendors, it may not be a good idea to actually have the job numbers generated
   * automatically.
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
