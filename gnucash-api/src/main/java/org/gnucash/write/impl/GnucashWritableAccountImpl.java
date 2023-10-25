package org.gnucash.write.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.gnucash.Const;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.CmdtyCurrNameSpace;
import org.gnucash.generated.GncAccount;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.Slot;
import org.gnucash.generated.SlotValue;
import org.gnucash.generated.SlotsType;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.write.GnucashWritableAccount;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableObject;
import org.gnucash.write.GnucashWritableTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of GnucashAccountImpl to allow writing instead of
 * read-only access.<br/>
 * Supported properties for the propertyChangeListeners:
 * <ul>
 * <li>name</li>
 * <li>code</li>
 * <li>currencyID</li>
 * <li>currencyNameSpace</li>
 * <li>description</li>
 * <li>type</li>
 * <li>parentAccount</li>
 * <li>transactionSplits (not giving the old value of the list)</li>
 * </ul>
 */
public class GnucashWritableAccountImpl extends GnucashAccountImpl 
                                        implements GnucashWritableAccount 
{
	/**
	 * Our logger for debug- and error-ourput.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableAccountImpl.class);

	/**
	 * Our helper to implement the GnucashWritableObject-interface.
	 */
	private GnucashWritableObjectImpl helper;

	/**
	 * {@inheritDoc}
	 *
	 * @see GnucashWritableObject#setUserDefinedAttribute(java.lang.String, java.lang.String)
	 */
	public void setUserDefinedAttribute(final String name, final String value) {
		if (helper == null) {
			helper = new GnucashWritableObjectImpl(super.helper);
		}
		LOGGER.debug("GnucashAccountWritingImpl[account-id="
				+ getId() + " name="
				+ getName() + "].setUserDefinedAttribute(name="
				+ name + ", value="
				+ value + ")");
		helper.setUserDefinedAttribute(name, value);
	}

	/**
	 * @see GnucashAccountImpl#GnucashAccountImpl(GncAccount, GnucashFile)
	 */
	public GnucashWritableAccountImpl(final GncAccount jwsdpPeer, final GnucashFileImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @see GnucashAccountImpl#GnucashAccountImpl(GncAccount, GnucashFile) )
	 */
	public GnucashWritableAccountImpl(final GnucashWritableFileImpl file) {
		super(createAccount(file, file.createGUID()), file);
	}

	/**
	 * @param file
	 * @return
	 */
	private static GncAccount createAccount(final GnucashWritableFileImpl file, final String accountguid) {
		ObjectFactory factory = file.getObjectFactory();

		GncAccount account = factory.createGncAccount();
		//left unset account.setActCode();
		account.setActCommodityScu(100); // x,yz
		account.setActDescription("no description yet");
		//      left unset account.setActLots();
		account.setActName("UNNAMED");
		//      left unset account.setActNonStandardScu();
		//left unset account.setActParent())
		account.setActType(GnucashAccount.TYPE_BANK);

		account.setVersion(Const.XML_FORMAT_VERSION);

		{
			GncAccount.ActCommodity currency = factory.createGncAccountActCommodity();
			currency.setCmdtyId(file.getDefaultCurrencyID());
			currency.setCmdtySpace(CmdtyCurrNameSpace.CURRENCY);
			account.setActCommodity(currency);
		}

		{
			GncAccount.ActId guid = factory.createGncAccountActId();
			guid.setType(Const.XML_DATA_TYPE_GUID);
			guid.setValue(accountguid);
			account.setActId(guid);
		}

		{
			SlotsType slots = factory.createSlotsType();
			account.setActSlots(slots);
		}

		{
			Slot slot = factory.createSlot();
			slot.setSlotKey("placeholder");
			SlotValue slottype = factory.createSlotValue();
			slottype.setType("string");
			slottype.getContent().add("false");
			slot.setSlotValue(slottype);
			account.getActSlots().getSlot().add(slot);
		}

		{
			Slot slot = factory.createSlot();
			slot.setSlotKey("notes");
			SlotValue slottype = factory.createSlotValue();
			slottype.setType("string");
			slottype.getContent().add("");
			slot.setSlotValue(slottype);
			account.getActSlots().getSlot().add(slot);
		}

		file.getRootElement().getGncBook().getBookElements().add(account);
		file.setModified(true);
		return account;
	}

	/**
	 * Remove this account from the sytem.<br/>
	 * Throws IllegalStateException if this account has splits or childres.
	 */
	public void remove() {
		if (getTransactionSplits().size() > 0) {
			throw new IllegalStateException("cannot remove account while it contains transaction-splits!");
		}
		if (this.getChildren().size() > 0) {
			throw new IllegalStateException("cannot remove account while it contains child-accounts!");
		}

		getWritableGnucashFile().getRootElement().getGncBook().getBookElements().remove(getJwsdpPeer());
		getWritableGnucashFile().removeAccount(this);
	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	public GnucashWritableFile getWritableGnucashFile() {
		return (GnucashWritableFileImpl) getGnucashFile();
	}

	/**
	 * @see GnucashAccount#addTransactionSplit(GnucashTransactionSplit)
	 */
	@Override
	public void addTransactionSplit(final GnucashTransactionSplit split) {
		super.addTransactionSplit(split);

		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("transactionSplits", null, getTransactionSplits());
		}
	}

	/**
	 * @param impl the split to remove
	 */
	protected void removeTransactionSplit(final GnucashWritableTransactionSplit impl) {
		List transactionSplits = getTransactionSplits();
		transactionSplits.remove(impl);

		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("transactionSplits", null, transactionSplits);
		}
	}

	/**
	 * @see GnucashWritableAccount#setName(java.lang.String)
	 */
	public void setName(final String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("null or empty name given!");
		}

		String oldName = getJwsdpPeer().getActName();
		if (oldName == name) {
			return; // nothing has changed
		}
		this.getJwsdpPeer().setActName(name);
		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("name", oldName, name);
		}
	}

	/**
	 * @see GnucashWritableAccount#setAccountCode(java.lang.String)
	 */
	public void setAccountCode(final String code) {
		if (code == null || code.trim().length() == 0) {
			throw new IllegalArgumentException("null or empty code given!");
		}

		String oldCode = getJwsdpPeer().getActCode();
		if (oldCode == code) {
			return; // nothing has changed
		}
		this.getJwsdpPeer().setActCode(code);
		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("code", oldCode, code);
		}
	}
	
	public void setCmdtyCurrID(final CmdtyCurrID cmdtyCurrID) {
	    setCmdtyCurrNameSpace(cmdtyCurrID.getNameSpace());
	    setCmdtyCurrCode(cmdtyCurrID.getCode());
	}	

	/**
	 * @param currNameSpace the new namespace
	 * @see {@link GnucashAccount#getCurrencyNameSpace()}
	 */
	private void setCmdtyCurrNameSpace(final String currNameSpace) {
		if (currNameSpace == null) {
			throw new IllegalArgumentException("null or empty currencyNameSpace given!");
		}

		String oldCurrNameSpace = getJwsdpPeer().getActCommodity().getCmdtySpace();
		if (oldCurrNameSpace == currNameSpace) {
			return; // nothing has changed
		}
		this.getJwsdpPeer().getActCommodity().setCmdtySpace(currNameSpace);
		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("currencyNameSpace", oldCurrNameSpace, currNameSpace);
		}
	}

	/**
	 * @param currencyID the new currency
	 * @see #setCurrencyNameSpace(String)
	 * @see {@link GnucashAccount#getCurrencyID()}
	 */
	private void setCmdtyCurrCode(final String currencyID) {
		if (currencyID == null) {
			throw new IllegalArgumentException("null or empty currencyID given!");
		}

		String oldCurrencyId = getJwsdpPeer().getActCommodity().getCmdtyId();
		if (oldCurrencyId == currencyID) {
			return; // nothing has changed
		}
		this.getJwsdpPeer().getActCommodity().setCmdtyId(currencyID);
		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("currencyID", oldCurrencyId, currencyID);
		}
	}

	/**
	 * set getWritableFile().setModified(true).
	 */
	protected void setIsModified() {
		GnucashWritableFile writableFile = getWritableGnucashFile();
		writableFile.setModified(true);
	}

	/**
	 * Used by ${@link #getBalance()} to cache the result.
	 */
	private FixedPointNumber myBalanceCached = null;

	/**
	 * Used by ${@link #getBalance()} to cache the result.
	 */
	private PropertyChangeListener myBalanceCachedInvalidtor = null;

	/**
	 * same as getBalance(new Date()).<br/>
	 * ignores transactions after the current date+time<br/>
	 * This implementation caches the result.<br/>
	 * We assume that time does never move backwards
	 *
	 * @see #getBalance(LocalDate)
	 */
	@Override
	public FixedPointNumber getBalance() {

		if (myBalanceCached != null) {
			return myBalanceCached;
		}

		Collection<GnucashTransactionSplit> after = new LinkedList<GnucashTransactionSplit>();
		FixedPointNumber balance = getBalance(LocalDate.now(), after);

		if (after.isEmpty()) {
			myBalanceCached = balance;

			// add a listener to keep the cache up to date
			if (myBalanceCachedInvalidtor != null) {
				myBalanceCachedInvalidtor = new PropertyChangeListener() {
					private final Collection<GnucashTransactionSplit> splitsWeAreAddedTo = new HashSet<GnucashTransactionSplit>();

					public void propertyChange(final PropertyChangeEvent evt) {
						myBalanceCached = null;

						// we don't handle the case of removing an account
						// because that happenes seldomly enough

						if (evt.getPropertyName().equals("account")
								&&
								evt.getSource() instanceof GnucashWritableTransactionSplit) {
							GnucashWritableTransactionSplit splitw = (GnucashWritableTransactionSplit) evt.getSource();
							if (splitw.getAccount() != GnucashWritableAccountImpl.this) {
								splitw.removePropertyChangeListener("account", this);
								splitw.removePropertyChangeListener("quantity", this);
								splitw.getTransaction().removePropertyChangeListener("datePosted", this);
								splitsWeAreAddedTo.remove(splitw);

							}

						}
						if (evt.getPropertyName().equals("transactionSplits")) {
							Collection<GnucashTransactionSplit> splits = (Collection<GnucashTransactionSplit>) evt.getNewValue();
							for (GnucashTransactionSplit split : splits) {
								if (!(split instanceof GnucashWritableTransactionSplit)
										||
										splitsWeAreAddedTo.contains(split)
										) {
									continue;
								}
								GnucashWritableTransactionSplit splitw = (GnucashWritableTransactionSplit) split;
								splitw.addPropertyChangeListener("account", this);
								splitw.addPropertyChangeListener("quantity", this);
								splitw.getTransaction().addPropertyChangeListener("datePosted", this);
								splitsWeAreAddedTo.add(splitw);
							}
						}
					}
				};
				addPropertyChangeListener("currencyID", myBalanceCachedInvalidtor);
				addPropertyChangeListener("currencyNameSpace", myBalanceCachedInvalidtor);
				addPropertyChangeListener("transactionSplits", myBalanceCachedInvalidtor);
			}
		}

		return balance;
	}

	/**
	 * @see GnucashWritableAccount#setName(java.lang.String)
	 */
	public void setDescription(final String descr) {
		if (descr == null) {
			throw new IllegalArgumentException("null or empty description given!");
		}

		String oldDescr = getJwsdpPeer().getActDescription();
		if (oldDescr == descr) {
			return; // nothing has changed
		}
		getJwsdpPeer().setActDescription(descr);
		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("description", oldDescr, descr);
		}
	}

	/**
	 * @see GnucashWritableAccount#setInvcType(java.lang.String)
	 */
	public void setType(final String type) {
		if (type == null) {
			throw new IllegalArgumentException("null type given!");
		}

		String oldType = getJwsdpPeer().getActDescription();
		if (oldType == type) {
			return; // nothing has changed
		}
		getJwsdpPeer().setActType(type);
		setIsModified();
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("type", oldType, type);
		}
	}

	/**
	 * @see GnucashWritableAccount#setParentAccount(GnucashAccount)
	 */
	public void setParentAccountId(final String newParent) {
		if (newParent == null || newParent.trim().length() == 0) {
			setParentAccount(null);
		} else {
			setParentAccount(getGnucashFile().getAccountByID(newParent));
		}
	}

	/**
	 * @see GnucashWritableAccount#setParentAccount(GnucashAccount)
	 */
	public void setParentAccount(final GnucashAccount prntAcct) {

		if (prntAcct == null) {
			this.getJwsdpPeer().setActParent(null);
			return;
		}

		if (prntAcct == this) {
			throw new IllegalArgumentException("I cannot be my own parent!");
		}

		// check if newparent is a child-account recusively
		if (isChildAccountRecursive(prntAcct)) {
			throw new IllegalArgumentException("I cannot be my own (grand-)parent!");
		}

		GnucashAccount oldPrntAcct = null;
		GncAccount.ActParent parent = getJwsdpPeer().getActParent();
		if (parent == null) {
			parent = ((GnucashWritableFileImpl) getWritableGnucashFile())
					.getObjectFactory().createGncAccountActParent();
			parent.setType(Const.XML_DATA_TYPE_GUID);
			parent.setValue(prntAcct.getId());
			getJwsdpPeer().setActParent(parent);

		} else {
			oldPrntAcct = getParentAccount();
			parent.setValue(prntAcct.getId());
		}
		setIsModified();

		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("parentAccount", oldPrntAcct, prntAcct);
		}
	}

	/**
	 * Get the sum of all transaction-splits
	 * affecting this account in the given time-frame.
	 *
	 * @param from when to start, inclusive
	 * @param to   when to stop, exlusive.
	 * @return the sum of all transaction-splits
	 * affecting this account in the given time-frame.
	 */
	public FixedPointNumber getBalanceChange(final LocalDate from, final LocalDate to) {
		FixedPointNumber retval = new FixedPointNumber();

		for (Object element : getTransactionSplits()) {
			GnucashTransactionSplit split = (GnucashTransactionSplit) element;
			LocalDateTime whenHappened = split.getTransaction().getDatePosted().toLocalDateTime();
			if (!whenHappened.isBefore(to.atStartOfDay())) {
				continue;
			}
			if (whenHappened.isBefore(from.atStartOfDay())) {
				continue;
			}
			retval = retval.add(split.getQuantity());
		}
		return retval;
	}

	//  -------------------------------------------------------

}
