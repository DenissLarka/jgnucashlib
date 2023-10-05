package org.gnucash.read;

import java.util.Collection;
import java.util.Locale;

import org.gnucash.generated.GncV2.GncBook.GncGncVendor.VendorTerms;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshAddress;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public interface GnucashVendor extends GnucashObject {

    /**
     * The prefered taxtable to use with this vendor (may be null).
     * @see {@link #getVendorTaxTableID()}
     */
    @SuppressWarnings("exports")
    VendorTerms getVendorTerms();

    /**
     * Date is not checked so invoiced that have entered payments in the future are considered Paid.
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException 
     */
    int getNofOpenBills() throws WrongInvoiceTypeException;

    /**
     * @return the sum of payments for invoices to this client
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getExpensesGenerated(GnucashGenerInvoice.ReadVariant readVar);

    /**
     * @return the sum of payments for invoices to this client
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getExpensesGenerated_direct();

    /**
     * @return the sum of payments for invoices to this client
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getExpensesGenerated_viaAllJobs();

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getExpensesGenerated()
     * Formatted acording to the current locale's currency-format
     */
    String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar);

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getExpensesGenerated()
     * Formatted acording to the given locale's currency-format
     */
    String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar, Locale l);

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getOutstandingValue(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException;

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getOutstandingValue_direct() throws WrongInvoiceTypeException;

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getOutstandingValue_viaAllJobs() throws WrongInvoiceTypeException;

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getOutstandingValue()
     * Formatted acording to the current locale's currency-format
     */
    String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException;

    /**
     *
     * @throws WrongInvoiceTypeException 
     * @see #getOutstandingValue()
     * Formatted acording to the given locale's currency-format
     */
    String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar, Locale l) throws WrongInvoiceTypeException;

    /**
     * The gnucash-file is the top-level class to contain everything.
     * @return the file we are associated with
     */
    GnucashFile getGnucashFile();

    /**
     * @return the unique-id to identify this object with across name- and hirarchy-changes
     */
    String getId();

    /**
     * @return the UNMODIFIABLE collection of jobs that have this vendor associated with them.
     * @throws WrongInvoiceTypeException 
     */
    Collection<GnucashVendorJob> getJobs() throws WrongInvoiceTypeException;

    /**
     *
     * @return the user-assigned number of this vendor (may contain non-digits)
     */
    String getNumber();

    /**
     *
     * @return the name of the vendor
     */
    String getName();

    /**
     * @return the address including the name
     */
    GCshAddress getAddress();

    // ----------------------------

    Collection<GnucashGenerInvoice> getBills() throws WrongInvoiceTypeException;
    
    Collection<GnucashVendorBill>   getPaidBills_direct() throws WrongInvoiceTypeException;
    
    Collection<GnucashJobInvoice>   getPaidBills_viaAllJobs() throws WrongInvoiceTypeException;
    
    Collection<GnucashVendorBill>   getUnpaidBills_direct() throws WrongInvoiceTypeException;
    
    Collection<GnucashJobInvoice>   getUnpaidBills_viaAllJobs() throws WrongInvoiceTypeException;
    
    // ----------------------------
    
    public static int getHighestNumber(GnucashVendor vend)
    {
      return vend.getGnucashFile().getHighestVendorNumber();
    }

    public static String getNewNumber(GnucashVendor vend)
    {
      return vend.getGnucashFile().getNewVendorNumber();
    }

}
