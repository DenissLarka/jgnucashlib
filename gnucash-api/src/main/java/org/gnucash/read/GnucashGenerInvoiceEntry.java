/**
 * GnucashTransactionSplit.java
 * License: GPLv3 or later
 * Created on 05.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 05.05.2005 - initial version
 * ...
 */
package org.gnucash.read;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * created: 05.05.2005 <br/>
 * Entry-Line in an invoice stating one position
 * with it's name, single-unit-price and count.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 *
 */
public interface GnucashGenerInvoiceEntry extends Comparable<GnucashGenerInvoiceEntry> {

  // For the following enumerations cf.:
  // https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gncEntry.h
  
  // ::TODO: Locale-specific, make generic
  // ::MAGIC
  public static final String ACTION_JOB      = "Auftrag";
  public static final String ACTION_MATERIAL = "Material";
  public static final String ACTION_HOURS    = "Stunden";
  
  // Not yet, for future releases:
//  public static final String ENTRY_DATE          = "date";
//  public static final String ENTRY_DATE_ENTERED  = "date-entered";
//  public static final String ENTRY_DESC          = "desc";
//  public static final String ENTRY_ACTION        = "action";
//  public static final String ENTRY_NOTES         = "notes";
//  public static final String ENTRY_QTY           = "qty";
//
//  public static final String ENTRY_IPRICE        = "iprice";
//  public static final String ENTRY_IACCT         = "invoice-account";
//  public static final String ENTRY_BACCT         = "bill-account";
//  public static final String ENTRY_BPRICE        = "bprice";
//  public static final String ENTRY_BILLABLE      = "billable?";
//  public static final String ENTRY_BILLTO        = "bill-to";
//
//  public static final String ENTRY_ORDER         = "order";
//  public static final String ENTRY_INVOICE       = "invoice";
//  public static final String ENTRY_BILL          = "bill";
//
//  public static final String ENTRY_INV_DISC_TYPE = "discount-type";
//  public static final String ENTRY_INV_DISC_HOW  = "discount-method";
//
//  public static final String ENTRY_INV_TAXABLE   = "invoice-taxable";
//  public static final String ENTRY_BILL_TAXABLE  = "bill-taxable";
//  public static final String ENTRY_INV_TAX_INC   = "invoice-tax-included";
//  public static final String ENTRY_BILL_TAX_INC  = "bill-tax-included";
//  public static final String ENTRY_INV_DISCOUNT  = "invoice-discount";
//  public static final String ENTRY_BILL_PAY_TYPE = "bill-payment-type";
  
  // -----------------------------------------------------------------

	/**
	 * @return the unique-id to identify this object with across name- and hirarchy-changes
	 */
	String getId();
	
    /**
     * @return the type of the customer/vendor invoice entry, 
     * i.e. the owner type of the entry's invoice
     */
	String getType();

	/**
	 *
	 * @return the unique-id of the invoice we belong to to
	 * @see GnucashGenerInvoice#getId()
	 */
	String getGenerInvoiceID();

	/**
	 * @return the invoice this entry belongs to
	 */
	GnucashGenerInvoice getGenerInvoice();

    // ---------------------------------------------------------------

	/**
	 * @return For a customer invoice, return the price of one single of the 
	 * ${@link #getQuantity()} items of type ${@link #getAction()}.
	 */
	FixedPointNumber getInvcPrice() throws WrongInvoiceTypeException;

    /**
     * @return For a vendor bill, return the price of one single of the 
     * ${@link #getQuantity()} items of type ${@link #getAction()}.
     */
    FixedPointNumber getBillPrice() throws WrongInvoiceTypeException;
    
    // ----------------------------

    /**
     * @return As ${@link #getInvcPrice()}, but formatted.
     */
    String getInvcPriceFormatted()throws WrongInvoiceTypeException;

    /**
     * @return As ${@link #getBillPrice()}, but formatted.
     */
    String getBillPriceFormatted()throws WrongInvoiceTypeException;
    
    // ---------------------------------------------------------------

