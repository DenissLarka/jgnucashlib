package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Vendor bill that can be modified if isModifiable() returns true
 */
public interface GnucashWritableVendorBill extends GnucashGenerInvoice {

    GnucashWritableVendorBillEntry getWritableEntryById(String id);
    
    // ---------------------------------------------------------------

    /**
     * Will throw an IllegalStateException if there are bills for this vendor.<br/>
     * 
     * @param vend the vendor who sent an invoice to us.
     * @throws WrongInvoiceTypeException
     */
    void setVendor(GnucashVendor vend) throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

    GnucashWritableVendorBillEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    GnucashWritableVendorBillEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final FixedPointNumber tax)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException;

    GnucashWritableVendorBillEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final GCshTaxTable tax)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException;
}
