package org.gnucash.write.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.gnucash.Const;
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
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.IllegalTransactionSplitActionException;
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.aux.GCshTaxTableEntry;
import org.gnucash.read.aux.WrongOwnerJITypeException;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashTransactionImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableTransaction;

import jakarta.xml.bind.JAXBElement;

/**
 * TODO write a comment what this type does here
 */
public class GnucashWritableGenerInvoiceImpl extends GnucashGenerInvoiceImpl 
                                             implements GnucashWritableGenerInvoice 
{

	/**
	 * Create an editable invoice facading an existing JWSDP-peer.
	 *
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @param file      the file to register under
	 * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice, GnucashFile)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableGenerInvoiceImpl(final GncV2.GncBook.GncGncInvoice jwsdpPeer, final GnucashFile file) {
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
	protected GnucashWritableGenerInvoiceImpl(final GnucashWritableFileImpl file,
			final String internalID,
			final String invoiceNumber,
			final GnucashGenerJob job,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final Date dueDate) {
		super(createInvoice(file, internalID, invoiceNumber, job, accountToTransferMoneyTo, dueDate), file);
	}

	/**
	 * create and add a new entry.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableGenerInvoiceEntry createGenerEntry(
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		GnucashWritableGenerInvoiceEntryImpl entry = new GnucashWritableGenerInvoiceEntryImpl(this, quantity, singleUnitPrice);
		return entry;
	}

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableGenerInvoiceEntry createGenerEntry(
		final FixedPointNumber singleUnitPrice,
		final FixedPointNumber quantity,
		final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntryImpl entry = new GnucashWritableGenerInvoiceEntryImpl(this, quantity, singleUnitPrice);
		if (tax == null
				||
				tax.getEntries().isEmpty()
				||
				(tax.getEntries().iterator().next())
						.getAmount().equals(new FixedPointNumber())) {
			// no taxes
			entry.setInvcTaxable(false);
		} else {
			entry.setInvcTaxTable(tax);
		}
		return entry;
	}

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableGenerInvoiceEntry createGenerEntry(
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity,
		final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntryImpl entry = new GnucashWritableGenerInvoiceEntryImpl(this, quantity, singleUnitPrice);
		if (tax.equals(new FixedPointNumber())) {
			// no taxes
			entry.setInvcTaxable(false);
		} else {
			//TODO: find taxtable to use for given percentage
		}
		return entry;
	}

	/**
	 * Use ${@link GnucashWritableFile#createWritableInvoice(String, GnucashGenerJob, GnucashAccount, java.util.Date)}
	 * instead of calling this method!
	 *
	 * @param accountToTransferMoneyTo e.g. "Forderungen aus Lieferungen und Leistungen "
	 */
	protected static GncV2.GncBook.GncGncInvoice createInvoice(
			final GnucashWritableFileImpl file,
			final String internalID,
			final String invoiceNumber,
			final GnucashGenerJob job,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final Date dueDate) {

		ObjectFactory factory = file.getObjectFactory();
		String invoiceguid = (internalID == null ? file.createGUID() : internalID);

		GncV2.GncBook.GncGncInvoice invc = file.createGncGncInvoiceType();
		invc.setInvoiceActive(1); //TODO: is this correct?
		invc.setInvoiceBillingId(invoiceNumber);
		{
			GncV2.GncBook.GncGncInvoice.InvoiceCurrency currency = factory.createGncV2GncBookGncGncInvoiceInvoiceCurrency();
			currency.setCmdtyId(file.getDefaultCurrencyID());
			currency.setCmdtySpace("ISO4217");
			invc.setInvoiceCurrency(currency);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoiceGuid invoiceref = factory.createGncV2GncBookGncGncInvoiceInvoiceGuid();
			invoiceref.setType("guid");
			invoiceref.setValue(invoiceguid);
			invc.setInvoiceGuid(invoiceref);
		}
		invc.setInvoiceId(invoiceNumber);
		{
			GncV2.GncBook.GncGncInvoice.InvoiceOpened opened = factory.createGncV2GncBookGncGncInvoiceInvoiceOpened();
			opened.setTsDate(DATE_OPENED_FORMAT_1.format(new Date()));
			invc.setInvoiceOpened(opened);
		}
		{

			GncV2.GncBook.GncGncInvoice.InvoiceOwner jobref = factory.createGncV2GncBookGncGncInvoiceInvoiceOwner();
			jobref.setOwnerType("gncJob");
			jobref.setVersion(Const.XML_FORMAT_VERSION);
			{
				OwnerId ownerIdRef = factory.createOwnerId();
				ownerIdRef.setType("guid");
				ownerIdRef.setValue(job.getId());
				jobref.setOwnerId(ownerIdRef);
			}
			invc.setInvoiceOwner(jobref);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePostacc postacc = factory.createGncV2GncBookGncGncInvoiceInvoicePostacc();
			postacc.setType("guid");
			postacc.setValue(accountToTransferMoneyTo.getId());
			invc.setInvoicePostacc(postacc);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePosted posted = factory.createGncV2GncBookGncGncInvoiceInvoicePosted();
			posted.setTsDate(DATE_OPENED_FORMAT_1.format(new Date()));
			invc.setInvoicePosted(posted);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePostlot lotref = factory.createGncV2GncBookGncGncInvoiceInvoicePostlot();
			lotref.setType("guid");

			GncAccount.ActLots.GncLot newlot = createLot(file, factory, invoiceguid, accountToTransferMoneyTo, job);

			lotref.setValue(newlot.getLotId().getValue());
			invc.setInvoicePostlot(lotref);
		}
		{
			GncV2.GncBook.GncGncInvoice.InvoicePosttxn transactionref = factory.createGncV2GncBookGncGncInvoiceInvoicePosttxn();
			transactionref.setType("guid");
			transactionref.setValue(createPostTransaction(file, factory, invoiceguid, dueDate).getId());

			invc.setInvoicePosttxn(transactionref);
		}
		invc.setVersion(Const.XML_FORMAT_VERSION);

		file.getRootElement().getGncBook().getBookElements().add(invc);
		file.setModified(true);
		return invc;
	}

	/**
	 * @see #GnucashInvoiceWritingImpl(GnucashWritableFileImpl, String, String, GnucashGenerJob, GnucashAccountImpl, Date)
	 */
	private static GnucashTransactionImpl createPostTransaction(
		final GnucashWritableFileImpl file,
		final ObjectFactory factory,
		final String invoiceID,
		final Date dueDate) {
		GnucashTransactionImpl postTransaction = new GnucashWritableTransactionImpl(file, file.createGUID());

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
			value.getContent().add(GnucashTransaction.TYPE_INVOICE);

			slot.setSlotValue(value);
			slots.getSlot().add(slot);
		}

		//      add trans-date-due -slot

		{
			Slot slot = factory.createSlot();
			SlotValue value = factory.createSlotValue();
			slot.setSlotKey("trans-date-due");
			value.setType("timespec");
			JAXBElement<String> tsDate = factory.createTsDate(DATE_OPENED_FORMAT_1.format(dueDate));
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

	private static GncAccount.ActLots.GncLot createLot(
			final GnucashWritableFileImpl file,
			final ObjectFactory factory,
			final String invoiceID,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final GnucashGenerJob job) {

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
		newlot.setVersion(Const.XML_FORMAT_VERSION);

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
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
	 */
	protected void removeInvcEntry(final GnucashWritableGenerInvoiceEntryImpl impl) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		if (!isModifiable()) {
			throw new IllegalStateException("This customer invoice has payments and is not modifiable!");
		}

		this.subtractInvcEntry(impl);
		entries.remove(impl);
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
	 */
	protected void removeBillEntry(final GnucashWritableGenerInvoiceEntryImpl impl) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		if (!isModifiable()) {
			throw new IllegalStateException("This vendor bill has payments and is not modifiable!");
		}

		this.subtractBillEntry(impl);
		entries.remove(impl);
	}

	/**
	 * Called by
	 * ${@link GnucashWritableGenerInvoiceEntryImpl#createInvoiceEntry(GnucashGenerInvoiceWritingImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
	 *
	 * @param entry the entry to add to our internal list of invoice-entries
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public void addInvcEntry(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		System.err.println("GnucashGenerInvoiceWritingImpl.addInvcEntry " + entry.toString());

		if (!isModifiable()) {
			throw new IllegalArgumentException("This invoice has payments and is"
					+ " thus not modifiable");
		}

		super.addGenerInvcEntry(entry);

		//==============================================================
		// update or add split in PostTransaction
		// that transferes the money from the tax-account

		boolean isTaxable = entry.isInvcTaxable();
		FixedPointNumber sumExclTaxes = entry.getInvcSumExclTaxes();
		FixedPointNumber sumInclTaxes = entry.getInvcSumInclTaxes();
		String accountToTransferMoneyFrom = getInvcAccountIDToTransferMoneyFrom(entry);

		GCshTaxTable taxtable = null;

		if (entry.isInvcTaxable()) {
			taxtable = entry.getInvcTaxTable();
			if (taxtable == null) {
				throw new IllegalArgumentException("The given entry has no i-tax-table (it's taxtable-id is '"
						+ entry.getJwsdpPeer().getEntryITaxtable().getValue()
						+ "')");
			}
		}

		updateEntry(taxtable, isTaxable, sumExclTaxes, sumInclTaxes, accountToTransferMoneyFrom);
		getFile().setModified(true);
	}

	/**
	 * Called by
	 * ${@link GnucashWritableGenerInvoiceEntryImpl#createInvoiceEntry(GnucashGenerInvoiceWritingImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
	 *
	 * @param entry the entry to add to our internal list of invoice-entries
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public void addBillEntry(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		System.err.println("GnucashGenerInvoiceWritingImpl.addBillEntry " + entry.toString());

		if (!isModifiable()) {
			throw new IllegalArgumentException("This invoice has payments and is"
					+ " thus not modifiable");
		}

		super.addGenerInvcEntry(entry);

		//==============================================================
		// update or add split in PostTransaction
		// that transferes the money from the tax-account

		boolean isTaxable = entry.isBillTaxable();
		FixedPointNumber sumExclTaxes = entry.getBillSumExclTaxes();
		FixedPointNumber sumInclTaxes = entry.getBillSumInclTaxes();
		String accountToTransferMoneyFrom = getBillAccountIDToTransferMoneyFrom(entry);

		GCshTaxTable taxtable = null;

		if (entry.isBillTaxable()) {
			taxtable = entry.getBillTaxTable();
			if (taxtable == null) {
				throw new IllegalArgumentException("The given entry has no b-tax-table (it's taxtable-id is '"
						+ entry.getJwsdpPeer().getEntryBTaxtable().getValue()
						+ "')");
			}
		}

		updateEntry(taxtable, isTaxable, sumExclTaxes, sumInclTaxes, accountToTransferMoneyFrom);
		getFile().setModified(true);
	}

	protected void subtractInvcEntry(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		System.err.println("GnucashGenerInvoiceWritingImpl.subtractInvcEntry " + entry.toString());
		//==============================================================
		// update or add split in PostTransaction
		// that transferes the money from the tax-account

		boolean isTaxable = entry.isInvcTaxable();
		FixedPointNumber sumExclTaxes = entry.getInvcSumExclTaxes().negate();
		FixedPointNumber sumInclTaxes = entry.getInvcSumInclTaxes().negate();
		String accountToTransferMoneyFrom = entry.getJwsdpPeer().getEntryIAcct().getValue();

		GCshTaxTable taxtable = null;

		if (entry.isInvcTaxable()) {
			taxtable = entry.getInvcTaxTable();
			if (taxtable == null) {
				throw new IllegalArgumentException("The given entry has no i-tax-table (its i-taxtable-id is '"
						+ entry.getJwsdpPeer().getEntryITaxtable().getValue() + "')");
			}
		}

		updateEntry(taxtable, isTaxable, sumExclTaxes, sumInclTaxes, accountToTransferMoneyFrom);
		getFile().setModified(true);
	}

	protected void subtractBillEntry(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		System.err.println("GnucashGenerInvoiceWritingImpl.subtractBillEntry " + entry.toString());
		//==============================================================
		// update or add split in PostTransaction
		// that transferes the money from the tax-account

		boolean isTaxable = entry.isBillTaxable();
		FixedPointNumber sumExclTaxes = entry.getBillSumExclTaxes().negate();
		FixedPointNumber sumInclTaxes = entry.getBillSumInclTaxes().negate();
		String accountToTransferMoneyFrom = entry.getJwsdpPeer().getEntryBAcct().getValue();

		GCshTaxTable taxtable = null;

		if (entry.isBillTaxable()) {
			taxtable = entry.getBillTaxTable();
			if (taxtable == null) {
				throw new IllegalArgumentException("The given entry has no b-tax-table (its b-taxtable-id is '"
						+ entry.getJwsdpPeer().getEntryBTaxtable().getValue() + "')");
			}
		}

		updateEntry(taxtable, isTaxable, sumExclTaxes, sumInclTaxes, accountToTransferMoneyFrom);
		getFile().setModified(true);
	}

	/**
	 * @return the AccountID of the Account to transfer the money from
	 */
	protected String getInvcAccountIDToTransferMoneyFrom(final GnucashGenerInvoiceEntryImpl entry) {
		return entry.getJwsdpPeer().getEntryIAcct().getValue();
	}

	/**
	 * @return the AccountID of the Account to transfer the money from
	 */
	protected String getBillAccountIDToTransferMoneyFrom(final GnucashGenerInvoiceEntryImpl entry) {
		return entry.getJwsdpPeer().getEntryBAcct().getValue();
	}

	/**
	 * @throws IllegalTransactionSplitActionException 
	 */
	private void updateEntry(final GCshTaxTable taxtable,
			final boolean isTaxable,
			final FixedPointNumber sumExclTaxes,
			final FixedPointNumber sumInclTaxes,
			final String accountToTransferMoneyFrom) throws IllegalTransactionSplitActionException {
		System.err.println("GnucashInvoiceWritingImpl.updateEntry "
				+ "isTaxable=" + isTaxable + " "
				+ "accountToTransferMoneyFrom=" + accountToTransferMoneyFrom + " ");

		GnucashWritableTransactionImpl postTransaction = (GnucashWritableTransactionImpl) getPostTransaction();
		if (postTransaction == null) {
			return; //invoice may not be posted
		}
		if (isTaxable) {

			// get the first account of the taxTable
			GCshTaxTableEntry taxTableEntry = taxtable.getEntries().iterator().next();
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
				GnucashWritableTransactionSplitImpl split = (GnucashWritableTransactionSplitImpl) element;
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
				GnucashWritableTransactionSplitImpl split =
						(GnucashWritableTransactionSplitImpl) postTransaction.createWritingSplit(accountToTransferTaxTo);
				split.setQuantity(((FixedPointNumber) entryTaxAmount.clone()).negate());
				split.setValue(((FixedPointNumber) entryTaxAmount.clone()).negate());

				//assert !split.getValue().isPositive();
				//assert !split.getQuantity().isPositive();

				split.setSplitAction(GnucashTransactionSplit.ACTION_INVOICE);

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
	 * @throws IllegalTransactionSplitActionException 
	 */
	private void updateNonTaxableEntry(final FixedPointNumber sumExclTaxes,
			final FixedPointNumber sumInclTaxes,
			final String accountToTransferMoneyFrom) throws IllegalTransactionSplitActionException {

		System.err.println("GnucashInvoiceWritingImpl.updateNonTaxableEntry "
				+ "accountToTransferMoneyFrom=" + accountToTransferMoneyFrom);

		GnucashWritableTransactionImpl postTransaction = (GnucashWritableTransactionImpl) getPostTransaction();
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
			GnucashWritableTransactionSplitImpl split = (GnucashWritableTransactionSplitImpl) element;
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
			GnucashWritableTransactionSplitImpl split =
					(GnucashWritableTransactionSplitImpl) postTransaction.createWritingSplit(getFile().getAccountByID(accountToTransferMoneyTo));
			split.setQuantity(sumInclTaxes);
			split.setValue(sumInclTaxes);
			split.setSplitAction(GnucashTransactionSplit.ACTION_INVOICE);

			// this split must have a reference to the lot
			// as has the transaction-split of the whole sum in the
			// transaction when the invoice is Paid
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
			GnucashWritableTransactionSplitImpl split = (GnucashWritableTransactionSplitImpl) element;
			if (split.getAccountID().equals(accountToTransferMoneyFrom)) {

				FixedPointNumber value = split.getValue();
				split.setQuantity(split.getQuantity().subtract(sumExclTaxes));
				split.setValue(value.subtract(sumExclTaxes));
				split.getJwsdpPeer().setSplitAction(GnucashTransactionSplit.ACTION_INVOICE);
				postTransactionNetSumUpdated = true;
				break;
			}
		}
		if (!postTransactionNetSumUpdated) {
			GnucashWritableTransactionSplitImpl split =
					new GnucashWritableTransactionSplitImpl(postTransaction, getFile().getAccountByID(accountToTransferMoneyFrom));
			split.setQuantity(((FixedPointNumber) sumExclTaxes.clone()).negate());
			split.setValue(((FixedPointNumber) sumExclTaxes.clone()).negate());
		}

		assert postTransaction.isBalanced();
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableGenerInvoice#isModifiable()
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
	
	// -----------------------------------------------------------

	// ::TODO
//	void setOwnerID(String ownerID, String type) {
//	    GCshOwner owner = new GCshOwner();
//	    getJwsdpPeer().setInvoiceOwner(new GCShOwner(xxx));
//	}
	
	public void setOwner(GCshOwner owner) throws WrongOwnerJITypeException {
	    if ( owner.getJIType() != GCshOwner.JIType.INVOICE ) 
		throw new WrongOwnerJITypeException();
	    
	    getJwsdpPeer().setInvoiceOwner(owner.getInvcOwner());
	}
	
	// -----------------------------------------------------------

	/**
	 * @see GnucashWritableGenerInvoice#setGenerJob(GnucashGenerJob)
	 */
	public void setGenerJob(final GnucashGenerJob job) {
		atemptChange();
		getJwsdpPeer().getInvoiceOwner().getOwnerId().setValue(job.getId());
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableGenerInvoice#setDateOpened(LocalDateTime)
	 */
	public void setDateOpened(final LocalDateTime d) {
		atemptChange();
		super.dateOpened = d.atZone(ZoneId.systemDefault());
		getJwsdpPeer().getInvoiceOpened().setTsDate(DATE_OPENED_FORMAT.format(d));
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableGenerInvoice#setDateOpened(java.lang.String)
	 */
	public void setDateOpened(final String d) throws java.text.ParseException {
		atemptChange();
		setDateOpened(LocalDateTime.parse(d, DATE_OPENED_FORMAT));
		getFile().setModified(true);
	}

	/**
	 * @see GnucashWritableGenerInvoice#setDatePosted(LocalDateTime)
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
	 * @see GnucashWritableGenerInvoice#setDatePosted(java.lang.String)
	 */
	public void setDatePosted(final String d) throws java.text.ParseException {
		setDatePosted(LocalDateTime.parse(d, DATE_OPENED_FORMAT));
	}

	/**
	 * @see GnucashGenerInvoice#getPayingTransactions()
	 */
	public Collection<GnucashWritableTransaction> getWritingPayingTransactions() {
		Collection<GnucashWritableTransaction> trxList = new LinkedList<GnucashWritableTransaction>();
		
		for ( GnucashTransaction trx : getPayingTransactions()) {
		    GnucashWritableTransaction newTrx = new GnucashWritableTransactionImpl(trx);
		    trxList.add(newTrx);
		}
		
		return trxList;
	}

	/**
	 * @return get a modifiable version of {@link GnucashGenerInvoiceImpl#getPostTransaction()}
	 */
	public GnucashWritableTransaction getWritingPostTransaction() {
		GncV2.GncBook.GncGncInvoice.InvoicePosttxn invoicePosttxn = jwsdpPeer.getInvoicePosttxn();
		if (invoicePosttxn == null) {
			return null; //invoice may not be posted
		}
		return getFile().getTransactionByID(invoicePosttxn.getValue());
	}

	/**
	 * @see GnucashWritableGenerInvoice#getWritableEntryById(java.lang.String)
	 */
	public GnucashWritableGenerInvoiceEntry getWritableEntryById(final String id) {
		return (GnucashWritableGenerInvoiceEntry) super.getGenerInvcEntryById(id);
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoice#remove()
	 */
	public void remove() throws WrongInvoiceTypeException, NoTaxTableFoundException {

		if (!isModifiable()) {
			throw new IllegalStateException("Invoice has payments and cannot be deleted!");
		}

		// we copy the list because element.remove() modifies it
		Collection<GnucashGenerInvoiceEntry> entries2 = new LinkedList<GnucashGenerInvoiceEntry>();
		entries2.addAll(this.getGenerInvcEntries());
		for (GnucashGenerInvoiceEntry element : entries2) {
			((GnucashWritableGenerInvoiceEntry) element).remove();
		}

		GnucashWritableTransaction post = (GnucashWritableTransaction) getPostTransaction();
		if (post != null) {
			post.remove();
		}

		((GnucashWritableFileImpl) getFile()).removeInvoice(this);

	}

}


