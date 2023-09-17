package org.gnucash.write;

import java.beans.PropertyChangeListener;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.IllegalTransactionSplitActionException;

/**
 * Transaction-Split that can be modified<br/>
 * For propertyChange we support the properties "value", "quantity"
 * "description",  "splitAction" and "accountID".
 */
public interface GnucashWritableTransactionSplit extends GnucashTransactionSplit, GnucashWritableObject {

	/**
	 * @return the transaction this is a split of.
	 */
	GnucashWritableTransaction getTransaction();

	/**
	 * Remove this split from the sytem.
	 */
	void remove();

	/**
	 * Does not convert the quantity to another
	 * currency if the new account has another
	 * one then the old!
	 * @param accountId the new account to give this
	 *        money to/take it from.
	 */
	void setAccountID(final String accountId);

	/**
	 * Does not convert the quantity to another
	 * currency if the new account has another
	 * one then the old!
	 * @param account the new account to give this
	 *        money to/take it from.
	 */
	void setAccount(GnucashAccount account);


	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setQuantity(FixedPointNumber)}.
	 * @param n the new quantity (in the currency of the account)
	 */
	void setInvcQuantity(String n);

	/**
	 * Same as ${@link #setInvcQuantity(String)}.
	 * @param n the new quantity (in the currency of the account)
	 */
	void setQuantityFormattedForHTML(String n);

	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setQuantity(FixedPointNumber)}.
	 * @param n the new quantity (in the currency of the account)
	 */
	void setInvcQuantity(FixedPointNumber n);

	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setValue(FixedPointNumber)}.
	 * @param n the new value (in the currency of the transaction)
	 */
	void setValue(String n);

	/**
	 * Same as ${@link #setValue(String)}.
	 * @param n the new value (in the currency of the transaction)
	 */
	void setValueFormattedForHTML(String n);

	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setValue(FixedPointNumber)}.
	 * @param n the new value (in the currency of the transaction)
	 */
	void setValue(FixedPointNumber n);

	/**
	 * Set the description-text.
	 * @param desc the new description
	 */
	void setDescription(String desc);

	/**
	 * Set the type of association this split has with
	 * an invoice's lot.
	 * @param action null, or one of the ACTION_xyz values defined
	 * @throws IllegalTransactionSplitActionException 
	 */
	void setSplitAction(String action) throws IllegalTransactionSplitActionException;


	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be added
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Add a PropertyChangeListener for a specific property.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param propertyName  The name of the property to listen on.
	 * @param listener  The PropertyChangeListener to be added
	 */
	void addPropertyChangeListener(String propertyName,
								   PropertyChangeListener listener);

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName  The name of the property that was listened on.
	 * @param listener  The PropertyChangeListener to be removed
	 */
	void removePropertyChangeListener(String propertyName,
									  PropertyChangeListener listener);

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be removed
	 */
	void removePropertyChangeListener(
			PropertyChangeListener listener);
}

