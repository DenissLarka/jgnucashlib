package org.gnucash.read.impl;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashVendorTerms;

public class GnucashVendorTermsImpl implements GnucashVendorTerms {

	/**
	 * the JWSDP-object we are facading.
	 */
	private final GncV2.GncBook.GncGncVendor.VendorTerms jwsdpPeer;

	/**
	 * The file we belong to.
	 */
	private final GnucashFile file;

	/**
	 * @param peer the JWSDP-object we are facading.
	 * @see #jwsdpPeer
	 * @param gncFile the file to register under
	 */
	public GnucashVendorTermsImpl(
			final GncV2.GncBook.GncGncVendor.VendorTerms peer,
			final GnucashFile gncFile) {
		super();
		jwsdpPeer = peer;
		file = gncFile;
	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 * @return the file we are associated with
	 */
	public GnucashFile getFile() {
		return file;
	}

	/**
	 *
	 * @return The JWSDP-Object we are wrapping.
	 */
	public GncV2.GncBook.GncGncVendor.VendorTerms getJwsdpPeer() {
		return jwsdpPeer;
	}

	public String getType() {
		return jwsdpPeer.getType();
	}

	public String getValue() {
		return jwsdpPeer.getValue();
	}
}
