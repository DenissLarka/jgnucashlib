package org.gnucash.write.impl.spec;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.gnucash.Const;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.OwnerId;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.auxiliary.GCshOwner;
import org.gnucash.read.impl.spec.GnucashVendorJobImpl;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.spec.GnucashWritableVendorJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modifiable version of a Job implemented.<br/>
 * <p>
 * Additional supported properties for PropertyChangeListeners:
 * <ul>
 * <li>vendor</li>
 * <li>active</li>
 * <li>name</li>
 * </ul>
 */

public class GnucashWritableVendorJobImpl extends GnucashVendorJobImpl implements GnucashWritableVendorJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableVendorJobImpl.class);

	/**
	 * @param jwsdpPeer the XML(jaxb)-object we are fronting.
	 * @param file      the file we belong to
	 */
	@SuppressWarnings("exports")
	public GnucashWritableVendorJobImpl(
			final GncV2.GncBook.GncGncJob jwsdpPeer,
			final GnucashFile file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param owner the vendor the job is from
	 * @param file  the file to add the jhe to
	 */
	public GnucashWritableVendorJobImpl(
			final GnucashWritableFileImpl file,
			final GnucashVendor owner,
			final String number,
			final String name) {
		super(createJob(file, file.createGUID(), owner, number, name), file);
	}

	// -----------------------------------------------------------------

	/**
	 * @see GnucashWritableVendorJob#remove()
	 */
	public void remove() throws WrongInvoiceTypeException {
		if (!getInvoices().isEmpty()) {
			throw new IllegalStateException("cannot remove a job that has invoices!");
		}
		GnucashWritableFileImpl writableFile = (GnucashWritableFileImpl) getFile();
		writableFile.getRootElement().getGncBook().getBookElements().remove(getJwsdpPeer());
		writableFile.removeGenerJob(this);
	}

	/**
	 * @param vend the vendor the job is from
	 * @param file the file to add the jhe to
	 * @param guid the internal id to use. May be null to generate an ID.
	 * @return the jaxb-job
	 */
	private static GncV2.GncBook.GncGncJob createJob(
			final GnucashWritableFileImpl file,
			final String guid,
			final GnucashVendor vend,
			final String number,
			final String name) {

		if (file == null) {
			throw new IllegalArgumentException("null file given");
		}

		if (vend == null) {
			throw new IllegalArgumentException("null vendor given");
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
			owner.setOwnerType(GCshOwner.TYPE_VENDOR);

			OwnerId ownerid = factory.createOwnerId();
			ownerid.setType(Const.XML_DATA_TYPE_GUID);
			ownerid.setValue(vend.getId());

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
	//     * @see GnucashWritableVendorJob#setVendorType(java.lang.String)
	//     */
	//    public void setVendorType(final String vendorType) {
	//	if (vendorType == null) {
	//	    throw new IllegalArgumentException("null 'vendorType' given!");
	//	}
	//
	//	Object old = getJwsdpPeer().getJobOwner().getOwnerType();
	//	if (old == vendorType) {
	//	    return; // nothing has changed
	//	}
	//	getJwsdpPeer().getJobOwner().setOwnerType(vendorType);
	//	getWritingFile().setModified(true);
	//	// <<insert code to react further to this change here
	//	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	//	if (propertyChangeFirer != null) {
	//	    propertyChangeFirer.firePropertyChange("vendorType", old, vendorType);
	//	}
	//    }

	/**
	 * @see GnucashWritableVendorJob#setVendor(GnucashVendor)
	 */
	public void setVendor(final GnucashVendor vend) throws WrongInvoiceTypeException {
		if (!getInvoices().isEmpty()) {
			throw new IllegalStateException("cannot change vendor of a job that has invoices!");
		}

		if (vend == null) {
			throw new IllegalArgumentException("null 'vendor' given!");
		}

		GnucashVendor oldVend = getVendor();
		if (oldVend == vend) {
			return; // nothing has changed
		}
		getJwsdpPeer().getJobOwner().getOwnerId().setValue(vend.getId());
		getWritingFile().setModified(true);
		// <<insert code to react further to this change here
		PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
		if (propertyChangeFirer != null) {
			propertyChangeFirer.firePropertyChange("vendor", oldVend, vend);
		}
	}

	/**
	 * @see GnucashWritableVendorJob#setJobNumber(java.lang.String)
	 */
	public void setJobNumber(final String jobId) {
		if (jobId == null || jobId.trim().length() == 0) {
			throw new IllegalArgumentException("null or empty job-number given!");
		}
		GnucashGenerJob otherJob = getWritingFile().getGenerJobByNumber(jobId);
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
	 * @see GnucashWritableVendorJob#setName(java.lang.String)
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
