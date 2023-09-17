package org.gnucash.read.impl.aux;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GnucashCustomerTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashCustomerTermsImpl implements GnucashCustomerTerms {

  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashCustomerTermsImpl.class);

	/**
	 * the JWSDP-object we are facading.
	 */
	private final GncV2.GncBook.GncGncCustomer.CustTerms jwsdpPeer;

	/**
	 * The file we belong to.
	 */
	private final GnucashFile file;

	/**
	 * @param peer the JWSDP-object we are facading.
	 * @see #jwsdpPeer
	 * @param gncFile the file to register under
	 */
	@SuppressWarnings("exports")
	public GnucashCustomerTermsImpl(
			final GncV2.GncBook.GncGncCustomer.CustTerms peer,
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
	@SuppressWarnings("exports")
    public GncV2.GncBook.GncGncCustomer.CustTerms getJwsdpPeer() {
		return jwsdpPeer;
	}

	public String getType() {
		return jwsdpPeer.getType();
	}

	public String getValue() {
		return jwsdpPeer.getValue();
	}
}
