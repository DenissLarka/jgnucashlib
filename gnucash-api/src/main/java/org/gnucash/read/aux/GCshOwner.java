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
    
    // -----------------------------------------------------------------
  
    public JIType getJIType();

    public String getType();
    
    public String getId() throws OwnerJITypeUnsetException;
    
    // -----------------------------------------------------------------
    
    @SuppressWarnings("exports")
    GncV2.GncBook.GncGncInvoice.InvoiceOwner getInvcOwner() throws WrongOwnerJITypeException;

    @SuppressWarnings("exports")
    GncV2.GncBook.GncGncJob.JobOwner getJobOwner() throws WrongOwnerJITypeException;
    
}