	/**
	 * The returned text is saved locale-specific. E.g. "Stunden" instead of "hours" for Germany.
	 * @return HOURS or ITEMS, ....
	 */
	String getAction();

	/**
	 * @return the number of items of price ${@link #getInvcPrice()} and type ${@link #getAction()}.
	 */
	FixedPointNumber getQuantity();

	/**
	 * @return the number of items of price ${@link #getInvcPrice()} and type ${@link #getAction()}.
	 */
	String getQuantityFormatted();

	/**
	 * @return the user-defined description for this object (may contain multiple lines and non-ascii-characters)
	 */
	String getDescription();

	/**
	 *
	 * @return true if any sales-tax applies at all to this item.
	 */
	boolean isInvcTaxable();

	/**
	 *
	 * @return e.g. "0.16" for "16%"
	 */
	FixedPointNumber getInvcApplicableTaxPercent();

    /**
    *
    * @return e.g. "0.16" for "16%"
    */
     FixedPointNumber getBillApplicableTaxPercent();

    /**
     * @return never null, "0%" if no taxtable is there
     */
    String getInvcApplicableTaxPercentFormatted();
     
    /**
     * @return never null, "0%" if no taxtable is there
     */
    String getBillApplicableTaxPercentFormatted();
     
	// ---------------------------------------------------------------

	/**
	 * This is the customer invoice sum as entered by the user.
	 * The user can decide to include or exclude taxes.
	 * @return count*single-unit-price excluding or including taxes.
	 * @throws WrongInvoiceTypeException 
	 * @see #getInvcSumExclTaxes()
	 * @see #getInvcSumInclTaxes()
	 */
	FixedPointNumber getInvcSum() throws WrongInvoiceTypeException;

	/**
	 * @return count*single-unit-price including taxes.
	 * @throws WrongInvoiceTypeException 
	 */
	FixedPointNumber getInvcSumInclTaxes() throws WrongInvoiceTypeException;

	/**
	 * @return count*single-unit-price excluding taxes.
	 * @throws WrongInvoiceTypeException 
	 */
	FixedPointNumber getInvcSumExclTaxes() throws WrongInvoiceTypeException;
	
    // ----------------------------

    /**
     * As ${@link #getInvcSum()}. but formatted.
     * @return count*single-unit-price excluding or including taxes.
     * @throws WrongInvoiceTypeException 
     * @see #getInvcSumExclTaxes()
     * @see #getInvcSumInclTaxes()
     */
    String getInvcSumFormatted() throws WrongInvoiceTypeException;

    /**
     * As ${@link #getInvcSumInclTaxes()}. but formatted.
     * @return count*single-unit-price including taxes.
     * @throws WrongInvoiceTypeException 
     */
    String getInvcSumInclTaxesFormatted() throws WrongInvoiceTypeException;

	/**
     * As ${@link #getInvcSumExclTaxes()}. but formatted.
	 * @return count*single-unit-price excluding taxes.
	 * @throws WrongInvoiceTypeException 
	 */
	String getInvcSumExclTaxesFormatted() throws WrongInvoiceTypeException;
	
    // ----------------------------

    /**
     * This is the vendor bill sum as entered by the user.
     * The user can decide to include or exclude taxes.
     * @return count*single-unit-price excluding or including taxes.
     * @throws WrongInvoiceTypeException 
     * @see #getInvcSumExclTaxes()
     * @see #getInvcSumInclTaxes()
     */
    FixedPointNumber getBillSum() throws WrongInvoiceTypeException;

    /**
     * @return count*single-unit-price including taxes.
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillSumInclTaxes() throws WrongInvoiceTypeException;

    /**
     * @return count*single-unit-price excluding taxes.
     * @throws WrongInvoiceTypeException 
     */
    FixedPointNumber getBillSumExclTaxes() throws WrongInvoiceTypeException;
    
    // ---------------------------------------------------------------

	@SuppressWarnings("exports")
    GncV2.GncBook.GncGncEntry getJwsdpPeer();
}
