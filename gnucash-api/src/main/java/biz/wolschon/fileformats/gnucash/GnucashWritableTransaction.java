/**
 * GnucashWritableTransaction.java
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
package biz.wolschon.fileformats.gnucash;


import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashTransaction;

import javax.xml.bind.JAXBException;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;


/**
 * created: 11.06.2005 <br/>
 * Transaction that can be modified.<br/>
 * For PropertyChange-Listeners we support the properties:
 * "description" and "splits".
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashWritableTransaction extends GnucashTransaction {

	/**
	 * @param id the new currency
	 * @see #setCurrencyNameSpace(String)
	 * @see {@link GnucashTransaction#getCurrencyID()}
	 */
	void setCurrencyID(final String id);

	/**
	 * @param id the new namespace
	 * @see {@link GnucashTransaction#getCurrencyNameSpace()}
	 */
	void setCurrencyNameSpace(final String id);

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 * @return the file we are associated with
	 */
	GnucashWritableFile getWritingFile();

	/**
	 * @param dateEntered the day (time is ignored)
	 *        that this transaction has been entered
	 *        into  the system
	 * @see {@link #setDatePosted(LocalDateTime)}
	 */
	void setDateEntered(final LocalDateTime dateEntered);

	/**
	 * @param datePosted the day (time is ignored)
	 *        that the money was transfered
	 * @see {@link #setDateEntered(LocalDateTime)}
	 */
	void setDatePosted(final LocalDateTime datePosted);


	void setDescription(final String desc);

	void setTransactionNumber(String string);


	/**
	 * @throws JAXBException if we have issues with the XML-backend
	 * @see GnucashTransaction#getFirstSplit()
	 */
	GnucashWritableTransactionSplit getWritingFirstSplit() throws JAXBException;

	/**
	 * @see GnucashTransaction#getSecondSplit()
	 * @throws JAXBException if we have issues with the XML-backend
	 */
	GnucashWritableTransactionSplit getWritingSecondSplit() throws JAXBException;

	/**
	 * @see GnucashTransaction#getSplitByID(String)
	 * @throws JAXBException if we have issues with the XML-backend
	 */
	GnucashWritableTransactionSplit getWritingSplitByID(String id);

	/**
	 *
	 * @return the first split of this transaction or null.
	 * @throws JAXBException if we have issues with the XML-backend
	 */
	GnucashWritableTransactionSplit getFirstSplit();

	/**
	 * @return the second split of this transaction or null.
	 * @throws JAXBException if we have issues with the XML-backend
	 */
	GnucashWritableTransactionSplit getSecondSplit();

	/**
	 * @throws JAXBException if we have issues with the XML-backend
	 * @see GnucashTransaction#getSplits()
	 */
	Collection<? extends GnucashWritableTransactionSplit> getWritingSplits() throws JAXBException;

	/**
	 * Create a new split, already atached to this transaction.
	 * @param account the account for the new split
	 * @return a new split, already atached to this transaction
	 * @throws JAXBException on xml-problems
	 */
	GnucashWritableTransactionSplit createWritingSplit(GnucashAccount account) throws JAXBException;

	/**
	 * Create a new split, already atached to this transaction.
	 * @param account the account for the new split
	 * @return a new split, already atached to this transaction
	 * @throws JAXBException on xml-problems
	 */
	GnucashWritableTransactionSplit createWritingSplit(GnucashAccount account, String splitID) throws JAXBException;

	/**
	 * Also removes the split from it's account.
	 * @param impl the split to remove from this transaction
	 * @throws JAXBException if we have issues accessing the XML-Backend.
	 */
	void remove(GnucashWritableTransactionSplit impl) throws JAXBException;

	/**
	 * remove this transaction.
	 * @throws JAXBException if we have issues with the XML-backend
	 */
	void remove() throws JAXBException;

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

	/**
	 * @param name the name of the user-defined attribute
	 * @param value the value or null if not set
	 * @throws JAXBException on problems with the xml-backend
	 * @see {@link org.gnucash.xml.GnucashObject#getUserDefinedAttribute(String)}
	 */
	void setUserDefinedAttribute(final String name, final String value) throws JAXBException;

}
