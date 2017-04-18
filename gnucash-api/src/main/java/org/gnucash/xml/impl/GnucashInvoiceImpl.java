/**
 * GnucashInvoiceImpl.java
 * License: GPLv3 or later
 * Created on 13.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 13.05.2005 - initial version
 * ...
 */
package org.gnucash.xml.impl;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.gnucash.generated.GncV2;

import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashCustomer;
import org.gnucash.xml.GnucashFile;
import org.gnucash.xml.GnucashInvoice;
import org.gnucash.xml.GnucashInvoiceEntry;
import org.gnucash.xml.GnucashJob;
import org.gnucash.xml.GnucashTransaction;
import org.gnucash.xml.GnucashTransactionSplit;
import org.gnucash.numbers.FixedPointNumber;

/**
 *
 * created: 13.05.2005 <br/>
 * Implementation of GnucashInvoice that uses JWSDP.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class GnucashInvoiceImpl implements GnucashInvoice {

	/**
	 * {@inheritDoc}
	 */
	public GnucashCustomer getCustomer() {
		return getJob().getCustomer();
	}

	/**
	 * @return getAmmountWithoutTaxes().isMoreThen(getAmmountPayedWithoutTaxes())
	 *
	 * @see GnucashInvoice#isNotFullyPayed()
	 */
	public boolean isNotFullyPayed() {
		return getAmmountWithTaxes().isMoreThen(getAmmountPayedWithTaxes());
	}

	/**
	 * Format of the JWSDP-field openedDate.
	 */
	protected static final DateFormat OPENEDDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");

	/**
	 * The transactions that are paying for this invoice.
	 */
	private final Collection<GnucashTransaction> payingTransactions = new LinkedList<GnucashTransaction>();

	/**
	 * {@inheritDoc}
	 */
	public void addPayingTransaction(final GnucashTransactionSplit trans) {

		//        System.err.println("DEBUG: "
		//                         + getClass().getName()
		//                         + ".addPayingTransaction(split-action="
		//                         + trans.getSplitAction()
		//                         + ")");

		payingTransactions.add(trans.getTransaction());

	}

	/**
	 * {@inheritDoc}
	 */
	public void addTransaction(final GnucashTransaction trans) {
		//

	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<GnucashTransaction> getPayingTransactions() {
		return payingTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAccountIDToTransferMoneyTo() {
		return jwsdpPeer.getInvoicePostacc().getValue();
	}

	/**
	 * @return the transaction that transferes the money from the customer to
	 *         the account for money you are to get and the one you owe the
	 *         taxes.
	 */
	public GnucashTransaction getPostTransaction() {
		if (jwsdpPeer.getInvoicePosttxn() == null) {
			return null; //unposted invoices have no postlot
		}
		return file.getTransactionByID(jwsdpPeer.getInvoicePosttxn().getValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public FixedPointNumber getAmmountPayedWithTaxes() {

		FixedPointNumber takenFromReceivableAccount = new FixedPointNumber();
		for (GnucashTransaction transaction : getPayingTransactions()) {

			for (GnucashTransactionSplit split : transaction.getSplits()) {

				if (split.getAccount().getType().equals(GnucashAccount.ACCOUNTTYPE_RECEIVABLE)
						&&
						!split.getValue().isPositive()
						) {
					takenFromReceivableAccount.subtract(split.getValue());
				}
			}

		}

		//        System.err.println("getAmmountPayedWithoutTaxes="+takenFromReceivableAccount.doubleValue());

		return takenFromReceivableAccount;
	}

	/**
	 * {@inheritDoc}
	 */
	public FixedPointNumber getAmmountUnPayed() {

		// System.err.println("debug: GnucashInvoiceImpl.getAmmountUnPayed(): "
		// + "getAmmountWithoutTaxes()="+getAmmountWithoutTaxes()+" getAmmountPayedWithTaxes()="+getAmmountPayedWithTaxes() );

		return ((FixedPointNumber) getAmmountWithTaxes().clone()).subtract(getAmmountPayedWithTaxes());

	}

	/**
	 * {@inheritDoc}
	 */
	public FixedPointNumber getAmmountWithoutTaxes() {

		FixedPointNumber retval = new FixedPointNumber();

		for (Object element : getEntries()) {
			GnucashInvoiceEntry entry = (GnucashInvoiceEntry) element;
			retval.add(entry.getSumExclTaxes());
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	public TaxedSum[] getTaxes() {

		List<TaxedSum> taxedSums = new LinkedList<TaxedSum>();

		invoiceentries:
		for (Object element : getEntries()) {
			GnucashInvoiceEntry entry = (GnucashInvoiceEntry) element;

			FixedPointNumber taxpercent = entry.getApplicableTaxPercend();

			for (TaxedSum taxedSum2 : taxedSums) {
				TaxedSum taxedSum = taxedSum2;
				if (taxedSum.getTaxpercent().equals(taxpercent)) {
					taxedSum.setTaxsum(
							taxedSum.getTaxsum().add(
									entry.getSumInclTaxes().subtract(entry.getSumExclTaxes())
							)
					);
					continue invoiceentries;
				}
			}

			TaxedSum taxedSum = new TaxedSum(taxpercent, entry.getSumInclTaxes().subtract(entry.getSumExclTaxes()));
			taxedSums.add(taxedSum);

		}

		return taxedSums.toArray(new TaxedSum[taxedSums.size()]);

	}

	/**
	 * {@inheritDoc}
	 */
	public FixedPointNumber getAmmountWithTaxes() {

		FixedPointNumber retval = new FixedPointNumber();

		//TODO: we should sum them without taxes grouped by tax% and
		//      multiply the sums with the tax% to be calculatng
		//      correctly

		for (Object element : getEntries()) {
			GnucashInvoiceEntry entry = (GnucashInvoiceEntry) element;
			retval.add(entry.getSumInclTaxes());
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashJob getJob() {
		return file.getJobByID(getJobID());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJobID() {
		return getJwsdpPeer().getInvoiceOwner().getOwnerId().getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJobType() {
		return getJwsdpPeer().getInvoiceOwner().getOwnerType();
	}

	/**
	 * the JWSDP-object we are facading.
	 */
	protected GncV2.GncBook.GncGncInvoice jwsdpPeer;

	/**
	 * The file we belong to.
	 */
	private final GnucashFile file;

	/**
	 * @param peer the JWSDP-object we are facading.
	 * @see #jwsdpPeer
	 * @param gncFile the file to register under
	 */
	public GnucashInvoiceImpl(
			final GncV2.GncBook.GncGncInvoice peer,
			final GnucashFile gncFile) {
		super();
		jwsdpPeer = peer;
		file = gncFile;

	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return getJwsdpPeer().getInvoiceGuid().getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLotID() {
		if (getJwsdpPeer().getInvoicePostlot() == null) {
			return null; //unposted invoices have no postlot
		}
		return getJwsdpPeer().getInvoicePostlot().getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return getJwsdpPeer().getInvoiceNotes();
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashFile getFile() {
		return file;
	}

	/**
	 * The entries of this invoice.
	 */
	protected Collection<GnucashInvoiceEntry> entries = new HashSet<GnucashInvoiceEntry>();

	/**
	 * {@inheritDoc}
	 */
	public Collection<GnucashInvoiceEntry> getEntries() {
		return entries;
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashInvoiceEntry getEntryById(final String id) {
		for (GnucashInvoiceEntry element : getEntries()) {
			if (element.getId().equals(id)) {
				return element;
			}

		}
		return null;
	}

	/**
	 *
	 * @see GnucashInvoice#getDateOpened()
	 */
	protected static final DateFormat DATEOPENEDFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");

	/**
	 * @see GnucashInvoice#getDateOpened()
	 */
	protected Date dateOpened;

	/**
	 * {@inheritDoc}
	 */
	public Date getDateOpened() {
		if (dateOpened == null) {
			String s = getJwsdpPeer().getInvoiceOpened().getTsDate();
			try {
				//"2001-09-18 00:00:00 +0200"
				dateOpened = DATEOPENEDFORMAT.parse(s);
			}
			catch (Exception e) {
				IllegalStateException ex = new IllegalStateException(
						"unparsable date '"
								+ s
								+ "' in invoice!");
				ex.initCause(e);
				throw ex;
			}

		}
		return dateOpened;
	}

	/**
	 * @see #getDateOpenedFormatet()
	 * @see #getDatePostedFormatet()
	 */
	private DateFormat dateFormat = null;

	/**
	 * @see #getDateOpenedFormatet()
	 * @see #getDatePostedFormatet()
	 * @return the Dateformat to use.
	 */
	protected DateFormat getDateFormat() {
		if (dateFormat == null) {
			dateFormat = DateFormat.getDateInstance();
		}

		return dateFormat;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDateOpenedFormatet() {
		return getDateFormat().format(getDateOpened());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDatePostedFormatet() {
		return getDateFormat().format(getDatePosted());
	}

	/**
	 *
	 * @see GnucashInvoice#getDatePosted()
	 */
	private static final DateFormat DATEPOSTEDFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");

	/**
	 * @see GnucashInvoice#getDatePosted()
	 */
	protected Date datePosted;

	/**
	 * {@inheritDoc}
	 */
	public Date getDatePosted() {
		if (datePosted == null) {
			String s = getJwsdpPeer().getInvoiceOpened().getTsDate();
			try {
				//"2001-09-18 00:00:00 +0200"
				datePosted = DATEPOSTEDFORMAT.parse(s);
			}
			catch (Exception e) {
				IllegalStateException ex = new IllegalStateException(
						"unparsable date '"
								+ s
								+ "' in invoice!");
				ex.initCause(e);
				throw ex;
			}

		}
		return datePosted;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInvoiceNumber() {
		return getJwsdpPeer().getInvoiceBillingId();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addEntry(final GnucashInvoiceEntry entry) {
		if (!entries.contains(entry)) {
			entries.add(entry);
		}
	}

	/**
	 * sorts primarily on the date the transaction happened
	 * and secondarily on the date it was entered.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @param o invoice to compare with
	 * @return -1 0 or 1
	 */
	public int compareTo(final GnucashInvoice o) {

		GnucashInvoice other = o;

		try {
			int compare = other.getDatePosted().compareTo(getDatePosted());
			if (compare != 0) {
				return compare;
			}

			return other.getDateOpened().compareTo(getDateOpened());
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 *
	 * @return The JWSDP-Object we are wrapping
	 */
	public GncV2.GncBook.GncGncInvoice getJwsdpPeer() {
		return jwsdpPeer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[GnucashInvoiceImpl:");
		buffer.append(" id: ");
		buffer.append(getId());
		buffer.append(" invoice-number: ");
		buffer.append(getInvoiceNumber());
		buffer.append(" description: ");
		buffer.append(getDescription());
		buffer.append(" #splits: ");
		buffer.append(entries.size());
		buffer.append(" dateOpened: ");
		try {
			buffer.append(DateFormat.getDateTimeInstance().format(getDateOpened()));
		}
		catch (Exception e) {
			e.printStackTrace();
			buffer.append("ERROR '" + e.getMessage() + "'");

		}
		buffer.append("]");
		return buffer.toString();
	}

	/**
	 * The currencyFormat to use for default-formating.<br/>
	 * Please access only using {@link #getCurrencyFormat()}.
	 * @see #getCurrencyFormat()
	 */
	private NumberFormat currencyFormat = null;

	/**
	 *
	 * @return the currency-format to use if no locale is given.
	 */
	protected NumberFormat getCurrencyFormat() {
		if (currencyFormat == null) {
			currencyFormat = NumberFormat.getCurrencyInstance();
		}

		return currencyFormat;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAmmountUnPayedFormatet() {
		return this.getCurrencyFormat().format(this.getAmmountUnPayed());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAmmountWithTaxesFormatet() {
		return this.getCurrencyFormat().format(this.getAmmountWithTaxes());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAmmountWithoutTaxesFormatet() {
		return this.getCurrencyFormat().format(this.getAmmountWithoutTaxes());
	}
}
