package org.gnucash.read;

import java.util.Collection;
import java.util.Locale;

import org.gnucash.generated.GncV2.GncBook.GncGncVendor.VendorTerms;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice.ReadVariant;
import org.gnucash.read.aux.GCshAddress;
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
    int getNofOpenBills();

    /**
     * @return the sum of payments for invoices to this client
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getExpensesGenerated();

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getExpensesGenerated()
     * Formatted acording to the current locale's currency-format
     */
    String getExpensesGeneratedFormatted();

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getExpensesGenerated()
     * Formatted acording to the given locale's currency-format
     */
    String getExpensesGeneratedFormatted(Locale l);

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getOutstandingValue() throws WrongInvoiceTypeException;

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getOutstandingValue()
     * Formatted acording to the current locale's currency-format
     */
    String getOutstandingValueFormatted() throws WrongInvoiceTypeException;

    /**
     *
     * @throws WrongInvoiceTypeException 
     * @see #getOutstandingValue()
     * Formatted acording to the given locale's currency-format
     */
    String getOutstandingValueFormatted(Locale l) throws WrongInvoiceTypeException;


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
     */
    Collection<GnucashVendorJob> getJobs();

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

    Collection<GnucashVendorBill> getPaidBills(ReadVariant readVar) throws WrongInvoiceTypeException;
    
    Collection<GnucashVendorBill> getUnpaidBills(ReadVariant readVar) throws WrongInvoiceTypeException;
    
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
