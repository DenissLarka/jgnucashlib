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

import org.gnucash.numbers.FixedPointNumber;

/**
 * created: 05.05.2005 <br/>
 * Entry-Line in an invoice stating one position
 * with it's name, single-unit-price and count.
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 *
 */
public interface GnucashInvoiceEntry extends Comparable<GnucashInvoiceEntry> {

	/**
	 * @return the unique-id to identify this object with across name- and hirarchy-changes
	 */
	String getId();

	/**
	 *
	 * @return the unique-id of the invoice we belong to to
	 * @see GnucashInvoice#getId()
	 */
	String getInvoiceID();

	/**
	 * @return the invoice this entry belongs to
	 */
	GnucashInvoice getInvoice();

	/**
	 * @return the price of a single of the ${@link #getQuantity()} items of
	 * type ${@link #getAction()}.
	 */
	FixedPointNumber getPrice();

	/**
	 * @return the price of a single of the ${@link #getQuantity()} items of
	 * type ${@link #getAction()}.
	 */
	String getPriceFormatet();

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
	 * @return the number of items of price ${@link #getPrice()} and type ${@link #getAction()}.
	 */
	FixedPointNumber getQuantity();

	/**
	 * @return the number of items of price ${@link #getPrice()} and type ${@link #getAction()}.
	 * @deprecated use ${@link #getQuantityFormated()}
	 */
	@Deprecated
	String getQuantityFormatet();

	/**
	 * @return the number of items of price ${@link #getPrice()} and type ${@link #getAction()}.
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
	boolean isTaxable();

	/**
	 *
	 * @return e.g. "0.16" for "16%"
	 */
	FixedPointNumber getApplicableTaxPercend();

	/**
	 * @return never null, "0%" if no taxtable is there
	 */
	String getApplicableTaxPercendFormatet();

	/**
	 * This is the sum as enteres by the user.
	 * The user can decide to include or exclude taxes.
	 * @return count*single-unit-price excluding or including taxes.
	 * @see #getSumExclTaxes()
	 * @see #getSumInclTaxes()
	 */
	FixedPointNumber getSum();

	/**
	 * This is the sum as enteres by the user.
	 * The user can decide to include or exclude taxes.
	 * @return count*single-unit-price excluding or including taxes.
	 * @see #getSumExclTaxes()
	 * @see #getSumInclTaxes()
	 */
	String getSumFormatet();

	/**
	 * @return count*single-unit-price including taxes.
	 */
	FixedPointNumber getSumInclTaxes();

	/**
	 * @return count*single-unit-price including taxes.
	 */
	String getSumInclTaxesFormatet();

	/**
	 * @return count*single-unit-price excluding taxes.
	 */
	FixedPointNumber getSumExclTaxes();

	/**
	 * @return count*single-unit-price excluding taxes.
	 */
	String getSumExclTaxesFormatet();
}
