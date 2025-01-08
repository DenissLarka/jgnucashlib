package org.gnucash.write;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.auxiliary.GCshAddress;
import org.gnucash.write.auxiliary.GCshWritableAddress;

/**
 * Customer that can be modified
 */
public interface GnucashWritableCustomer extends GnucashCustomer, 
                                                 GnucashWritableObject 
{

	void remove();


	/**
	 * @see {@link GnucashCustomer#getNumber()}
	 * @param number the user-assigned number of this customer (may contain non-digits)
	 */
	void setNumber(String number);

	void setDiscount(FixedPointNumber discount);

	void setCredit(FixedPointNumber credit);

	/**
	 * @param notes user-defined notes about the customer (may be null)
	 */
	void setNotes(String notes);


	void setName(String name);

	void setAddress(GCshAddress adr);

	void setShippingAddress(GCshAddress adr);

	GCshWritableAddress getWritableAddress();

	GCshWritableAddress getWritableShippingAddress();

	GCshWritableAddress getAddress();

	GCshWritableAddress getShippingAddress();


	/**
	 * @param name the name of the user-defined attribute
	 * @param value the value or null if not set
	 * @see {@link GnucashObject#getUserDefinedAttribute(String)}
	 */
	void setUserDefinedAttribute(final String name, final String value);
}
