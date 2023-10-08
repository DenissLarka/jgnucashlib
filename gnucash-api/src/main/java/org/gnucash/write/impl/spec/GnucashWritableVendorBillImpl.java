package org.gnucash.write.impl.spec;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.impl.aux.WrongOwnerTypeException;
import org.gnucash.read.impl.spec.GnucashVendorBillEntryImpl;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceImpl;
import org.gnucash.write.spec.GnucashWritableVendorBill;
import org.gnucash.write.spec.GnucashWritableVendorBillEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO write a comment what this type does here
 */
public class GnucashWritableVendorBillImpl extends GnucashWritableGenerInvoiceImpl 
                                           implements GnucashWritableVendorBill
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableVendorBillImpl.class);

    /**
     * Create an editable invoice facading an existing JWSDP-peer.
     *
     * @param jwsdpPeer the JWSDP-object we are facading.
     * @param file      the file to register under
     * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice,
     *      GnucashFile)
     */
    @SuppressWarnings("exports")
    public GnucashWritableVendorBillImpl(
	    final GncV2.GncBook.GncGncInvoice jwsdpPeer, 
	    final GnucashFile file) {
	super(jwsdpPeer, file);
    }

    /**
     * @param file the file we are associated with.
     * @throws WrongOwnerTypeException 
     */
    public GnucashWritableVendorBillImpl(
	    final GnucashWritableFileImpl file, 
	    final String number,
	    final GnucashVendor vend, 
	    final GnucashAccountImpl expensesAcct,
	    final GnucashAccountImpl payableAcct,
	    final LocalDate openedDate,
	    final LocalDate postDate,
	    final LocalDate dueDate) throws WrongOwnerTypeException {
	super(createVendorBill_int(file, 
		                   number, vend,
		                   false, // <-- caution!
		                   expensesAcct, payableAcct,
		                   openedDate, postDate, dueDate), 
	      file);
    }

    /**
     * @param file the file we are associated with.
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException 
     */
    public GnucashWritableVendorBillImpl(final GnucashWritableGenerInvoiceImpl invc)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {
	super(invc.getJwsdpPeer(), invc.getFile());

	// No, we cannot check that first, because the super() method
	// always has to be called first.
	if (!invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT).equals(GnucashGenerInvoice.TYPE_VENDOR))
	    throw new WrongInvoiceTypeException();

	// Caution: In the following two loops, we may *not* iterate directly over
	// invc.getGenerEntries(), because else, we will produce a ConcurrentModificationException.
	// (It only works if the invoice has one single entry.)
	// Hence the indirection via the redundant "entries" hash set.
	Collection<GnucashGenerInvoiceEntry> entries = new HashSet<GnucashGenerInvoiceEntry>();
	for ( GnucashGenerInvoiceEntry entry : invc.getGenerEntries() ) {
	    entries.add(entry);
	}
	for ( GnucashGenerInvoiceEntry entry : entries ) {
	    addEntry(new GnucashWritableVendorBillEntryImpl(entry));
	}

	// Caution: Indirection via a redundant "trxs" hash set. 
	// Same reason as above.
	Collection<GnucashTransaction> trxs = new HashSet<GnucashTransaction>();
	for ( GnucashTransaction trx : invc.getPayingTransactions() ) {
	    trxs.add(trx);
	}
	for ( GnucashTransaction trx : trxs ) {
	    for (GnucashTransactionSplit splt : trx.getSplits()) {
		String lot = splt.getLotID();
		if (lot != null) {
		    for (GnucashGenerInvoice invc1 : splt.getTransaction().getGnucashFile().getGenerInvoices()) {
			String lotID = invc1.getLotID();
			if (lotID != null && lotID.equals(lot)) {
			    // Check if it's a payment transaction.
			    // If so, add it to the invoice's list of payment transactions.
			    if (splt.getAction().equals(GnucashTransactionSplit.ACTION_PAYMENT)) {
				addPayingTransaction(splt);
			    }
			} // if lotID
		    } // for invc
		} // if lot
	    } // for splt
	} // for trx
    }

    // ---------------------------------------------------------------

    /**
     * The gnucash-file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    protected GnucashWritableFileImpl getWritingFile() {
	return (GnucashWritableFileImpl) getFile();
    }

    /**
     * support for firing PropertyChangeEvents. (gets initialized only if we really
     * have listeners)
     */
    private volatile PropertyChangeSupport myPropertyChange = null;

    /**
     * Returned value may be null if we never had listeners.
     *
     * @return Our support for firing PropertyChangeEvents
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
	return myPropertyChange;
    }

    /**
     * Add a PropertyChangeListener to the listener list. The listener is registered
     * for all properties.
     *
     * @param listener The PropertyChangeListener to be added
     */
    public final void addPropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange == null) {
	    myPropertyChange = new PropertyChangeSupport(this);
	}
	myPropertyChange.addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property. The listener will be
     * invoked only when a call on firePropertyChange names that specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	if (myPropertyChange == null) {
	    myPropertyChange = new PropertyChangeSupport(this);
	}
	myPropertyChange.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener     The PropertyChangeListener to be removed
     */
    public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(propertyName, listener);
	}
    }

    /**
     * Remove a PropertyChangeListener from the listener list. This removes a
     * PropertyChangeListener that was registered for all properties.
     *
     * @param listener The PropertyChangeListener to be removed
     */
    public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(listener);
	}
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void setVendor(GnucashVendor vend) throws WrongInvoiceTypeException {
	// ::TODO
	GnucashVendor oldVend = getVendor();
	if (oldVend == vend) {
	    return; // nothing has changed
	}

	getJwsdpPeer().getInvoiceOwner().getOwnerId().setValue(vend.getId());
	getWritingFile().setModified(true);

	// <<insert code to react further to this change here
	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	if (propertyChangeFirer != null) {
	    propertyChangeFirer.firePropertyChange("vendor", oldVend, vend);
	}
    }

    // -----------------------------------------------------------

    /**
     * create and add a new entry.
     * 
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    public GnucashWritableVendorBillEntry createEntry(
	    final GnucashAccount acct,
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	GnucashWritableVendorBillEntry entry = createVendBillEntry(acct, 
		                                                   singleUnitPrice, quantity);
	return entry;
    }

    /**
     * create and add a new entry.<br/>
     * The entry will use the accounts of the SKR03.
     * 
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    public GnucashWritableVendorBillEntry createEntry(
	    final GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final String taxTabName) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	GnucashWritableVendorBillEntry entry = createVendBillEntry(acct, 
		                                                   singleUnitPrice, quantity, 
		                                                   taxTabName);
	return entry;
    }

    /**
     * create and add a new entry.<br/>
     *
     * @return an entry using the given Tax-Table
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    public GnucashWritableVendorBillEntry createEntry(
	    final GnucashAccount acct,
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final GCshTaxTable taxTab) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	GnucashWritableVendorBillEntry entry = createVendBillEntry(acct, 
		                                                   singleUnitPrice, quantity, 
		                                                   taxTab);
	return entry;
    }

    // -----------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
     */
    protected void removeEntry(final GnucashWritableVendorBillEntryImpl impl)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {

	removeBillEntry(impl);
    }

    /**
     * Called by
     * ${@link GnucashWritableVendorBillEntryImpl#createVendBillEntry_int(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
     *
     * @param entry the entry to add to our internal list of vendor-bill-entries
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    protected void addEntry(final GnucashWritableVendorBillEntryImpl entry)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {

	addBillEntry(entry);
    }

    protected void subtractEntry(final GnucashGenerInvoiceEntryImpl entry)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException {
	subtractBillEntry(entry);
    }

    /**
     * @return the ID of the Account to transfer the money from
     * @throws WrongInvoiceTypeException
     */
    private String getAccountIDToTransferMoneyFrom(final GnucashVendorBillEntryImpl entry)
	    throws WrongInvoiceTypeException {
	return getBillPostAccountID(entry);
    }

    @Override
    protected String getInvcPostAccountID(final GnucashGenerInvoiceEntryImpl entry)
	    throws WrongInvoiceTypeException {
	throw new WrongInvoiceTypeException();
    }

    @Override
    protected String getJobPostAccountID(final GnucashGenerInvoiceEntryImpl entry)
	    throws WrongInvoiceTypeException {
	throw new WrongInvoiceTypeException();
    }

    /**
     * Throw an IllegalStateException if we are not modifiable.
     *
     * @see #isModifiable()
     */
    protected void attemptChange() {
	if (!isModifiable()) {
	    throw new IllegalStateException(
		    "this vendor bill is NOT changable because there are already payment for it made!");
	}
    }

    /**
     * @see GnucashWritableGenerInvoice#getWritableGenerEntryById(java.lang.String)
     */
    public GnucashWritableVendorBillEntry getWritableEntryById(final String id) {
	return new GnucashWritableVendorBillEntryImpl(getGenerEntryById(id));
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getVendorId() {
	return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    public GnucashVendor getVendor() {
	return getFile().getVendorByID(getVendorId());
    }

    // ---------------------------------------------------------------

    @Override
    public void post(final GnucashAccount expensesAcct, 
	             final GnucashAccount payablAcct, 
	             final LocalDate postDate, 
	             final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException {
	postVendorBill(
		getFile(), 
		this, getVendor(), 
		expensesAcct, payablAcct, 
		postDate, dueDate);
    }

}
