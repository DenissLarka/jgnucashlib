package org.gnucash.read.spec;

import java.util.Collection;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;

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
 * @see GnucashGenerJob
 * @see GnucashCustomer
 */
public interface GnucashJobInvoice extends GnucashGenerInvoice {

    /**
     * @return ID of customer this invoice/bill has been sent to.
     */
    String getJobId();

    /**
     * @return ID of customer this invoice/bill has been sent to.
     */
    String getCustomerId() throws WrongInvoiceTypeException;

    /**
     * @return ID of vendor this invoice/bill has been sent from.
     */
    String getVendorId() throws WrongInvoiceTypeException;
    
    // ----------------------------

    /**
     * @return Customer this invoice has been sent to.
     * @throws WrongInvoiceTypeException 
     */
    GnucashCustomerJob getCustJob() throws WrongInvoiceTypeException;
	
    /**
     * @return Vendor this bill has been sent from.
     * @throws WrongInvoiceTypeException 
     */
    GnucashVendorJob getVendJob() throws WrongInvoiceTypeException;
	
    // ----------------------------

    /**
     * @return Customer this invoice has been sent to.
     * @throws WrongInvoiceTypeException 
     */
    GnucashCustomer getCustomer() throws WrongInvoiceTypeException;
	
    /**
     * @return Vendor this bill has been sent from.
     * @throws WrongInvoiceTypeException 
     */
    GnucashVendor getVendor() throws WrongInvoiceTypeException;
	
    // ---------------------------------------------------------------

    GnucashJobInvoiceEntry getEntryById(String id) throws WrongInvoiceTypeException;

    Collection<GnucashJobInvoiceEntry> getEntries() throws WrongInvoiceTypeException;

    void addEntry(final GnucashJobInvoiceEntry entry);
    
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
