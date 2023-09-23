package org.gnucash.write.impl.spec;

import java.time.LocalDate;
import java.util.Date;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.impl.spec.GnucashVendorBillEntryImpl;
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceImpl;
import org.gnucash.write.spec.GnucashWritableVendorBill;
import org.gnucash.write.spec.GnucashWritableVendorBillEntry;

/**
 * TODO write a comment what this type does here
 */
public class GnucashWritableVendorBillImpl extends GnucashWritableGenerInvoiceImpl 
                                           implements GnucashWritableVendorBill
{

	/**
	 * Create an editable invoice facading an existing JWSDP-peer.
	 *
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @param file      the file to register under
	 * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice, GnucashFile)
	 */
	public GnucashWritableVendorBillImpl(final GncV2.GncBook.GncGncInvoice jwsdpPeer, final GnucashFile file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param file the file we are associated with.
	 */
	protected GnucashWritableVendorBillImpl(
			final GnucashWritableFileImpl file,
			final String invoiceNumber,
			final GnucashGenerJob job,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final LocalDate dueDate) {
		super(createJobInvoice(file, invoiceNumber, job, accountToTransferMoneyTo, dueDate), file);
	}

	/**
	 * create and add a new entry.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableVendorBillEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity);
		return new GnucashWritableVendorBillEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableVendorBillEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice,
		final FixedPointNumber quantity,
		final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity, tax);
		return new GnucashWritableVendorBillEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableVendorBillEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity,
		final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity, tax);
		return new GnucashWritableVendorBillEntryImpl(entryGener);
	}
	
	// -----------------------------------------------------------

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
	 */
	protected void removeEntry(final GnucashWritableVendorBillEntryImpl impl) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		removeBillEntry(impl);
	}

	/**
	 * Called by
	 * ${@link GnucashWritableVendorBillEntryImpl#createInvoiceEntry(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
	 *
	 * @param entry the entry to add to our internal list of vendor-bill-entries
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	protected void addEntry(final GnucashVendorBillEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		addBillEntry(entry);
	}

	protected void subtractEntry(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		subtractBillEntry(entry);
	}

	/**
	 * @return the ID of the Account to transfer the money from
	 * @throws WrongInvoiceTypeException 
	 */
	private String getAccountIDToTransferMoneyFrom(final GnucashVendorBillEntryImpl entry) throws WrongInvoiceTypeException {
		return getBillAccountIDToTransferMoneyFrom(entry);
	}

	@Override
	protected String getInvcAccountIDToTransferMoneyFrom(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException {
		throw new WrongInvoiceTypeException();
	}

	/**
	 * Throw an IllegalStateException if we are not modifiable.
	 *
	 * @see #isModifiable()
	 */
	protected void attemptChange() {
		if (!isModifiable()) {
			throw new IllegalStateException("this vendor bill is NOT changable because there are already payment for it made!");
		}
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @see GnucashWritableGenerInvoice#setGenerJob(GnucashGenerJob)
	 */
	public void setJob(final GnucashVendorJob job) throws WrongInvoiceTypeException {
		setGenerJob(job);
	}

	/**
	 * @see GnucashWritableGenerInvoice#getWritableGenerEntryById(java.lang.String)
	 */
	public GnucashWritableVendorBillEntry getWritableEntryById(final String id) {
		return new GnucashWritableVendorBillEntryImpl(getGenerEntryById(id));
	}

}

