package org.gnucash.write.spec;

import java.time.LocalDate;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.impl.TaxTableNotFoundException;
import org.gnucash.read.impl.auxiliary.WrongOwnerTypeException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;

/**
 * Vendor bill that can be modified if isModifiable() returns true
 */
public interface GnucashWritableVendorBill extends GnucashWritableGenerInvoice {

	GnucashWritableVendorBillEntry getWritableEntryById(String id);

	// ---------------------------------------------------------------

	/**
	 * Will throw an IllegalStateException if there are bills for this vendor.<br/>
	 *
	 * @param vend the vendor who sent an invoice to us.
	 */
	void setVendor(GnucashVendor vend) throws WrongInvoiceTypeException;

	// ---------------------------------------------------------------

	GnucashWritableVendorBillEntry createEntry(
			GnucashAccount acct,
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException;

	GnucashWritableVendorBillEntry createEntry(
			GnucashAccount acct,
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity,
			final String taxTabName)
			throws WrongInvoiceTypeException, TaxTableNotFoundException;

	GnucashWritableVendorBillEntry createEntry(
			GnucashAccount acct,
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity,
			final GCshTaxTable taxTab)
			throws WrongInvoiceTypeException, TaxTableNotFoundException;

	// ---------------------------------------------------------------

	void post(final GnucashAccount expensesAcct,
			final GnucashAccount payablAcct,
			final LocalDate postDate,
			final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException;

}
