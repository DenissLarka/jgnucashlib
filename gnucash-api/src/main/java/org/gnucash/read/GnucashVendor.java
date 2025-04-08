package org.gnucash.read;

import java.util.Collection;
import java.util.Locale;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.auxiliary.GCshAddress;
import org.gnucash.read.auxiliary.GCshBillTerms;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * A vendor that can issue jobs and send bills paid by us
 * (and hopefully pay them).
 *
 * @see GnucashVendorJob
 * @see GnucashVendorBill
 */
public interface GnucashVendor extends GnucashObject {

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	GnucashFile getGnucashFile();

	// ------------------------------------------------------------

	/**
	 * @return the unique-id to identify this object with across name- and
	 * hirarchy-changes
	 */
	String getId();

	/**
	 * @return the user-assigned number of this vendor (may contain non-digits)
	 */
	String getNumber();

	/**
	 * @return the name of the vendor
	 */
	String getName();

	/**
	 * @return the address including the name
	 */
	GCshAddress getAddress();

	// ------------------------------------------------------------

	/**
	 * The id of the default tax table to use with this customer (may be null).
	 *
	 * @see {@link #getTaxTable()}
	 */
	String getTaxTableID();

	/**
	 * The default tax table to use with this customer (may be null).
	 *
	 * @see {@link #getTaxTableID()}
	 */
	GCshTaxTable getTaxTable();

	// ------------------------------------------------------------

	/**
	 * The id of the default terms to use with this customer (may be null).
	 *
	 * @see {@link #getTaxTable()}
	 */
	String getTermsID();

	/**
	 * The default terms to use with this customer (may be null).
	 *
	 * @see {@link #getTaxTableID()}
	 */
	GCshBillTerms getTerms();

	// ------------------------------------------------------------

	/**
	 * Date is not checked so invoiced that have entered payments in the future are
	 * considered Paid.
	 *
	 * @return the current number of Unpaid invoices
	 */
	int getNofOpenBills() throws WrongInvoiceTypeException;

	// -------------------------------------

	/**
	 * @return the sum of payments for invoices to this client
	 */
	FixedPointNumber getExpensesGenerated(GnucashGenerInvoice.ReadVariant readVar);

	/**
	 * @return the sum of payments for invoices to this client
	 */
	FixedPointNumber getExpensesGenerated_direct();

	/**
	 * @return the sum of payments for invoices to this client
	 */
	FixedPointNumber getExpensesGenerated_viaAllJobs();

	/**
	 * @see #getExpensesGenerated() Formatted acording to the current locale's
	 * currency-format
	 */
	String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar);

	/**
	 * @see #getExpensesGenerated() Formatted acording to the given locale's
	 * currency-format
	 */
	String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar, Locale l);

	// -------------------------------------

	/**
	 * @return the sum of left to pay Unpaid invoiced
	 */
	FixedPointNumber getOutstandingValue(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException;

	/**
	 * @return the sum of left to pay Unpaid invoiced
	 */
	FixedPointNumber getOutstandingValue_direct() throws WrongInvoiceTypeException;

	/**
	 * @return the sum of left to pay Unpaid invoiced
	 */
	FixedPointNumber getOutstandingValue_viaAllJobs() throws WrongInvoiceTypeException;

	/**
	 * @see #getOutstandingValue() Formatted acording to the current locale's
	 * currency-format
	 */
	String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException;

	/**
	 * @see #getOutstandingValue() Formatted acording to the given locale's
	 * currency-format
	 */
	String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar, Locale l) throws WrongInvoiceTypeException;

	// ------------------------------------------------------------

	/**
	 * @return the UNMODIFIABLE collection of jobs that have this vendor associated
	 * with them.
	 */
	Collection<GnucashVendorJob> getJobs() throws WrongInvoiceTypeException;

	// ------------------------------------------------------------

	Collection<GnucashGenerInvoice> getBills() throws WrongInvoiceTypeException;

	Collection<GnucashVendorBill> getPaidBills_direct() throws WrongInvoiceTypeException;

	Collection<GnucashJobInvoice> getPaidBills_viaAllJobs() throws WrongInvoiceTypeException;

	Collection<GnucashVendorBill> getUnpaidBills_direct() throws WrongInvoiceTypeException;

	Collection<GnucashJobInvoice> getUnpaidBills_viaAllJobs() throws WrongInvoiceTypeException;

	// ------------------------------------------------------------

	public static int getHighestNumber(GnucashVendor vend) {
		return vend.getGnucashFile().getHighestVendorNumber();
	}

	public static String getNewNumber(GnucashVendor vend) {
		return vend.getGnucashFile().getNewVendorNumber();
	}

}
