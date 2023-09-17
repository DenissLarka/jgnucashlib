/**
 * GnucashTransactionWritingImpl.java
 * Created on 16.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * <p>
 * Permission is granted to use, modify, publish and sub-license this code
 * as specified in the contract. If nothing else is specified these rights
 * are given non-exclusively with no restrictions solely to the contractor(s).
 * If no specified otherwise I reserve the right to use, modify, publish and
 * sub-license this code to other parties myself.
 * <p>
 * Otherwise, this code is made available under GPLv3 or later.
 * <p>
 * -----------------------------------------------------------
 * major Changes:
 * 16.05.2005 - initial version
 * ...
 */
package org.gnucash.write.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedList;

import org.gnucash.Const;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashTransactionImpl;
import org.gnucash.read.impl.GnucashTransactionSplitImpl;
import org.gnucash.write.GnucashWritableObject;
import org.gnucash.write.GnucashWritableTransaction;
import org.gnucash.write.GnucashWritableTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWSDP-Implmentation of a Transaction that can be changed.
 */
public class GnucashTransactionWritingImpl extends GnucashTransactionImpl implements GnucashWritableTransaction {

	/**
	 * Our logger for debug- and error-ourput.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashTransactionWritingImpl.class);

	/**
	 * Our helper to implement the GnucashWritableObject-interface.
	 */
	private final GnucashWritableObjectHelper helper = new GnucashWritableObjectHelper(this);

	/**
	 * @see GnucashWritableObject#setUserDefinedAttribute(java.lang.String, java.lang.String)
	 */
	public void setUserDefinedAttribute(final String name, final String value) {
		helper.setUserDefinedAttribute(name, value);
	}

	/**
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 */
	public GnucashTransactionWritingImpl(final GncTransaction jwsdpPeer, final GnucashFileImpl file) {
		super(jwsdpPeer, file);

		// repair a broken file
		if (jwsdpPeer.getTrnDatePosted() == null) {
			LOGGER.warn("repairing broken transaction " + jwsdpPeer.getTrnId() + " with no date-posted!");
			//we use our own ObjectFactory because:   Exception in thread "AWT-EventQueue-0" java.lang.IllegalAccessError: tried to access
			// method org.gnucash.write.jwsdpimpl.GnucashFileImpl.getObjectFactory()
			// Lbiz/wolschon/fileformats/gnucash/jwsdpimpl/generated/ObjectFactory; from class org.gnucash.write.jwsdpimpl
			// .GnucashTransactionWritingImpl
			//ObjectFactory factory =  file.getObjectFactory();
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
	public GnucashTransactionWritingImpl(final GnucashFileWritingImpl file, final String id) {
		super(createTransaction(file, id), file);
		file.addTransaction(this);
	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	public GnucashFileWritingImpl getWritingFile() {
		return (GnucashFileWritingImpl) getGnucashFile();
	}

	/**
	 * Create a new split for a split found in the jaxb-data.
	 *
	 * @param element the jaxb-data
	 * @return the new split-instance
	 */
	@Override
	protected GnucashTransactionSplitImpl createSplit(final GncTransaction.TrnSplits.TrnSplit element) {
		GnucashTransactionSplitWritingImpl gnucashTransactionSplitWritingImpl = new GnucashTransactionSplitWritingImpl(element, this);
		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange("splits", null, getWritingSplits());
		}
		return gnucashTransactionSplitWritingImpl;
	}

	/**
	 * @see GnucashWritableTransaction#createWritingSplit(GnucashAccount)
	 */
	public GnucashWritableTransactionSplit createWritingSplit(final GnucashAccount account) {
		GnucashTransactionSplitWritingImpl gnucashTransactionSplitWritingImpl = new GnucashTransactionSplitWritingImpl(this, account);
		addSplit(gnucashTransactionSplitWritingImpl);
		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange("splits", null, getWritingSplits());
		}
		return gnucashTransactionSplitWritingImpl;
	}

	/**
	 * @see GnucashWritableTransaction#createWritingSplit(GnucashAccount)
	 */
	public GnucashWritableTransactionSplit createWritingSplit(final GnucashAccount account, final String splitID) {
		GnucashTransactionSplitWritingImpl gnucashTransactionSplitWritingImpl = new GnucashTransactionSplitWritingImpl(this, account, splitID);
		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange("splits", null, getWritingSplits());
		}
		return gnucashTransactionSplitWritingImpl;
	}

	/**
	 * Creates a new Transaction and add's it to the given gnucash-file
	 * Don't modify the ID of the new transaction!
	 */
	protected static GncTransaction createTransaction(final GnucashFileWritingImpl file, final String newId) {

		ObjectFactory factory = file.getObjectFactory();
		GncTransaction transaction = file.createGncTransaction();

		{
			GncTransaction.TrnId id = factory.createGncTransactionTrnId();
			id.setType("guid");
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
			currency.setCmdtySpace("ISO4217");
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
		getJwsdpPeer().getTrnSplits().getTrnSplit().remove(((GnucashTransactionSplitWritingImpl) impl).getJwsdpPeer());
		getWritingFile().setModified(true);
		if (mySplits != null) {
			mySplits.remove(impl);
		}
		GnucashAccountWritingImpl account = (GnucashAccountWritingImpl)
				impl.getAccount();
		if (account != null) {
			account.removeTransactionSplit(impl);
		}

		//there is no count for splits up to now getWritingFile().decrementCountDataFor()

		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange("splits", null, getWritingSplits());
		}
	}

	/**
	 * @see GnucashWritableTransaction#getWritingFirstSplit()
	 */
	@Override
	public GnucashWritableTransactionSplit getFirstSplit() {
		return (GnucashWritableTransactionSplit) super.getFirstSplit();
	}

	/**
	 * @see GnucashWritableTransaction#getWritingFirstSplit()
	 */
	public GnucashWritableTransactionSplit getWritingFirstSplit() {
		return (GnucashWritableTransactionSplit) super.getFirstSplit();
	}

	/**
	 * @see GnucashWritableTransaction#getWritingSecondSplit()
	 */
	@Override
	public GnucashWritableTransactionSplit getSecondSplit() {
		return (GnucashWritableTransactionSplit) super.getSecondSplit();
	}

	/**
	 * @see GnucashWritableTransaction#getWritingSecondSplit()
	 */
	public GnucashWritableTransactionSplit getWritingSecondSplit() {
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
	public Collection getWritingSplits() {
		return super.getSplits();
	}

	/**
	 * @param impl the split to add to mySplits
	 */
	protected void addSplit(final GnucashTransactionSplitWritingImpl impl) {
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
	public void setDatePosted(final ZonedDateTime datePosted) {
		this.datePosted = datePosted;
		getJwsdpPeer().getTrnDatePosted().setTsDate(DATE_ENTERED_FORMAT.format(datePosted));
		getWritingFile().setModified(true);
	}

	/**
	 * @see GnucashWritableTransaction#setDescription(java.lang.String)
	 */
	public void setDescription(final String desc) {
		if (desc == null) {
			throw new IllegalArgumentException("null description given! Please use the empty string instead of null for an empty description");
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
	 * @see GnucashWritableTransaction#setTransactionNumber(java.lang.String)
	 */
	public void setTransactionNumber(final String tnum) {
		if (tnum == null) {
			throw new IllegalArgumentException("null transaction-number given! Please use the empty string instead of null for an empty "
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

  @Override
  public void setDatePosted(LocalDateTime datePosted) {
    setDatePosted(datePosted.atZone(ZoneId.systemDefault()));
  }

}
