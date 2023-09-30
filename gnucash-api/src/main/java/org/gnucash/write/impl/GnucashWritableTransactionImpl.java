package org.gnucash.write.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;

import org.gnucash.Const;
import org.gnucash.currency.CurrencyNameSpace;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashTransactionImpl;
import org.gnucash.read.impl.GnucashTransactionSplitImpl;
import org.gnucash.read.impl.SplitNotFoundException;
import org.gnucash.write.GnucashWritableObject;
import org.gnucash.write.GnucashWritableTransaction;
import org.gnucash.write.GnucashWritableTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWSDP-Implmentation of a Transaction that can be changed.
 */
public class GnucashWritableTransactionImpl extends GnucashTransactionImpl 
                                            implements GnucashWritableTransaction 
{

    /**
     * Our logger for debug- and error-ourput.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableTransactionImpl.class);

    /**
     * Our helper to implement the GnucashWritableObject-interface.
     */
    private final GnucashWritableObjectImpl helper = new GnucashWritableObjectImpl(this);

    // -----------------------------------------------------------

    /**
     * @param file      the file we belong to
     * @param jwsdpPeer the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
    public GnucashWritableTransactionImpl(final GncTransaction jwsdpPeer, final GnucashFileImpl file) {
	super(jwsdpPeer, file);

	// repair a broken file
	if (jwsdpPeer.getTrnDatePosted() == null) {
	    LOGGER.warn("repairing broken transaction " + jwsdpPeer.getTrnId() + " with no date-posted!");
	    // we use our own ObjectFactory because: Exception in thread "AWT-EventQueue-0"
	    // java.lang.IllegalAccessError: tried to access
	    // method org.gnucash.write.jwsdpimpl.GnucashFileImpl.getObjectFactory()
	    // Lbiz/wolschon/fileformats/gnucash/jwsdpimpl/generated/ObjectFactory; from
	    // class org.gnucash.write.jwsdpimpl
	    // .GnucashTransactionWritingImpl
	    // ObjectFactory factory = file.getObjectFactory();
	    ObjectFactory factory = new ObjectFactory();
	    GncTransaction.TrnDatePosted datePosted = factory.createGncTransactionTrnDatePosted();
	    datePosted.setTsDate(jwsdpPeer.getTrnDateEntered().getTsDate());
	    jwsdpPeer.setTrnDatePosted(datePosted);
	}

    }

    /**
     * Create a new Transaction and add it to the file.
     *
     * @param file the file we belong to
     */
    public GnucashWritableTransactionImpl(final GnucashWritableFileImpl file) {
	super(createTransaction(file, file.createGUID()), file);
	file.addTransaction(this);
    }

    public GnucashWritableTransactionImpl(final GnucashTransaction trx) {
	super(trx.getJwsdpPeer(), trx.getGnucashFile());

	// ::TODO
	System.err.println("NOT IMPLEMENTED YET");
//	    for ( GnucashTransactionSplit splt : trx.getSplits() ) 
//	    {
//		addSplit(new GnucashTransactionSplitImpl(splt.getJwsdpPeer(), trx));
//	    }
    }

    // -----------------------------------------------------------

    /**
     * @see GnucashWritableObject#setUserDefinedAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void setUserDefinedAttribute(final String name, final String value) {
	helper.setUserDefinedAttribute(name, value);
    }

    /**
     * The gnucash-file is the top-level class to contain everything.
     *
     * @return the file we are associated with
     */
    public GnucashWritableFileImpl getWritingFile() {
	return (GnucashWritableFileImpl) getGnucashFile();
    }

    /**
     * Create a new split for a split found in the jaxb-data.
     *
     * @param element the jaxb-data
     * @return the new split-instance
     */
    @Override
    protected GnucashTransactionSplitImpl createSplit(final GncTransaction.TrnSplits.TrnSplit element) {
	GnucashWritableTransactionSplitImpl gnucashTransactionSplitWritingImpl = new GnucashWritableTransactionSplitImpl(
		element, this);
	if (getPropertyChangeSupport() != null) {
	    getPropertyChangeSupport().firePropertyChange("splits", null, getWritingSplits());
	}
	return gnucashTransactionSplitWritingImpl;
    }

    /**
     * @see GnucashWritableTransaction#createWritingSplit(GnucashAccount)
     */
    public GnucashWritableTransactionSplit createWritingSplit(final GnucashAccount account) {
	GnucashWritableTransactionSplitImpl splt = new GnucashWritableTransactionSplitImpl(this, account);
	addSplit(splt);
	if (getPropertyChangeSupport() != null) {
	    getPropertyChangeSupport().firePropertyChange("splits", null, getWritingSplits());
	}
	return splt;
    }

    /**
     * Creates a new Transaction and add's it to the given gnucash-file Don't modify
     * the ID of the new transaction!
     */
    protected static GncTransaction createTransaction(final GnucashWritableFileImpl file, final String newId) {

	ObjectFactory factory = file.getObjectFactory();
	GncTransaction transaction = file.createGncTransaction();

	{
	    GncTransaction.TrnId id = factory.createGncTransactionTrnId();
	    id.setType(Const.XML_DATA_TYPE_GUID);
	    id.setValue(newId);
	    transaction.setTrnId(id);
	}

	{
	    GncTransaction.TrnDateEntered dateEntered = factory.createGncTransactionTrnDateEntered();
	    dateEntered.setTsDate(DATE_ENTERED_FORMAT.format(ZonedDateTime.now()));
	    transaction.setTrnDateEntered(dateEntered);
	}

	{
	    GncTransaction.TrnDatePosted datePosted = factory.createGncTransactionTrnDatePosted();
	    datePosted.setTsDate(DATE_ENTERED_FORMAT.format(ZonedDateTime.now()));
	    transaction.setTrnDatePosted(datePosted);
	}

	{
	    GncTransaction.TrnCurrency currency = factory.createGncTransactionTrnCurrency();
	    currency.setCmdtyId(file.getDefaultCurrencyID());
	    currency.setCmdtySpace(CurrencyNameSpace.NAMESPACE_CURRENCY);
	    transaction.setTrnCurrency(currency);
	}

	{
	    GncTransaction.TrnSplits splits = factory.createGncTransactionTrnSplits();
	    transaction.setTrnSplits(splits);
	}

	transaction.setVersion(Const.XML_FORMAT_VERSION);
	transaction.setTrnDescription("-");

	return transaction;
    }

    /**
     * @param impl the split to remove from this transaction
     */
    public void remove(final GnucashWritableTransactionSplit impl) {
	getJwsdpPeer().getTrnSplits().getTrnSplit().remove(((GnucashWritableTransactionSplitImpl) impl).getJwsdpPeer());
	getWritingFile().setModified(true);
	if (mySplits != null) {
	    mySplits.remove(impl);
	}
	GnucashWritableAccountImpl account = (GnucashWritableAccountImpl) impl.getAccount();
	if (account != null) {
	    account.removeTransactionSplit(impl);
	}

	// there is no count for splits up to now
	// getWritingFile().decrementCountDataFor()

	if (getPropertyChangeSupport() != null) {
	    getPropertyChangeSupport().firePropertyChange("splits", null, getWritingSplits());
	}
    }

    /**
     * @throws SplitNotFoundException 
     * @see GnucashWritableTransaction#getWritingFirstSplit()
     */
    @Override
    public GnucashWritableTransactionSplit getFirstSplit() throws SplitNotFoundException {
	return (GnucashWritableTransactionSplit) super.getFirstSplit();
    }

    /**
     * @see GnucashWritableTransaction#getWritingFirstSplit()
     */
    public GnucashWritableTransactionSplit getWritingFirstSplit() throws SplitNotFoundException {
	return (GnucashWritableTransactionSplit) super.getFirstSplit();
    }

    /**
     * @see GnucashWritableTransaction#getWritingSecondSplit()
     */
    @Override
    public GnucashWritableTransactionSplit getSecondSplit()  throws SplitNotFoundException {
	return (GnucashWritableTransactionSplit) super.getSecondSplit();
    }

    /**
     * @see GnucashWritableTransaction#getWritingSecondSplit()
     */
    public GnucashWritableTransactionSplit getWritingSecondSplit()  throws SplitNotFoundException {
	return (GnucashWritableTransactionSplit) super.getSecondSplit();
    }

    /**
     * @see GnucashWritableTransaction#getWritingSplitByID(java.lang.String)
     */
    public GnucashWritableTransactionSplit getWritingSplitByID(final String id) {
	return (GnucashWritableTransactionSplit) super.getSplitByID(id);
    }

    /**
     * @see GnucashWritableTransaction#getWritingSplits()
     */
    @SuppressWarnings("unchecked")
    public Collection<? extends GnucashWritableTransactionSplit> getWritingSplits() {
	return (Collection<? extends GnucashWritableTransactionSplit>) super.getSplits();
    }

    /**
     * @param impl the split to add to mySplits
     */
    protected void addSplit(final GnucashWritableTransactionSplitImpl impl) {
	super.addSplit(impl);
    }

    /**
     * @see GnucashWritableTransaction#remove()
     */
    public void remove() {
	getWritingFile().removeTransaction(this);
	Collection<GnucashWritableTransactionSplit> c = new LinkedList<GnucashWritableTransactionSplit>();
	c.addAll(getWritingSplits());
	for (GnucashWritableTransactionSplit element : c) {
	    element.remove();
	}

    }

    /**
     * @param id the new currency
     * @see #setCurrencyNameSpace(String)
     * @see {@link GnucashTransaction#getCurrencyID()}
     */
    public void setCurrencyID(final String id) {
	this.getJwsdpPeer().getTrnCurrency().setCmdtyId(id);
    }

    /**
     * @param id the new namespace
     * @see {@link GnucashTransaction#getCurrencyNameSpace()}
     */
    public void setCurrencyNameSpace(final String id) {
	this.getJwsdpPeer().getTrnCurrency().setCmdtySpace(id);
    }

    /**
     * @see GnucashWritableTransaction#setDateEntered(LocalDateTime)
     */
    public void setDateEntered(final ZonedDateTime dateEntered) {
	this.dateEntered = dateEntered;
	getJwsdpPeer().getTrnDateEntered().setTsDate(DATE_ENTERED_FORMAT.format(dateEntered));
	getWritingFile().setModified(true);
    }

    /**
     * @see GnucashWritableTransaction#setDatePosted(LocalDateTime)
     */
    public void setDatePosted(final LocalDate datePosted) {
	this.datePosted = ZonedDateTime.of(datePosted, LocalTime.MIN, ZoneId.systemDefault());
	getJwsdpPeer().getTrnDatePosted().setTsDate(DATE_ENTERED_FORMAT.format(this.datePosted));
	getWritingFile().setModified(true);
    }

    /**
     * @see GnucashWritableTransaction#setNotes(java.lang.String)
     */
    public void setDescription(final String desc) {
	if (desc == null) {
	    throw new IllegalArgumentException(
		    "null description given! Please use the empty string instead of null for an empty description");
	}

	String old = getJwsdpPeer().getTrnDescription();
	getJwsdpPeer().setTrnDescription(desc);
	getWritingFile().setModified(true);

	if (old == null || !old.equals(desc)) {
	    if (getPropertyChangeSupport() != null) {
		getPropertyChangeSupport().firePropertyChange("description", old, desc);
	    }
	}
    }

    /**
     * @see GnucashWritableTransaction#setNumber(java.lang.String)
     */
    public void setNumber(final String tnum) {
	if (tnum == null) {
	    throw new IllegalArgumentException(
		    "null transaction-number given! Please use the empty string instead of null for an empty "
			    + "description");
	}

	String old = getJwsdpPeer().getTrnNum();
	getJwsdpPeer().setTrnNum(tnum);
	getWritingFile().setModified(true);

	if (old == null || !old.equals(tnum)) {
	    if (getPropertyChangeSupport() != null) {
		getPropertyChangeSupport().firePropertyChange("transactionNumber", old, tnum);
	    }
	}
    }

    @Override
    public void setDateEntered(LocalDateTime dateEntered) {
	setDateEntered(dateEntered.atZone(ZoneId.systemDefault()));
    }

}
