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
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.aux.GCshTaxTableEntry;
import org.gnucash.read.impl.spec.GnucashJobInvoiceImpl;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of GnucashInvoiceEntry that uses JWSDP.
 */
public class GnucashGenerInvoiceEntryImpl extends GnucashObjectImpl 
                                          implements GnucashGenerInvoiceEntry 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashGenerInvoiceEntryImpl.class);

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
     * The taxtable in the gnucash xml-file. It defines what sales-tax-rates are
     * known.
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
    public GnucashGenerInvoiceEntryImpl(
	    final GnucashGenerInvoice invoice, 
	    final GncV2.GncBook.GncGncEntry peer,
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
    public GnucashGenerInvoiceEntryImpl(
	    final GncV2.GncBook.GncGncEntry peer, 
	    final GnucashFileImpl gncFile) {
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
     * 
     * @throws WrongInvoiceTypeException
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
	    LOGGER.error("file contains an invoice-entry with GUID=" + getId()
		    + " without an invoice-element (customer) AND " + "without a bill-element (vendor)");
	    return "ERROR";
	} else if (entrInvc != null && entrBill == null) {
	    return entrInvc.getValue();
	} else if (entrInvc == null && entrBill != null) {
	    return entrBill.getValue();
	} else if (entrInvc != null && entrBill != null) {
	    LOGGER.error("file contains an invoice-entry with GUID=" + getId()
		    + " with BOTH an invoice-element (customer) and " + "a bill-element (vendor)");
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
		    throw new IllegalStateException("No customer/vendor invoice/bill with id '" + getGenerInvoiceID()
			    + "' for invoiceEntry with id '" + getId() + "'");
		}
	    }
	}
	return myInvoice;
    }

    // ---------------------------------------------------------------

    /**
     * @param aTaxtable the taxtable to set
     * @throws NoTaxTableFoundException
     * @throws WrongInvoiceTypeException
     */
    protected void setInvcTaxTable(final GCshTaxTable aTaxtable)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {
	myInvcTaxtable = aTaxtable;
    }

    /**
     * @param aTaxtable the taxtable to set
     * @throws NoTaxTableFoundException
     * @throws WrongInvoiceTypeException
     */
    protected void setBillTaxTable(final GCshTaxTable aTaxtable)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {
	myBillTaxtable = aTaxtable;
    }

    protected void setJobTaxTable(final GCshTaxTable aTaxtable)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {
	
	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    setInvcTaxTable(aTaxtable);
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    setBillTaxTable(aTaxtable);
    }

    /**
     * @return The taxtable in the gnucash xml-file. It defines what sales-tax-rates
     *         are known.
     * @throws NoTaxTableFoundException
     * @throws WrongInvoiceTypeException
     */
    public GCshTaxTable getInvcTaxTable() throws NoTaxTableFoundException, WrongInvoiceTypeException {
	if ( ! getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && 
	     ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	if (myInvcTaxtable == null) {
	    EntryITaxtable taxTableEntry = jwsdpPeer.getEntryITaxtable();
	    if (taxTableEntry == null) {
		throw new NoTaxTableFoundException();
	    }

	    String taxTableId = taxTableEntry.getValue();
	    if (taxTableId == null) {
		System.err.println(
			"Customer invoice with id '" + getId() + 
			"' is i-taxable but has empty id for the i-taxtable");
		return null;
	    }
	    myInvcTaxtable = getGnucashFile().getTaxTableByID(taxTableId);

	    if (myInvcTaxtable == null) {
		System.err.println("Customer invoice with id '" + getId() + 
			"' is i-taxable but has an unknown "
			+ "i-taxtable-id '" + taxTableId + "'!");
	    }
	} // myInvcTaxtable == null

	return myInvcTaxtable;
    }

    /**
     * @return The taxtable in the gnucash xml-file. It defines what sales-tax-rates
     *         are known.
     * @throws NoTaxTableFoundException
     * @throws WrongInvoiceTypeException
     */
    public GCshTaxTable getBillTaxTable() throws NoTaxTableFoundException, WrongInvoiceTypeException {
	if ( ! getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && 
	     ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	if (myBillTaxtable == null) {
	    EntryBTaxtable taxTableEntry = jwsdpPeer.getEntryBTaxtable();
	    if (taxTableEntry == null) {
		throw new NoTaxTableFoundException();
	    }

	    String taxTableId = taxTableEntry.getValue();
	    if (taxTableId == null) {
		System.err.println("Vendor bill with id '" + getId() + 
			"' is b-taxable but has empty id for the b-taxtable");
		return null;
	    }
	    myBillTaxtable = getGnucashFile().getTaxTableByID(taxTableId);

	    if (myBillTaxtable == null) {
		System.err.println("Vendor bill with id '" + getId() + 
			"' is b-taxable but has an unknown "
			+ "b-taxtable-id '" + taxTableId + "'!");
	    }
	} // myBillTaxtable == null

	return myBillTaxtable;
    }

    public GCshTaxTable getJobTaxTable() throws NoTaxTableFoundException, WrongInvoiceTypeException {
	if ( ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    return getInvcTaxTable();
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    return getBillTaxTable();

	return null; // Compiler happy
    }

    // ---------------------------------------------------------------

    /**
     * @return e.g. "0.19" for "19%"
     * @throws WrongInvoiceTypeException
     */
    public FixedPointNumber getInvcApplicableTaxPercent() throws WrongInvoiceTypeException {

	if ( ! getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && 
	     ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	if (!isInvcTaxable()) {
	    return new FixedPointNumber();
	}

	if (jwsdpPeer.getEntryITaxtable() != null) {
	    if (!jwsdpPeer.getEntryITaxtable().getType().equals(Const.XML_DATA_TYPE_GUID)) {
		LOGGER.error("Customer invoice entry with id '" + getId() + 
			"' has i-taxtable with type='"
			+ jwsdpPeer.getEntryITaxtable().getType() + "' != 'guid'");
	    }
	}

	GCshTaxTable taxtable = null;
	try {
	    taxtable = getInvcTaxTable();
	} catch (NoTaxTableFoundException exc) {
	    LOGGER.error("Customer invoice entry with id '" + getId()
		    + "' is taxable but JWSDP peer has no i-taxtable-entry! " + "Assuming 0%");
	    return new FixedPointNumber("0");
	}

	if (taxtable == null) {
	    LOGGER.error("Customer invoice entry with id '" + getId() + 
		    "' is taxable but has an unknown i-taxtable! "
		    + "Assuming 19%");
	    return new FixedPointNumber("1900000/10000000");
	}

	GCshTaxTableEntry taxTableEntry = taxtable.getEntries().iterator().next();
	if (!taxTableEntry.getType().equals(GCshTaxTableEntry.TYPE_PERCENT)) {
	    LOGGER.error("Customer invoice entry with id '" + getId() + 
		    "' is taxable but has a i-taxtable "
		    + "that is not in percent but in '" + taxTableEntry.getType() + "'! Assuming 19%");
	    return new FixedPointNumber("1900000/10000000");
	}

	FixedPointNumber val = taxTableEntry.getAmount();

	// the file contains 19 for 19%, we need 0,19
	return ((FixedPointNumber) val.clone()).divideBy(new FixedPointNumber("100"));

    }

    /**
     * @return e.g. "0.19" for "19%"
     * @throws WrongInvoiceTypeException
     */
    public FixedPointNumber getBillApplicableTaxPercent() throws WrongInvoiceTypeException {

	if ( ! getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && 
	     ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	if (!isBillTaxable()) {
	    return new FixedPointNumber();
	}

	if (jwsdpPeer.getEntryBTaxtable() != null) {
	    if (!jwsdpPeer.getEntryBTaxtable().getType().equals(Const.XML_DATA_TYPE_GUID)) {
		LOGGER.error("Vendor bill entry with id '" + getId() + "' has b-taxtable with type='"
			+ jwsdpPeer.getEntryBTaxtable().getType() + "' != 'guid'");
	    }
	}

	GCshTaxTable taxtable = null;
	try {
	    taxtable = getBillTaxTable();
	} catch (NoTaxTableFoundException exc) {
	    LOGGER.error("Vendor bill entry with id '" + getId()
		    + "' is taxable but JWSDP peer has no b-taxtable-entry! " + "Assuming 0%");
	    return new FixedPointNumber("0");
	}

	if (taxtable == null) {
	    LOGGER.error("Vendor bill entry with id '" + getId() + 
		    "' is taxable but has an unknown b-taxtable! "
		    + "Assuming 19%");
	    return new FixedPointNumber("1900000/10000000");
	}

	GCshTaxTableEntry taxTableEntry = taxtable.getEntries().iterator().next();
	if (!taxTableEntry.getType().equals(GCshTaxTableEntry.TYPE_PERCENT)) {
	    LOGGER.error("Vendor bill entry with id '" + getId() + 
		    "' is taxable but has a b-taxtable "
		    + "that is not in percent but in '" + taxTableEntry.getType() + "'! Assuming 19%");
	    return new FixedPointNumber("1900000/10000000");
	}

	FixedPointNumber val = taxTableEntry.getAmount();

	// the file contains 19 for 19%, we need 0,19
	return ((FixedPointNumber) val.clone()).divideBy(new FixedPointNumber("100"));

    }

    public FixedPointNumber getJobApplicableTaxPercent() throws WrongInvoiceTypeException {

	if ( ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    return getInvcApplicableTaxPercent();
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    return getBillApplicableTaxPercent();

	return null; // Compiler happy
    }

    // ----------------------------

    /**
     * @return never null, "0%" if no taxtable is there
     * @throws WrongInvoiceTypeException
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
     * @throws WrongInvoiceTypeException
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
     * @throws WrongInvoiceTypeException
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
	if ( ! getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && 
	     ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	return new FixedPointNumber(jwsdpPeer.getEntryIPrice());
    }

    /**
     * @see GnucashGenerInvoiceEntry#getInvcPrice()
     */
    public FixedPointNumber getBillPrice() throws WrongInvoiceTypeException {
	if ( ! getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && 
	     ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	return new FixedPointNumber(jwsdpPeer.getEntryBPrice());
    }

    /**
     * @see GnucashGenerInvoiceEntry#getInvcPrice()
     */
    public FixedPointNumber getJobPrice() throws WrongInvoiceTypeException {
	if ( ! getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    return getInvcPrice();
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    return getBillPrice();

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
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#getInvcSumInclTaxes()
     */
    public FixedPointNumber getInvcSumInclTaxes() throws WrongInvoiceTypeException {
	if (jwsdpPeer.getEntryITaxincluded() == 1) {
	    return getInvcSum();
	}

	return getInvcSum().multiply(getInvcApplicableTaxPercent().add(1));
    }

    /**
     * @throws WrongInvoiceTypeException
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
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#getInvcSum()
     */
    public String getInvcSumFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getInvcSum());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     */
    public String getInvcSumInclTaxesFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getInvcSumInclTaxes());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
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
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#getInvcSumInclTaxes()
     */
    public FixedPointNumber getBillSumInclTaxes() throws WrongInvoiceTypeException {
	if (jwsdpPeer.getEntryBTaxincluded() == 1) {
	    return getBillSum();
	}

	return getBillSum().multiply(getBillApplicableTaxPercent().add(1));
    }

    /**
     * @throws WrongInvoiceTypeException
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
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#getInvcSum()
     */
    public String getBillSumFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getBillSum());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     */
    public String getBillSumInclTaxesFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getBillSumInclTaxes());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     */
    public String getBillSumExclTaxesFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getBillSumExclTaxes());
    }

    // ----------------------------

    /**
     * @see GnucashGenerInvoiceEntry#getInvcSum()
     */
    public FixedPointNumber getJobSum() throws WrongInvoiceTypeException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    return getInvcSum();
	else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    return getBillSum();

	return null; // Compiler happy
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#getInvcSumInclTaxes()
     */
    public FixedPointNumber getJobSumInclTaxes() throws WrongInvoiceTypeException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    return getInvcSumInclTaxes();
	else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    return getBillSumInclTaxes();

	return null; // Compiler happy
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#getInvcSumExclTaxes()
     */
    public FixedPointNumber getJobSumExclTaxes() throws WrongInvoiceTypeException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    return getInvcSumExclTaxes();
	else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    return getBillSumExclTaxes();

	return null; // Compiler happy
    }

    // ----------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#getInvcSum()
     */
    public String getJobSumFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getJobSum());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     */
    public String getJobSumInclTaxesFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getJobSumInclTaxes());
    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     */
    public String getJobSumExclTaxesFormatted() throws WrongInvoiceTypeException {
	return ((GnucashGenerInvoiceImpl) getGenerInvoice()).getCurrencyFormat().format(getJobSumExclTaxes());
    }

    // ---------------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#isInvcTaxable()
     */
    public boolean isInvcTaxable() throws WrongInvoiceTypeException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	return (jwsdpPeer.getEntryITaxable() == 1);
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#isInvcTaxable()
     */
    public boolean isBillTaxable() throws WrongInvoiceTypeException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	return (jwsdpPeer.getEntryBTaxable() == 1);
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashGenerInvoiceEntry#isInvcTaxable()
     */
    public boolean isJobTaxable() throws WrongInvoiceTypeException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashJobInvoice jobInvc = new GnucashJobInvoiceImpl(getGenerInvoice());
	if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_CUSTOMER))
	    return isInvcTaxable();
	else if (jobInvc.getJobType().equals(GnucashGenerJob.TYPE_VENDOR))
	    return isBillTaxable();

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
	 * @see #getDateOpenedFormatted()
	 * @see #getDatePostedFormatted()
	 * @return the Dateformat to use.
	 */
	protected DateFormat getDateFormat() {
		if ( dateFormat == null ) {
		    if ( ((GnucashGenerInvoiceImpl) getGenerInvoice()).getDateFormat() != null ) {
			dateFormat = ((GnucashGenerInvoiceImpl) getGenerInvoice()).getDateFormat();
		    }
		    else {
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
     * @return the number-format to use for non-currency-numbers if no locale is
     *         given.
     */
    protected NumberFormat getNumberFormat() {
	if (numberFormat == null) {
	    numberFormat = NumberFormat.getInstance();
	}

	return numberFormat;
    }

    /**
     * @return the number-format to use for percentage-numbers if no locale is
     *         given.
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
		LOGGER.error("Duplicate invoice-entry-id!! " + otherEntr.getId() + " and " + getId());
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
//      //      buffer.append(" cust/vend-invoice: ");
//      //      GnucashGenerInvoice invc = getGenerInvoice();
//      //      buffer.append(invoice==null?"null":invc.getName());
	buffer.append(" description: '");
	buffer.append(getDescription() + "'");
	buffer.append(" date: ");
	try {
	    buffer.append(getDate().toLocalDate().format(DATE_FORMAT_PRINT));
	}
	catch (Exception e) {
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
	    } else
		buffer.append("ERROR");
	} catch (WrongInvoiceTypeException e) {
	    buffer.append("ERROR");
	}
	buffer.append(" quantity: ");
	buffer.append(getQuantity());
	buffer.append("]");
	return buffer.toString();
    }

}
