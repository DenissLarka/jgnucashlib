/**
 * GnucashTransaction.java
 * License: GPLv3 or later
 * Created on 05.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 05.05.2005 - initial version
 * ...
 */
package org.gnucash.read;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * created: 26.05.2005 <br>
 * This class represents an invoice that is sent to a customer
 * so (s)he knows what to pay you. <br>
 * <br>
 * Implementations of this interface are comparable and sorts primarily on the date the Invoice was
 * created and secondarily on the date it should be paid.
 *
 * @see GnucashJob
 * @see GnucashCustomer
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public interface GnucashCustVendInvoice extends Comparable<GnucashCustVendInvoice> {
  
  // ::MAGIC
  final static String TYPE_CUSTOMER = "gncCustomer";
  final static String TYPE_VENDOR   = "gncVendor";

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

    GncGncInvoice getPeer();

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
	String getDateOpenedFormatet();

	/**
	 * @return the date when this transaction hapened.
	 */
	ZonedDateTime getDatePosted();

	/**
	 * @return the date when this transaction hapened.
	 */
	String getDatePostedFormatet();

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
	GnucashJob getJob();
	
	// ---------------------------------------------------------------

	/**
	 * @return what the customer must still pay (incl. taxes)
	 * @throws WrongInvoiceTypeException 
	 */
	FixedPointNumber getInvcAmmountUnPayedWithTaxes() throws WrongInvoiceTypeException;

	/**
	 * @return what the customer has already pay (incl. taxes)
	 */
	FixedPointNumber getInvcAmmountPayedWithTaxes();

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getInvcAmmountPayedWithoutTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getInvcAmmountWithTaxes() throws WrongInvoiceTypeException;
    
	/**
	 * @return what the customer needs to pay in total (excl. taxes)
	 * @throws WrongInvoiceTypeException 
	 */
	FixedPointNumber getInvcAmmountWithoutTaxes() throws WrongInvoiceTypeException;

    // ----------------------------

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmmountUnPayedWithTaxesFormatet() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     */
    String getInvcAmmountPayedWithTaxesFormatet();

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmmountPayedWithoutTaxesFormatet() throws WrongInvoiceTypeException;

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmmountWithTaxesFormatet() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getInvcAmmountWithoutTaxesFormatet() throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

    /**
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmmountUnPayedWithTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     */
    FixedPointNumber getBillAmmountPayedWithTaxes();

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmmountPayedWithoutTaxes() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmmountWithTaxes() throws WrongInvoiceTypeException;
    
    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillAmmountWithoutTaxes() throws WrongInvoiceTypeException;

    // ----------------------------

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer must still pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmmountUnPayedWithTaxesFormatet() throws WrongInvoiceTypeException;

    /**
     * @return what the customer has already pay (incl. taxes)
     */
    String getBillAmmountPayedWithTaxesFormatet();

    /**
     * @return what the customer has already pay (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmmountPayedWithoutTaxesFormatet() throws WrongInvoiceTypeException;

    /**
     * Formating uses the default-locale's currency-format.
     * @return what the customer needs to pay in total (incl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmmountWithTaxesFormatet() throws WrongInvoiceTypeException;

    /**
     * @return what the customer needs to pay in total (excl. taxes)
     * @throws WrongInvoiceTypeException 
     */
    String getBillAmmountWithoutTaxesFormatet() throws WrongInvoiceTypeException;

    // ---------------------------------------------------------------

	/**
	 *
	 * (c) 2005 by Wolschon Softwaredesign und Beratung.<br/>
	 * Project: gnucashReader<br/>
	 * GnucashInvoice.java<br/>
	 * created: 07.08.2005 08:58:04 <br/>
	 * This Class represents a sum of the taxes of
	 * multiple invoice-lines for one of the different
	 * tax-percentages that occured.<br/>
	 * e.g. you may have 2 sales-tax-rates of 7% and 16%
	 * and both occur, so you will get 2 instances
	 * of this class. One sum of the 7%-items and one for
	 * the 16%-items.
	 * @author <a href="Marcus@Wolschon.biz">Marcus Wolschon</a>
	 */
	class TaxedSum {
		/**
		 * @param pTaxpercent how much tax it is
		 * @param pTaxsum the sum of payed taxes
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
		 * The sum of payed taxes.
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
		 * @return The sum of payed taxes.
		 */
		public FixedPointNumber getTaxsum() {
			return taxsum;
		}

		/**
		 *
		 * @param pTaxsum The sum of payed taxes.
		 */
		public void setTaxsum(final FixedPointNumber pTaxsum) {
			taxsum = pTaxsum;
		}
	}

	/**
	 *
	 * @return how much sales-taxes are to pay.
	 * @throws WrongInvoiceTypeException 
	 * @see TaxedSum
	 */
	TaxedSum[] getInvcTaxes() throws WrongInvoiceTypeException;

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
	 * @return the transactions the customer payed this invoice vis.
	 */
	Collection<? extends GnucashTransaction> getPayingTransactions();

	/**
	 *
	 * @param trans a transaction the customer payed a part of this invoice vis.
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
    GnucashCustVendInvoiceEntry getCustVendInvcEntryById(String id);

    /**
     *
     * @return the content of the invoice
     * @see ${@link GnucashCustVendInvoiceEntry}
    */
	Collection<GnucashCustVendInvoiceEntry> getCustVendInvcEntries();

	/**
	 * This method is used internally during the loading of a file.
	 * @param entry the entry to ad during loading.
	 */
	void addCustVendInvcEntry(GnucashCustVendInvoiceEntry entry);

    // ---------------------------------------------------------------

	/**
	 * @return (getAmmountWithoutTaxes().isMoreThen(getAmmountPayedWithoutTaxes()))
	 * @throws WrongInvoiceTypeException 
	 */
	boolean isNotFullyPayed() throws WrongInvoiceTypeException;

}
