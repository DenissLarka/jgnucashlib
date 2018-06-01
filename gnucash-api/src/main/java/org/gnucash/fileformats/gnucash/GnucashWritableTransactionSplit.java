/**
 * GnucashWritableTransactionSplit.java
 * Created on 11.06.2005
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
 * 11.06.2005 - initial version
 * ...
 */
package org.gnucash.fileformats.gnucash;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashTransactionSplit;

import javax.xml.bind.JAXBException;
import java.beans.PropertyChangeListener;

/**
 * created: 11.06.2005 <br/>
 * Transaction-Split that can be modified<br/>
 * For propertyChange we support the properties "value", "quantity"
 * "description",  "splitAction" and "accountID".
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashWritableTransactionSplit extends GnucashTransactionSplit, GnucashWritableObject {

	/**
	 * @return the transaction this is a split of.
	 */
	GnucashWritableTransaction getTransaction();

	/**
	 * Remove this split from the sytem.
	 * @throws JAXBException if we have issues accessing the XML-Backend.
	 */
	void remove() throws JAXBException;

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
	void setQuantity(String n);

	/**
	 * Same as ${@link #setQuantity(String)}.
	 * @param n the new quantity (in the currency of the account)
	 */
	void setQuantityFormatetForHTML(String n);

	/**
	 * If the currencies of transaction and account match, this also does
	 * ${@link #setQuantity(FixedPointNumber)}.
	 * @param n the new quantity (in the currency of the account)
	 */
	void setQuantity(FixedPointNumber n);

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
	void setValueFormatetForHTML(String n);

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
	 * @param action null, "Zahlung" or "Rechnung"
	 */
	void setSplitAction(String action);


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

