package org.gnucash.read;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.impl.aux.GCshTaxedSumImpl;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * This class represents an invoice that is sent to a customer
 * so (s)he knows what to pay you. <br>
 * <br>
 * Implementations of this interface are comparable and sorts primarily on the date the Invoice was
 * created and secondarily on the date it should be paid.
 *
 * @see GnucashGenerJob
 * @see GnucashCustomer
 */
public interface GnucashGenerInvoice extends Comparable<GnucashGenerInvoice> {
  
  // For the following types cf.:
  // https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gncInvoice.h
  
  /**
   * @deprecated Use {@link GCshOwner#TYPE_CUSTOMER} instead
   */
  public static final String TYPE_CUSTOMER = GCshOwner.TYPE_CUSTOMER;
  /**
   * @deprecated Use {@link GCshOwner#TYPE_VENDOR} instead
   */
  public static final String TYPE_VENDOR   = GCshOwner.TYPE_VENDOR;
  /**
   * @deprecated Use {@link GCshOwner#TYPE_EMPLOYEE} instead
   */
  public static final String TYPE_EMPLOYEE = GCshOwner.TYPE_EMPLOYEE; // Not used yet, for future releases
  /**
   * @deprecated Use {@link GCshOwner#TYPE_JOB} instead
   */
  public static final String TYPE_JOB      = GCshOwner.TYPE_JOB;
  
  // ------------------------------

  public enum ReadVariant {
    DIRECT,  // The entity that directly owns the
             // invoice, be it a customer invoice, 
             // a vendor bill or a job invoice (thus,
             // the customer's / vendor's / job's ID.
    VIA_JOB, // If it's a job invoice, then this option means
             // that we want the ID of the customer / vendor 
             // who is the owner of the job (depending of the
             // job's type).
  }
  
  // -----------------------------------------------------------------

	/**
	 *
	 * @return the unique-id to identify this object with across name- and hirarchy-changes
	 */
	String getId();
	
	String getType();

	/**
	 * @return the user-defined description for this object
	 *         (may contain multiple lines and non-ascii-characters)
	 */
	String getDescription();
	
    // ----------------------------

    @SuppressWarnings("exports")
    GncGncInvoice getJwsdpPeer();

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 * @return the file we are associated with
	 */
	GnucashFile getFile();

    // ----------------------------

	/**
	 * @return the date when this transaction was added to or modified in the books.
	 */
	ZonedDateTime getDateOpened();

	/**
	 * @return the date when this transaction was added to or modified in the books.
	 */
	String getDateOpenedFormatted();

	/**
	 * @return the date when this transaction hapened.
	 */
	ZonedDateTime getDatePosted();

	/**
	 * @return the date when this transaction hapened.
	 */
	String getDatePostedFormatted();

	/**
	 * @return the lot-id that identifies transactions to belong to
	 *         an invoice with that lot-id.
	 */
	String getLotID();

	/**
	 *
	 * @return the user-defines number of this invoice (may contain non-digits)
	 */
	String getNumber();

    /**
    *
    * @return Invoice' owner ID 
     * @throws WrongInvoiceTypeException 
    */
	String getOwnerId(ReadVariant readvar) throws WrongInvoiceTypeException;

//    /**
//    *
//    * @return Invoice' owner structure 
//    */
//    InvoiceOwner getOwner();
    
    String getOwnerType(ReadVariant readvar) throws WrongInvoiceTypeException;
    
	// ---------------------------------------------------------------

	/**
	 * @return what the customer must still pay (incl. taxes)
	 * @throws WrongInvoiceTypeException 
	 */
	FixedPointNumber getInvcAmountUnpaidWithTaxes() throws WrongInvoiceTypeException;

	/**
	 * @return what the customer has already pay (incl. taxes)
	 * @throws WrongInvoiceTypeException 
	 */
	FixedPointNumber getInvcAmountPaidWithTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getInvcAmountPaidWithoutTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getInvcAmountWithTaxes() throws WrongInvoiceTypeException;
    
	/**
	 * @return what the customer needs to pay in total (excl. taxes)
	 * @throws WrongInvoiceTypeException 
	 */
	FixedPointNumber getInvcAmountWithoutTaxes() throws WrongInvoiceTypeException;

    // ----------------------------

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmountWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

    /**
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmountUnpaidWithTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmountPaidWithTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmountPaidWithoutTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmountWithTaxes() throws WrongInvoiceTypeException;
    
    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmountWithoutTaxes() throws WrongInvoiceTypeException;

    // ----------------------------

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmountWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException;
    
    // ---------------------------------------------------------------

    /**
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getJobAmountUnpaidWithTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getJobAmountPaidWithTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getJobAmountPaidWithoutTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getJobAmountWithTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException
     */
    FixedPointNumber getJobAmountWithoutTaxes() throws WrongInvoiceTypeException;

// ----------------------------

    /**
     * Formating uses the default-locale's currency-format.
     * 
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    String getJobAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    String getJobAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    String getJobAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * Formating uses the default-locale's currency-format.
     * 
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException
     */
    String getJobAmountWithTaxesFormatted() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException
     */
    String getJobAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

	/**
	 *
	 * @return For a customer invoice: How much sales-taxes are to pay.
	 * @throws WrongInvoiceTypeException 
	 * @see GCshTaxedSumImpl
	 */
	GCshTaxedSumImpl[] getInvcTaxes() throws WrongInvoiceTypeException;

    /**
    *
    * @return For a vendor bill: How much sales-taxes are to pay.
    * @throws WrongInvoiceTypeException 
    * @see GCshTaxedSumImpl
    */
	GCshTaxedSumImpl[] getBillTaxes() throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

	/**
	 * @return the id of the {@link GnucashAccount} the payment is made to.
	 */
	String getAccountIDToTransferMoneyTo();

    // ---------------------------------------------------------------

	/**
	 * @return the transaction that transferes the money from the customer to
	 *         the account for money you are to get and the one you owe the
	 *         taxes.
	 */
	GnucashTransaction getPostTransaction();

	/**
	 *
	 * @return the transactions the customer Paid this invoice vis.
	 */
	Collection<? extends GnucashTransaction> getPayingTransactions();

	/**
	 *
	 * @param trans a transaction the customer Paid a part of this invoice vis.
	 */
	void addPayingTransaction(GnucashTransactionSplit trans);

	/**
	 *
	 * @param trans a transaction that is the transaction due to handing out this invoice
	 */
	void addTransaction(GnucashTransaction trans);
	
	// ---------------------------------------------------------------

    /**
     * Look for an entry by it's id.
     * @param id the id to look for
     * @return the Entry found or null
     */
    GnucashGenerInvoiceEntry getGenerInvcEntryById(String id);

    /**
     *
     * @return the content of the invoice
     * @see ${@link GnucashGenerInvoiceEntry}
    */
	Collection<GnucashGenerInvoiceEntry> getGenerInvcEntries();

	/**
	 * This method is used internally during the loading of a file.
	 * @param entry the entry to ad during loading.
	 */
	void addGenerInvcEntry(GnucashGenerInvoiceEntry entry);

    // ---------------------------------------------------------------

    boolean isInvcFullyPaid() throws WrongInvoiceTypeException;

    boolean isNotInvcFullyPaid() throws WrongInvoiceTypeException;
    
    // ----------------------------

    boolean isBillFullyPaid() throws WrongInvoiceTypeException;

    boolean isNotBillFullyPaid() throws WrongInvoiceTypeException;

    // ----------------------------

    boolean isJobFullyPaid() throws WrongInvoiceTypeException;

    boolean isNotJobFullyPaid() throws WrongInvoiceTypeException;

}
