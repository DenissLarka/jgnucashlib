package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.GnucashWritableObject;
import org.gnucash.write.impl.UnknownInvoiceTypeException;

/**
 * Invoice-Entry that can be modified.
 */
public interface GnucashWritableJobInvoiceEntry extends GnucashWritableGenerInvoiceEntry, 
                                                        GnucashWritableObject 
{

    void setTaxable(boolean val) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException, UnknownInvoiceTypeException;

    void setTaxTable(GCshTaxTable taxTab) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException, UnknownInvoiceTypeException;

    // ---------------------------------------------------------------

    void setPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException, UnknownInvoiceTypeException;

    void setPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException, NumberFormatException, UnknownInvoiceTypeException;

}
