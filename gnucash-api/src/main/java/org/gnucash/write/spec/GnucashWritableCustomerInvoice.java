package org.gnucash.write.spec;

import java.time.LocalDate;

import org.gnucash.basetypes.InvalidCmdtyCurrTypeException;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.IllegalTransactionSplitActionException;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.TaxTableNotFoundException;
import org.gnucash.read.impl.aux.WrongOwnerTypeException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;

/**
 * Customer invoice that can be modified if isModifiable() returns true
 */
public interface GnucashWritableCustomerInvoice extends GnucashWritableGenerInvoice {

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
	    final FixedPointNumber quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    GnucashWritableCustomerInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final String taxTabName)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    GnucashWritableCustomerInvoiceEntry createEntry(
	    GnucashAccount acct, 
	    final FixedPointNumber singleUnitPrice,
	    final FixedPointNumber quantity, 
	    final GCshTaxTable taxTab)
	    throws WrongInvoiceTypeException, TaxTableNotFoundException, IllegalTransactionSplitActionException, NumberFormatException, InvalidCmdtyCurrTypeException;

    // ---------------------------------------------------------------
    
    void post(final GnucashAccount incomeAcct,
	      final GnucashAccount receivableAcct,
	      final LocalDate postDate,
	      final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException, NumberFormatException, InvalidCmdtyCurrTypeException;

}
