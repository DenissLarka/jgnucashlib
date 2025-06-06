package org.gnucash.read.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.gnucash.currency.CurrencyNameSpace;
import org.gnucash.generated.GncAccount;
import org.gnucash.generated.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.GnucashTransactionSplit;

/**
 * Implementation of GnucashAccount that used a
 * jwsdp-generated backend.
 */
public class GnucashAccountImpl extends SimpleAccount implements GnucashAccount {
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashAccountImpl.class);

	/**
	 * Helper to implement the {@link GnucashObject}-interface.
	 */
	protected GnucashObjectImpl helper;

	/**
	 * @param peer    the JWSDP-object we are facading.
	 * @param gncFile the file to register under
	 */
	@SuppressWarnings("exports")
	public GnucashAccountImpl(final GncAccount peer, final GnucashFile gncFile) {
		super(gncFile);

		if (peer.getActSlots() == null) {
			peer.setActSlots(new ObjectFactory().createSlotsType());
		}

		jwsdpPeer = peer;
		// ::TODO
		// file = gncfile;

		helper = new GnucashObjectImpl(peer.getActSlots(), gncFile);
	}

	/**
	 * Examples: The user-defined-attribute "hidden"="true"/"false" was introduced
	 * in gnucash2.0 to hide accounts.
	 *
	 * @param name the name of the user-defined attribute
	 * @return the value or null if not set
	 */
	public String getUserDefinedAttribute(final String name) {
		return helper.getUserDefinedAttribute(name);
	}

	/**
	 * @return all keys that can be used with
	 * ${@link #getUserDefinedAttribute(String)}}.
	 */
	public Collection<String> getUserDefinedAttributeKeys() {
		return helper.getUserDefinedAttributeKeys();
	}

	/**
	 * the JWSDP-object we are facading.
	 */
	private GncAccount jwsdpPeer;

	/**
	 * @see GnucashAccount#getId()
	 */
	public String getId() {
		return jwsdpPeer.getActId().getValue();
	}

	/**
	 * @see GnucashAccount#getParentAccountId()
	 */
	public String getParentAccountId() {
		GncAccount.ActParent parent = jwsdpPeer.getActParent();
		if (parent == null) {
			return null;
		}

		return parent.getValue();
	}

	/**
	 * @see GnucashAccount#getChildren()
	 */
	public Collection<GnucashAccount> getChildren() {
		return getGnucashFile().getAccountsByParentID(getId());
	}

	/**
	 * @see GnucashAccount#getName()
	 */
	public String getName() {
		return jwsdpPeer.getActName();
	}

	/**
	 * @return "ISO4217" for a currency "FUND" or a fond,...
	 * @see GnucashAccount#getCurrencyNameSpace()
	 */
	public String getCurrencyNameSpace() {
		if (jwsdpPeer.getActCommodity() == null) {
			return CurrencyNameSpace.NAMESPACE_CURRENCY; // default-currency because gnucash 2.2 has no currency on the root-account
		}
		return jwsdpPeer.getActCommodity().getCmdtySpace();
	}

	/**
	 * @see GnucashAccount#getCurrencyID()
	 */
	public String getCurrencyID() {
		if (jwsdpPeer.getActCommodity() == null) {
			return "EUR"; // default-currency because gnucash 2.2 has no currency on the root-account
		}
		return jwsdpPeer.getActCommodity().getCmdtyId();
	}

	/**
	 * @see GnucashAccount#getDescription()
	 */
	public String getDescription() {
		return jwsdpPeer.getActDescription();
	}

	/**
	 * @see GnucashAccount#getCode()
	 */
	public String getCode() {
		return jwsdpPeer.getActCode();
	}

	/**
	 * @see GnucashAccount#getType()
	 */
	public String getType() {
		return jwsdpPeer.getActType();
	}

	/**
	 * The splits of this transaction. May not be fully initialized during loading
	 * of the gnucash-file.
	 *
	 * @see #mySplitsNeedSorting
	 */
	private final List<GnucashTransactionSplit> mySplits = new LinkedList<GnucashTransactionSplit>();

	/**
	 * If {@link #mySplits} needs to be sorted because it was modified. Sorting is
	 * done in a lazy way.
	 */
	private boolean mySplitsNeedSorting = false;

	/**
	 * @see GnucashAccount#getTransactionSplits()
	 */
	public List<GnucashTransactionSplit> getTransactionSplits() {

		if (mySplitsNeedSorting) {
			Collections.sort(mySplits);
			mySplitsNeedSorting = false;
		}

		return mySplits;
	}

	/**
	 * @see GnucashAccount#addTransactionSplit(GnucashTransactionSplit)
	 */
	public void addTransactionSplit(final GnucashTransactionSplit split) {

		GnucashTransactionSplit old = getTransactionSplitByID(split.getId());
		if (old != null) {
			if (old != split) {
				IllegalStateException ex = new IllegalStateException("DEBUG");
				ex.printStackTrace();
				replaceTransactionSplit(old, split);
			}
		} else {
			mySplits.add(split);
			mySplitsNeedSorting = true;
		}
	}

	/**
	 * For internal use only.
	 *
	 * @param transactionSplitByID -
	 * @param impl                 -
	 */
	private void replaceTransactionSplit(final GnucashTransactionSplit transactionSplitByID,
			final GnucashTransactionSplit impl) {
		if (!mySplits.remove(transactionSplitByID)) {
			throw new IllegalArgumentException("old object not found!");
		}

		mySplits.add(impl);
	}

	/**
	 * @return the JWSDP-object we are wrapping.
	 */
	@SuppressWarnings("exports")
	public GncAccount getJwsdpPeer() {
		return jwsdpPeer;
	}

	/**
	 * @param newPeer the JWSDP-object we are wrapping.
	 */
	protected void setJwsdpPeer(final GncAccount newPeer) {
		if (newPeer == null) {
			throw new IllegalArgumentException("null not allowed for field this.jwsdpPeer");
		}

		jwsdpPeer = newPeer;
	}

}
