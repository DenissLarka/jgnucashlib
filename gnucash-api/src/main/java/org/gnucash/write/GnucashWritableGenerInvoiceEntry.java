package org.gnucash.write;

import java.time.LocalDate;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.aux.GCshTaxTable;
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

    void setDate(final LocalDate date);

    /**
     * Set the description-text.
     *
     * @param desc the new description
     */
    void setDescription(final String desc);

    // -----------------------------------------------------------

    void setInvcPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    void setInvcPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    void setInvcPriceFormatted(String price) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    // ------------------------

    void setBillPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    void setBillPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    void setBillPriceFormatted(String price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    // ------------------------

    void setJobPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    void setJobPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    void setJobPriceFormatted(String price)
	    throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    // -----------------------------------------------------------

    void setAction(String a);

    void setQuantity(String quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    void setQuantity(FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    void setQuantityFormatted(String n) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    void remove() throws WrongInvoiceTypeException, NoTaxTableFoundException;

    // -----------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    void setInvcTaxable(boolean val) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    /**
     * @param tax the new taxtable to use. Null sets isTaxable to false.
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    void setInvcTaxTable(GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    // ------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    void setBillTaxable(boolean val) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    /**
     * @param tax the new taxtable to use. Null sets isTaxable to false.
     * @throws WrongInvoiceTypeException
     * @throws NoTaxTableFoundException
     */
    void setBillTaxTable(GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    // -----------------------------------------------------------

    /**
     * @param name  the name of the user-defined attribute
     * @param value the value or null if not set
     * @see {@link GnucashObject#getUserDefinedAttribute(String)}
     */
    void setUserDefinedAttribute(final String name, final String value);
}
