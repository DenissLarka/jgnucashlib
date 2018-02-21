/**
 * GnucashWritableJob.java
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

import org.gnucash.xml.GnucashCustomer;
import org.gnucash.xml.GnucashJob;

import java.beans.PropertyChangeListener;

/**
 * created: 11.06.2005 <br/>
 * Job that can be modified.<br/>
 * Supports propertyChangeListeners for all properties that have a setter.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashWritableJob extends GnucashJob {

	/**
	 * Not used.
	 * @param a not used.
	 * @see GnucashJob#CUSTOMETYPE_CUSTOMER
	 */
	void setCustomerType(String a);

	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 * @param newCustomer the Customer who issued this job.
	 */
	void setCustomer(GnucashCustomer newCustomer);

	/**
	 * Set the user-defined number for the job. <br/>
	 * Should be only one line but may contain non-digits and non-ascii -characters.
	 * @param newNumber the new name
	 */
	void setJobNumber(String newNumber);

	/**
	 * Set the user-defined name for the job. <br/>
	 * Should be only one line but may contain non-ascii -characters.
	 * @param newName the new name
	 */
	void setName(String newName);

	/**
	 * @param active true is the job is to be (re)activated, false to deactivate
	 */
	void setJobActive(boolean active);


	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 * Remove this job.
	 */
	void remove();

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be added
	 */
	void addPropertyChangeListener(
			final PropertyChangeListener listener);

	/**
	 * Add a PropertyChangeListener for a specific property.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param propertyName  The name of the property to listen on.
	 * @param listener  The PropertyChangeListener to be added
	 */
	void addPropertyChangeListener(
			final String propertyName,
			final PropertyChangeListener listener);

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName  The name of the property that was listened on.
	 * @param listener  The PropertyChangeListener to be removed
	 */
	void removePropertyChangeListener(
			final String propertyName,
			final PropertyChangeListener listener);

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener  The PropertyChangeListener to be removed
	 */
	void removePropertyChangeListener(
			final PropertyChangeListener listener);

}
