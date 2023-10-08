package org.gnucash.write.impl;

import java.text.ParseException;

import org.gnucash.Const;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.Slot;
import org.gnucash.generated.SlotValue;
import org.gnucash.generated.SlotsType;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.IllegalTransactionSplitActionException;
import org.gnucash.read.impl.GnucashTransactionSplitImpl;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableObject;
import org.gnucash.write.GnucashWritableTransaction;
import org.gnucash.write.GnucashWritableTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction-Split that can be newly created or removed from it's transaction.
 */
public class GnucashWritableTransactionSplitImpl extends GnucashTransactionSplitImpl 
                                                 implements GnucashWritableTransactionSplit 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableTransactionSplitImpl.class);

    	/**
	 * Our helper to implement the GnucashWritableObject-interface.
	 */
	private final GnucashWritableObjectImpl helper = new GnucashWritableObjectImpl(this);

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
	@SuppressWarnings("exports")
	public GnucashWritableTransactionSplitImpl(
		final GncTransaction.TrnSplits.TrnSplit jwsdpPeer, 
		final GnucashWritableTransaction transaction) {
		super(jwsdpPeer, transaction);
	}

	/**
	 * create a new split and and add it to the given transaction.
	 *
	 * @param transaction transaction the transaction we will belong to
	 * @param account     the account we take money (or other things) from or give it to
	 */
	public GnucashWritableTransactionSplitImpl(
		final GnucashWritableTransactionImpl transaction, 
		final GnucashAccount account) {
		super(createTransactionSplit(transaction, account, 
				(transaction.getWritingFile()).createGUID()), 
				transaction);

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
	protected static GncTransaction.TrnSplits.TrnSplit createTransactionSplit(
		final GnucashWritableTransactionImpl transaction,
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

		GnucashWritableFileImpl gnucashFileImpl = transaction.getWritingFile();
		ObjectFactory factory = gnucashFileImpl.getObjectFactory();

		GncTransaction.TrnSplits.TrnSplit split = gnucashFileImpl.createGncTransactionTypeTrnSplitsTypeTrnSplitType();
		{
			GncTransaction.TrnSplits.TrnSplit.SplitId id = factory.createGncTransactionTrnSplitsTrnSplitSplitId();
			id.setType(Const.XML_DATA_TYPE_GUID);
			id.setValue(pSplitID);
			split.setSplitId(id);
		}

		split.setSplitReconciledState(GnucashTransactionSplit.NREC);

		split.setSplitQuantity("0/100");
		split.setSplitValue("0/100");
		{
			GncTransaction.TrnSplits.TrnSplit.SplitAccount splitaccount = factory.createGncTransactionTrnSplitsTrnSplitSplitAccount();
			splitaccount.setType(Const.XML_DATA_TYPE_GUID);
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
		getJwsdpPeer().getSplitAccount().setType(Const.XML_DATA_TYPE_GUID);
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
	 * @param action null, or one of the defined ACTION_xyz values
	 * @throws IllegalTransactionSplitActionException 
	 */
	public void setSplitAction(final String action) throws IllegalTransactionSplitActionException {
//		if ( action != null &&
//             ! action.equals(ACTION_PAYMENT) &&
//             ! action.equals(ACTION_INVOICE) &&
//             ! action.equals(ACTION_BILL) && 
//             ! action.equals(ACTION_BUY) && 
//             ! action.equals(ACTION_SELL) ) {
//                throw new IllegalSplitActionException();
//		}

		String old = getJwsdpPeer().getSplitAction();
		getJwsdpPeer().setSplitAction(action);
		((GnucashWritableFile) getGnucashFile()).setModified(true);

		if (old == null || !old.equals(action)) {
			if (getPropertyChangeSupport() != null) {
				getPropertyChangeSupport().firePropertyChange("splitAction", old, action);
			}
		}
	}

	public void setLotID(final String lotID) {

		GnucashWritableTransactionImpl transaction = (GnucashWritableTransactionImpl) getTransaction();
		GnucashWritableFileImpl writingFile = transaction.getWritingFile();
		ObjectFactory factory = writingFile.getObjectFactory();

		if (getJwsdpPeer().getSplitLot() == null) {
			GncTransaction.TrnSplits.TrnSplit.SplitLot lot = factory.createGncTransactionTrnSplitsTrnSplitSplitLot();
			getJwsdpPeer().setSplitLot(lot);
		}
		getJwsdpPeer().getSplitLot().setValue(lotID);
		getJwsdpPeer().getSplitLot().setType(Const.XML_DATA_TYPE_GUID);

		// if we have a lot, and if we are a paying transaction, then check the slots
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
			value.getContent().add(GnucashTransaction.TYPE_PAYMENT);
			slot.setSlotValue(value);
			slots.getSlot().add(slot);
		}

	}

	// --------------------- support for propertyChangeListeners ---------------

	/**
	 * @see GnucashWritableTransactionSplit#setQuantityFormattedForHTML(java.lang.String)
	 */
	public void setQuantityFormattedForHTML(final String n) {
		this.setQuantity(n);
	}

	/**
	 * @see GnucashWritableTransactionSplit#setValueFormattedForHTML(java.lang.String)
	 */
	public void setValueFormattedForHTML(final String n) {
		this.setValue(n);
	}

	/**
	 * ${@inheritDoc}.
	 */
	public GnucashWritableFile getWritableGnucashFile() {
		return (GnucashWritableFile) getGnucashFile();
	}
}
