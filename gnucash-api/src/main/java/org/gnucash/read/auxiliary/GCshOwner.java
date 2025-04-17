package org.gnucash.read.auxiliary;

import org.gnucash.generated.GncV2;

public interface GCshOwner {

	enum JIType { // ::TODO in search of a better name...
		INVOICE,
		JOB,
		UNSET
	}

	// -----------------------------------------------------------------

	// ::MAGIC
	String TYPE_CUSTOMER = "gncCustomer";
	String TYPE_VENDOR = "gncVendor";
	String TYPE_EMPLOYEE = "gncEmployee"; // Not used yet, for future releases
	String TYPE_JOB = "gncJob";

	String TYPE_UNSET = "UNSET";

	// -------------------------------------------
	// Caution: Do *not* change the following enum -- neither the number of entries
	// nor their order. Reason: they implicitly define indices (by calling the orginal()
	// method), and these, in turn, are directly mapped to numeric values in the GnuCash file.
	//
	// Cf. https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gncOwner.h
	// -------------------------------------------
	enum Type {
		NONE,
		UNDEFINED,
		CUSTOMER,
		JOB,
		VENDOR,
		EMPLOYEE
	}

	// -----------------------------------------------------------------

	JIType getJIType();

	String getInvcType() throws WrongOwnerJITypeException;

	String getId() throws OwnerJITypeUnsetException;

	// -----------------------------------------------------------------

	@SuppressWarnings("exports")
	GncV2.GncBook.GncGncInvoice.InvoiceOwner getInvcOwner() throws WrongOwnerJITypeException;

	@SuppressWarnings("exports")
	GncV2.GncBook.GncGncJob.JobOwner getJobOwner() throws WrongOwnerJITypeException;

}
