package org.gnucash.read.spec;

import java.util.Collection;

import org.gnucash.read.GnucashGenerInvoice;
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
public interface GnucashVendorBill extends GnucashGenerInvoice {

	/**
	 * @return ID of vendor this invoice has been sent from
	 */
	String getVendorId();

	/**
	 * @return Customer this invoice has been sent to.
	 */
	GnucashVendor getVendor() throws WrongInvoiceTypeException;

	// ---------------------------------------------------------------

	GnucashVendorBillEntry getEntryById(String id) throws WrongInvoiceTypeException;

	Collection<GnucashVendorBillEntry> getEntries() throws WrongInvoiceTypeException;

	void addEntry(final GnucashVendorBillEntry entry);

}
