/**
 * GnucashInvoiceWritingImpl.java
 * Created on 16.05.2005
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
 * 16.05.2005 - initial version
 * ...
 */
package org.gnucash.write.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.gnucash.generated.GncAccount;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.generated.OwnerId;
import org.gnucash.generated.Slot;
import org.gnucash.generated.SlotValue;
import org.gnucash.generated.SlotsType;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashInvoice;
import org.gnucash.read.GnucashInvoiceEntry;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashTaxTable;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashInvoiceImpl;
import org.gnucash.read.impl.GnucashTransactionImpl;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableInvoice;
import org.gnucash.write.GnucashWritableInvoiceEntry;
import org.gnucash.write.GnucashWritableTransaction;

import jakarta.xml.bind.JAXBElement;

/**
 * @author Marcus@Wolschon.biz
 * created: 16.05.2005
 * TODO write a comment what this type does here
 */
public class GnucashInvoiceWritingImpl extends GnucashInvoiceImpl implements GnucashWritableInvoice {

	/**
	 * Create an editable invoice facading an existing JWSDP-peer.
	 *
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @param file      the file to register under
	 * @see GnucashInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice, GnucashFile)
	 */
	public GnucashInvoiceWritingImpl(final GncV2.GncBook.GncGncInvoice jwsdpPeer, final GnucashFile file) {
		super(jwsdpPeer, file);
	}

	/**
	 * The gnucash-file is the top-level class to contain everything.
	 *
	 * @return the file we are associated with
	 */
	@Override
	public GnucashWritableFile getFile() {
		return (GnucashWritableFile) super.getFile();
	}

	/**
	 * @param file the file we are associated with.
	 */
	protected GnucashInvoiceWritingImpl(final GnucashFileWritingImpl file,
			final String internalID,
			final String invoiceNumber,
			final GnucashJob job,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final Date dueDate) {
		super(createInvoice(file, internalID, invoiceNumber, job, accountToTransferMoneyTo, dueDate), file);
	}

