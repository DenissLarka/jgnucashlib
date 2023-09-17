package org.gnucash.write;

import org.gnucash.read.GnucashVendor;
import org.gnucash.read.aux.GnucashAddress;
import org.gnucash.write.aux.GnucashWritableAddress;
import org.gnucash.read.GnucashObject;

/**
 * Vendor that can be modified
 */
public interface GnucashWritableVendor extends GnucashVendor, 
                                               GnucashWritableObject 
{

	void remove();

	/**
	 * @see {@link GnucashVendor#getNumber()}
	 * @param number the user-assigned number of this Vendor (may contain non-digits)
	 */
	void setNumber(String number);

	void setName(String name);

	void setAddress(GnucashAddress adr);

	GnucashWritableAddress getWritableAddress();

	GnucashWritableAddress getAddress();


	/**
	 * @param name the name of the user-defined attribute
	 * @param value the value or null if not set
	 * @see {@link GnucashObject#getUserDefinedAttribute(String)}
	 */
	void setUserDefinedAttribute(final String name, final String value);
}
