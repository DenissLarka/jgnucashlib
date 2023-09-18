package org.gnucash.write.impl;

import java.beans.PropertyChangeSupport;

import org.gnucash.Const;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.aux.GCshAddress;
import org.gnucash.read.impl.GnucashVendorImpl;
import org.gnucash.read.impl.aux.GCshAddressImpl;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableObject;
import org.gnucash.write.GnucashWritableVendor;
import org.gnucash.write.aux.GCshWritableAddress;
import org.gnucash.write.impl.aux.GCshWritableAddressImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorWritingImpl extends GnucashVendorImpl 
                                      implements GnucashWritableVendor 
{
	/**
	 * Automatically created logger for debug and error-output.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorWritingImpl.class);

	/**
	 * Our helper to implement the GnucashWritableObject-interface.
	 */
	private final GnucashWritableObjectHelper helper = new GnucashWritableObjectHelper(this);

	/**
	 * @see GnucashWritableObject#setUserDefinedAttribute(java.lang.String, java.lang.String)
	 */
	public void setUserDefinedAttribute(final String name, final String value) {
		helper.setUserDefinedAttribute(name, value);
	}

	/**
	 * Please use ${@link GnucashWritableFile#createWritableVendor()}.
	 *
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 */
	protected GnucashVendorWritingImpl(final GncV2.GncBook.GncGncVendor jwsdpPeer, final GnucashFileWritingImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * Please use ${@link GnucashWritableFile#createWritableVendor()}.
	 *
	 * @param file the file we belong to
	 * @param id   the ID we shall have
	 */
	protected GnucashVendorWritingImpl(final GnucashFileWritingImpl file,
			final String id) {
		super(createVendor(file, id), file);
	}

	/**
	 * Delete this Vendor and remove it from the file.
	 *
	 * @see GnucashWritableVendor#remove()
	 */
	public void remove() {
		GncV2.GncBook.GncGncVendor peer = getJwsdpPeer();
		(getGnucashFile()).getRootElement().getGncBook().getBookElements().remove(peer);
		(getGnucashFile()).removeVendor(this);
	}

	/**
	 * Creates a new Transaction and add's it to the given gnucash-file
	 * Don't modify the ID of the new transaction!
	 *
	 * @param file the file we will belong to
	 * @param guid the ID we shall have
	 * @return a new jwsdp-peer alredy entered into th jwsdp-peer of the file
	 */
	protected static GncV2.GncBook.GncGncVendor createVendor(final GnucashFileWritingImpl file, final String guid) {

		if (guid == null) {
			throw new IllegalArgumentException("null guid given!");
		}

		ObjectFactory factory = file.getObjectFactory();

		GncV2.GncBook.GncGncVendor vend = file.createGncGncVendorType();

		vend.setVendorTaxincluded("USEGLOBAL");
		vend.setVersion(Const.XML_FORMAT_VERSION);
		vend.setVendorUseTt(0);
		vend.setVendorName("no name given");

		{
			GncV2.GncBook.GncGncVendor.VendorGuid id = factory.createGncV2GncBookGncGncVendorVendorGuid();
			id.setType("guid");
			id.setValue(guid);
			vend.setVendorGuid(id);
			vend.setVendorId(id.getValue());
		}

		{
			org.gnucash.generated.Address addr = factory.createAddress();
			addr.setAddrAddr1("");
			addr.setAddrAddr2("");
			addr.setAddrName("");
			addr.setAddrAddr3("");
			addr.setAddrAddr4("");
			addr.setAddrName("");
			addr.setAddrEmail("");
			addr.setAddrFax("");
			addr.setAddrPhone("");
			addr.setVersion(Const.XML_FORMAT_VERSION);
			vend.setVendorAddr(addr);
		}

		{
			GncV2.GncBook.GncGncVendor.VendorCurrency currency = factory.createGncV2GncBookGncGncVendorVendorCurrency();
			currency.setCmdtyId(file.getDefaultCurrencyID());
			currency.setCmdtySpace("ISO4217");
			vend.setVendorCurrency(currency);
		}

		vend.setVendorActive(1);

		file.getRootElement().getGncBook().getBookElements().add(vend);
		file.setModified(true);
		
		return vend;
	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	public GnucashFileWritingImpl getWritableGnucashFile() {
		return (GnucashFileWritingImpl) super.getGnucashFile();
	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	@Override
	public GnucashFileWritingImpl getGnucashFile() {
		return (GnucashFileWritingImpl) super.getGnucashFile();
	}

	/**
	 * @see GnucashWritableVendor#setNumber(java.lang.String)
	 */
	public void setNumber(final String number) {
		Object old = getNumber();
		getJwsdpPeer().setVendorId(number);
		getGnucashFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("VendorNumber", old, number);
		}
	}

	/**
	 * @see GnucashWritableVendor#setName(java.lang.String)
	 */
	public void setName(final String name) {
		Object old = getName();
		getJwsdpPeer().setVendorName(name);
		getGnucashFile().setModified(true);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("name", old, name);
		}
	}

	/**
	 * @see GnucashVendor#getAddress()
	 */
	@Override
	public GCshWritableAddress getAddress() {
		return getWritableAddress();
	}

	/**
	 * @see GnucashWritableVendor#getWritableAddress()
	 */
	public GCshWritableAddress getWritableAddress() {
		return new GCshWritableAddressImpl(getJwsdpPeer().getVendorAddr());
	}

	/**
	 * @see GnucashWritableVendor#setAdress(org.gnucash.fileformats.gnucash.GnucashVendor.ShippingAdress)
	 */
	public void setAddress(final GCshAddress adr) {
		/*if (adr instanceof AddressImpl) {
            AddressImpl adrImpl = (AddressImpl) adr;
            getJwsdpPeer().setVendAddr(adrImpl.getJwsdpPeer());
        } else */
		{

			if (getJwsdpPeer().getVendorAddr() == null) {
				getJwsdpPeer().setVendorAddr(getGnucashFile().getObjectFactory().createAddress());
			}

			getJwsdpPeer().getVendorAddr().setAddrAddr1(adr.getAddressLine1());
			getJwsdpPeer().getVendorAddr().setAddrAddr2(adr.getAddressLine2());
			getJwsdpPeer().getVendorAddr().setAddrAddr3(adr.getAddressLine3());
			getJwsdpPeer().getVendorAddr().setAddrAddr4(adr.getAddressLine4());
			getJwsdpPeer().getVendorAddr().setAddrName(adr.getAddressName());
			getJwsdpPeer().getVendorAddr().setAddrEmail(adr.getEmail());
			getJwsdpPeer().getVendorAddr().setAddrFax(adr.getFax());
			getJwsdpPeer().getVendorAddr().setAddrPhone(adr.getTel());
		}

		getGnucashFile().setModified(true);
	}
}
