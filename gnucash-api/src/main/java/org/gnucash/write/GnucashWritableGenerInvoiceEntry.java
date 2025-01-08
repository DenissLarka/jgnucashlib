package org.gnucash.write;

import java.time.LocalDate;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.impl.TaxTableNotFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.impl.UnknownInvoiceTypeException;

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

    void setDate(final LocalDate date);

    /**
     * Set the description-text.
     *
     * @param desc the new description
     */
    void setDescription(final String desc);

    // -----------------------------------------------------------

    void setInvcPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException;

    void setInvcPrice(FixedPointNumber price) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    void setInvcPriceFormatted(String price) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    // ------------------------

    void setBillPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException;

    void setBillPrice(FixedPointNumber price) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    void setBillPriceFormatted(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException;

    // ------------------------

    void setJobPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException;

    void setJobPrice(FixedPointNumber price) throws WrongInvoiceTypeException, TaxTableNotFoundException, NumberFormatException, UnknownInvoiceTypeException;

    void setJobPriceFormatted(String price)
	    throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException;

    // -----------------------------------------------------------

    void setAction(String a);

    void setQuantity(String quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    void setQuantity(FixedPointNumber quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    void setQuantityFormatted(String n) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     */
    void remove() throws WrongInvoiceTypeException, TaxTableNotFoundException;

    // -----------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     */
    void setInvcTaxable(boolean val) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    /**
     * @param tax the new taxtable to use. Null sets isTaxable to false.
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     */
    void setInvcTaxTable(GCshTaxTable tax) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    // ------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     */
    void setBillTaxable(boolean val) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    /**
     * @param tax the new taxtable to use. Null sets isTaxable to false.
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     */
    void setBillTaxTable(GCshTaxTable tax) throws WrongInvoiceTypeException, TaxTableNotFoundException;

    // -----------------------------------------------------------

    /**
     * @param name  the name of the user-defined attribute
     * @param value the value or null if not set
     * @see {@link GnucashObject#getUserDefinedAttribute(String)}
     */
    void setUserDefinedAttribute(final String name, final String value);
}
