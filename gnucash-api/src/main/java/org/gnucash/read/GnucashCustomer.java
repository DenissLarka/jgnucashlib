package org.gnucash.read;

import java.util.Locale;
import java.util.Collection;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice.ReadVariant;
import org.gnucash.read.aux.GCshAddress;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

//TODO: model taxes and implement getTaxTable

/**
 * A customer that can issue jobs, get invoices sent to
 * him/her/it and pay them.
 *
 * @see GnucashGenerJob
 * @see GnucashGenerInvoice
 */
public interface GnucashCustomer extends GnucashObject {

    /**
     * The id of the prefered taxtable to use with this customer (may be null).
     * @see {@link #getCustomerTaxTable()}
     */
    String getCustomerTaxTableID();

    /**
     * The prefered taxtable to use with this customer (may be null).
     * @see {@link #getCustomerTaxTableID()}
     */
    GCshTaxTable getCustomerTaxTable();

    /**
     * Date is not checked so invoiced that have entered payments in the future are considered Paid.
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException 
     */
    int getNofOpenInvoices();

    /**
     * @return the sum of payments for invoices to this client
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getIncomeGenerated();

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getIncomeGenerated()
     * Formatted acording to the current locale's currency-format
     */
    String getIncomeGeneratedFormatted();

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getIncomeGenerated()
     * Formatted acording to the given locale's currency-format
     */
    String getIncomeGeneratedFormatted(Locale l);

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getOutstandingValue();

    /**
     * @throws WrongInvoiceTypeException 
     * @see #getOutstandingValue()
     * Formatted acording to the current locale's currency-format
     */
    String getOutstandingValueFormatted();

    /**
     *
     * @throws WrongInvoiceTypeException 
     * @see #getOutstandingValue()
     * Formatted acording to the given locale's currency-format
     */
    String getOutstandingValueFormatted(Locale l);

    
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
     * @return the UNMODIFIABLE collection of jobs that have this customer associated with them.
     */
    Collection<GnucashCustomerJob> getJobs();

    /**
     *
     * @return the user-assigned number of this customer (may contain non-digits)
     */
    String getNumber();

    /**
     *
     * @return unused at the moment
     */
    String getDiscount();

    /**
     * @return user-defined notes about the customer (may be null)
     */
    String getNotes();


    /**
     *
     * @return the name of the customer
     */
    String getName();

    /**
     * @return the address including the name
     */
    GCshAddress getAddress();

    /**
     * @return the shipping-address including the name
     */
    GCshAddress getShippingAddress();
    
    // ----------------------------

    Collection<GnucashCustomerInvoice> getPaidInvoices(ReadVariant readVar) throws WrongInvoiceTypeException;
    
    Collection<GnucashCustomerInvoice> getUnpaidInvoices(ReadVariant readVar) throws WrongInvoiceTypeException;
    
    // ----------------------------
    
    public static int getHighestNumber(GnucashCustomer cust)
    {
      return cust.getGnucashFile().getHighestCustomerNumber();
    }

    public static String getNewNumber(GnucashCustomer cust)
    {
      return cust.getGnucashFile().getNewCustomerNumber();
    }

}
