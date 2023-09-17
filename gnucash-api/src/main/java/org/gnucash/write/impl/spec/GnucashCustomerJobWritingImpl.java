/**
 * GnucashJobWritingImpl.java
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
package org.gnucash.write.impl.spec;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.gnucash.Const;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.OwnerId;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.aux.Owner;
import org.gnucash.read.impl.spec.GnucashCustomerJobImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.write.spec.GnucashWritableCustomerJob;
import org.gnucash.write.impl.GnucashFileWritingImpl;

/**
 * Modifiable version of a Job implemented.<br/>
 * <p>
 * Additional supported properties for PropertyChangeListeners:
 * <ul>
 * <li>customer</li>
 * <li>active</li>
 * <li>name</li>
 * </ul>
 */

public class GnucashCustomerJobWritingImpl extends GnucashCustomerJobImpl 
                                           implements GnucashCustomerJob,
                                                      GnucashWritableCustomerJob 
{
  /**
   * @param jwsdpPeer the XML(jaxb)-object we are fronting.
   * @param file      the file we belong to
   */
  public GnucashCustomerJobWritingImpl(
          final GncV2.GncBook.GncGncJob jwsdpPeer,
          final GnucashFile file) {
      super(jwsdpPeer, file);
  }

  /**
   * @param owner the customer the job is from
   * @param file  the file to add the jhe to
   */
  public GnucashCustomerJobWritingImpl(
          final GnucashFileWritingImpl file,
          final String id,
          final GnucashCustomer owner) {
      super(createJob(file, id, owner), file);
  }

  // -----------------------------------------------------------------
  
	/**
	 * @see GnucashWritableCustomerJob#remove()
	 */
	public void remove() {
		if (!getInvoices().isEmpty()) {
			throw new IllegalStateException("cannot remove a job that has invoices!");
		}
		GnucashFileWritingImpl writableFile = (GnucashFileWritingImpl) getFile();
		writableFile.getRootElement().getGncBook().getBookElements().remove(getJwsdpPeer());
		writableFile.removeJob(this);
	}

	/**
	 * @param customer the customer the job is from
	 * @param file     the file to add the jhe to
	 * @param guid     the internal id to use. May be null to generate an ID.
	 * @return the jaxb-job
	 */
	@SuppressWarnings("unchecked")
	private static GncV2.GncBook.GncGncJob createJob(
			final GnucashFileWritingImpl file,
			final String guid,
			final GnucashCustomer customer) {

		if (file == null) {
			throw new IllegalArgumentException("null file given");
		}

		if (customer == null) {
			throw new IllegalArgumentException("null customer given");
		}


		ObjectFactory factory = file.getObjectFactory();


		GncV2.GncBook.GncGncJob job = file.createGncGncJobType();

		job.setJobActive(1);
		job.setJobId("");
		job.setJobName("");
		job.setVersion(Const.XML_FORMAT_VERSION);

		{
			GncV2.GncBook.GncGncJob.JobGuid id = factory.createGncV2GncBookGncGncJobJobGuid();
			id.setType("guid");
			id.setValue((guid == null ? file.createGUID() : guid));
			job.setJobGuid(id);
		}

		{
			GncV2.GncBook.GncGncJob.JobOwner owner = factory.createGncV2GncBookGncGncJobJobOwner();
			owner.setOwnerType(Owner.TYPE_CUSTOMER);

			OwnerId ownerid = factory.createOwnerId();
			ownerid.setType("guid");
			ownerid.setValue(customer.getId());

			owner.setOwnerId(ownerid);
			owner.setVersion(Const.XML_FORMAT_VERSION);
			job.setJobOwner(owner);
		}


		file.getRootElement().getGncBook().getBookElements().add(job);
		file.setModified(true);
		return job;

	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	protected GnucashFileWritingImpl getWritingFile() {
		return (GnucashFileWritingImpl) getFile();
	}

	/**
	 * @see GnucashWritableCustomerJob#setCustomerType(java.lang.String)
	 */
	public void setCustomerType(final String customerType) {
		if (customerType == null) {
			throw new IllegalArgumentException("null 'customerType' given!");
		}

		Object old = getJwsdpPeer().getJobOwner().getOwnerType();
		if (old == customerType) {
			return; // nothing has changed
		}
		getJwsdpPeer().getJobOwner().setOwnerType(customerType);
		getWritingFile().setModified(true);
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("customerType", old, customerType);
		}
	}

	/**
	 * @see GnucashWritableCustomerJob#setCustomer(GnucashCustomer)
	 */
	public void setCustomer(final GnucashCustomer customer) {
		if (!getInvoices().isEmpty()) {
			throw new IllegalStateException("cannot change customer of a job that has invoices!");
		}


		if (customer == null) {
			throw new IllegalArgumentException("null 'customer' given!");
		}

		Object old = getCustomer();
		if (old == customer) {
			return; // nothing has changed
		}
		getJwsdpPeer().getJobOwner().getOwnerId().setValue(customer.getId());
		getWritingFile().setModified(true);
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("customer", old, customer);
		}
	}


	/**
	 * @see GnucashWritableCustomerJob#setJobNumber(java.lang.String)
	 */
	public void setJobNumber(final String jobId) {
		if (jobId == null || jobId.trim().length() == 0) {
			throw new IllegalArgumentException("null or empty job-number given!");
		}
		GnucashGenerJob otherJob = getWritingFile().getJobByNumber(jobId);
		if (otherJob != null
				&&
				!otherJob.getId().equals(getId())) {
			throw new IllegalArgumentException("another job (id='" + otherJob.getId() + "' already exists with given jobNumber '" + jobId + "')");
		}


		Object old = getJwsdpPeer().getJobId();
		if (old == jobId) {
			return; // nothing has changed
		}
		getJwsdpPeer().setJobId(jobId);
		getWritingFile().setModified(true);
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("jobId", old, jobId);
		}

	}

	/**
	 * @see GnucashWritableCustomerJob#setName(java.lang.String)
	 */
	public void setName(final String jobName) {
		if (jobName == null || jobName.trim().length() == 0) {
			throw new IllegalArgumentException("null or empty job-name given!");
		}


		Object old = getJwsdpPeer().getJobName();
		if (old == jobName) {
			return; // nothing has changed
		}
		getJwsdpPeer().setJobName(jobName);
		getWritingFile().setModified(true);
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("jobName", old, jobName);
		}
	}

	/**
	 * @param jobActive true is the job is to be (re)activated, false to deactivate
	 */
	public void setJobActive(final boolean jobActive) {


		boolean old = getJwsdpPeer().getJobActive() != 0;
		if (old == jobActive) {
			return; // nothing has changed
		}
		if (jobActive) {
			getJwsdpPeer().setJobActive(1);
		} else {
			getJwsdpPeer().setJobActive(0);
		}
		getWritingFile().setModified(true);
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("jobActive", old, jobActive);
		}
	}


// ------------------------ support for propertyChangeListeners ------------------

	/**
	 * support for firing PropertyChangeEvents.
	 * (gets initialized only if we really have listeners)
	 */
	private volatile PropertyChangeSupport myPropertyChange = null;

	/**
	 * Returned value may be null if we never had listeners.
	 *
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChange;
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param propertyName The name of the property to listen on.
	 * @param listener     The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(
			final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName The name of the property that was listened on.
	 * @param listener     The PropertyChangeListener to be removed
	 */
	public final void removePropertyChangeListener(
			final String propertyName,
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(propertyName, listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	public synchronized void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

// -------------------------------------------------------


}
