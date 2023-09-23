package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Customer invoice that can be modified if isModifiable() returns true
 */
public interface GnucashWritableCustomerInvoice extends GnucashGenerInvoice {

    void setJob(GnucashCustomerJob job) throws WrongInvoiceTypeException;

    GnucashWritableCustomerInvoiceEntry getWritableEntryById(String id);
    
    // ---------------------------------------------------------------

    GnucashWritableCustomerInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    GnucashWritableCustomerInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final FixedPointNumber tax)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException;

    GnucashWritableCustomerInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final GCshTaxTable tax)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException;
}
