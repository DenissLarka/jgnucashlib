/**
 * GnucashWritableInvoice.java
 * Created on 12.06.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * <p>
 * Permission is granted to use, modify, publish and sub-license this code
 * as specified in the contract. If nothing else is specified these rights
 * are given non-exclusively with no restrictions solely to the contractor(s).
 * If no specified otherwise I reserve the right to use, modify, publish and
 * sub-license this code to other parties myself.
 * <p>
 * Otherwise, this code is made available under GPLv3 or later.
 * <p>
 * -----------------------------------------------------------
 * major Changes:
 * 12.06.2005 - initial version
 * ...
 */
package org.gnucash.write;

import java.time.LocalDateTime;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashInvoice;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashTaxTable;
import org.gnucash.read.GnucashTransaction;

/**
 * created: 12.06.2005 <br/>
 * Invoice that can be modified if isModifiable() returns true
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 * @see #isModifiable()
 */
public interface GnucashWritableInvoice extends GnucashInvoice {

	/**
	 * The gnucash-file is tohe top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	GnucashWritableFile getFile();

	/**
	 * @return false if already payments have been made or this invoice sent to a customer!
	 */
	boolean isModifiable();

	void setJob(GnucashJob job);

	void setDatePosted(LocalDateTime d);

	void setDatePosted(String d) throws java.text.ParseException;

	void setDateOpened(LocalDateTime d);

	void setDateOpened(String d) throws java.text.ParseException;


	/**
	 * @return the transaction that adds this invoice's sum to
	 * the expected money.
	 */
	GnucashTransaction getPostTransaction();

	/**
	 * @param id the id to look for
	 * @return the modifiable version of the entry
	 * @see GnucashInvoice#getEntryById(String)
	 */
	GnucashWritableInvoiceEntry getWritableEntryById(String id);

	/**
	 * remove this invoice from the system.
	 *
	 */
	void remove();

	/**
	 * create and add a new entry.<br/>
	 * The entry will have 16% salex-tax and use the accounts of the
	 * SKR03.
	 *
	 */
	GnucashWritableInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity);

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 *
	 */
	GnucashWritableInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity, final FixedPointNumber tax);

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 */
	GnucashWritableInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice,
											final FixedPointNumber quantity,
											final GnucashTaxTable tax);
}
