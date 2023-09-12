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
public interface GnucashCustVendInvoiceEntry extends Comparable<GnucashCustVendInvoiceEntry> {

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
	 * @see GnucashCustVendInvoice#getId()
	 */
	String getCustVendInvoiceID();

	/**
	 * @return the invoice this entry belongs to
	 */
	GnucashCustVendInvoice getCustVendInvoice();

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
	 * Possible value for {@link #getAction()}.
	 */
	String ACTION_JOB = "Auftrag";
	/**
	 * Possible value for {@link #getAction()}.
	 */
	String ACTION_MATERIAL = "Material";
	/**
	 * Possible value for {@link #getAction()}.
	 */
	String ACTION_HOURS = "Stunden";

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
	 * @deprecated use ${@link #getQuantityFormated()}
	 */
	@Deprecated
	String getQuantityFormatted();

	/**
	 * @return the number of items of price ${@link #getInvcPrice()} and type ${@link #getAction()}.
	 */
	String getQuantityFormated();

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
	FixedPointNumber getApplicableTaxPercent();

	/**
	 * @return never null, "0%" if no taxtable is there
	 */
	String getApplicableTaxPercendFormatted();
	
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

	GncV2.GncBook.GncGncEntry getJwsdpPeer();
}
