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
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.impl.spec.GnucashCustomerJobImpl;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.spec.GnucashWritableCustomerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class GnucashWritableCustomerJobImpl extends GnucashCustomerJobImpl 
                                            implements GnucashWritableCustomerJob 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableCustomerJobImpl.class);

    /**
     * @param jwsdpPeer the XML(jaxb)-object we are fronting.
     * @param file      the file we belong to
     */
    @SuppressWarnings("exports")
    public GnucashWritableCustomerJobImpl(
	    final GncV2.GncBook.GncGncJob jwsdpPeer, 
	    final GnucashFile file) {
	super(jwsdpPeer, file);
    }

    /**
     * @param owner the customer the job is from
     * @param file  the file to add the jhe to
     */
    public GnucashWritableCustomerJobImpl(
	    final GnucashWritableFileImpl file, 
	    final GnucashCustomer owner,
	    final String number, 
	    final String name) {
	super(createJob(file, file.createGUID(), owner, number, name), file);
    }

    // -----------------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException 
     * @see GnucashWritableCustomerJob#remove()
     */
    public void remove() throws WrongInvoiceTypeException {
	if (!getInvoices().isEmpty()) {
	    throw new IllegalStateException("cannot remove a job that has invoices!");
	}
	GnucashWritableFileImpl writableFile = (GnucashWritableFileImpl) getFile();
	writableFile.getRootElement().getGncBook().getBookElements().remove(getJwsdpPeer());
	writableFile.removeJob(this);
    }

    /**
     * @param cust the customer the job is from
     * @param file     the file to add the jhe to
     * @param guid     the internal id to use. May be null to generate an ID.
     * @return the jaxb-job
     */
    private static GncV2.GncBook.GncGncJob createJob(
	    final GnucashWritableFileImpl file, 
	    final String guid,
	    final GnucashCustomer cust,
	    final String number,
	    final String name) {

	if (file == null) {
	    throw new IllegalArgumentException("null file given");
	}

	if (cust == null) {
	    throw new IllegalArgumentException("null customer given");
	}

	ObjectFactory factory = file.getObjectFactory();

	GncV2.GncBook.GncGncJob job = file.createGncGncJobType();

	job.setJobActive(1);
	job.setJobId(number);
	job.setJobName(name);
	job.setVersion(Const.XML_FORMAT_VERSION);

	{
	    GncV2.GncBook.GncGncJob.JobGuid id = factory.createGncV2GncBookGncGncJobJobGuid();
	    id.setType(Const.XML_DATA_TYPE_GUID);
	    id.setValue((guid == null ? file.createGUID() : guid));
	    job.setJobGuid(id);
	}

	{
	    GncV2.GncBook.GncGncJob.JobOwner owner = factory.createGncV2GncBookGncGncJobJobOwner();
	    owner.setOwnerType(GCshOwner.TYPE_CUSTOMER);

	    OwnerId ownerid = factory.createOwnerId();
	    ownerid.setType(Const.XML_DATA_TYPE_GUID);
	    ownerid.setValue(cust.getId());

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
    protected GnucashWritableFileImpl getWritingFile() {
	return (GnucashWritableFileImpl) getFile();
    }

//    /**
//     * @see GnucashWritableCustomerJob#setCustomerType(java.lang.String)
//     */
//    public void setCustomerType(final String customerType) {
//	if (customerType == null) {
//	    throw new IllegalArgumentException("null 'customerType' given!");
//	}
//
//	Object old = getJwsdpPeer().getJobOwner().getOwnerType();
//	if (old == customerType) {
//	    return; // nothing has changed
//	}
//	getJwsdpPeer().getJobOwner().setOwnerType(customerType);
//	getWritingFile().setModified(true);
//	// <<insert code to react further to this change here
//	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
//	if (propertyChangeFirer != null) {
//	    propertyChangeFirer.firePropertyChange("customerType", old, customerType);
//	}
//    }

    /**
     * @throws WrongInvoiceTypeException
     * @see GnucashWritableCustomerJob#setCustomer(GnucashCustomer)
     */
    public void setCustomer(final GnucashCustomer cust) throws WrongInvoiceTypeException {
	if (!getInvoices().isEmpty()) {
	    throw new IllegalStateException("cannot change customer of a job that has invoices!");
	}

	if (cust == null) {
	    throw new IllegalArgumentException("null 'customer' given!");
	}

	GnucashCustomer oldCust = getCustomer();
	if (oldCust == cust) {
	    return; // nothing has changed
	}
	getJwsdpPeer().getJobOwner().getOwnerId().setValue(cust.getId());
	getWritingFile().setModified(true);
	// <<insert code to react further to this change here
	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	if (propertyChangeFirer != null) {
	    propertyChangeFirer.firePropertyChange("customer", oldCust, cust);
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
	if (otherJob != null && !otherJob.getId().equals(getId())) {
	    throw new IllegalArgumentException(
		    "another job (id='" + otherJob.getId() + "' already exists with given jobNumber '" + jobId + "')");
	}

	String oldJobId = getJwsdpPeer().getJobId();
	if (oldJobId == jobId) {
	    return; // nothing has changed
	}
	getJwsdpPeer().setJobId(jobId);
	getWritingFile().setModified(true);
	// <<insert code to react further to this change here
	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	if (propertyChangeFirer != null) {
	    propertyChangeFirer.firePropertyChange("jobId", oldJobId, jobId);
	}

    }

    /**
     * @see GnucashWritableCustomerJob#setName(java.lang.String)
     */
    public void setName(final String jobName) {
	if (jobName == null || jobName.trim().length() == 0) {
	    throw new IllegalArgumentException("null or empty job-name given!");
	}

	String oldJobName = getJwsdpPeer().getJobName();
	if (oldJobName == jobName) {
	    return; // nothing has changed
	}
	getJwsdpPeer().setJobName(jobName);
	getWritingFile().setModified(true);
	// <<insert code to react further to this change here
	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	if (propertyChangeFirer != null) {
	    propertyChangeFirer.firePropertyChange("jobName", oldJobName, jobName);
	}
    }

    /**
     * @param jobActive true is the job is to be (re)activated, false to deactivate
     */
    public void setActive(final boolean jobActive) {

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
     * support for firing PropertyChangeEvents. (gets initialized only if we really
     * have listeners)
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
     * Add a PropertyChangeListener to the listener list. The listener is registered
     * for all properties.
     *
     * @param listener The PropertyChangeListener to be added
     */
    public final void addPropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange == null) {
	    myPropertyChange = new PropertyChangeSupport(this);
	}
	myPropertyChange.addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property. The listener will be
     * invoked only when a call on firePropertyChange names that specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
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
    public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(propertyName, listener);
	}
    }

    /**
     * Remove a PropertyChangeListener from the listener list. This removes a
     * PropertyChangeListener that was registered for all properties.
     *
     * @param listener The PropertyChangeListener to be removed
     */
    public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(listener);
	}
    }

}
