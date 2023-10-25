package org.gnucash.read.impl;

import java.text.NumberFormat;
import java.util.Locale;

import org.gnucash.Const;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.CurrencyID;
import org.gnucash.currency.InvalidCmdtyCurrIDException;
import org.gnucash.currency.InvalidCmdtyCurrTypeException;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of GnucashTransactionSplit that uses JWSDSP.
 */
public class GnucashTransactionSplitImpl extends GnucashObjectImpl 
                                         implements GnucashTransactionSplit 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashTransactionSplitImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    private GncTransaction.TrnSplits.TrnSplit jwsdpPeer;

    /**
     * the transaction this split belongs to.
     */
    private final GnucashTransaction myTransaction;

    // ---------------------------------------------------------------

    /**
     * @param peer the JWSDP-object we are facading.
     * @param trx  the transaction this split belongs to
     * @see #jwsdpPeer
     * @see #myTransaction
     */
    @SuppressWarnings("exports")
    public GnucashTransactionSplitImpl(
	    final GncTransaction.TrnSplits.TrnSplit peer, 
	    final GnucashTransaction trx) {
	super((peer.getSplitSlots() == null) ? new ObjectFactory().createSlotsType() : peer.getSplitSlots(),
		trx.getGnucashFile());

	jwsdpPeer = peer;
	myTransaction = trx;

	GnucashAccount acct = getAccount();
	if (acct == null) {
	    LOGGER.error("No such Account id='" + getAccountID() + "' for Transactions-Split with id '" + getId()
		    + "' description '" + getDescription() + "' in transaction with id '" + getTransaction().getId()
		    + "' description '" + getTransaction().getDescription() + "'");
	} else {
	    acct.addTransactionSplit(this);
	}

	String lot = getLotID();
	if (lot != null) {
	    for (GnucashGenerInvoice invc : getTransaction().getGnucashFile().getGenerInvoices()) {
		String lotID = invc.getLotID();
		if (lotID != null && lotID.equals(lot)) {
		    // Check if it's a payment transaction.
		    // If so, add it to the invoice's list of payment transactions.
		    if (getAction().equals(ACTION_PAYMENT)) {
			invc.addPayingTransaction(this);
		    }
		}
	    }
	}

    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are facading.
     */
    @SuppressWarnings("exports")
    public GncTransaction.TrnSplits.TrnSplit getJwsdpPeer() {
	return jwsdpPeer;
    }

    /**
     * @param newPeer the JWSDP-object we are facading.
     */
    protected void setJwsdpPeer(final GncTransaction.TrnSplits.TrnSplit newPeer) {
	if (newPeer == null) {
	    throw new IllegalArgumentException("null not allowed for field this.jwsdpPeer");
	}

	jwsdpPeer = newPeer;
    }

    // ---------------------------------------------------------------

    /**
     * @return the lot-id that identifies this transaction to belong to an invoice
     *         with that lot-id.
     */
    public String getLotID() {
	if (getJwsdpPeer().getSplitLot() == null) {
	    return null;
	}

	return getJwsdpPeer().getSplitLot().getValue();

    }

    /**
     * @see GnucashTransactionSplit#getAction()
     */
    public String getAction() {
	if (getJwsdpPeer().getSplitAction() == null) {
	    return "";
	}

	return getJwsdpPeer().getSplitAction();
    }

    /**
     * @see GnucashTransactionSplit#getId()
     */
    public String getId() {
	return jwsdpPeer.getSplitId().getValue();
    }

    /**
     * @see GnucashTransactionSplit#getAccountID()
     */
    public String getAccountID() {
	assert jwsdpPeer.getSplitAccount().getType().equals(Const.XML_DATA_TYPE_GUID);
	String id = jwsdpPeer.getSplitAccount().getValue();
	assert id != null;
	return id;
    }

    /**
     * @see GnucashTransactionSplit#getAccount()
     */
    public GnucashAccount getAccount() {
	return myTransaction.getGnucashFile().getAccountByID(getAccountID());
    }

    /**
     * @see GnucashTransactionSplit#getTransaction()
     */
    public GnucashTransaction getTransaction() {
	return myTransaction;
    }

    /**
     * @see GnucashTransactionSplit#getValue()
     */
    public FixedPointNumber getValue() {
	return new FixedPointNumber(jwsdpPeer.getSplitValue());
    }

    /**
     * @return The currencyFormat for the quantity to use when no locale is given.
     * @throws InvalidCmdtyCurrTypeException 
     */
    protected NumberFormat getQuantityCurrencyFormat() throws InvalidCmdtyCurrTypeException {

	return ((GnucashAccountImpl) getAccount()).getCurrencyFormat();
    }

    /**
     * @return the currency-format of the transaction
     * @throws InvalidCmdtyCurrIDException 
     * @throws InvalidCmdtyCurrTypeException 
     */
    public NumberFormat getValueCurrencyFormat() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {

	return ((GnucashTransactionImpl) getTransaction()).getCurrencyFormat();
    }

    /**
     * @throws InvalidCmdtyCurrIDException 
     * @throws InvalidCmdtyCurrTypeException 
     * @see GnucashTransactionSplit#getValueFormatted()
     */
    public String getValueFormatted() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	NumberFormat nf = getValueCurrencyFormat();
	if ( getTransaction().getCmdtyCurrID().getType() == CmdtyCurrID.Type.CURRENCY ) {
	    // redundant, but symmetry:
	    nf.setCurrency(new CurrencyID(getTransaction().getCmdtyCurrID()).getCurrency());
	    return nf.format(getValue());
	}
	else {
	    return nf.format(getValue()) + " " + getTransaction().getCmdtyCurrID().toString(); 
	}
    }

    /**
     * @throws InvalidCmdtyCurrIDException 
     * @throws InvalidCmdtyCurrTypeException 
     * @see GnucashTransactionSplit#getValueFormatted(java.util.Locale)
     */
    public String getValueFormatted(final Locale lcl) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {

	NumberFormat nf = NumberFormat.getInstance(lcl);
	if ( getTransaction().getCmdtyCurrID().getType() == CmdtyCurrID.Type.CURRENCY ) {
	    // redundant, but symmetry:
	    nf.setCurrency(new CurrencyID(getTransaction().getCmdtyCurrID()).getCurrency());
	    return nf.format(getValue());
	}
	else {
	    return nf.format(getValue()) + " " + getTransaction().getCmdtyCurrID().toString(); 
	}
    }

    /**
     * @throws InvalidCmdtyCurrIDException 
     * @throws InvalidCmdtyCurrTypeException 
     * @see GnucashTransactionSplit#getValueFormattedForHTML()
     */
    public String getValueFormattedForHTML() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getValueFormatted().replaceFirst("€", "&euro;");
    }

    /**
     * @throws InvalidCmdtyCurrIDException 
     * @throws InvalidCmdtyCurrTypeException 
     * @see GnucashTransactionSplit#getValueFormattedForHTML(java.util.Locale)
     */
    public String getValueFormattedForHTML(final Locale lcl) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getValueFormatted(lcl).replaceFirst("€", "&euro;");
    }

    /**
     * @see GnucashTransactionSplit#getAccountBalance()
     */
    public FixedPointNumber getAccountBalance() {
	return getAccount().getBalance(this);
    }

    /**
     * @throws InvalidCmdtyCurrTypeException 
     * @see GnucashTransactionSplit#getAccountBalanceFormatted()
     */
    public String getAccountBalanceFormatted() throws InvalidCmdtyCurrTypeException {
	return ((GnucashAccountImpl) getAccount()).getCurrencyFormat().format(getAccountBalance());
    }

    /**
     * @throws InvalidCmdtyCurrTypeException 
     * @see GnucashTransactionSplit#getAccountBalanceFormatted(java.util.Locale)
     */
    public String getAccountBalanceFormatted(final Locale lcl) throws InvalidCmdtyCurrTypeException {
	return getAccount().getBalanceFormatted(lcl);
    }

    /**
     * @see GnucashTransactionSplit#getQuantity()
     */
    public FixedPointNumber getQuantity() {
	return new FixedPointNumber(jwsdpPeer.getSplitQuantity());
    }

    /**
     * The value is in the currency of the account!
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    public String getQuantityFormatted() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	NumberFormat nf = getQuantityCurrencyFormat();
	if ( getAccount().getCmdtyCurrID().getType() == CmdtyCurrID.Type.CURRENCY ) {
	    nf.setCurrency(new CurrencyID(getAccount().getCmdtyCurrID()).getCurrency());
	    return nf.format(getQuantity());
	}
	else {
	    return nf.format(getQuantity()) + " " + getAccount().getCmdtyCurrID().toString(); 
	}
    }

    /**
     * The value is in the currency of the account!
     *
     * @param lcl the locale to format to
     * @return the formatted number
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    public String getQuantityFormatted(final Locale lcl) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	NumberFormat nf = NumberFormat.getCurrencyInstance(lcl);
	if ( getAccount().getCmdtyCurrID().getType() == CmdtyCurrID.Type.CURRENCY ) {
	    nf.setCurrency(new CurrencyID(getAccount().getCmdtyCurrID()).getCurrency());
	    return nf.format(getQuantity());
	}
	else {
	    return nf.format(getQuantity()) + " " + getAccount().getCmdtyCurrID().toString(); 
	}
    }

    /**
     * The value is in the currency of the account!
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    public String getQuantityFormattedForHTML() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getQuantityFormatted().replaceFirst("€", "&euro;");
    }

    /**
     * The value is in the currency of the account!
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    public String getQuantityFormattedForHTML(final Locale lcl) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getQuantityFormatted(lcl).replaceFirst("€", "&euro;");
    }

    /**
     * {@inheritDoc}
     *
     * @see GnucashTransactionSplit#getDescription()
     */
    public String getDescription() {
	if (jwsdpPeer.getSplitMemo() == null) {
	    return "";
	}
	return jwsdpPeer.getSplitMemo();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[GnucashTransactionSplitImpl:");

	buffer.append(" id: ");
	buffer.append(getId());

	buffer.append(" Action: '");
	buffer.append(getAction() + "'");

	buffer.append(" transaction-id: ");
	buffer.append(getTransaction().getId());

	buffer.append(" accountID: ");
	buffer.append(getAccountID());

	buffer.append(" account: ");
	GnucashAccount account = getAccount();
	buffer.append(account == null ? "null" : "'" + account.getQualifiedName() + "'");

	buffer.append(" description: '");
	buffer.append(getDescription() + "'");

	buffer.append(" transaction-description: '");
	buffer.append(getTransaction().getDescription() + "'");

	buffer.append(" value: ");
	buffer.append(getValue());

	buffer.append(" quantity: ");
	buffer.append(getQuantity());

	buffer.append("]");
	return buffer.toString();
    }

    /**
     * @see java.lang.Comparable#compareTo(GnucashTransactionSplit)
     */
    public int compareTo(final GnucashTransactionSplit otherSplt) {
	try {
	    GnucashTransaction otherTrans = otherSplt.getTransaction();
	    int c = otherTrans.compareTo(getTransaction());
	    if (c != 0) {
		return c;
	    }

	    c = otherSplt.getId().compareTo(getId());
	    if (c != 0) {
		return c;
	    }

	    if (otherSplt != this) {
		LOGGER.error("Duplicate transaction-split-id!! " + otherSplt.getId() + "["
			+ otherSplt.getClass().getName() + "] and " + getId() + "[" + getClass().getName() + "]\n"
			+ "split0=" + otherSplt.toString() + "\n" + "split1=" + toString() + "\n");
		IllegalStateException x = new IllegalStateException("DEBUG");
		x.printStackTrace();

	    }

	    return 0;

	} catch (Exception e) {
	    e.printStackTrace();
	    return 0;
	}
    }

}
