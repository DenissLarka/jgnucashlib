package org.gnucash.read.impl;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.gnucash.Const;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.GncV2.GncBook.GncGncEntry.EntryBTaxtable;
import org.gnucash.generated.GncV2.GncBook.GncGncEntry.EntryBill;
import org.gnucash.generated.GncV2.GncBook.GncGncEntry.EntryITaxtable;
import org.gnucash.generated.GncV2.GncBook.GncGncEntry.EntryInvoice;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.auxiliary.GCshTaxTableEntry;
import org.gnucash.read.impl.spec.GnucashJobInvoiceImpl;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.impl.UnknownInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of GnucashInvoiceEntry that uses JWSDP.
 */
public class GnucashGenerInvoiceEntryImpl extends GnucashObjectImpl implements GnucashGenerInvoiceEntry {
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashGenerInvoiceEntryImpl.class);
  private static ApplicationMessages bundle = ApplicationMessages.getInstance();

  protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
  protected static final DateTimeFormatter DATE_FORMAT_BOOK = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
  protected static final DateTimeFormatter DATE_FORMAT_PRINT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Format of the JWSDP-Field for the entry-date.
   */
  protected static final DateFormat ENTRY_DATE_FORMAT = new SimpleDateFormat(Const.STANDARD_DATE_FORMAT);

  // ------------------------------

  /**
   * the JWSDP-object we are facading.
   */
  protected final GncV2.GncBook.GncGncEntry jwsdpPeer;

  // ------------------------------

  /**
   * @see GnucashGenerInvoice#getDateOpened()
   */
  protected ZonedDateTime date;

  /**
   * The taxtable in the gnucash xml-file. It defines what sales-tax-rates are known.
   */
  private GCshTaxTable myInvcTaxtable;
  private GCshTaxTable myBillTaxtable;

  // ----------------------------

  /**
   * @see #getDateOpenedFormatted()
   * @see #getDatePostedFormatted()
   */
  private DateFormat dateFormat = null;

  /**
   * The numberFormat to use for non-currency-numbers for default-formating.<br/>
   * Please access only using {@link #getNumberFormat()}.
   *
   * @see #getNumberFormat()
   */
  private NumberFormat numberFormat = null;

  /**
   * The numberFormat to use for percentFormat-numbers for default-formating.<br/>
   * Please access only using {@link #getPercentFormat()}.
   *
   * @see #getPercentFormat()
   */
  private NumberFormat percentFormat = null;

  // ---------------------------------------------------------------

  /**
   * This constructor is used when an invoice is created by java-code.
   *
   * @param invoice The invoice we belong to.
   * @param peer    the JWSDP-Object we are wrapping.
   */
  @SuppressWarnings("exports")
  public GnucashGenerInvoiceEntryImpl(final GnucashGenerInvoice invoice, final GncV2.GncBook.GncGncEntry peer,
      final boolean addEntryToInvoice) {
    super((peer.getEntrySlots() == null) ? new ObjectFactory().createSlotsType() : peer.getEntrySlots(),
        invoice.getFile());

    if (peer.getEntrySlots() == null) {
      peer.setEntrySlots(getSlots());
    }

    this.myInvoice = invoice;
    this.jwsdpPeer = peer;

    if (addEntryToInvoice) {
      if (invoice != null) {
        invoice.addGenerEntry(this);
      }
    }
  }

  /**
   * This code is used when an invoice is loaded from a file.
   *
   * @param gncFile tne file we belong to
   * @param peer    the JWSDP-object we are facading.
   * @see #jwsdpPeer
   */
  @SuppressWarnings("exports")
  public GnucashGenerInvoiceEntryImpl(final GncV2.GncBook.GncGncEntry peer, final GnucashFileImpl gncFile) {
    super((peer.getEntrySlots() == null) ? new ObjectFactory().createSlotsType() : peer.getEntrySlots(), gncFile);

    if (peer.getEntrySlots() == null) {
      peer.setEntrySlots(getSlots());
    }

    this.jwsdpPeer = peer;

    // an exception is thrown here if we have an invoice-ID but the invoice does not
    // exist
    GnucashGenerInvoice invoice = getGenerInvoice();
    if (invoice != null) {
      // ...so we only need to handle the case of having no invoice-id at all
      invoice.addGenerEntry(this);
    }
  }

  // Copy-constructor
  public GnucashGenerInvoiceEntryImpl(final GnucashGenerInvoiceEntry entry) {
    super(entry.getGenerInvoice().getFile());

    if (entry.getJwsdpPeer().getEntrySlots() == null) {
      setSlots(new ObjectFactory().createSlotsType());
    } else {
      setSlots(entry.getJwsdpPeer().getEntrySlots());
    }

    this.myInvoice = entry.getGenerInvoice();
    this.jwsdpPeer = entry.getJwsdpPeer();
  }

  // ---------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public String getId() {
    return jwsdpPeer.getEntryGuid().getValue();
  }

  /**
   * {@inheritDoc}
   */
  public String getType() throws WrongInvoiceTypeException {
    return getGenerInvoice().getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT);
  }

  /**
   * MAY RETURN NULL. {@inheritDoc}
   */
  public String getGenerInvoiceID() {
    EntryInvoice entrInvc = null;
    EntryBill entrBill = null;

    try {
      entrInvc = jwsdpPeer.getEntryInvoice();
    } catch (Exception exc) {
      // ::EMPTY
    }

    try {
      entrBill = jwsdpPeer.getEntryBill();
    } catch (Exception exc) {
      // ::EMPTY
    }

    if (entrInvc == null && entrBill == null) {
      LOGGER.error(bundle.getMessage("Err_InvNoCustNoBill", getId()));
      return "ERROR";
    } else if (entrInvc != null && entrBill == null) {
      return entrInvc.getValue();
    } else if (entrInvc == null && entrBill != null) {
      return entrBill.getValue();
    } else if (entrInvc != null && entrBill != null) {
      LOGGER.error(bundle.getMessage("Err_InvBothCustBill", getId()));
      return "ERROR";
    }

    return "ERROR";
  }

  /**
   * The invoice this entry is from.
   */
  protected GnucashGenerInvoice myInvoice;

  /**
   * {@inheritDoc}
   */
  public GnucashGenerInvoice getGenerInvoice() {
    if (myInvoice == null) {
      String invcId = getGenerInvoiceID();
      if (invcId != null) {
        myInvoice = getGnucashFile().getGenerInvoiceByID(invcId);
        if (myInvoice == null) {
          String msgstr = bundle.getMessage("Err_NoCustVendNoInvBill", getGenerInvoiceID(), getId());
          throw new IllegalStateException(msgstr);
        }
      }
    }
    return myInvoice;
  }

  // ---------------------------------------------------------------

  /**
   * @param aTaxtable the taxtable to set
   */
  protected void setInvcTaxTable(final GCshTaxTable aTaxtable)
      throws WrongInvoiceTypeException, TaxTableNotFoundException {
    myInvcTaxtable = aTaxtable;
  }

  /**
   * @param aTaxtable the taxtable to set
   */
  protected void setBillTaxTable(final GCshTaxTable aTaxtable)
      throws WrongInvoiceTypeException, TaxTableNotFoundException {
    myBillTaxtable = aTaxtable;
  }

  protected void setJobTaxTable(final GCshTaxTable aTaxtable)
      throws WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException {

    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      setInvcTaxTable(aTaxtable);
    }
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      setBillTaxTable(aTaxtable);
    }
  }

  /**
   * @return The taxtable in the gnucash xml-file. It defines what sales-tax-rates are known.
   */
  public GCshTaxTable getInvcTaxTable() throws TaxTableNotFoundException, WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    if (myInvcTaxtable == null) {
      EntryITaxtable taxTableEntry = jwsdpPeer.getEntryITaxtable();
      if (taxTableEntry == null) {
        throw new TaxTableNotFoundException();
      }

      String taxTableId = taxTableEntry.getValue();
      if (taxTableId == null) {
        LOGGER.error(bundle.getMessage("Err_InvNoTaxId", getId()));
        return null;
      }
      myInvcTaxtable = getGnucashFile().getTaxTableByID(taxTableId);

      if (myInvcTaxtable == null) {
        LOGGER.error(bundle.getMessage("Err_InvUnkTaxId", getId(), taxTableId));
      }
    } // myInvcTaxtable == null

    return myInvcTaxtable;
  }

  /**
   * @return The taxtable in the gnucash xml-file. It defines what sales-tax-rates are known.
   */
  public GCshTaxTable getBillTaxTable() throws TaxTableNotFoundException, WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    if (myBillTaxtable == null) {
      EntryBTaxtable taxTableEntry = jwsdpPeer.getEntryBTaxtable();
      if (taxTableEntry == null) {
        throw new TaxTableNotFoundException();
      }

      String taxTableId = taxTableEntry.getValue();
      if (taxTableId == null) {
        LOGGER.error(bundle.getMessage("Err_VendEmpTaxId", getId()));
        return null;
      }
      myBillTaxtable = getGnucashFile().getTaxTableByID(taxTableId);

      if (myBillTaxtable == null) {
        LOGGER.error(bundle.getMessage("Err_VendUnkTaxId", getId(), taxTableId));
      }
    } // myBillTaxtable == null

    return myBillTaxtable;
  }

  public GCshTaxTable getJobTaxTable() throws TaxTableNotFoundException, WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      return getInvcTaxTable();
    }
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      return getBillTaxTable();
    }

    return null; // Compiler happy
  }

  // ---------------------------------------------------------------

  /**
   * @return e.g. "0.19" for "19%"
   */
  public FixedPointNumber getInvcApplicableTaxPercent() throws WrongInvoiceTypeException {

    if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    if (!isInvcTaxable()) {
      return new FixedPointNumber();
    }

    if (jwsdpPeer.getEntryITaxtable() != null) {
      if (!jwsdpPeer.getEntryITaxtable().getType().equals(Const.XML_DATA_TYPE_GUID)) {
        LOGGER.error("getInvcApplicableTaxPercent: Customer invoice entry with id '" + getId()
            + "' has i-taxtable with type='" + jwsdpPeer.getEntryITaxtable().getType() + "' != 'guid'");
      }
    }

    GCshTaxTable taxTab = null;
    try {
      taxTab = getInvcTaxTable();
    } catch (TaxTableNotFoundException exc) {
      LOGGER.error(bundle.getMessage("Err_NoTaxEntryCustInv"));
      return new FixedPointNumber("0");
    }

    // ::TODO ::CHECK
    // Overly specific code / pseudo-improvement
    // Reasons:
    // - We should not correct data errors -- why only check here?
    // There are hundreds of instances where we could check...
    // - For this lib, the data in the GnuCash file is the truth.
    // If it happens to be wrong, then it should be corrected, period.
    // - This is not the way GnuCash works. It never just takes a
    // tax rate directly, but rather goes via a tax table (entry).
    // - Assuming standard German VAT is overly specific.
    // if (taxTab == null) {
    // LOGGER.error("getInvcApplicableTaxPercent: Customer invoice entry with id '" + getId() +
    // "' is taxable but has an unknown i-taxtable! "
    // + "Assuming 19%");
    // return new FixedPointNumber("1900000/10000000");
    // }
    // Instead:
    if (taxTab == null) {
      LOGGER.error(bundle.getMessage("Err_CustInvUnkTaxId", getId()));
      return new FixedPointNumber("0");
    }

    GCshTaxTableEntry taxTabEntr = taxTab.getEntries().iterator().next();
    // ::TODO ::CHECK
    // Overly specific code / pseudo-improvement
    // Reasons:
    // - We should not correct data errors -- why only check here?
    // There are hundreds of instances where we could check...
    // - Assuming standard German VAT is overly specific.
    // if ( ! taxTabEntr.getType().equals(GCshTaxTableEntry.TYPE_PERCENT) ) {
    // LOGGER.error("getInvcApplicableTaxPercent: Customer invoice entry with id '" + getId() +
    // "' is taxable but has a i-taxtable "
    // + "that is not in percent but in '" + taxTabEntr.getType() + "'! Assuming 19%");
    // return new FixedPointNumber("1900000/10000000");
    // }
    // Instead:
    // ::TODO
    if (taxTabEntr.getType().equals(GCshTaxTableEntry.TYPE_VALUE)) {
      LOGGER.error(bundle.getMessage("NotImplCustInvTax", getId(), taxTabEntr.getType()));
      return new FixedPointNumber("0");
    }

    FixedPointNumber val = taxTabEntr.getAmount();

    // the file contains, say, 19 for 19%, we need to convert it to 0,19.
    return ((FixedPointNumber) val.clone()).divideBy(new FixedPointNumber("100"));

  }

  /**
   * @return e.g. "0.19" for "19%"
   */
  public FixedPointNumber getBillApplicableTaxPercent() throws WrongInvoiceTypeException {

    if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    if (!isBillTaxable()) {
      return new FixedPointNumber();
    }

    if (jwsdpPeer.getEntryBTaxtable() != null) {
      if (!jwsdpPeer.getEntryBTaxtable().getType().equals(Const.XML_DATA_TYPE_GUID)) {
        LOGGER.error(bundle.getMessage("Err_VendBillWrgGUID", getId(), jwsdpPeer.getEntryBTaxtable().getType()));
      }
    }

    GCshTaxTable taxTab = null;
    try {
      taxTab = getBillTaxTable();
    } catch (TaxTableNotFoundException exc) {
      LOGGER.error(bundle.getMessage("Err_VendBillNoTaxId", getId()));
      return new FixedPointNumber("0");
    }

    // Cf. getInvcApplicableTaxPercent()
    if (taxTab == null) {
      LOGGER.error(bundle.getMessage("Err_VendBillNoTaxId", getId()));
      return new FixedPointNumber("0");
    }

    GCshTaxTableEntry taxTabEntr = taxTab.getEntries().iterator().next();
    // ::TODO ::CHECK
    // Cf. getInvcApplicableTaxPercent()
    // ::TODO
    if (taxTabEntr.getType().equals(GCshTaxTableEntry.TYPE_VALUE)) {
      LOGGER.error(bundle.getMessage("NotImplVendBillTax", getId(), taxTabEntr.getType()));
      return new FixedPointNumber("0");
    }

    FixedPointNumber val = taxTabEntr.getAmount();

    // the file contains, say, 19 for 19%, we need to convert it to 0,19.
    return ((FixedPointNumber) val.clone()).divideBy(new FixedPointNumber("100"));

  }

  public FixedPointNumber getJobApplicableTaxPercent() throws WrongInvoiceTypeException {

    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      return getInvcApplicableTaxPercent();
    }
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      return getBillApplicableTaxPercent();
    }

    return null; // Compiler happy
  }

  // ----------------------------

  /**
   * @return never null, "0%" if no taxtable is there
   */
  public String getInvcApplicableTaxPercentFormatted() throws WrongInvoiceTypeException {
    FixedPointNumber applTaxPerc = getInvcApplicableTaxPercent();
    if (applTaxPerc == null) {
      return this.getPercentFormat().format(0);
    }
    return this.getPercentFormat().format(applTaxPerc);
  }

  /**
   * @return never null, "0%" if no taxtable is there
   */
  public String getBillApplicableTaxPercentFormatted() throws WrongInvoiceTypeException {
    FixedPointNumber applTaxPerc = getBillApplicableTaxPercent();
    if (applTaxPerc == null) {
      return this.getPercentFormat().format(0);
    }
    return this.getPercentFormat().format(applTaxPerc);
  }

  /**
   * @return never null, "0%" if no taxtable is there
   */
  public String getJobApplicableTaxPercentFormatted() throws WrongInvoiceTypeException {
    FixedPointNumber applTaxPerc = getJobApplicableTaxPercent();
    if (applTaxPerc == null) {
      return this.getPercentFormat().format(0);
    }
    return this.getPercentFormat().format(applTaxPerc);
  }

  // ---------------------------------------------------------------

  /**
   * @see GnucashGenerInvoiceEntry#getInvcPrice()
   */
  public FixedPointNumber getInvcPrice() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    return new FixedPointNumber(jwsdpPeer.getEntryIPrice());
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcPrice()
   */
  public FixedPointNumber getBillPrice() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    return new FixedPointNumber(jwsdpPeer.getEntryBPrice());
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcPrice()
   */
  public FixedPointNumber getJobPrice() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      return getInvcPrice();
    }
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      return getBillPrice();
    }

    return null; // Compiler happy
  }

  // ----------------------------

  /**
   * {@inheritDoc}
   */
  public String getInvcPriceFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getInvcPrice());
  }

  /**
   * {@inheritDoc}
   */
  public String getBillPriceFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getBillPrice());
  }

  /**
   * {@inheritDoc}
   */
  public String getJobPriceFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getJobPrice());
  }

  // ---------------------------------------------------------------

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSum()
   */
  public FixedPointNumber getInvcSum() throws WrongInvoiceTypeException {
    return getInvcPrice().multiply(getQuantity());
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSumInclTaxes()
   */
  public FixedPointNumber getInvcSumInclTaxes() throws WrongInvoiceTypeException {
    if (jwsdpPeer.getEntryITaxincluded() == 1) {
      return getInvcSum();
    }

    return getInvcSum().multiply(getInvcApplicableTaxPercent().add(1));
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSumExclTaxes()
   */
  public FixedPointNumber getInvcSumExclTaxes() throws WrongInvoiceTypeException {

    // System.err.println("debug: GnucashInvoiceEntryImpl.getSumExclTaxes():"
    // taxIncluded="+jwsdpPeer.getEntryITaxincluded()+" getSum()="+getSum()+"
    // getApplicableTaxPercend()="+getApplicableTaxPercent());

    if (jwsdpPeer.getEntryITaxincluded() == 0) {
      return getInvcSum();
    }

    return getInvcSum().divideBy(getInvcApplicableTaxPercent().add(1));
  }

  // ----------------------------

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSum()
   */
  public String getInvcSumFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getInvcSum());
  }

  /**
   * {@inheritDoc}
   */
  public String getInvcSumInclTaxesFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getInvcSumInclTaxes());
  }

  /**
   * {@inheritDoc}
   */
  public String getInvcSumExclTaxesFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getInvcSumExclTaxes());
  }

  // ----------------------------

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSum()
   */
  public FixedPointNumber getBillSum() throws WrongInvoiceTypeException {
    return getBillPrice().multiply(getQuantity());
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSumInclTaxes()
   */
  public FixedPointNumber getBillSumInclTaxes() throws WrongInvoiceTypeException {
    if (jwsdpPeer.getEntryBTaxincluded() == 1) {
      return getBillSum();
    }

    return getBillSum().multiply(getBillApplicableTaxPercent().add(1));
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSumExclTaxes()
   */
  public FixedPointNumber getBillSumExclTaxes() throws WrongInvoiceTypeException {

    // System.err.println("debug: GnucashInvoiceEntryImpl.getSumExclTaxes():"
    // taxIncluded="+jwsdpPeer.getEntryITaxincluded()+" getSum()="+getSum()+"
    // getApplicableTaxPercend()="+getApplicableTaxPercent());

    if (jwsdpPeer.getEntryBTaxincluded() == 0) {
      return getBillSum();
    }

    return getBillSum().divideBy(getBillApplicableTaxPercent().add(1));
  }

  // ----------------------------

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSum()
   */
  public String getBillSumFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getBillSum());
  }

  /**
   * {@inheritDoc}
   */
  public String getBillSumInclTaxesFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getBillSumInclTaxes());
  }

  /**
   * {@inheritDoc}
   */
  public String getBillSumExclTaxesFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getBillSumExclTaxes());
  }

  // ----------------------------

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSum()
   */
  public FixedPointNumber getJobSum() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      return getInvcSum();
    } else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      return getBillSum();
    }

    return null; // Compiler happy
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSumInclTaxes()
   */
  public FixedPointNumber getJobSumInclTaxes() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      return getInvcSumInclTaxes();
    } else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      return getBillSumInclTaxes();
    }

    return null; // Compiler happy
  }

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSumExclTaxes()
   */
  public FixedPointNumber getJobSumExclTaxes() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      return getInvcSumExclTaxes();
    } else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      return getBillSumExclTaxes();
    }

    return null; // Compiler happy
  }

  // ----------------------------

  /**
   * @see GnucashGenerInvoiceEntry#getInvcSum()
   */
  public String getJobSumFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getJobSum());
  }

  /**
   * {@inheritDoc}
   */
  public String getJobSumInclTaxesFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getJobSumInclTaxes());
  }

  /**
   * {@inheritDoc}
   */
  public String getJobSumExclTaxesFormatted() throws WrongInvoiceTypeException {
    return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getJobSumExclTaxes());
  }

  // ---------------------------------------------------------------

  /**
   * @see GnucashGenerInvoiceEntry#isInvcTaxable()
   */
  public boolean isInvcTaxable() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    return (jwsdpPeer.getEntryITaxable() == 1);
  }

  /**
   * @see GnucashGenerInvoiceEntry#isInvcTaxable()
   */
  public boolean isBillTaxable() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    return (jwsdpPeer.getEntryBTaxable() == 1);
  }

  /**
   * @see GnucashGenerInvoiceEntry#isInvcTaxable()
   */
  public boolean isJobTaxable() throws WrongInvoiceTypeException {
    if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
      throw new WrongInvoiceTypeException();
    }

    GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
    if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER)) {
      return isInvcTaxable();
    } else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR)) {
      return isBillTaxable();
    }

    return false; // Compiler happy
  }

  /**
   * @see GnucashGenerInvoiceEntry#getAction()
   */
  public String getAction() {
    return jwsdpPeer.getEntryAction();
  }

  /**
   * @see GnucashGenerInvoiceEntry#getQuantity()
   */
  public FixedPointNumber getQuantity() {
    String val = getJwsdpPeer().getEntryQty();
    return new FixedPointNumber(val);
  }

  /**
   * {@inheritDoc}
   */
  public String getQuantityFormatted() {
    return getNumberFormat().format(getQuantity());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ZonedDateTime getDate() {
    if (date == null) {
      String dateStr = getJwsdpPeer().getEntryDate().getTsDate();
      try {
        // "2001-09-18 00:00:00 +0200"
        date = ZonedDateTime.parse(dateStr, DATE_FORMAT);
      } catch (Exception e) {
        IllegalStateException ex = new IllegalStateException("unparsable date '" + dateStr + "' in invoice!");
        ex.initCause(e);
        throw ex;
      }

    }
    return date;
  }

  /**
   * @return the Dateformat to use.
   * @see #getDateOpenedFormatted()
   * @see #getDatePostedFormatted()
   */
  protected DateFormat getDateFormat() {
    if (dateFormat == null) {
      if (((GnucashGenerInvoiceImpl) getGenerInvoice()).getDateFormat() != null) {
        dateFormat = ((GnucashGenerInvoiceImpl) getGenerInvoice()).getDateFormat();
      } else {
        dateFormat = DateFormat.getDateInstance();
      }
    }

    return dateFormat;
  }

  /**
   * {@inheritDoc}
   */
  public String getDateFormatted() {
    return getDateFormat().format(getDate());
  }

  /**
   * {@inheritDoc}
   */
  public String getDescription() {
    if (getJwsdpPeer().getEntryDescription() == null) {
      return "";
    }

    return getJwsdpPeer().getEntryDescription();
  }

  /**
   * @return the number-format to use for non-currency-numbers if no locale is given.
   */
  protected NumberFormat getNumberFormat() {
    if (numberFormat == null) {
      numberFormat = NumberFormat.getInstance();
    }

    return numberFormat;
  }

  /**
   * @return the number-format to use for percentage-numbers if no locale is given.
   */
  protected NumberFormat getPercentFormat() {
    if (percentFormat == null) {
      percentFormat = NumberFormat.getPercentInstance();
    }

    return percentFormat;
  }

  // ---------------------------------------------------------------

  /**
   * @return The JWSDP-Object we are wrapping.
   */
  @SuppressWarnings("exports")
  public GncV2.GncBook.GncGncEntry getJwsdpPeer() {
    return jwsdpPeer;
  }

  // ---------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public int compareTo(final GnucashGenerInvoiceEntry otherEntr) {
    try {
      GnucashGenerInvoice otherInvc = otherEntr.getGenerInvoice();
      if (otherInvc != null && getGenerInvoice() != null) {
        int c = otherInvc.compareTo(getGenerInvoice());
        if (c != 0) {
          return c;
        }
      }

      int c = otherEntr.getId().compareTo(getId());
      if (c != 0) {
        return c;
      }

      if (otherEntr != this) {
        LOGGER.error(bundle.getMessage("Err_DuplInvcId", otherEntr.getId(), getId()));
      }

      return 0;

    } catch (Exception e) {
      LOGGER.error("error comparing", e);
      return 0;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[GnucashGenerInvoiceEntryImpl:");
    buffer.append(" id: ");
    buffer.append(getId());
    buffer.append(" type: ");
    try {
      buffer.append(getType());
    } catch (WrongInvoiceTypeException e) {
      buffer.append("ERROR");
    }
    buffer.append(" cust/vend-invoice-id: ");
    buffer.append(getGenerInvoiceID());
    // // buffer.append(" cust/vend-invoice: ");
    // // GnucashGenerInvoice invc = getGenerInvoice();
    // // buffer.append(invoice==null?"null":invc.getName());
    buffer.append(" description: '");
    buffer.append(getDescription() + "'");
    buffer.append(" date: ");
    try {
      buffer.append(getDate().toLocalDate().format(DATE_FORMAT_PRINT));
    } catch (Exception e) {
      buffer.append(getDate().toLocalDate().toString());
    }
    buffer.append(" action: '");
    buffer.append(getAction() + "'");
    buffer.append(" price: ");
    try {
      if (getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
        buffer.append(getInvcPrice());
      } else if (getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
        buffer.append(getBillPrice());
      } else if (getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
        buffer.append(getJobPrice());
      } else {
        buffer.append("ERROR");
      }
    } catch (WrongInvoiceTypeException e) {
      buffer.append("ERROR");
    }
    buffer.append(" quantity: ");
    buffer.append(getQuantity());
    buffer.append("]");
    return buffer.toString();
  }

}
