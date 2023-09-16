/**
 * GnucashWritableInvoiceEntry.java
 * License: GPLv3 or later
 * created: 14.06.2005 09:50:15
 */
package org.gnucash.write;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashInvoiceEntry;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.GnucashTaxTable;


//other imports

/**
 * Project: gnucashReader <br/>
 * GnucashWritableInvoiceEntry.java <br/>
 * created: 14.06.2005 09:50:15 <br/>
 * <p>
 * <p>
 * Invoice-Entry that can be modified.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashWritableInvoiceEntry extends GnucashInvoiceEntry, GnucashWritableObject {

	/**
	 * @see GnucashInvoiceEntry#getInvoice() .
	 */
	GnucashWritableInvoice getInvoice();

	/**
	 * Set the description-text.
	 *
	 * @param desc the new description
	 */
	void setDescription(String desc);

	void setPrice(String price);

	void setPrice(FixedPointNumber price);

	void setAction(String a);

	void setQuantity(String quantity);

	void setQuantity(FixedPointNumber quantity);

	void setQuantityFormated(String n);

	/**
	 *
	 */
	void remove();

	/**
	 * @param tax the new taxtable to use. Null sets isTaxable to false.
	 */
	void setTaxTable(GnucashTaxTable tax);

	/**
	 * @param name  the name of the user-defined attribute
	 * @param value the value or null if not set
	 * @see {@link GnucashObject#getUserDefinedAttribute(String)}
	 */
	void setUserDefinedAttribute(final String name, final String value);
}

