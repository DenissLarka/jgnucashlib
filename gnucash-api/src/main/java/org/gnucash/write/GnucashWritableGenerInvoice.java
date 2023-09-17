package org.gnucash.write;

import java.time.LocalDateTime;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.aux.GnucashTaxTable;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 * Invoice that can be modified if isModifiable() returns true
 *
 * @see #isModifiable()
 */
public interface GnucashWritableGenerInvoice extends GnucashGenerInvoice {

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

	void setJob(GnucashGenerJob job);

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
	 * @see GnucashGenerInvoice#getGenerInvcEntryById(String)
	 */
	GnucashWritableGenerInvoiceEntry getWritableEntryById(String id);

	/**
	 * remove this invoice from the system.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 *
	 */
	void remove() throws WrongInvoiceTypeException, NoTaxTableFoundException;

	/**
	 * create and add a new entry.<br/>
	 * The entry will have 16% salex-tax and use the accounts of the
	 * SKR03.
	 * @throws WrongInvoiceTypeException 
	 *
	 */
	GnucashWritableGenerInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity) throws WrongInvoiceTypeException;

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	GnucashWritableGenerInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity, final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	GnucashWritableGenerInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice,
                                                    final FixedPointNumber quantity,
                                                    final GnucashTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException;
}
