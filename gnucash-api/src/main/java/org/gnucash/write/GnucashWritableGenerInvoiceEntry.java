package org.gnucash.write;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.aux.GnucashTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Invoice-Entry that can be modified.
 */
public interface GnucashWritableGenerInvoiceEntry extends GnucashGenerInvoiceEntry, 
                                                          GnucashWritableObject 
{

	/**
	 * @see GnucashGenerInvoiceEntry#getGenerInvoice() .
	 */
	GnucashWritableGenerInvoice getGenerInvoice();

	/**
	 * Set the description-text.
	 *
	 * @param desc the new description
	 */
	void setDescription(String desc);

	void setInvcPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

	void setInvcPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	void setAction(String a);

	void setInvcQuantity(String quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	void setInvcQuantity(FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	void setQuantityFormatted(String n) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	void remove() throws WrongInvoiceTypeException, NoTaxTableFoundException;

	/**
	 * @param tax the new taxtable to use. Null sets isTaxable to false.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	void setInvcTaxTable(GnucashTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	/**
	 * @param name  the name of the user-defined attribute
	 * @param value the value or null if not set
	 * @see {@link GnucashObject#getUserDefinedAttribute(String)}
	 */
	void setUserDefinedAttribute(final String name, final String value);
}

