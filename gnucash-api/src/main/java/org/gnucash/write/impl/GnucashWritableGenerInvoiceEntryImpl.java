package org.gnucash.write.impl;

import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.gnucash.Const;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.IllegalGenerInvoiceEntryActionException;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.impl.spec.GnucashWritableJobInvoiceEntryImpl;
import org.gnucash.write.spec.GnucashWritableJobInvoiceEntry;

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
public class GnucashWritableGenerInvoiceEntryImpl extends GnucashGenerInvoiceEntryImpl 
                                                  implements GnucashWritableGenerInvoiceEntry 
{

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
     * @throws WrongInvoiceTypeException 
     * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
     */
    protected static GncV2.GncBook.GncGncEntry createCustInvoiceEntry(
	    final GnucashWritableGenerInvoiceImpl invc, // important: NOT GnucashWritableCustomerInvoiceImpl
	    final GnucashAccount acct, 
	    final FixedPointNumber quantity, 
	    final FixedPointNumber price) throws WrongInvoiceTypeException {

	if ( ! invc.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) &&
		! invc.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	// TODO: keep count-data in file intact <gnc:count-data
	// cd:type="gnc:GncEntry">18</gnc:count-data>

	if (!invc.isModifiable()) {
	    throw new IllegalArgumentException("The given customer invoice has payments and is" + " thus not modifiable");
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
	    GncV2.GncBook.GncGncEntry.EntryITaxtable taxtableref = factory.createGncV2GncBookGncGncEntryEntryITaxtable();
	    taxtableref.setType(Const.XML_DATA_TYPE_GUID);

	    GCshTaxTable taxTable = null;
	    // ::TODO
	    // GnucashCustomer customer = invoice.getCustomer();
	    // if (customer != null) {
	    // taxTable = customer.getCustomerTaxTable();
	    // }

	    // use first tax-table found
	    if (taxTable == null) {
		taxTable = invc.getFile().getTaxTables().iterator().next();
	    }

	    /*
	     * GncV2Type.GncBookType.GncGncTaxTableType taxtable =
	     * (GncV2Type.GncBookType.GncGncTaxTableType) ((GnucashFileImpl)
	     * invoice.getFile()).getRootElement().getGncBook().getGncGncTaxTable().get(0);
	     * 
	     * taxtableref.setValue(taxtable.getTaxtableGuid().getValue());
	     */
	    taxtableref.setValue(taxTable.getId());
	    entry.setEntryITaxtable(taxtableref);
	}

	entry.setEntryQty(quantity.toGnucashString());
	entry.setVersion(Const.XML_FORMAT_VERSION);

	invc.getFile().getRootElement().getGncBook().getBookElements().add(entry);
	invc.getFile().setModified(true);

	return entry;
    }

    /**
     * @throws WrongInvoiceTypeException 
     * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
     */
    protected static GncV2.GncBook.GncGncEntry createVendBillEntry(
	    final GnucashWritableGenerInvoiceImpl invc, // important: NOT GnucashWritableVendorBillImpl
	    final GnucashAccount acct, 
	    final FixedPointNumber quantity, 
	    final FixedPointNumber price) throws WrongInvoiceTypeException {

	if ( ! invc.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) &&
		! invc.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	// TODO: keep count-data in file intact <gnc:count-data
	// cd:type="gnc:GncEntry">18</gnc:count-data>

	if (!invc.isModifiable()) {
	    throw new IllegalArgumentException("The given vendor bill has payments and is" + " thus not modifiable");
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

	    GncV2.GncBook.GncGncEntry.EntryInvoice inv = factory.createGncV2GncBookGncGncEntryEntryInvoice();
	    inv.setType(Const.XML_DATA_TYPE_GUID);
	    inv.setValue(invc.getId());
	    entry.setEntryInvoice(inv);
	}
	
	entry.setEntryBPrice(price.toGnucashString());
	entry.setEntryBTaxable(1);
	entry.setEntryBTaxincluded(0);
	
	{
	    // TODO: use not the first but the default taxtable
	    GncV2.GncBook.GncGncEntry.EntryBTaxtable taxtableref = factory.createGncV2GncBookGncGncEntryEntryBTaxtable();
	    taxtableref.setType(Const.XML_DATA_TYPE_GUID);

	    GCshTaxTable taxTable = null;
	    // ::TODO
	    // GnucashCustomer customer = invoice.getCustomer();
	    // if (customer != null) {
	    // taxTable = customer.getCustomerTaxTable();
	    // }

	    // use first tax-table found
	    if (taxTable == null) {
		taxTable = invc.getFile().getTaxTables().iterator().next();
	    }

	    /*
	     * GncV2Type.GncBookType.GncGncTaxTableType taxtable =
	     * (GncV2Type.GncBookType.GncGncTaxTableType) ((GnucashFileImpl)
	     * invoice.getFile()).getRootElement().getGncBook().getGncGncTaxTable().get(0);
	     * 
	     * taxtableref.setValue(taxtable.getTaxtableGuid().getValue());
	     */
	    taxtableref.setValue(taxTable.getId());
	    entry.setEntryBTaxtable(taxtableref);
	}

	entry.setEntryQty(quantity.toGnucashString());
	entry.setVersion(Const.XML_FORMAT_VERSION);

	invc.getFile().getRootElement().getGncBook().getBookElements().add(entry);
	invc.getFile().setModified(true);

	return entry;
    }

    /**
     * @throws WrongInvoiceTypeException 
     * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
     */
    protected static GncV2.GncBook.GncGncEntry createJobInvoiceEntry(
	    final GnucashWritableGenerInvoiceImpl invc, // important: NOT GnucashWritableJobInvoiceImpl
	    final GnucashAccount acct, 
	    final FixedPointNumber quantity, 
	    final FixedPointNumber price) throws WrongInvoiceTypeException {
	
	if ( ! invc.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
	    throw new WrongInvoiceTypeException();

	// TODO: keep count-data in file intact <gnc:count-data
	// cd:type="gnc:GncEntry">18</gnc:count-data>

	if (!invc.isModifiable()) {
	    throw new IllegalArgumentException("The given job invoice has payments and is" + " thus not modifiable");
	}
	
	if ( invc.getOwnerType(GnucashGenerInvoice.ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_CUSTOMER) )
	    return createCustInvoiceEntry(invc, acct, quantity, price);
	else if ( invc.getOwnerType(GnucashGenerInvoice.ReadVariant.VIA_JOB).equals(GnucashGenerInvoice.TYPE_VENDOR) )
	    return createVendBillEntry(invc, acct, quantity, price);
	
	return null; // Compiler happy
    }

    @SuppressWarnings("unused")
    private static GncV2.GncBook.GncGncEntry createGenerInvoiceEntryCommon(
	    final GnucashWritableGenerInvoiceImpl invoice,
	    final GnucashWritableFileImpl gnucashFileWritingImpl,
	    final ObjectFactory factory) {

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

	entry.setEntryAction(ACTION_HOURS);
	
	{
	    GncV2.GncBook.GncGncEntry.EntryDate entryDate = factory.createGncV2GncBookGncGncEntryEntryDate();
	    // ::TODO
	    entryDate.setTsDate(ENTRY_DATE_FORMAT.format(new Date()));
	    entry.setEntryDate(entryDate);
	}
	
	entry.setEntryDescription("no description");
	
	{
	    GncV2.GncBook.GncGncEntry.EntryEntered entered = factory.createGncV2GncBookGncGncEntryEntryEntered();
	    // ::TODO
	    entered.setTsDate(ENTRY_DATE_FORMAT.format(new Date()));
	    entry.setEntryEntered(entered);
	}
	
	return entry;
    }
    
    // ---------------------------------------------------------------

    /**
     * @param gnucashFile the file we belong to
     * @param jwsdpPeer   the JWSDP-object we are facading.
     * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GncV2.GncBook.GncGncEntry,
     *      GnucashFileImpl)
     */
    @SuppressWarnings("exports")
    public GnucashWritableGenerInvoiceEntryImpl(final GncV2.GncBook.GncGncEntry jwsdpPeer,
	    final GnucashWritableFileImpl gnucashFile) {
	super(jwsdpPeer, gnucashFile);
    }

    /**
     * @param invoice   tne invoice this entry shall belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GnucashGenerInvoice,
     *      GncV2.GncBook.GncGncEntry)
     */
    @SuppressWarnings("exports")
    public GnucashWritableGenerInvoiceEntryImpl(final GnucashWritableGenerInvoiceImpl invoice,
	    final GncV2.GncBook.GncGncEntry jwsdpPeer) {
	super(invoice, jwsdpPeer, true);

	this.invoice = invoice;
    }

    // -----------------------------------------------------------

    /**
     * Create a taxable invoiceEntry. (It has the taxtable of the customer with a
     * fallback to the first taxtable found assigned)
     *
     * @param invoice  the invoice to add this split to
     * @param account  the income-account the money comes from
     * @param quantity see ${@link GnucashGenerInvoiceEntry#getQuantity()}
     * @param price    see ${@link GnucashGenerInvoiceEntry#getInvcPrice()}}
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    public GnucashWritableGenerInvoiceEntryImpl(final GnucashWritableGenerInvoiceImpl invoice,
	    final GnucashAccount account, final FixedPointNumber quantity, final FixedPointNumber price)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {
	super(invoice, createCustInvoiceEntry(invoice, account, quantity, price), true);

	invoice.addRawGenerEntry(this);
	this.invoice = invoice;
    }

    // -----------------------------------------------------------

    /*
     * public GncV2Type.GncBookType.GncGncEntryType getJwsdpPeer() { return
     * super.getJwsdpPeer(); }
     */

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
     * Set the description-text.
     *
     * @param desc the new description
     */
    public void setDescription(final String desc) {
	if (desc == null) {
	    throw new IllegalArgumentException(
		    "null description given! Please use the empty string instead of null for an empty description");
	}
	if (!this.getGenerInvoice().isModifiable()) {
	    throw new IllegalStateException("This Invoice has payments and is not modifiable!");
	}
	Object old = getDescription();
	getJwsdpPeer().setEntryDescription(desc);

	PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
	if (propertyChangeSupport != null) {
	    propertyChangeSupport.firePropertyChange("description", old, desc);
	}
    }

    // -----------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashGenerInvoiceEntry#isInvcTaxable()
     */
    @Override
    public void setInvcTaxable(final boolean val) throws WrongInvoiceTypeException, NoTaxTableFoundException {

	if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);
	if (val) {
	    getJwsdpPeer().setEntryITaxable(1);
	} else {
	    getJwsdpPeer().setEntryITaxable(0);
	}
	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    public void setInvcTaxTable(final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);

	super.setInvcTaxTable(tax);
	if (tax == null) {
	    getJwsdpPeer().setEntryITaxable(0);
	} else {
	    getJwsdpPeer().setEntryITaxable(1);
	    if (getJwsdpPeer().getEntryITaxtable() == null) {
		getJwsdpPeer().setEntryITaxtable(((GnucashWritableFileImpl) getGenerInvoice().getFile())
			.getObjectFactory().createGncV2GncBookGncGncEntryEntryITaxtable());
		getJwsdpPeer().getEntryITaxtable().setValue(Const.XML_DATA_TYPE_GUID);
	    }
	    getJwsdpPeer().getEntryITaxtable().setValue(tax.getId());
	}

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

    }

    // ------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashGenerInvoiceEntry#isInvcTaxable()
     */
    public void setBillTaxable(final boolean val) throws WrongInvoiceTypeException, NoTaxTableFoundException {

	if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractBillEntry(this);
	if (val) {
	    getJwsdpPeer().setEntryBTaxable(1);
	} else {
	    getJwsdpPeer().setEntryBTaxable(0);
	}
	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addBillEntry(this);

    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    public void setBillTaxTable(final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);

	super.setBillTaxTable(tax);
	if (tax == null) {
	    getJwsdpPeer().setEntryBTaxable(0);
	} else {
	    getJwsdpPeer().setEntryBTaxable(1);
	    if (getJwsdpPeer().getEntryBTaxtable() == null) {
		getJwsdpPeer().setEntryBTaxtable(((GnucashWritableFileImpl) getGenerInvoice().getFile())
			.getObjectFactory().createGncV2GncBookGncGncEntryEntryBTaxtable());
		getJwsdpPeer().getEntryBTaxtable().setValue(Const.XML_DATA_TYPE_GUID);
	    }
	    getJwsdpPeer().getEntryBTaxtable().setValue(tax.getId());
	}

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addBillEntry(this);

    }

    // ------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashGenerInvoiceEntry#isInvcTaxable()
     */
    public void setJobTaxable(final boolean val) throws WrongInvoiceTypeException, NoTaxTableFoundException {

	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashWritableJobInvoiceEntry jobInvcEntr = new GnucashWritableJobInvoiceEntryImpl(this);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
	    setInvcTaxable(val);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
	    setBillTaxable(val);

    }

    /**
     * {@inheritDoc}
     * 
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    public void setJobTaxTable(final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashWritableJobInvoiceEntry jobInvcEntr = new GnucashWritableJobInvoiceEntryImpl(this);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
	    setInvcTaxTable(tax);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
	    setBillTaxTable(tax);

    }

    // -----------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NumberFormatException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
     */
    public void setInvcPrice(final String n)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	this.setInvcPrice(new FixedPointNumber(n));
    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
     */
    public void setInvcPrice(final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	if (!this.getGenerInvoice().isModifiable()) {
	    throw new IllegalStateException("This customer invoice has payments and is not modifiable!");
	}

	Object old = getInvcPrice();

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);
	getJwsdpPeer().setEntryIPrice(price.toGnucashString());
	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

	PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
	if (propertyChangeSupport != null) {
	    propertyChangeSupport.firePropertyChange("price", old, price);
	}

    }

    public void setInvcPriceFormatted(final String n)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	this.setInvcPrice(new FixedPointNumber(n));
    }

    // ------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NumberFormatException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
     */
    @Override
    public void setBillPrice(final String n)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	this.setBillPrice(new FixedPointNumber(n));
    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
     */
    @Override
    public void setBillPrice(final FixedPointNumber price)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	if (!getType().equals(GnucashGenerInvoice.TYPE_VENDOR) && 
		!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	if (!this.getGenerInvoice().isModifiable()) {
	    throw new IllegalStateException("This vendor bill has payments and is not modifiable!");
	}

	Object old = getBillPrice();

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractBillEntry(this);
	getJwsdpPeer().setEntryBPrice(price.toGnucashString());
	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addBillEntry(this);

	PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
	if (propertyChangeSupport != null) {
	    propertyChangeSupport.firePropertyChange("price", old, price);
	}

    }

    public void setBillPriceFormatted(final String n)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	this.setBillPrice(new FixedPointNumber(n));
    }

    // ------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NumberFormatException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
     */
    @Override
    public void setJobPrice(final String n)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {

	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashWritableJobInvoiceEntry jobInvcEntr = new GnucashWritableJobInvoiceEntryImpl(this);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
	    setInvcPrice(n);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
	    setBillPrice(n);

    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
     */
    @Override
    public void setJobPrice(final FixedPointNumber price)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {

	if (!getType().equals(GnucashGenerInvoice.TYPE_JOB))
	    throw new WrongInvoiceTypeException();

	GnucashWritableJobInvoiceEntry jobInvcEntr = new GnucashWritableJobInvoiceEntryImpl(this);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
	    setInvcPrice(price);
	if (jobInvcEntr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
	    setBillPrice(price);

    }

    public void setJobPriceFormatted(final String n)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	this.setJobPrice(new FixedPointNumber(n));
    }

    // -----------------------------------------------------------

    /**
     * @see GnucashWritableGenerInvoiceEntry#setAction(java.lang.String)
     */
    public void setAction(final String action) {
	if (action != null && 
		!action.equals(ACTION_JOB) && 
		!action.equals(ACTION_MATERIAL) && 
		!action.equals(ACTION_HOURS)) {
	    throw new IllegalGenerInvoiceEntryActionException();
	}

	if (!this.getGenerInvoice().isModifiable()) {
	    throw new IllegalStateException("This Invoice has payments and is not modifiable!");
	}

	Object old = getAction();
	getJwsdpPeer().setEntryAction(action);

	PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
	if (propertyChangeSupport != null) {
	    propertyChangeSupport.firePropertyChange("action", old, action);
	}

    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setQuantity(FixedPointNumber)
     */
    public void setQuantity(final String n) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	FixedPointNumber fp = new FixedPointNumber(n);
	System.out.println(
		"DEBUG: GnucashInvoiceEntryWritingImpl.setQuantity('" + n + "') - setting to " + fp.toGnucashString());
	this.setQuantity(fp);
    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setQuantityFormatted(String)
     */
    public void setQuantityFormatted(final String n) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	FixedPointNumber fp = new FixedPointNumber(n);
	System.out.println("DEBUG: GnucashInvoiceEntryWritingImpl.setQuantityFormatted('" + n + "') - setting to "
		+ fp.toGnucashString());
	this.setQuantity(fp);
    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#setQuantity(FixedPointNumber)
     */
    public void setQuantity(final FixedPointNumber quantity)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {
	if (!this.getGenerInvoice().isModifiable()) {
	    throw new IllegalStateException("This Invoice has payments and is not modifiable!");
	}

	Object old = getQuantity();

	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).subtractInvcEntry(this);
	getJwsdpPeer().setEntryQty(quantity.toGnucashString());
	((GnucashWritableGenerInvoiceImpl) getGenerInvoice()).addInvcEntry(this);

	PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
	if (propertyChangeSupport != null) {
	    propertyChangeSupport.firePropertyChange("quantity", old, quantity);
	}

    }

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see GnucashWritableGenerInvoiceEntry#remove()
     */
    public void remove() throws WrongInvoiceTypeException, NoTaxTableFoundException {
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
