package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Vendor bill that can be modified if isModifiable() returns true
 */
public interface GnucashWritableVendorBill extends GnucashGenerInvoice {

	void setJob(GnucashVendorJob job);

	GnucashWritableVendorBillEntry getWritableEntryById(String id);

	GnucashWritableVendorBillEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity) throws WrongInvoiceTypeException;

	GnucashWritableVendorBillEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity, final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	GnucashWritableVendorBillEntry createEntry(final FixedPointNumber singleUnitPrice,
                                                   final FixedPointNumber quantity,
                                                   final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;
}
