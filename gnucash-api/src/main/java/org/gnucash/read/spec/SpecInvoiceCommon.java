package org.gnucash.read.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerJob;
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
 * @see GnucashGenerJob
 * @see GnucashVendor
 */
public interface SpecInvoiceCommon {

	FixedPointNumber getAmountUnpaidWithTaxes() throws WrongInvoiceTypeException;

	FixedPointNumber getAmountPaidWithTaxes() throws WrongInvoiceTypeException;

	FixedPointNumber getAmountPaidWithoutTaxes() throws WrongInvoiceTypeException;

	FixedPointNumber getAmountWithTaxes() throws WrongInvoiceTypeException;

	FixedPointNumber getAmountWithoutTaxes() throws WrongInvoiceTypeException;

	// ----------------------------

	String getAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException;

	String getAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException;

	String getAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException;

	String getAmountWithTaxesFormatted() throws WrongInvoiceTypeException;

	String getAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException;

	// ---------------------------------------------------------------

	boolean isFullyPaid() throws WrongInvoiceTypeException;

	boolean isNotFullyPaid() throws WrongInvoiceTypeException;

}
