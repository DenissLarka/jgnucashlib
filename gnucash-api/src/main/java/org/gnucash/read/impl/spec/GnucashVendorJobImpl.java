package org.gnucash.read.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.GnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorJobImpl extends GnucashGenerJobImpl implements GnucashVendorJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorJobImpl.class);

	/**
	 * @param peer    the JWSDP-object we are facading.
	 * @param gncFile the file to register under
	 * @see #jwsdpPeer
	 */
	@SuppressWarnings("exports")
	public GnucashVendorJobImpl(
			final GncV2.GncBook.GncGncJob peer,
			final GnucashFile gncFile) {
		super(peer, gncFile);
	}

	public GnucashVendorJobImpl(final GnucashGenerJob job) throws WrongInvoiceTypeException {
		super(job.getJwsdpPeer(), job.getFile());

		// No, we cannot check that first, because the super() method
		// always has to be called first.
		if (!job.getOwnerType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
			throw new WrongInvoiceTypeException();
		}

		// ::TODO
		//	for ( GnucashGenerInvoice invc : job.getInvoices() )
		//	{
		//	    addInvoice(new GnucashJobInvoiceImpl(invc));
		//	}
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public String getVendorId() {
		return getOwnerId();
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashVendor getVendor() {
		return file.getVendorByID(getVendorId());
	}

	// -----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[GnucashVendorJobImpl:");
		buffer.append(" id: ");
		buffer.append(getId());

		buffer.append(" number: ");
		buffer.append(getNumber());

		buffer.append(" name: '");
		buffer.append(getName() + "'");

		buffer.append(" vendor-id: ");
		buffer.append(getVendorId());

		buffer.append(" is-active: ");
		buffer.append(isActive());

		buffer.append("]");
		return buffer.toString();
	}

}
