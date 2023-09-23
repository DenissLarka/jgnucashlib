package org.gnucash.write.impl.spec;

import java.time.LocalDate;
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
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashTransactionImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.impl.spec.GnucashCustomerInvoiceEntryImpl;
import org.gnucash.read.spec.GnucashCustomerInvoiceEntry;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableTransaction;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceImpl;
import org.gnucash.write.impl.GnucashWritableTransactionSplitImpl;
import org.gnucash.write.impl.GnucashWritableTransactionImpl;
import org.gnucash.write.spec.GnucashWritableCustomerInvoice;
import org.gnucash.write.spec.GnucashWritableCustomerInvoiceEntry;

import jakarta.xml.bind.JAXBElement;

/**
 * TODO write a comment what this type does here
 */
public class GnucashWritableCustomerInvoiceImpl extends GnucashWritableGenerInvoiceImpl 
                                                implements GnucashWritableCustomerInvoice 
{

	/**
	 * Create an editable invoice facading an existing JWSDP-peer.
	 *
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @param file      the file to register under
	 * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice, GnucashFile)
	 */
	public GnucashWritableCustomerInvoiceImpl(final GncV2.GncBook.GncGncInvoice jwsdpPeer, final GnucashFile file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param file the file we are associated with.
	 */
	protected GnucashWritableCustomerInvoiceImpl(
			final GnucashWritableFileImpl file,
			final String invoiceNumber,
			final GnucashGenerJob job,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final LocalDate dueDate) {
		super(createJobInvoice(file, invoiceNumber, job, accountToTransferMoneyTo, dueDate), file);
	}
	
	/**
	 * @param file the file we are associated with.
	 * @throws WrongInvoiceTypeException 
	 */
	protected GnucashWritableCustomerInvoiceImpl(final GnucashWritableGenerInvoiceImpl invc) throws WrongInvoiceTypeException {
	    super(invc.getJwsdpPeer(), invc.getFile());

	    // No, we cannot check that first, because the super() method
	    // always has to be called first.
	    if ( ! invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT).equals(GnucashGenerInvoice.TYPE_CUSTOMER) )
	      throw new WrongInvoiceTypeException();

	    // ::TODO
//	    for ( GnucashGenerInvoiceEntry entry : invc.getGenerEntries() )
//	    {
//	      addEntry(new GnucashWritableCustomerInvoiceEntryImpl(entry));
//	    }

	    for ( GnucashTransaction trx : invc.getPayingTransactions() )
	    {
	      for ( GnucashTransactionSplit splt : trx.getSplits() ) 
	      {
	        String lot = splt.getLotID();
	        if ( lot != null ) {
	            for ( GnucashGenerInvoice invc1 : splt.getTransaction().getGnucashFile().getGenerInvoices() ) {
	                String lotID = invc1.getLotID();
	                if ( lotID != null &&
	                     lotID.equals(lot) ) {
	                    // Check if it's a payment transaction. 
	                    // If so, add it to the invoice's list of payment transactions.
	                    if ( splt.getAction().equals(GnucashTransactionSplit.ACTION_PAYMENT) ) {
	                        addPayingTransaction(splt);
	                    }
	                } // if lotID
	            } // for invc
	        } // if lot
	      } // for splt
	    } // for trx
	}
	
	// -----------------------------------------------------------

	/**
	 * create and add a new entry.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableCustomerInvoiceEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity);
		return new GnucashWritableCustomerInvoiceEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableCustomerInvoiceEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice,
		final FixedPointNumber quantity,
		final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity, tax);
		return new GnucashWritableCustomerInvoiceEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableCustomerInvoiceEntry createEntry(
		final GnucashAccount acct,
		final FixedPointNumber singleUnitPrice, 
		final FixedPointNumber quantity,
		final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

	    GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(acct, singleUnitPrice, quantity, tax);
	    return new GnucashWritableCustomerInvoiceEntryImpl(entryGener);
	}
	
	// -----------------------------------------------------------

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
	 */
	protected void removeEntry(final GnucashWritableCustomerInvoiceEntryImpl impl) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		removeInvcEntry(impl);
	}

	/**
	 * Called by
	 * ${@link GnucashWritableCustomerInvoiceEntryImpl#createInvoiceEntry(GnucashWritableGenerInvoiceImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
	 *
	 * @param entry the entry to add to our internal list of customer-invoice-entries
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	protected void addEntry(final GnucashCustomerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		addInvcEntry(entry);
	}

	protected void subtractEntry(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		subtractInvcEntry(entry);
	}

	/**
	 * @return the ID of the Account to transfer the money from
	 * @throws WrongInvoiceTypeException 
	 */
	private String getAccountIDToTransferMoneyFrom(final GnucashCustomerInvoiceEntryImpl entry) throws WrongInvoiceTypeException {
		return getInvcAccountIDToTransferMoneyFrom(entry);
	}

	protected String getBillAccountIDToTransferMoneyFrom(final GnucashGenerInvoiceEntryImpl entry) throws WrongInvoiceTypeException {
		throw new WrongInvoiceTypeException();
	}

	/**
	 * Throw an IllegalStateException if we are not modifiable.
	 *
	 * @see #isModifiable()
	 */
	protected void attemptChange() {
		if (!isModifiable()) {
			throw new IllegalStateException("this customer invoice is NOT changable because there are already payment for it made!");
		}
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @see GnucashWritableGenerInvoice#setGenerJob(GnucashGenerJob)
	 */
	public void setJob(final GnucashCustomerJob job) throws WrongInvoiceTypeException {
		setGenerJob(job);
	}

	/**
	 * @see GnucashWritableGenerInvoice#getWritableGenerEntryById(java.lang.String)
	 */
	public GnucashWritableCustomerInvoiceEntry getWritableEntryById(final String id) {
		return new GnucashWritableCustomerInvoiceEntryImpl(getGenerEntryById(id));
	}

}

