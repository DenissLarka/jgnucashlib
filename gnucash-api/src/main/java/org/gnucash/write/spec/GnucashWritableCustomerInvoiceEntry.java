package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.GnucashWritableObject;

/**
 * Invoice-Entry that can be modified.
 */
public interface GnucashWritableCustomerInvoiceEntry extends GnucashWritableGenerInvoiceEntry, 
                                                             GnucashWritableObject 
{

    void setTaxable(boolean val) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    void setTaxTable(GCshTaxTable taxTab) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    // ---------------------------------------------------------------

    void setPrice(String price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException;

    void setPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException;

}
