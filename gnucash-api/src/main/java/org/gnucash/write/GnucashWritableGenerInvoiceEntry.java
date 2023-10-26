package org.gnucash.write;

import java.time.LocalDate;

import org.gnucash.basetypes.InvalidCmdtyCurrTypeException;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashObject;
import org.gnucash.read.IllegalTransactionSplitActionException;
import org.gnucash.read.aux.GCshTaxTable;
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

    void setInvcPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    void setInvcPrice(FixedPointNumber price) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    void setInvcPriceFormatted(String price) throws WrongInvoiceTypeException, TaxTableNotFoundException, NumberFormatException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    // ------------------------

    void setBillPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    void setBillPrice(FixedPointNumber price) throws WrongInvoiceTypeException, TaxTableNotFoundException, NumberFormatException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    void setBillPriceFormatted(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    // ------------------------

    void setJobPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    void setJobPrice(FixedPointNumber price) throws WrongInvoiceTypeException, TaxTableNotFoundException, NumberFormatException, UnknownInvoiceTypeException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    void setJobPriceFormatted(String price)
	    throws NumberFormatException, WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException, IllegalTransactionSplitActionException, InvalidCmdtyCurrTypeException;

    // -----------------------------------------------------------

    void setAction(String a);

    void setQuantity(String quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    void setQuantity(FixedPointNumber quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    void setQuantityFormatted(String n) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws NumberFormatException 
     * @throws IllegalTransactionSplitActionException 
     */
    void remove() throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    // -----------------------------------------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws NumberFormatException 
     * @throws IllegalTransactionSplitActionException 
     */
    void setInvcTaxable(boolean val) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    /**
     * @param tax the new taxtable to use. Null sets isTaxable to false.
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     */
    void setInvcTaxTable(GCshTaxTable tax) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    // ------------------------

    /**
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     * @throws InvalidCmdtyCurrTypeException 
     * @throws NumberFormatException 
     * @throws IllegalTransactionSplitActionException 
     */
    void setBillTaxable(boolean val) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    /**
     * @param tax the new taxtable to use. Null sets isTaxable to false.
     * @throws WrongInvoiceTypeException
     * @throws TaxTableNotFoundException
     */
    void setBillTaxTable(GCshTaxTable tax) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    // -----------------------------------------------------------

    /**
     * @param name  the name of the user-defined attribute
     * @param value the value or null if not set
     * @see {@link GnucashObject#getUserDefinedAttribute(String)}
     */
    void setUserDefinedAttribute(final String name, final String value);
}
