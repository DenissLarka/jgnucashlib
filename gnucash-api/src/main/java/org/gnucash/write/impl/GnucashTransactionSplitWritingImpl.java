/**
 * GnucashTransactionSplitWritingImpl.java
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

import java.text.ParseException;

import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.Slot;
import org.gnucash.generated.SlotValue;
import org.gnucash.generated.SlotsType;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.impl.GnucashTransactionSplitImpl;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableObject;
import org.gnucash.write.GnucashWritableTransaction;
import org.gnucash.write.GnucashWritableTransactionSplit;

/**
 * created: 16.05.2005 <br/>
 * <p>
 * Transaction-Split that can be newly created or removed from it's transaction.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class GnucashTransactionSplitWritingImpl extends GnucashTransactionSplitImpl implements GnucashWritableTransactionSplit {

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
	 * @see GnucashTransactionSplitImpl#getTransaction()
	 */
	@Override
	public GnucashWritableTransaction getTransaction() {
		return (GnucashWritableTransaction) super.getTransaction();
	}

	/**
	 * @param jwsdpPeer   the JWSDP-object we are facading.
	 * @param transaction the transaction we belong to
	 */
	public GnucashTransactionSplitWritingImpl(final GncTransaction.TrnSplits.TrnSplit jwsdpPeer, final GnucashWritableTransaction transaction) {
		super(jwsdpPeer, transaction);
	}

	/**
	 * create a new split and and add it to the given transaction.
	 *
	 * @param transaction transaction the transaction we will belong to
	 * @param account     the account we take money (or other things) from or give it to
	 */
	public GnucashTransactionSplitWritingImpl(final GnucashTransactionWritingImpl transaction, final GnucashAccount account) {
		this(transaction, account, (transaction.getWritingFile()).createGUID());
	}

	/**
	 * create a new split and and add it to the given transaction.
	 *
	 * @param transaction transaction the transaction we will belong to
	 * @param account     the account we take money (or other things) from or give it to
	 */
	public GnucashTransactionSplitWritingImpl(final GnucashTransactionWritingImpl transaction, final GnucashAccount account, String pSplitID) {
		super(createTransactionSplit(transaction, account, pSplitID), transaction);

		// this is a workaound.
		// if super does account.addSplit(this) it adds an instance on GnucashTransactionSplitImpl that is "!=
		// (GnucashTransactionSplitWritingImpl)this";
		// thus we would get warnings about dublicate split-ids and can no longer compare splits by instance.
		//        if(account!=null)
		//            ((GnucashAccountImpl)account).replaceTransactionSplit(account.getTransactionSplitByID(getId()),
		// GnucashTransactionSplitWritingImpl.this);

		transaction.addSplit(this);
	}

	/**
	 * Creates a new Transaction and add's it to the given gnucash-file
	 * Don't modify the ID of the new transaction!
	 */
	protected static GncTransaction.TrnSplits.TrnSplit createTransactionSplit(final GnucashTransactionWritingImpl transaction,
			final GnucashAccount account,
			final String pSplitID) {

		if (transaction == null) {
			throw new IllegalArgumentException("null transaction given");
		}

		if (account == null) {
			throw new IllegalArgumentException("null account given");
		}

		if (pSplitID == null || pSplitID.trim().length() == 0) {
			throw new IllegalArgumentException("null or empty pSplitID given");
		}

		// this is needed because transaction.addSplit() later
		// must have an already build List of splits.
		// if not it will create the list from the JAXB-Data
		// thus 2 instances of this GnucashTransactionSplitWritingImpl
		// will exist. One created in getSplits() from this JAXB-Data
		// the other is this object.
		transaction.getSplits();

		GnucashFileWritingImpl gnucashFileImpl = transaction.getWritingFile();
		ObjectFactory factory = gnucashFileImpl.getObjectFactory();

		GncTransaction.TrnSplits.TrnSplit split = gnucashFileImpl.createGncTransactionTypeTrnSplitsTypeTrnSplitType();
		{
			GncTransaction.TrnSplits.TrnSplit.SplitId id = factory.createGncTransactionTrnSplitsTrnSplitSplitId();
			id.setType("guid");
			id.setValue(pSplitID);
			split.setSplitId(id);
		}

		split.setSplitReconciledState("n");

		split.setSplitQuantity("0/100");
		split.setSplitValue("0/100");
		{
			GncTransaction.TrnSplits.TrnSplit.SplitAccount splitaccount = factory.createGncTransactionTrnSplitsTrnSplitSplitAccount();
			splitaccount.setType("guid");
			splitaccount.setValue(account.getId());
			split.setSplitAccount(splitaccount);
		}
		return split;
	}

	/**
	 * remove this split from it's transaction.
	 */
	public void remove() {
		getTransaction().remove(this);
	}

	/**
	 * @see GnucashWritableTransactionSplit#setAccount(GnucashAccount)
	 */
	public void setAccountID(final String accountId) {
		setAccount(getTransaction().getGnucashFile().getAccountByID(accountId));
	}

	/**
	 * @see GnucashWritableTransactionSplit#setAccount(GnucashAccount)
	 */
	public void setAccount(final GnucashAccount account) {
		if (account == null) {
			throw new NullPointerException("null account given");
		}
		String old = (getJwsdpPeer().getSplitAccount() == null ? null
				:
						getJwsdpPeer().getSplitAccount().getValue());
		getJwsdpPeer().getSplitAccount().setType("guid");
		getJwsdpPeer().getSplitAccount().setValue(account.getId());
		((GnucashWritableFile) getGnucashFile()).setModified(true);

		if (old == null || !old.equals(account.getId())) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange("accountID", old, account.getId());
			}
		}

	}

	/**
	 * @see GnucashWritableTransactionSplit#setQuantity(FixedPointNumber)
	 */
	public void setQuantity(final String n) {
		try {
			this.setQuantity(new FixedPointNumber(n.toLowerCase().replaceAll("&euro;", "").replaceAll("&pound;", "")));
		}
		catch (NumberFormatException e) {
			try {
				Number parsed = this.getQuantityCurrencyFormat().parse(n);
				this.setQuantity(new FixedPointNumber(parsed.toString()));
			}
			catch (NumberFormatException e1) {
				throw e;
			}
			catch (ParseException e1) {
				throw e;
			}
		}
	}

	/**
	 * @return true if the currency of transaction and account match
	 */
	private boolean isCurrencyMatching() {
		GnucashAccount account = getAccount();
		if (account == null) {
			return false;
		}
		GnucashWritableTransaction transaction = getTransaction();
		if (transaction == null) {
			return false;
		}
		String actCID = account.getCurrencyID();
		if (actCID == null) {
			return false;
		}
		String actCNS = account.getCurrencyNameSpace();
		if (actCNS == null) {
			return false;
		}
		return (actCID.equals(transaction.getCurrencyID())
				&&
				actCNS.equals(transaction.getCurrencyNameSpace())
		);
	}

	/**
	 * @see GnucashWritableTransactionSplit#setQuantity(FixedPointNumber)
	 */
	public void setQuantity(final FixedPointNumber n) {
		if (n == null) {
			throw new NullPointerException("null quantity given");
		}

		String old = getJwsdpPeer().getSplitQuantity();
		getJwsdpPeer().setSplitQuantity(n.toGnucashString());
		((GnucashWritableFile) getGnucashFile()).setModified(true);
		if (isCurrencyMatching()) {
			String oldvalue = getJwsdpPeer().getSplitValue();
			getJwsdpPeer().setSplitValue(n.toGnucashString());
			if (old == null || !old.equals(n.toGnucashString())) {
				if (getPropertyChangeSupport() != null) {
					getPropertyChangeSupport().firePropertyChange("value", new FixedPointNumber(oldvalue), n);
				}
			}
		}

		if (old == null || !old.equals(n.toGnucashString())) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange("quantity", new FixedPointNumber(old), n);
			}
		}
	}

	/**
	 * @see GnucashWritableTransactionSplit#setValue(FixedPointNumber)
	 */
	public void setValue(final String n) {
		try {
			this.setValue(new FixedPointNumber(n.toLowerCase().replaceAll("&euro;", "").replaceAll("&pound;", "")));
		}
		catch (NumberFormatException e) {
			try {
				Number parsed = this.getValueCurrencyFormat().parse(n);
				this.setValue(new FixedPointNumber(parsed.toString()));
			}
			catch (NumberFormatException e1) {
				throw e;
			}
			catch (ParseException e1) {
				throw e;
			}
		}
	}

	/**
	 * @see GnucashWritableTransactionSplit#setValue(FixedPointNumber)
	 */
	public void setValue(final FixedPointNumber n) {
		if (n == null) {
			throw new NullPointerException("null value given");
		}
		String old = getJwsdpPeer().getSplitValue();
		getJwsdpPeer().setSplitValue(n.toGnucashString());
		((GnucashWritableFile) getGnucashFile()).setModified(true);
		if (isCurrencyMatching()) {
			String oldquantity = getJwsdpPeer().getSplitQuantity();
			getJwsdpPeer().setSplitQuantity(n.toGnucashString());
			if (old == null || !old.equals(n.toGnucashString())) {
				if (getPropertyChangeSupport() != null) {
					getPropertyChangeSupport().firePropertyChange("quantity", new FixedPointNumber(oldquantity), n);
				}
			}
		}

		if (old == null || !old.equals(n.toGnucashString())) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange("value", new FixedPointNumber(old), n);
			}
		}
	}

	/**
	 * Set the description-text.
	 *
	 * @param desc the new description
	 */
	public void setDescription(final String desc) {
		if (desc == null) {
			throw new IllegalArgumentException("null description given! Please use the empty string instead of null for an empty description");
		}

		String old = getJwsdpPeer().getSplitMemo();
		getJwsdpPeer().setSplitMemo(desc);
		((GnucashWritableFile) getGnucashFile()).setModified(true);

		if (old == null || !old.equals(desc)) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange("description", old, desc);
			}
		}
	}

	/**
	 * Set the type of association this split has with
	 * an invoice's lot.
	 *
	 * @param action null, "Zahlung" or "Rechnung" or freeform-string
	 */
	public void setSplitAction(final String action) {
		/*if (action != null
				&&
            !action.equals("Zahlung")
                &&
            !action.equals("Rechnung")) {
                throw new IllegalArgumentException("action may only be null, 'Zahlung' or 'Rechnung'");
            }*/

		String old = getJwsdpPeer().getSplitAction();
		getJwsdpPeer().setSplitAction(action);
		((GnucashWritableFile) getGnucashFile()).setModified(true);

		if (old == null || !old.equals(action)) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange("splitAction", old, action);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setLotID(final String lotID) {

		GnucashTransactionWritingImpl transaction = (GnucashTransactionWritingImpl) getTransaction();
		GnucashFileWritingImpl writingFile = transaction.getWritingFile();
		ObjectFactory factory = writingFile.getObjectFactory();

		if (getJwsdpPeer().getSplitLot() == null) {
			GncTransaction.TrnSplits.TrnSplit.SplitLot lot = factory.createGncTransactionTrnSplitsTrnSplitSplitLot();
			getJwsdpPeer().setSplitLot(lot);
		}
		getJwsdpPeer().getSplitLot().setValue(lotID);
		getJwsdpPeer().getSplitLot().setType("guid");

		// if we have a lot, we are a "P"aying transaction, check the slots
		SlotsType slots = getJwsdpPeer().getSplitSlots();
		if (slots == null) {
			slots = factory.createSlotsType();
			getJwsdpPeer().setSplitSlots(slots);
		}
		if (slots.getSlot() == null) {
			Slot slot = factory.createSlot();
			slot.setSlotKey("trans-txn-type");
			SlotValue value = factory.createSlotValue();
			value.setType("string");
			value.getContent().add("P");
			slot.setSlotValue(value);
			slots.getSlot().add(slot);
		}

	}

	// --------------------- support for propertyChangeListeners ---------------

	/**
	 * @see GnucashWritableTransactionSplit#setQuantityFormatetForHTML(java.lang.String)
	 */
	public void setQuantityFormatetForHTML(final String n) {
		this.setQuantity(n);
	}

	/**
	 * @see GnucashWritableTransactionSplit#setValueFormatetForHTML(java.lang.String)
	 */
	public void setValueFormatetForHTML(final String n) {
		this.setValue(n);
	}

	/**
	 * ${@inheritDoc}.
	 */
	public GnucashWritableFile getWritableGnucashFile() {
		return (GnucashWritableFile) getGnucashFile();
	}
}
