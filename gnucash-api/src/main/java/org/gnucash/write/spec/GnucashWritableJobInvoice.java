package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Job invoice that can be modified if isModifiable() returns true
 */
public interface GnucashWritableJobInvoice extends GnucashGenerInvoice {

    GnucashWritableJobInvoiceEntry getWritableEntryById(String id);
    
    // ---------------------------------------------------------------

    /**
     * Will throw an IllegalStateException if there are invoices for this job.<br/>
     * 
     * @param job the customer/vendor job that we link this invoice to.
     * @throws WrongInvoiceTypeException
     */
    void setGenerJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

    /**
     * Will throw an IllegalStateException if there are invoices for this job.<br/>
     * 
     * @param job the customer job that we link this invoice to.
     * @throws WrongInvoiceTypeException
     */
    void setCustomerJob(GnucashCustomerJob job) throws WrongInvoiceTypeException;

    /**
     * Will throw an IllegalStateException if there are invoices for this job.<br/>
     * 
     * @param job the vendor job that we link this invoice to.
     * @throws WrongInvoiceTypeException
     */
    void setVendorJob(GnucashVendorJob job) throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

    GnucashWritableJobInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException;

    GnucashWritableJobInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final FixedPointNumber tax)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException;

    GnucashWritableJobInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final GCshTaxTable tax)
	    throws WrongInvoiceTypeException, NoTaxTableFoundException;
}
