package org.gnucash.read.impl.auxiliary;

import org.gnucash.read.GnucashVendor;
import org.gnucash.read.auxiliary.GCshAddress;

public class GCshAddressImpl implements GCshAddress {

	/**
	 * The JWSDP-object we are wrapping.
	 */
	private final org.gnucash.generated.Address jwsdpPeer;
	
	// -----------------------------------------------------------

	/**
	 * @param newPeer the JWSDP-object we are wrapping.
	 */
	@SuppressWarnings("exports")
	public GCshAddressImpl(final org.gnucash.generated.Address newPeer) {
		super();
		
		jwsdpPeer = newPeer;
	}

	// -----------------------------------------------------------

	/**
	 * @return The JWSDP-object we are wrapping.
	 */
	@SuppressWarnings("exports")
	public org.gnucash.generated.Address getJwsdpPeer() {
		return jwsdpPeer;
	}

	/**
	 * @see GnucashVendor.GCshAddress#getAddressName()
	 */
	public String getAddressName() {
		if (jwsdpPeer.getAddrName() == null) {
			return "";
		}
		return jwsdpPeer.getAddrName();
	}

	/**
	 * @see GnucashVendor.GCshAddress#getAddressLine1()
	 */
	public String getAddressLine1() {
		if (jwsdpPeer.getAddrAddr1() == null) {
			return "";
		}
		return jwsdpPeer.getAddrAddr1();
	}

	/**
	 * @see GnucashVendor.GCshAddress#getAddressLine2()
	 */
	public String getAddressLine2() {
		if (jwsdpPeer.getAddrAddr2() == null) {
			return "";
		}
		return jwsdpPeer.getAddrAddr2();
	}

	/**
	 * @return third and last line below the name
	 */
	public String getAddressLine3() {
		if (jwsdpPeer.getAddrAddr3() == null) {
			return "";
		}
		return jwsdpPeer.getAddrAddr3();
	}

	/**
	 * @return fourth and last line below the name
	 */
	public String getAddressLine4() {
		if (jwsdpPeer.getAddrAddr4() == null) {
			return "";
		}
		return jwsdpPeer.getAddrAddr4();
	}

	/**
	 * @return telephone
	 */
	public String getTel() {
		if (jwsdpPeer.getAddrPhone() == null) {
			return "";
		}
		return jwsdpPeer.getAddrPhone();
	}

	/**
	 * @return Fax
	 */
	public String getFax() {
		if (jwsdpPeer.getAddrFax() == null) {
			return "";
		}
		return jwsdpPeer.getAddrFax();
	}

	/**
	 * @return Email
	 */
	public String getEmail() {
		if (jwsdpPeer.getAddrEmail() == null) {
			return "";
		}
		return jwsdpPeer.getAddrEmail();
	}

	// ---------------------------------------------------------------
	    
	@Override
	public String toString() {
	        StringBuffer buffer = new StringBuffer();
	        
	        buffer.append(getAddressName() + "\n");
	        buffer.append("\n");
	        buffer.append(getAddressLine1() + "\n");
	        buffer.append(getAddressLine2() + "\n");
	        buffer.append(getAddressLine3() + "\n");
	        buffer.append(getAddressLine4() + "\n");
	        buffer.append("\n");
	        buffer.append("Tel.:   " + getTel()   + "\n");
	        buffer.append("Fax:    " + getFax()   + "\n");
	        buffer.append("eMail:  " + getEmail() + "\n");

	        return buffer.toString();
	}
}
