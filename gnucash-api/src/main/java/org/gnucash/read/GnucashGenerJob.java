package org.gnucash.read;

import java.util.Collection;
import java.util.Locale;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.WrongInvoiceTypeException;


/**
 * A job needs to be done. Once it or a part of it<br>
 * is done an invoice can be created and later be Paid by the customer<br>
 * of this job.
 * @see GnucashGenerInvoice
 * @see GnucashCustomer
 */
public interface GnucashGenerJob {

    /**
     * @deprecated Use {@link GCshOwner#TYPE_CUSTOMER} instead
     */
    public static final String TYPE_CUSTOMER = GCshOwner.TYPE_CUSTOMER;
    /**
     * @deprecated Use {@link GCshOwner#TYPE_VENDOR} instead
     */
    public static final String TYPE_VENDOR = GCshOwner.TYPE_VENDOR;

    // -----------------------------------------------------------------

    @SuppressWarnings("exports")
    GncV2.GncBook.GncGncJob getJwsdpPeer();

    /**
     * The gnucash-file is the top-level class to contain everything.
     * 
     * @return the file we are associated with
     */
    GnucashFile getFile();

    // -----------------------------------------------------------------

    /**
     * @return the unique-id to identify this object with across name- and
     *         hirarchy-changes
     */
    String getId();

    /**
     * @return true if the job is still active
     */
    boolean isActive();

    /**
     *
     * @return the user-defines number of this job (may contain non-digits)
     */
    String getNumber();

    /**
     *
     * @return the name the user gave to this job.
     */
    String getName();

    // ----------------------------

    /**
     * Not used.
     * 
     * @return CUSTOMETYPE_CUSTOMER
     */
    String getOwnerType();

    /**
     *
     * @return the id of the customer this job is from.
     * @see #getCustomer()
     */
    String getOwnerId();
    
    // ---------------------------------------------------------------

    /**
     * Date is not checked so invoiced that have entered payments in the future are
     * considered Paid.
     * 
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException
     */
    int getNofOpenInvoices() throws WrongInvoiceTypeException;

    /**
     * @return the sum of payments for invoices to this client
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getIncomeGenerated();

    /**
     * @throws WrongInvoiceTypeException
     * @see #getIncomeGenerated() Formatted acording to the current locale's
     *      currency-format
     */
    String getIncomeGeneratedFormatted();

    /**
     * @throws WrongInvoiceTypeException
     * @see #getIncomeGenerated() Formatted acording to the given locale's
     *      currency-format
     */
    String getIncomeGeneratedFormatted(Locale l);

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getOutstandingValue();

    /**
     * @throws WrongInvoiceTypeException
     * @see #getOutstandingValue() Formatted acording to the current locale's
     *      currency-format
     */
    String getOutstandingValueFormatted();

    /**
     *
     * @throws WrongInvoiceTypeException
     * @see #getOutstandingValue() Formatted acording to the given locale's
     *      currency-format
     */
    String getOutstandingValueFormatted(Locale l);

    // ---------------------------------------------------------------

    Collection<GnucashJobInvoice> getPaidInvoices() throws WrongInvoiceTypeException;

    Collection<GnucashJobInvoice> getUnpaidInvoices() throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

    public static int getHighestNumber(GnucashCustomer cust) {
	return cust.getGnucashFile().getHighestJobNumber();
    }

    public static String getNewNumber(GnucashCustomer cust) {
	return cust.getGnucashFile().getNewJobNumber();
    }

}
