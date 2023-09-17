package org.gnucash.write.spec;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.aux.TaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Customer invoice that can be modified if isModifiable() returns true
 */
public interface GnucashWritableCustomerInvoice extends GnucashGenerInvoice {

	void setJob(GnucashCustomerJob job);

	GnucashWritableCustomerInvoiceEntry getWritableEntryById(String id);

	GnucashWritableCustomerInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity) throws WrongInvoiceTypeException;

	GnucashWritableCustomerInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity, final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	GnucashWritableCustomerInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice,
                                                        final FixedPointNumber quantity,
                                                        final TaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;
}
