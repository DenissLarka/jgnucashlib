package org.gnucash.write.impl;

import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.gnucash.Const;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoice.ReadVariant;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.IllegalGenerInvoiceEntryActionException;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.TaxTableNotFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.impl.spec.GnucashWritableJobInvoiceEntryImpl;
import org.gnucash.write.spec.GnucashWritableJobInvoiceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Additional supported properties for PropertyChangeListeners:
 * <ul>
 * <li>description</li>
 * <li>price</li>
 * <li>quantity</li>
 * <li>action</li>
 * </ul>
 * Entry-Line in an invoice that can be created or removed.
 */
public class GnucashWritableGenerInvoiceEntryImpl extends GnucashGenerInvoiceEntryImpl implements GnucashWritableGenerInvoiceEntry {
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableGenerInvoiceEntryImpl.class);

	/**
	 * Our helper to implement the GnucashWritableObject-interface.
	 */
	private final GnucashWritableObjectImpl helper = new GnucashWritableObjectImpl(this);

	/**
	 * @see {@link #getGenerInvoice()}
	 */
	private GnucashWritableGenerInvoice invoice;

	// -----------------------------------------------------------

	/**
	 * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
	 */
	protected static GncV2.GncBook.GncGncEntry createCustInvoiceEntry_int(final GnucashWritableGenerInvoiceImpl invc, // important:
			// NOT
			// GnucashWritableCustomerInvoiceImpl
			final GnucashAccount acct, final FixedPointNumber quantity, final FixedPointNumber price)
			throws WrongInvoiceTypeException {

		if (!invc.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER)
				&& !invc.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		// TODO: keep count-data in file intact <gnc:count-data
		// cd:type="gnc:GncEntry">18</gnc:count-data>

		if (!invc.isModifiable()) {
			throw new IllegalArgumentException("The given customer invoice has payments and is thus not modifiable");
		}

		GnucashWritableFileImpl gcshWFile = (GnucashWritableFileImpl) invc.getFile();
		ObjectFactory factory = gcshWFile.getObjectFactory();

		GncV2.GncBook.GncGncEntry entry = createGenerInvoiceEntryCommon(invc, gcshWFile, factory);

		{
			GncV2.GncBook.GncGncEntry.EntryIAcct iacct = factory.createGncV2GncBookGncGncEntryEntryIAcct();
			iacct.setType(Const.XML_DATA_TYPE_GUID);
			iacct.setValue(acct.getId());
			entry.setEntryIAcct(iacct);
		}

		entry.setEntryIDiscHow("PRETAX");
		entry.setEntryIDiscType("PERCENT");

		{

			GncV2.GncBook.GncGncEntry.EntryInvoice inv = factory.createGncV2GncBookGncGncEntryEntryInvoice();
			inv.setType(Const.XML_DATA_TYPE_GUID);
			inv.setValue(invc.getId());
			entry.setEntryInvoice(inv);
		}

		entry.setEntryIPrice(price.toGnucashString());
		entry.setEntryITaxable(1);
		entry.setEntryITaxincluded(0);

		{
			// TODO: use not the first but the default taxtable
			GncV2.GncBook.GncGncEntry.EntryITaxtable taxTabRef = factory.createGncV2GncBookGncGncEntryEntryITaxtable();
			taxTabRef.setType(Const.XML_DATA_TYPE_GUID);

			GCshTaxTable taxTab = null;
			// ::TODO
			// GnucashCustomer customer = invoice.getCustomer();
			// if (customer != null) {
			// taxTable = customer.getTaxTable();
			// }

			// use first tax-table found
			if (taxTab == null) {
				taxTab = invc.getFile().getTaxTables().iterator().next();
			}

			/*
			 * GncV2Type.GncBookType.GncGncTaxTableType taxtable = (GncV2Type.GncBookType.GncGncTaxTableType)
			 * ((GnucashFileImpl) invoice.getFile()).getRootElement().getGncBook().getGncGncTaxTable().get(0);
			 *
			 * taxtableref.setValue(taxtable.getTaxtableGuid().getValue());
			 */
			taxTabRef.setValue(taxTab.getId());
			entry.setEntryITaxtable(taxTabRef);
		}

		entry.setEntryQty(quantity.toGnucashString());
		entry.setVersion(Const.XML_FORMAT_VERSION);

		invc.getFile().getRootElement().getGncBook().getBookElements().add(entry);
		invc.getFile().setModified(true);

		return entry;
	}

	/**
	 * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
	 */
	protected static GncV2.GncBook.GncGncEntry createVendBillEntry_int(final GnucashWritableGenerInvoiceImpl invc, // important:
			// NOT
			// GnucashWritableVendorBillImpl
			final GnucashAccount acct, final FixedPointNumber quantity, final FixedPointNumber price)
			throws WrongInvoiceTypeException {

		if (!invc.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !invc.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		// TODO: keep count-data in file intact <gnc:count-data
		// cd:type="gnc:GncEntry">18</gnc:count-data>

		if (!invc.isModifiable()) {
			throw new IllegalArgumentException("The given vendor bill has payments and is thus not modifiable");
		}

		GnucashWritableFileImpl gcshWFile = (GnucashWritableFileImpl) invc.getFile();
		ObjectFactory factory = gcshWFile.getObjectFactory();

		GncV2.GncBook.GncGncEntry entry = createGenerInvoiceEntryCommon(invc, gcshWFile, factory);

		{
			GncV2.GncBook.GncGncEntry.EntryBAcct iacct = factory.createGncV2GncBookGncGncEntryEntryBAcct();
			iacct.setType(Const.XML_DATA_TYPE_GUID);
			iacct.setValue(acct.getId());
			entry.setEntryBAcct(iacct);
		}

		{

			GncV2.GncBook.GncGncEntry.EntryBill bll = factory.createGncV2GncBookGncGncEntryEntryBill();
			bll.setType(Const.XML_DATA_TYPE_GUID);
			bll.setValue(invc.getId());
			entry.setEntryBill(bll);
		}

		entry.setEntryBPrice(price.toGnucashString());
		entry.setEntryBTaxable(1);
		entry.setEntryBTaxincluded(0);

		{
			// TODO: use not the first but the default taxtable
			GncV2.GncBook.GncGncEntry.EntryBTaxtable taxTabRef = factory.createGncV2GncBookGncGncEntryEntryBTaxtable();
			taxTabRef.setType(Const.XML_DATA_TYPE_GUID);

			GCshTaxTable taxTab = null;
			// ::TODO
			// GnucashVendor vend = invoice.getVendor();
			// if (vend != null) {
			// taxTable = vend.getTaxTable();
			// }

			// use first tax-table found
			if (taxTab == null) {
				taxTab = invc.getFile().getTaxTables().iterator().next();
			}

			/*
			 * GncV2Type.GncBookType.GncGncTaxTableType taxtable = (GncV2Type.GncBookType.GncGncTaxTableType)
			 * ((GnucashFileImpl) invoice.getFile()).getRootElement().getGncBook().getGncGncTaxTable().get(0);
			 *
			 * taxtableref.setValue(taxtable.getTaxtableGuid().getValue());
			 */
			taxTabRef.setValue(taxTab.getId());
			entry.setEntryBTaxtable(taxTabRef);
		}

		entry.setEntryQty(quantity.toGnucashString());
		entry.setVersion(Const.XML_FORMAT_VERSION);

		invc.getFile().getRootElement().getGncBook().getBookElements().add(entry);
		invc.getFile().setModified(true);

		return entry;
	}

	/**
	 * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
	 */
	protected static GncV2.GncBook.GncGncEntry createJobInvoiceEntry_int(final GnucashWritableGenerInvoiceImpl invc, // important:
			// NOT
			// GnucashWritableJobInvoiceImpl
			final GnucashAccount acct, final FixedPointNumber quantity, final FixedPointNumber price)
			throws WrongInvoiceTypeException {

		if (!invc.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		// TODO: keep count-data in file intact <gnc:count-data
		// cd:type="gnc:GncEntry">18</gnc:count-data>

		if (!invc.isModifiable()) {
			throw new IllegalArgumentException("The given job invoice has payments and is thus not modifiable");
		}

		if (invc.getOwnerType(GnucashGenerInvoice.ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
			return createCustInvoiceEntry_int(invc, acct, quantity, price);
		} else if (invc.getOwnerType(GnucashGenerInvoice.ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_VENDOR)) {
			return createVendBillEntry_int(invc, acct, quantity, price);
		}

		return null; // Compiler happy
	}

	@SuppressWarnings("unused")
	private static GncV2.GncBook.GncGncEntry createGenerInvoiceEntryCommon(final GnucashWritableGenerInvoiceImpl invoice,
			final GnucashWritableFileImpl gnucashFileWritingImpl, final ObjectFactory factory) {

		// TODO: keep count-data in file intact <gnc:count-data
		// cd:type="gnc:GncEntry">18</gnc:count-data>

		if (!invoice.isModifiable()) {
			throw new IllegalArgumentException("The given invoice has payments and is" + " thus not modifiable");
		}

		GncV2.GncBook.GncGncEntry entry = gnucashFileWritingImpl.createGncGncEntryType();

		{
			GncV2.GncBook.GncGncEntry.EntryGuid guid = factory.createGncV2GncBookGncGncEntryEntryGuid();
			guid.setType(Const.XML_DATA_TYPE_GUID);
			guid.setValue((gnucashFileWritingImpl).createGUID());
			entry.setEntryGuid(guid);
		}

		entry.setEntryAction(Const.ACTION_HOURS);

		{
			GncV2.GncBook.GncGncEntry.EntryDate entryDate = factory.createGncV2GncBookGncGncEntryEntryDate();
			ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault());
			String dateTimeStr = dateTime.format(DATE_FORMAT_BOOK);
			entryDate.setTsDate(dateTimeStr);
			entry.setEntryDate(entryDate);
		}

		entry.setEntryDescription("no description");

		{
			GncV2.GncBook.GncGncEntry.EntryEntered entered = factory.createGncV2GncBookGncGncEntryEntryEntered();
			ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault());
			String dateTimeStr = dateTime.format(DATE_FORMAT_BOOK);
			entered.setTsDate(dateTimeStr);
			entry.setEntryEntered(entered);
		}

		return entry;
	}

	// ---------------------------------------------------------------

	/**
	 * @param gnucashFile the file we belong to
	 * @param jwsdpPeer   the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GncV2.GncBook.GncGncEntry, GnucashFileImpl)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableGenerInvoiceEntryImpl(final GncV2.GncBook.GncGncEntry jwsdpPeer,
			final GnucashWritableFileImpl gnucashFile) {
		super(jwsdpPeer, gnucashFile);
	}

	/**
	 * @param invoice   tne invoice this entry shall belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GnucashGenerInvoice, GncV2.GncBook.GncGncEntry)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableGenerInvoiceEntryImpl(final GnucashWritableGenerInvoiceImpl invoice,
			final GncV2.GncBook.GncGncEntry jwsdpPeer) {
		super(invoice, jwsdpPeer, true);

		this.invoice = invoice;
	}

	/**
	 * Create a taxable invoiceEntry. (It has the taxtable of the customer with a fallback to the first taxtable found
	 * assigned)
	 *
	 * @param invoice  the invoice to add this split to
	 * @param account  the income-account the money comes from
	 * @param quantity see ${@link GnucashGenerInvoiceEntry#getQuantity()}
	 * @param price    see ${@link GnucashGenerInvoiceEntry#getInvcPrice()}}
	 */
	public GnucashWritableGenerInvoiceEntryImpl(final GnucashWritableGenerInvoiceImpl invoice,
			final GnucashAccount account, final FixedPointNumber quantity, final FixedPointNumber price)
			throws WrongInvoiceTypeException, TaxTableNotFoundException {
		super(invoice, createCustInvoiceEntry_int(invoice, account, quantity, price), true);

		invoice.addRawGenerEntry(this);
		this.invoice = invoice;
	}

	public GnucashWritableGenerInvoiceEntryImpl(GnucashGenerInvoiceEntry entry) {
		super(entry.getGenerInvoice(), entry.getJwsdpPeer(), false); // <-- last one: important!
	}

	// -----------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public void setUserDefinedAttribute(final String name, final String value) {
		helper.setUserDefinedAttribute(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GnucashWritableGenerInvoice getGenerInvoice() {
		if (invoice == null) {
			invoice = (GnucashWritableGenerInvoice) super.getGenerInvoice();
		}
		return invoice;

	}

	/**
	 * {@inheritDoc}
	 */
	public void setDate(final LocalDate date) {
		if (date == null) {
			throw new IllegalArgumentException("null date given!");
		}
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}
		ZonedDateTime oldDate = getDate();
		ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.of(date, LocalTime.MIN), ZoneId.systemDefault());
		String dateTimeStr = dateTime.format(DATE_FORMAT_BOOK);
		getJwsdpPeer().getEntryDate().setTsDate(dateTimeStr);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("date", oldDate, date);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(final String descr) {
		if (descr == null) {
			throw new IllegalArgumentException(
					"null description given! Please use the empty string instead of null for an empty description");
		}
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}
		String oldDescr = getDescription();
		getJwsdpPeer().setEntryDescription(descr);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("description", oldDescr, descr);
		}
	}

	// -----------------------------------------------------------

	/**
	 * @see GnucashGenerInvoiceEntry#isInvcTaxable()
	 */
	@Override
	public void setInvcTaxable(final boolean val) throws WrongInvoiceTypeException, TaxTableNotFoundException {

		if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);

		setInvcTaxable_core(val);

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

	}

	private void setInvcTaxable_core(final boolean val) throws WrongInvoiceTypeException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (val) {
			getJwsdpPeer().setEntryITaxable(1);
		} else {
			getJwsdpPeer().setEntryITaxable(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInvcTaxTable(final GCshTaxTable taxTab) throws WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);

		super.setInvcTaxTable(taxTab);
		setInvcTaxTable_core(taxTab);

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

	}

	private void setInvcTaxTable_core(final GCshTaxTable taxTab)
			throws WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (taxTab == null) {
			getJwsdpPeer().setEntryITaxable(0);
		} else {
			getJwsdpPeer().setEntryITaxable(1);
			if (getJwsdpPeer().getEntryITaxtable() == null) {
				getJwsdpPeer().setEntryITaxtable(((GnucashWritableFileImpl) getGenerInvoice().getFile()).getObjectFactory()
						.createGncV2GncBookGncGncEntryEntryITaxtable());
				getJwsdpPeer().getEntryITaxtable().setType(Const.XML_DATA_TYPE_GUID);
			}
			getJwsdpPeer().getEntryITaxtable().setValue(taxTab.getId());
		}
	}

	// ------------------------

	/**
	 * @see GnucashGenerInvoiceEntry#isInvcTaxable()
	 */
	public void setBillTaxable(final boolean val) throws WrongInvoiceTypeException, TaxTableNotFoundException {

		if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractBillEntry(this);

		setBillTaxable_core(val);

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addBillEntry(this);

	}

	private void setBillTaxable_core(final boolean val) throws WrongInvoiceTypeException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (val) {
			getJwsdpPeer().setEntryBTaxable(1);
		} else {
			getJwsdpPeer().setEntryBTaxable(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBillTaxTable(final GCshTaxTable taxTab) throws WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractBillEntry(this);

		super.setBillTaxTable(taxTab);
		setBillTaxTable_core(taxTab);

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addBillEntry(this);

	}

	private void setBillTaxTable_core(final GCshTaxTable taxTab)
			throws WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (taxTab == null) {
			getJwsdpPeer().setEntryBTaxable(0);
		} else {
			getJwsdpPeer().setEntryBTaxable(1);
			if (getJwsdpPeer().getEntryBTaxtable() == null) {
				getJwsdpPeer().setEntryBTaxtable(((GnucashWritableFileImpl) getGenerInvoice().getFile()).getObjectFactory()
						.createGncV2GncBookGncGncEntryEntryBTaxtable());
				getJwsdpPeer().getEntryBTaxtable().setType(Const.XML_DATA_TYPE_GUID);
			}
			getJwsdpPeer().getEntryBTaxtable().setValue(taxTab.getId());
		}
	}

	// ------------------------

	/**
	 * @see GnucashGenerInvoiceEntry#isInvcTaxable()
	 */
	public void setJobTaxable(final boolean val)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException {

		if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractJobEntry(this);

		setJobTaxable_core(val);

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addJobEntry(this);

	}

	private void setJobTaxable_core(final boolean val) throws WrongInvoiceTypeException, UnknownInvoiceTypeException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (getGenerInvoice().getOwnerType(ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
			setInvcTaxable_core(val);
		} else if (getGenerInvoice().getOwnerType(ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_VENDOR)) {
			setBillTaxable_core(val);
		} else {
			throw new UnknownInvoiceTypeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setJobTaxTable(final GCshTaxTable taxTab)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractJobEntry(this);

		super.setJobTaxTable(taxTab);
		setJobTaxTable_core(taxTab);

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addJobEntry(this);

	}

	private void setJobTaxTable_core(final GCshTaxTable taxTab)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (getGenerInvoice().getOwnerType(ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
			setInvcTaxTable_core(taxTab);
		} else if (getGenerInvoice().getOwnerType(ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_VENDOR)) {
			setBillTaxTable_core(taxTab);
		} else {
			throw new UnknownInvoiceTypeException();
		}
	}

	// -----------------------------------------------------------

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	public void setInvcPrice(final String n)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException {
		this.setInvcPrice(new FixedPointNumber(n));
	}

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	public void setInvcPrice(final FixedPointNumber price) throws WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This customer invoice has payments and is not modifiable!");
		}

		FixedPointNumber oldPrice = getInvcPrice();

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);

		getJwsdpPeer().setEntryIPrice(price.toGnucashString());

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("price", oldPrice, price);
		}

	}

	public void setInvcPriceFormatted(final String n)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException {
		this.setInvcPrice(new FixedPointNumber(n));
	}

	// ------------------------

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	@Override
	public void setBillPrice(final String n)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException {
		this.setBillPrice(new FixedPointNumber(n));
	}

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	@Override
	public void setBillPrice(final FixedPointNumber price)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && !getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This vendor bill has payments and is not modifiable!");
		}

		FixedPointNumber oldPrice = getBillPrice();

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractBillEntry(this);

		getJwsdpPeer().setEntryBPrice(price.toGnucashString());

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addBillEntry(this);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("price", oldPrice, price);
		}

	}

	public void setBillPriceFormatted(final String n)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException {
		this.setBillPrice(new FixedPointNumber(n));
	}

	// ------------------------

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	@Override
	public void setJobPrice(final String n)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException {

		if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		// ::TODO: not quite so -- call "core" variant, as with setTaxable/setTaxTable
		if (getGenerInvoice().getOwnerType(ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
			setInvcPrice(n);
		} else if (getGenerInvoice().getOwnerType(ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_VENDOR)) {
			setBillPrice(n);
		} else {
			throw new UnknownInvoiceTypeException();
		}

	}

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	@Override
	public void setJobPrice(final FixedPointNumber price)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException {

		if (!getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
			throw new WrongInvoiceTypeException();
		}

		// ::TODO: not quite so -- call "core" variant, as with setTaxable/setTaxTable
		GnucashWritableJobInvoiceEntry jobInvcEntr = new GnucashWritableJobInvoiceEntryImpl(this);
		if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
			setInvcPrice(price);
		} else if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
			setBillPrice(price);
		} else {
			throw new UnknownInvoiceTypeException();
		}

	}

	public void setJobPriceFormatted(final String n)
			throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException {
		this.setJobPrice(new FixedPointNumber(n));
	}

	// -----------------------------------------------------------

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setAction(java.lang.String)
	 */
	public void setAction(final String action) {
		if (action != null && !action.equals(Const.ACTION_JOB) && !action.equals(Const.ACTION_MATERIAL)
				&& !action.equals(Const.ACTION_HOURS)) {
			throw new IllegalGenerInvoiceEntryActionException();
		}

		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}

		String oldAction = getAction();
		getJwsdpPeer().setEntryAction(action);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("action", oldAction, action);
		}

	}

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setQuantity(FixedPointNumber)
	 */
	public void setQuantity(final String n) throws WrongInvoiceTypeException, TaxTableNotFoundException {
		FixedPointNumber fp = new FixedPointNumber(n);
		LOGGER.debug("setQuantity('" + n + "') - setting to " + fp.toGnucashString());
		this.setQuantity(fp);
	}

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setQuantityFormatted(String)
	 */
	public void setQuantityFormatted(final String n) throws WrongInvoiceTypeException, TaxTableNotFoundException {
		FixedPointNumber fp = new FixedPointNumber(n);
		LOGGER.debug("setQuantityFormatted('" + n + "') - setting to " + fp.toGnucashString());
		this.setQuantity(fp);
	}

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setQuantity(FixedPointNumber)
	 */
	public void setQuantity(final FixedPointNumber qty) throws WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}

		FixedPointNumber oldQty = getQuantity();

		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);
		getJwsdpPeer().setEntryQty(qty.toGnucashString());
		((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("quantity", oldQty, qty);
		}

	}

	/**
	 * @see GnucashWritableGenerInvoiceEntry#remove()
	 */
	public void remove() throws WrongInvoiceTypeException, TaxTableNotFoundException {
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}
		GnucashWritableGenerInvoiceImpl gnucashInvoiceWritingImpl = ((GnucashWritableGenerInvoiceImpl) getGenerInvoice());
		gnucashInvoiceWritingImpl.removeInvcEntry(this);
		gnucashInvoiceWritingImpl.getFile().getRootElement().getGncBook().getBookElements().remove(this.getJwsdpPeer());
		((GnucashWritableFileImpl) gnucashInvoiceWritingImpl.getFile()).decrementCountDataFor("gnc:GncEntry");
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashWritableFile getWritableGnucashFile() {
		return (GnucashWritableFile) super.getGnucashFile();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GnucashWritableFile getGnucashFile() {
		return (GnucashWritableFile) super.getGnucashFile();
	}

}
