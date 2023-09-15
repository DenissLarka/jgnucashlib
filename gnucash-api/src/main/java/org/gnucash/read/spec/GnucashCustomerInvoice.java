package org.gnucash.read.spec;

import java.util.Collection;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashJob;

/**
 * This class represents an invoice that is sent to a customer
 * so (s)he knows what to pay you. <br>
 * <br>
 * Note: The correct business term is "invoice" (as opposed to "bill"), 
 * as used in the GnuCash documentation. However, on a technical level, both 
 * customer invoices and vendor bills are referred to as "GncInvoice" objects.
 * <br>
 * Implementations of this interface are comparable and sorts primarily on the date the Invoice was
 * created and secondarily on the date it should be paid.
 *
 * @see GnucashJob
 * @see GnucashCustomer
 */
public interface GnucashCustomerInvoice extends GnucashCustVendInvoice {

    /**
     * @return ID of customer this invoice has been sent to.
     */
    String getCustomerId(GnucashCustVendInvoice.ReadVariant readVar);

    /**
     * @return Customer this invoice has been sent to.
     * @throws WrongInvoiceTypeException 
     */
    GnucashCustomer getCustomer() throws WrongInvoiceTypeException;
	
    // ---------------------------------------------------------------

    GnucashCustomerInvoiceEntry getEntryById(String id) throws WrongInvoiceTypeException;

    Collection<GnucashCustomerInvoiceEntry> getEntries() throws WrongInvoiceTypeException;

    void addEntry(final GnucashCustomerInvoiceEntry entry);
    
    // ---------------------------------------------------------------

    public FixedPointNumber getAmountUnpaidWithTaxes() throws WrongInvoiceTypeException;

    public FixedPointNumber getAmountPaidWithTaxes() throws WrongInvoiceTypeException;

    public FixedPointNumber getAmountPaidWithoutTaxes() throws WrongInvoiceTypeException;

    public FixedPointNumber getAmountWithTaxes() throws WrongInvoiceTypeException;
    
    public FixedPointNumber getAmountWithoutTaxes() throws WrongInvoiceTypeException;

    // ----------------------------

    public String getAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    public String getAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    public String getAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException;

    public String getAmountWithTaxesFormatted() throws WrongInvoiceTypeException;

    public String getAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

    public boolean isFullyPaid() throws WrongInvoiceTypeException;

    public boolean isNotFullyPaid() throws WrongInvoiceTypeException;

}
