package org.gnucash.read.spec;

import java.util.Collection;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashVendor;

/**
 * This class represents a bill that is sent from a vendor
 * so you know what to pay him/her.<br>
 * <br>
 * Note: The correct business term is "bill" (as opposed to "invoice"), 
 * as used in the GnuCash documentation. However, on a technical level, both 
 * customer invoices and vendor bills are referred to as "GncInvoice" objects.
 * <br>
 * Implementations of this interface are comparable and sorts primarily on the date the Invoice was
 * created and secondarily on the date it should be paid.
 *
 * @see GnucashJob
 * @see GnucashVendor
 */
public interface GnucashVendorBill extends GnucashCustVendInvoice {

    /**
     * @return ID of vendor this invoice has been sent from 
     */
    String getVendorId(GnucashCustVendInvoice.ReadVariant readVar);

    /**
     * @return Customer this invoice has been sent to.
     */
    GnucashVendor getVendor();
	
    // ---------------------------------------------------------------

    GnucashVendorBillEntry getEntryById(String id) throws WrongInvoiceTypeException;

    Collection<GnucashVendorBillEntry> getEntries() throws WrongInvoiceTypeException;

    void addEntry(final GnucashVendorBillEntry entry);

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
