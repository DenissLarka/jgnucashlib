package org.gnucash.read.spec;

import java.util.Collection;

import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashVendor;

/**
 * This class represents a bill that is sent from a vendor
 * so you know what to pay him/her.<br>
 * <br>
 * Note: The correct business term is "bill" (as opposed to "invoice"), 
 * as used in the GnuCash documentation, but on a technical level, both 
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
    *
    * @return return vendor this invoice has been sent from 
    */
    String getVendorId(GnucashCustVendInvoice.ReadVariant readVar);

	/**
	 * @return getJob().getCustomer()
	 */
	GnucashVendor getVendor();
	
	// ---------------------------------------------------------------

    GnucashVendorBillEntry getEntryById(String id);

    Collection<GnucashVendorBillEntry> getEntries();

    void addEntry(final GnucashVendorBillEntry entry);

}
