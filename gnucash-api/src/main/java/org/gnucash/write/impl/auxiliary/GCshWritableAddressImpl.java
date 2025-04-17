package org.gnucash.write.impl.auxiliary;

import org.gnucash.read.impl.auxiliary.GCshAddressImpl;
import org.gnucash.write.GnucashWritableCustomer;
import org.gnucash.write.auxiliary.GCshWritableAddress;

/**
 * Writable implementation in {@link GCshAddressImpl}
 */
public class GCshWritableAddressImpl extends GCshAddressImpl 
                                     implements GCshWritableAddress 
{

	/**
	 * @param jwsdpPeer
	 */
	public GCshWritableAddressImpl(final org.gnucash.generated.Address jwsdpPeer) {
		super(jwsdpPeer);
	}
	
	// -----------------------------------------------------------

	/**
	 * @see GCshWritableAddress#setAddressName(java.lang.String)
	 */
	public void setAddressName(final String a) {
		getJwsdpPeer().setAddrName(a);
		//TODO: setModified()
	}

	/**
	 * @see GCshWritableAddress#setAddressLine1(java.lang.String)
	 */
	public void setAddressLine1(final String a) {
		getJwsdpPeer().setAddrAddr1(a);
		//TODO: setModified()
	}

	/**
	 * @see GCshWritableAddress#setAddressLine2(java.lang.String)
	 */
	public void setAddressLine2(final String a) {
		getJwsdpPeer().setAddrAddr2(a);
		//TODO: setModified()
	}

	/**
	 * @see GnucashWritableCustomer.WritableAddress#setAddressLine4(java.lang.String)
	 */
	public void setAddressLine3(final String a) {
		getJwsdpPeer().setAddrAddr3(a);
		//TODO: setModified()
	}

	/**
	 * @see GnucashWritableCustomer.WritableAddress#setAddressLine4(java.lang.String)
	 */
	public void setAddressLine4(final String a) {
		getJwsdpPeer().setAddrAddr4(a);
		//TODO: setModified()
	}

	/**
	 * @see GnucashWritableCustomer.WritableAddress#setTel(java.lang.String)
	 */
	public void setTel(final String a) {
		getJwsdpPeer().setAddrPhone(a);
		//TODO: setModified()
	}

	/**
	 * @see GnucashWritableCustomer.WritableAddress#setFac(java.lang.String)
	 */
	public void setFax(final String a) {
		getJwsdpPeer().setAddrFax(a);
		//TODO: setModified()
	}

	/**
	 * @see GnucashWritableCustomer.WritableAddress#setEmail(java.lang.String)
	 */
	public void setEmail(final String a) {
		getJwsdpPeer().setAddrEmail(a);
		//TODO: setModified()
	}

}
