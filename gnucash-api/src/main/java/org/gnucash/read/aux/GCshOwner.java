package org.gnucash.read.aux;

import org.gnucash.generated.GncV2;

public interface GCshOwner {

    public enum JIType { // ::TODO in search of a better name...
	      INVOICE,
	      JOB,
	      UNSET
    }

    // -----------------------------------------------------------------
    
    // ::MAGIC
    public static final String TYPE_CUSTOMER = "gncCustomer";
    public static final String TYPE_VENDOR   = "gncVendor";
    public static final String TYPE_EMPLOYEE = "gncEmployee"; // Not used yet, for future releases
    public static final String TYPE_JOB      = "gncJob";
    
    public static final String TYPE_UNSET    = "UNSET";
    
    // -------------------------------------------
    // Caution: Do *not* change the following enum -- neither the number of entries
    // nor their order. Reason: they implicitly define indices (by calling the orginal()
    // method), and these, in turn, are directly mapped to numeric values in the GnuCash file.
    // 
    // Cf. https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gncOwner.h
    // -------------------------------------------
    public enum Type
    {
	NONE,
	UNDEFINED,
	CUSTOMER,
	JOB,
	VENDOR,
	EMPLOYEE
    }
    
    // -----------------------------------------------------------------
  
    public JIType getJIType();

    public String getInvcType() throws WrongOwnerJITypeException;
    
    public String getId() throws OwnerJITypeUnsetException;
    
    // -----------------------------------------------------------------
    
    @SuppressWarnings("exports")
    GncV2.GncBook.GncGncInvoice.InvoiceOwner getInvcOwner() throws WrongOwnerJITypeException;

    @SuppressWarnings("exports")
    GncV2.GncBook.GncGncJob.JobOwner getJobOwner() throws WrongOwnerJITypeException;
    
}
