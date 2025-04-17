package org.gnucash.write.spec;

import java.time.LocalDate;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.impl.TaxTableNotFoundException;
import org.gnucash.read.impl.auxiliary.WrongOwnerTypeException;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.impl.UnknownInvoiceTypeException;

/**
 * Job invoice that can be modified if isModifiable() returns true
 */
public interface GnucashWritableJobInvoice extends GnucashWritableGenerInvoice {

	GnucashWritableJobInvoiceEntry getWritableEntryById(String id);

	// ---------------------------------------------------------------

	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 *
	 * @param job the customer/vendor job that we link this invoice to.
	 */
	void setGenerJob(GnucashGenerJob job) throws WrongInvoiceTypeException;

	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 *
	 * @param job the customer job that we link this invoice to.
	 */
	void setCustomerJob(GnucashCustomerJob job) throws WrongInvoiceTypeException;

	/**
	 * Will throw an IllegalStateException if there are invoices for this job.<br/>
	 *
	 * @param job the vendor job that we link this invoice to.
	 */
	void setVendorJob(GnucashVendorJob job) throws WrongInvoiceTypeException;

	// ---------------------------------------------------------------

	GnucashWritableJobInvoiceEntry createEntry(
			GnucashAccount acct,
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity) throws WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException;

	GnucashWritableJobInvoiceEntry createEntry(
			GnucashAccount acct,
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity,
			final String taxTabName)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException;

	GnucashWritableJobInvoiceEntry createEntry(
			GnucashAccount acct,
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity,
			final GCshTaxTable taxTab)
			throws WrongInvoiceTypeException, TaxTableNotFoundException, UnknownInvoiceTypeException;

	// ---------------------------------------------------------------

	void post(final GnucashAccount incExpAcct,
			final GnucashAccount recvblPayablAcct,
			final LocalDate postDate,
			final LocalDate dueDate) throws WrongInvoiceTypeException, WrongOwnerTypeException;

}