	/**
	 * create and add a new entry.
	 */
	public GnucashWritableInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity) {
		GnucashInvoiceEntryWritingImpl entry = new GnucashInvoiceEntryWritingImpl(this, quantity, singleUnitPrice);
		return entry;
	}

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 */
	public GnucashWritableInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity,
			final GnucashTaxTable tax) {

		GnucashInvoiceEntryWritingImpl entry = new GnucashInvoiceEntryWritingImpl(this, quantity, singleUnitPrice);
		if (tax == null
				||
				tax.getEntries().isEmpty()
				||
				(tax.getEntries().iterator().next())
						.getAmount().equals(new FixedPointNumber())) {
			// no taxes
			entry.setTaxable(false);
		} else {
			entry.setTaxTable(tax);
		}
		return entry;
	}

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 */
	public GnucashWritableInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity,
			final FixedPointNumber tax) {

		GnucashInvoiceEntryWritingImpl entry = new GnucashInvoiceEntryWritingImpl(this, quantity, singleUnitPrice);
		if (tax.equals(new FixedPointNumber())) {
			// no taxes
			entry.setTaxable(false);
		} else {
			//TODO: find taxtable to use for given percentage
		}
		return entry;
	}

	/**
	 * Use ${@link GnucashWritableFile#createWritableInvoice(String, GnucashJob, GnucashAccount, java.util.Date)}
	 * instead of calling this method!
	 *
	 * @param accountToTransferMoneyTo e.g. "Forderungen aus Lieferungen und Leistungen "
	 */
	protected static GncV2.GncBook.GncGncInvoice createInvoice(final GnucashFileWritingImpl file,
			final String internalID,
			final String invoiceNumber,
			final GnucashJob job,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final Date dueDate) {

		ObjectFactory factory = file.getObjectFactory();
		String invoiceguid = (internalID == null ? file.createGUID() : internalID);

		GncV2.GncBook.GncGncInvoice invoice = file.createGncGncInvoiceType();
		invoice.setInvoiceActive(1); //TODO: is this correct?
		invoice.setInvoiceBillingId(invoiceNumber);
		{
			GncV2.GncBook.GncGncInvoice.InvoiceCurrency currency = factory.createGncV2GncBookGncGncInvoiceInvoiceCurrency();
			currency.setCmdtyId(file.getDefaultCurrencyID());
			currency.setCmdtySpace("ISO4217");
			invoice.setInvoiceCurrency(currency);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoiceGuid invoiceref = factory.createGncV2GncBookGncGncInvoiceInvoiceGuid();
			invoiceref.setType("guid");
			invoiceref.setValue(invoiceguid);
			invoice.setInvoiceGuid(invoiceref);
		}
		invoice.setInvoiceId(invoiceNumber);
		{
			GncV2.GncBook.GncGncInvoice.InvoiceOpened opened = factory.createGncV2GncBookGncGncInvoiceInvoiceOpened();
			opened.setTsDate(OPENEDDATEFORMAT.format(new Date()));
			invoice.setInvoiceOpened(opened);
		}
		{

			GncV2.GncBook.GncGncInvoice.InvoiceOwner jobref = factory.createGncV2GncBookGncGncInvoiceInvoiceOwner();
			jobref.setOwnerType("gncJob");
			jobref.setVersion("2.0.0");
			{
				OwnerId ownerIdRef = factory.createOwnerId();
				ownerIdRef.setType("guid");
				ownerIdRef.setValue(job.getId());
				jobref.setOwnerId(ownerIdRef);
			}
			invoice.setInvoiceOwner(jobref);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePostacc postacc = factory.createGncV2GncBookGncGncInvoiceInvoicePostacc();
			postacc.setType("guid");
			postacc.setValue(accountToTransferMoneyTo.getId());
			invoice.setInvoicePostacc(postacc);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePosted posted = factory.createGncV2GncBookGncGncInvoiceInvoicePosted();
			posted.setTsDate(OPENEDDATEFORMAT.format(new Date()));
			invoice.setInvoicePosted(posted);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePostlot lotref = factory.createGncV2GncBookGncGncInvoiceInvoicePostlot();
			lotref.setType("guid");

			GncAccount.ActLots.GncLot newlot = createlot(file, factory, invoiceguid, accountToTransferMoneyTo, job);

			lotref.setValue(newlot.getLotId().getValue());
			invoice.setInvoicePostlot(lotref);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePosttxn transactionref = factory.createGncV2GncBookGncGncInvoiceInvoicePosttxn();
			transactionref.setType("guid");
			transactionref.setValue(createPostTransaction(file, factory, invoiceguid, dueDate).getId());

			invoice.setInvoicePosttxn(transactionref);
		}
		invoice.setVersion("2.0.0");

		file.getRootElement().getGncBook().getBookElements().add(invoice);
		file.setModified(true);
		return invoice;
	}

	/**
	 * @see #GnucashInvoiceWritingImpl(GnucashFileWritingImpl, String, String, GnucashJob, GnucashAccountImpl, Date)
	 */
	private static GnucashTransactionImpl createPostTransaction(final GnucashFileWritingImpl file,
			final ObjectFactory factory,
			final String invoiceID,
			final Date dueDate) {
		GnucashTransactionImpl postTransaction = new GnucashTransactionWritingImpl(file, file.createGUID());

		SlotsType slots = postTransaction.getJwsdpPeer().getTrnSlots();

		if (slots == null) {
			slots = factory.createSlotsType();
			postTransaction.getJwsdpPeer().setTrnSlots(slots);
		}

		//      add trans-txn-type -slot

		{
			Slot slot = factory.createSlot();
			SlotValue value = factory.createSlotValue();
			slot.setSlotKey("trans-txn-type");
			value.setType("string");
			value.getContent().add("I"); // I like invoice, P like payment

			slot.setSlotValue(value);
			slots.getSlot().add(slot);
		}

		//      add trans-date-due -slot

		{
			Slot slot = factory.createSlot();
			SlotValue value = factory.createSlotValue();
			slot.setSlotKey("trans-date-due");
			value.setType("timespec");
			JAXBElement<String> tsDate = factory.createTsDate(OPENEDDATEFORMAT.format(dueDate));
			value.getContent().add(tsDate);
			slot.setSlotValue(value);
			slots.getSlot().add(slot);
		}

		//add invoice-slot

		{
			Slot slot = factory.createSlot();
			SlotValue value = factory.createSlotValue();
			slot.setSlotKey("gncInvoice");
			value.setType("frame");
			{
				Slot subslot = factory.createSlot();
				SlotValue subvalue = factory.createSlotValue();

				subslot.setSlotKey("invoice-guid");
				subvalue.setType("guid");
				subvalue.getContent().add(invoiceID);
				subslot.setSlotValue(subvalue);

				value.getContent().add(subslot);
			}

			slot.setSlotValue(value);
			slots.getSlot().add(slot);
		}

		return postTransaction;
	}

	private static GncAccount.ActLots.GncLot createlot(final GnucashFileWritingImpl file,
			final ObjectFactory factory,
			final String invoiceID,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final GnucashJob job) {

		GncAccount.ActLots lots = accountToTransferMoneyTo.getJwsdpPeer().getActLots();
		if (lots == null) {
			lots = factory.createGncAccountActLots();
			accountToTransferMoneyTo.getJwsdpPeer().setActLots(lots);
		}

		GncAccount.ActLots.GncLot newlot = factory.createGncAccountActLotsGncLot();
		{
			GncAccount.ActLots.GncLot.LotId id = factory.createGncAccountActLotsGncLotLotId();
			id.setType("guid");
			id.setValue(file.createGUID());
			newlot.setLotId(id);
		}
		newlot.setVersion("2.0.0");

		{
			SlotsType slots = factory.createSlotsType();
			newlot.setLotSlots(slots);
		}

		//add owner-slot (job)
		{
			Slot slot = factory.createSlot();
			SlotValue value = factory.createSlotValue();
			slot.setSlotKey("gncOwner");
			value.setType("frame");
			{
				Slot subslot = factory.createSlot();
				SlotValue subvalue = factory.createSlotValue();

				subslot.setSlotKey("owner-type");
				subvalue.setType("integer");
				subvalue.getContent().add("3");
				subslot.setSlotValue(subvalue);

				value.getContent().add(subslot);
			}

			{
				Slot subslot = factory.createSlot();
				SlotValue subvalue = factory.createSlotValue();

				subslot.setSlotKey("owner-guid");
				subvalue.setType("guid");
				subvalue.getContent().add(job.getId());
				subslot.setSlotValue(subvalue);

				value.getContent().add(subslot);
			}

			slot.setSlotValue(value);
			newlot.getLotSlots().getSlot().add(slot);
		}
		//add invoice-slot

		{
			Slot slot = factory.createSlot();
			SlotValue value = factory.createSlotValue();
			slot.setSlotKey("gncInvoice");
			value.setType("frame");
			{
				Slot subslot = factory.createSlot();
				SlotValue subvalue = factory.createSlotValue();

				subslot.setSlotKey("invoice-guid");
				subvalue.setType("guid");
				subvalue.getContent().add(invoiceID);
				subslot.setSlotValue(subvalue);

				value.getContent().add(subslot);
			}

			slot.setSlotValue(value);
			newlot.getLotSlots().getSlot().add(slot);
		}

		lots.getGncLot().add(newlot);

		return newlot;
	}

	/**
	 * @see #addEntry(GnucashInvoiceEntryImpl)
	 */
	protected void removeEntry(final GnucashInvoiceEntryWritingImpl impl) {

		if (!isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}

		this.subtractEntry(impl);
		entries.remove(impl);
	}

	/**
	 * Called by
	 * ${@link GnucashInvoiceEntryWritingImpl#createInvoiceEntry(GnucashInvoiceWritingImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
	 *
	 * @param entry the entry to add to our internal list of invoice-entries
	 */
	protected void addEntry(final GnucashInvoiceEntryImpl entry) {

		System.err.println("GnucashInvoiceWritingImpl.addEntry " + entry.toString());

		if (!isModifiable()) {
			throw new IllegalArgumentException("This invoice has payments and is"
					+ " thus not modifiable");
		}

		super.addEntry(entry);

		//==============================================================
		// update or add split in PostTransaction
		// that transferes the money from the tax-account

		boolean isTaxable = entry.isTaxable();
		FixedPointNumber sumExclTaxes = entry.getSumExclTaxes();
		FixedPointNumber sumInclTaxes = entry.getSumInclTaxes();
		String accountToTransferMoneyFrom = getAccountIDToTransferMoneyFrom(entry);

		GnucashTaxTable taxtable = null;

		if (entry.isTaxable()) {
			taxtable = entry.getTaxTable();
			if (taxtable == null) {
				throw new IllegalArgumentException("The given entry has no tax-table (it's taxtable-id is '"
						+ entry.getJwsdpPeer().getEntryITaxtable().getValue()
						+ "')");
			}
		}

		updateEntry(taxtable, isTaxable, sumExclTaxes, sumInclTaxes, accountToTransferMoneyFrom);
		getFile().setModified(true);
	}

	/**
	 * @return the AccountID of the Account to transfer the money from
	 */
	private String getAccountIDToTransferMoneyFrom(final GnucashInvoiceEntryImpl entry) {
		return entry.getJwsdpPeer().getEntryIAcct().getValue();
	}

	protected void subtractEntry(final GnucashInvoiceEntryImpl entry) {
		System.err.println("GnucashInvoiceWritingImpl.subtractEntry " + entry.toString());
		//==============================================================
		// update or add split in PostTransaction
		// that transferes the money from the tax-account

		boolean isTaxable = entry.isTaxable();
		FixedPointNumber sumExclTaxes = entry.getSumExclTaxes().negate();
		FixedPointNumber sumInclTaxes = entry.getSumInclTaxes().negate();
		String accountToTransferMoneyFrom = entry.getJwsdpPeer().getEntryIAcct().getValue();

		GnucashTaxTable taxtable = null;

		if (entry.isTaxable()) {
			taxtable = entry.getTaxTable();
			if (taxtable == null) {
				throw new IllegalArgumentException("The given entry has no tax-table (it's taxtable-id is '"
						+ entry.getJwsdpPeer().getEntryITaxtable().getValue() + "')");
			}
		}

		updateEntry(taxtable, isTaxable, sumExclTaxes, sumInclTaxes, accountToTransferMoneyFrom);
		getFile().setModified(true);
	}

	/**
	 */
	private void updateEntry(final GnucashTaxTable taxtable,
			final boolean isTaxable,
			final FixedPointNumber sumExclTaxes,
			final FixedPointNumber sumInclTaxes,
			final String accountToTransferMoneyFrom) {
		System.err.println("GnucashInvoiceWritingImpl.updateEntry "
				+ "isTaxable=" + isTaxable + " "
				+ "accountToTransferMoneyFrom=" + accountToTransferMoneyFrom + " ");

		GnucashTransactionWritingImpl postTransaction = (GnucashTransactionWritingImpl) getPostTransaction();
		if (postTransaction == null) {
			return; //invoice may not be posted
		}
		if (isTaxable) {

			// get the first account of the taxTable
			GnucashTaxTable.TaxTableEntry taxTableEntry = taxtable.getEntries().iterator().next();
			GnucashAccount accountToTransferTaxTo = taxTableEntry.getAccount();
			FixedPointNumber entryTaxAmount = ((FixedPointNumber) sumInclTaxes.clone()).subtract(sumExclTaxes);

			System.err.println("GnucashInvoiceWritingImpl.updateEntry "
					+ "isTaxable=" + isTaxable + " "
					+ "accountToTransferMoneyFrom=" + accountToTransferMoneyFrom + " "
					+ "accountToTransferTaxTo=" + accountToTransferTaxTo.getQualifiedName() + " "
					+ "entryTaxAmount=" + entryTaxAmount + " "
					+ "#splits=" + postTransaction.getSplits().size());

			//failed for subtractEntry assert entryTaxAmount.isPositive() || entryTaxAmount.equals(new FixedPointNumber());

			boolean postTransactionTaxUpdated = false;
			for (Object element : postTransaction.getSplits()) {
				GnucashTransactionSplitWritingImpl split = (GnucashTransactionSplitWritingImpl) element;
				if (split.getAccountID().equals(accountToTransferTaxTo.getId())) {

					//quantity gets updated automagically                 split.setQuantity(split.getQuantity().subtract(entryTaxAmount));
					split.setValue(split.getValue().subtract(entryTaxAmount));

					//               failed for subtractEntry                  assert !split.getValue().isPositive();
					//               failed for subtractEntry                  assert !split.getQuantity().isPositive();

					System.err.println("GnucashInvoiceWritingImpl.updateEntry "
							+ "updated tax-split=" + split.getId() + " "
							+ " of account " + split.getAccount().getQualifiedName()
							+ " to value " + split.getValue());

					postTransactionTaxUpdated = true;
					break;
				}
				System.err.println("GnucashInvoiceWritingImpl.updateEntry "
						+ "ignoring non-tax-split=" + split.getId() + " "
						+ " of value " + split.getValue()
						+ " and account " + split.getAccount().getQualifiedName());
			}
			if (!postTransactionTaxUpdated) {
				GnucashTransactionSplitWritingImpl split =
						(GnucashTransactionSplitWritingImpl) postTransaction.createWritingSplit(accountToTransferTaxTo);
				split.setQuantity(((FixedPointNumber) entryTaxAmount.clone()).negate());
				split.setValue(((FixedPointNumber) entryTaxAmount.clone()).negate());

				//assert !split.getValue().isPositive();
				//assert !split.getQuantity().isPositive();

				split.setSplitAction("Rechnung");

				System.err.println("GnucashInvoiceWritingImpl.updateEntry "
						+ "created new tax-split=" + split.getId() + " "
						+ " of value " + split.getValue()
						+ " and account " + split.getAccount().getQualifiedName());
			}

		}

		updateNonTaxableEntry(sumExclTaxes, sumInclTaxes, accountToTransferMoneyFrom);
		getFile().setModified(true);
	}

	/**
	 * @param sumExclTaxes
	 * @param sumInclTaxes
	 * @param accountToTransferMoneyFrom
	 */
	private void updateNonTaxableEntry(final FixedPointNumber sumExclTaxes,
			final FixedPointNumber sumInclTaxes,
			final String accountToTransferMoneyFrom) {

		System.err.println("GnucashInvoiceWritingImpl.updateNonTaxableEntry "
				+ "accountToTransferMoneyFrom=" + accountToTransferMoneyFrom);

		GnucashTransactionWritingImpl postTransaction = (GnucashTransactionWritingImpl) getPostTransaction();
		if (postTransaction == null) {
			return; //invoice may not be posted
		}

		//==============================================================
		// update transaction-split that transferes the sum incl. taxes from the incomeAccount
		// (e.g. "Umsatzerl�se 19%")
		String accountToTransferMoneyTo = getAccountIDToTransferMoneyTo();
		boolean postTransactionSumUpdated = false;

		System.err.println("GnucashInvoiceWritingImpl.updateNonTaxableEntry "
				+ " #slits=" + postTransaction.getSplits().size());

		for (Object element : postTransaction.getSplits()) {
			GnucashTransactionSplitWritingImpl split = (GnucashTransactionSplitWritingImpl) element;
			if (split.getAccountID().equals(accountToTransferMoneyTo)) {

				FixedPointNumber value = split.getValue();
				split.setQuantity(split.getQuantity().add(sumInclTaxes));
				split.setValue(value.add(sumInclTaxes));
				postTransactionSumUpdated = true;

				System.err.println("GnucashInvoiceWritingImpl.updateNonTaxableEntry "
						+ " updated split " + split.getId());
				break;
			}
		}
		if (!postTransactionSumUpdated) {
			GnucashTransactionSplitWritingImpl split =
					(GnucashTransactionSplitWritingImpl) postTransaction.createWritingSplit(getFile().getAccountByID(accountToTransferMoneyTo));
			split.setQuantity(sumInclTaxes);
			split.setValue(sumInclTaxes);
			split.setSplitAction("Rechnung");

			// this split must have a reference to the lot
			// as has the transaction-split of the whole sum in the
			// transaction when the invoice is payed
			GncTransaction.TrnSplits.TrnSplit.SplitLot lotref
					= ((GnucashFileImpl) getFile()).getObjectFactory()
					.createGncTransactionTrnSplitsTrnSplitSplitLot();
			lotref.setType(getJwsdpPeer().getInvoicePostlot().getType());
			lotref.setValue(getJwsdpPeer().getInvoicePostlot().getValue());
			split.getJwsdpPeer().setSplitLot(lotref);

			System.err.println("GnucashInvoiceWritingImpl.updateNonTaxableEntry "
					+ " created split " + split.getId());
		}

		//      ==============================================================
		// update transaction-split that transferes the sum incl. taxes to the postAccount
		// (e.g. "Forderungen aus Lieferungen und Leistungen")

		boolean postTransactionNetSumUpdated = false;
		for (Object element : postTransaction.getSplits()) {
			GnucashTransactionSplitWritingImpl split = (GnucashTransactionSplitWritingImpl) element;
			if (split.getAccountID().equals(accountToTransferMoneyFrom)) {

				FixedPointNumber value = split.getValue();
				split.setQuantity(split.getQuantity().subtract(sumExclTaxes));
				split.setValue(value.subtract(sumExclTaxes));
				split.getJwsdpPeer().setSplitAction("Rechnung");
				postTransactionNetSumUpdated = true;
				break;
			}
		}
		if (!postTransactionNetSumUpdated) {
			GnucashTransactionSplitWritingImpl split =
					new GnucashTransactionSplitWritingImpl(postTransaction, getFile().getAccountByID(accountToTransferMoneyFrom));
			split.setQuantity(((FixedPointNumber) sumExclTaxes.clone()).negate());
			split.setValue(((FixedPointNumber) sumExclTaxes.clone()).negate());
		}

		assert postTransaction.isBalanced();
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableInvoice#isModifiable()
	 */
	public boolean isModifiable() {
		return getPayingTransactions().size() == 0;
	}

	/**
	 * Throw an IllegalStateException if we are not modifiable.
	 *
	 * @see #isModifiable()
	 */
	protected void atemptChange() {
		if (!isModifiable()) {
			throw new IllegalStateException("this invoice is NOT changable because there are already payment for it made!");
		}
	}

	/**
	 * @see GnucashWritableInvoice#setJob(GnucashJob)
	 */
	public void setJob(final GnucashJob job) {
		atemptChange();
		getJwsdpPeer().getInvoiceOwner().getOwnerId().setValue(job.getId());
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableInvoice#setDateOpened(LocalDateTime)
	 */
	public void setDateOpened(final LocalDateTime d) {
		atemptChange();
		super.dateOpened = d.atZone(ZoneId.systemDefault());
		getJwsdpPeer().getInvoiceOpened().setTsDate(DATE_OPENED_FORMAT.format(d));
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableInvoice#setDateOpened(java.lang.String)
	 */
	public void setDateOpened(final String d) throws java.text.ParseException {
		atemptChange();
		setDateOpened(LocalDateTime.parse(d, DATE_OPENED_FORMAT));
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableInvoice#setDatePosted(LocalDateTime)
	 */
	public void setDatePosted(final LocalDateTime d) {

		atemptChange();

		super.datePosted = d.atZone(ZoneId.systemDefault());
		getJwsdpPeer().getInvoicePosted().setTsDate(DATE_OPENED_FORMAT.format(d));
		getFile().setModified(true);

		// change the date of the transaction too
		GnucashWritableTransaction postTr = getWritingPostTransaction();
		if (postTr != null) {
			postTr.setDatePosted(d);
		}
	}

	/**
	 * @see GnucashWritableInvoice#setDatePosted(java.lang.String)
	 */
	public void setDatePosted(final String d) throws java.text.ParseException {
		setDatePosted(LocalDateTime.parse(d, DATE_OPENED_FORMAT));
	}

	/**
	 * @see GnucashInvoice#getPayingTransactions()
	 */
	public Collection getWritingPayingTransactions() {
		return getPayingTransactions();
	}

	/**
	 * @return get a modifiable version of {@link GnucashInvoiceImpl#getPostTransaction()}
	 */
	public GnucashWritableTransaction getWritingPostTransaction() {
		GncV2.GncBook.GncGncInvoice.InvoicePosttxn invoicePosttxn = jwsdpPeer.getInvoicePosttxn();
		if (invoicePosttxn == null) {
			return null; //invoice may not be posted
		}
		return getFile().getTransactionByID(invoicePosttxn.getValue());
	}

	/**
	 * @see GnucashWritableInvoice#getWritableEntryById(java.lang.String)
	 */
	public GnucashWritableInvoiceEntry getWritableEntryById(final String id) {
		return (GnucashWritableInvoiceEntry) super.getEntryById(id);
	}

	/**
	 * @see GnucashWritableInvoice#remove()
	 */
	public void remove() {

		if (!isModifiable()) {
			throw new IllegalStateException("Invoice has payments and cannot be deleted!");
		}

		// we copy the list because element.remove() modifies it
		Collection<GnucashInvoiceEntry> entries2 = new LinkedList<GnucashInvoiceEntry>();
		entries2.addAll(this.getEntries());
		for (GnucashInvoiceEntry element : entries2) {
			((GnucashWritableInvoiceEntry) element).remove();

		}

		GnucashWritableTransaction post = (GnucashWritableTransaction) getPostTransaction();
		if (post != null) {
			post.remove();
		}

		((GnucashFileWritingImpl) getFile()).removeInvoice(this);

	}

}


