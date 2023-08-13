/**
 * GnucashAccountImpl.java
 * License: GPLv3 or later
 * Created on 13.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 13.05.2005 - initial version
 * ...
 */
package org.gnucash.read.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.gnucash.generated.GncAccount;
import org.gnucash.generated.ObjectFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.GnucashTransactionSplit;

/**
 * created: 13.05.2005 <br/>
 * Implementation of GnucashAccount that used a jwsdp-generated backend.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class GnucashAccountImpl extends SimpleAccount implements GnucashAccount {

  /**
   * Our logger for debug- and error-ourput.
   */
  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(GnucashAccountImpl.class);

  /**
   * Helper to implement the {@link GnucashObject}-interface.
   */
  protected GnucashObjectImpl helper;

  /**
   * @param peer    the JWSDP-object we are facading.
   * @param gncfile the file to register under
   */
  public GnucashAccountImpl(final GncAccount peer, final GnucashFile gncfile) {
    super(gncfile);
    jwsdpPeer = peer;
    if (peer.getActSlots() == null) {
      peer.setActSlots(new ObjectFactory().createSlotsType());
    }
    helper = new GnucashObjectImpl(peer.getActSlots(), gncfile);
  }

  /**
   * Examples: The user-defined-attribute "hidden"="true"/"false" was introduced
   * in gnucash2.0 to hide accounts.
   *
   * @param name the name of the user-defined attribute
   * @return the value or null if not set
   */
  @Override
  public String getUserDefinedAttribute(final String name) {
    return helper.getUserDefinedAttribute(name);
  }

  /**
   * @return all keys that can be used with
   *         ${@link #getUserDefinedAttribute(String)}}.
   */
  @Override
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
  @Override
  public String getId() {
    return jwsdpPeer.getActId().getValue();
  }

  /**
   * @see GnucashAccount#getParentAccountId()
   */
  @Override
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
  @Override
  public Collection getChildren() {
    return getGnucashFile().getAccountsByParentID(getId());
  }

  /**
   * @see GnucashAccount#getName()
   */
  @Override
  public String getName() {
    return jwsdpPeer.getActName();
  }

  /**
   * @return "ISO4217" for a currency "FUND" or a fond,...
   * @see GnucashAccount#getCurrencyNameSpace()
   */
  @Override
  public String getCurrencyNameSpace() {
    if (jwsdpPeer.getActCommodity() == null) {
      return "ISO4217"; // default-currency because gnucash 2.2 has no currency on the root-account
    }
    return jwsdpPeer.getActCommodity().getCmdtySpace();
  }

  /**
   * @see GnucashAccount#getCurrencyID()
   */
  @Override
  public String getCurrencyID() {
    if (jwsdpPeer.getActCommodity() == null) {
      return "EUR"; // default-currency because gnucash 2.2 has no currency on the root-account
    }
    return jwsdpPeer.getActCommodity().getCmdtyId();
  }

  /**
   * @see GnucashAccount#getDescription()
   */
  @Override
  public String getDescription() {
    return jwsdpPeer.getActDescription();
  }

  /**
   * @see GnucashAccount#getAccountCode()
   */
  @Override
  public String getAccountCode() {
    return jwsdpPeer.getActCode();
  }

  /**
   * @see GnucashAccount#getType()
   */
  @Override
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
  @Override
  @SuppressWarnings("unchecked")
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
  @Override
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
