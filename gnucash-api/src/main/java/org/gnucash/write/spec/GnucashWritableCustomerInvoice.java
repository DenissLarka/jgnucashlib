package org.gnucash.write.spec;

import java.time.LocalDate;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Customer invoice that can be modified if isModifiable() returns true
 */
public interface GnucashWritableCustomerInvoice extends GnucashGenerInvoice {

    GnucashWritableCustomerInvoiceEntry getWritableEntryById(String id);
    
    // ---------------------------------------------------------------

    /**
     * Will throw an IllegalStateException if there are invoices for this customer.<br/>
     * 
     * @param cust the customer to whom we send an invoice to
     * @throws WrongInvoiceTypeException
     */
    void setCustomer(GnucashCustomer cust) throws WrongInvoiceTypeException;

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

    // ---------------------------------------------------------------
    
    void post(final GnucashAccount incomeAcct,
	      final GnucashAccount receivableAcct,
	      final LocalDate postDate,
	      final LocalDate dueDate);

}
