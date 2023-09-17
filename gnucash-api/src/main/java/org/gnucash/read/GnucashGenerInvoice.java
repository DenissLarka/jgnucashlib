package org.gnucash.read;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GnucashOwner;
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
   * @deprecated Use {@link GnucashOwner#TYPE_CUSTOMER} instead
   */
  public static final String TYPE_CUSTOMER = GnucashOwner.TYPE_CUSTOMER;
  /**
   * @deprecated Use {@link GnucashOwner#TYPE_VENDOR} instead
   */
  public static final String TYPE_VENDOR   = GnucashOwner.TYPE_VENDOR;
  /**
   * @deprecated Use {@link GnucashOwner#TYPE_EMPLOYEE} instead
   */
  public static final String TYPE_EMPLOYEE = GnucashOwner.TYPE_EMPLOYEE; // Not used yet, for future releases
  
  // ------------------------------

  public enum ReadVariant {
    DIRECT,
    VIA_JOB
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
    */
	String getOwnerId(ReadVariant readvar);

//    /**
//    *
//    * @return Invoice' owner structure 
//    */
//    InvoiceOwner getOwner();
    
    String getOwnerType();
    
	/**
	 * Note that a job may lead to multiple o no invoices.
	 * (e.g. a monthly payment for a long lasting contract.)
	 * @return the ID of the job this invoice is for.
	 */
	String getJobID();

	/**
	 *
	 * @return unused.
	 */
	String getJobType();

	/**
	 * @return the job this invoice is for
	 */
	GnucashGenerJob getJob();
	
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
	 * This Class represents a sum of the taxes of
	 * multiple invoice-lines for one of the different
	 * tax-percentages that occured.<br/>
	 * e.g. you may have 2 sales-tax-rates of 7% and 16%
	 * and both occur, so you will get 2 instances
	 * of this class. One sum of the 7%-items and one for
	 * the 16%-items.
	 */
	class TaxedSum {
		/**
		 * @param pTaxpercent how much tax it is
		 * @param pTaxsum the sum of Paid taxes
		 */
		public TaxedSum(final FixedPointNumber pTaxpercent,
				final FixedPointNumber pTaxsum) {
			super();
			myTaxpercent = pTaxpercent;
			taxsum = (FixedPointNumber) pTaxsum.clone();
		}

		/**
		 * How much tax it is.
		 * 16%=0.16
		 */
		private FixedPointNumber myTaxpercent;

		/**
		 * The sum of Paid taxes.
		 */
		private FixedPointNumber taxsum;

		/**
		 * @param taxpercent How much tax it is.
		 */
		public TaxedSum(final FixedPointNumber taxpercent) {
			super();
			myTaxpercent = taxpercent;
		}

		/**
		 *
		 * @return How much tax it is.
		 */
		public FixedPointNumber getTaxpercent() {
			return myTaxpercent;
		}

		/**
		 *
		 * @param taxpercent How much tax it is.
		 */
		public void setTaxpercent(final FixedPointNumber taxpercent) {
			if (taxpercent.doubleValue() < 0.0) {
				throw new IllegalArgumentException(
						"negative value '"
								+ taxpercent
								+ "' not allowed for field this.taxpercent");
			}

			myTaxpercent = taxpercent;
		}

		/**
		 *
		 * @return The sum of Paid taxes.
		 */
		public FixedPointNumber getTaxsum() {
			return taxsum;
		}

		/**
		 *
		 * @param pTaxsum The sum of Paid taxes.
		 */
		public void setTaxsum(final FixedPointNumber pTaxsum) {
			taxsum = pTaxsum;
		}
	}
	
	// ----------------------------

	/**
	 *
	 * @return For a customer invoice: How much sales-taxes are to pay.
	 * @throws WrongInvoiceTypeException 
	 * @see TaxedSum
	 */
	TaxedSum[] getInvcTaxes() throws WrongInvoiceTypeException;

    /**
    *
    * @return For a vendor bill: How much sales-taxes are to pay.
    * @throws WrongInvoiceTypeException 
    * @see TaxedSum
    */
	TaxedSum[] getBillTaxes() throws WrongInvoiceTypeException;

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

    /**
     * @return (getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes()))
     * @throws WrongInvoiceTypeException 
     */
    boolean isInvcFullyPaid() throws WrongInvoiceTypeException;

	/**
	 * @return (getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes()))
	 * @throws WrongInvoiceTypeException 
	 */
	boolean isNotInvcFullyPaid() throws WrongInvoiceTypeException;

    /**
     * @return (getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes()))
     * @throws WrongInvoiceTypeException 
     */
    boolean isBillFullyPaid() throws WrongInvoiceTypeException;

    /**
     * @return (getAmountWithoutTaxes().isGreaterThan(getAmountPaidWithoutTaxes()))
     * @throws WrongInvoiceTypeException 
     */
    boolean isNotBillFullyPaid() throws WrongInvoiceTypeException;

}
