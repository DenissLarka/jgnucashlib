package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.aux.TaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Vendor bill that can be modified if isModifiable() returns true
 */
public interface GnucashWritableVendorInvoice extends GnucashGenerInvoice {

	void setJob(GnucashVendorJob job);

	GnucashWritableVendorInvoiceEntry getWritableEntryById(String id);

	GnucashWritableVendorInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity) throws WrongInvoiceTypeException;

	GnucashWritableVendorInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity, final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	GnucashWritableVendorInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice,
                                                      final FixedPointNumber quantity,
                                                      final TaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;
}
