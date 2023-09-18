package org.gnucash.write.impl.spec;

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
import org.gnucash.write.impl.GnucashFileWritingImpl;
import org.gnucash.write.impl.GnucashGenerInvoiceWritingImpl;
import org.gnucash.write.impl.GnucashTransactionSplitWritingImpl;
import org.gnucash.write.impl.GnucashTransactionWritingImpl;
import org.gnucash.write.spec.GnucashWritableCustomerInvoice;
import org.gnucash.write.spec.GnucashWritableCustomerInvoiceEntry;

import jakarta.xml.bind.JAXBElement;

/**
 * TODO write a comment what this type does here
 */
public class GnucashCustomerInvoiceWritingImpl extends GnucashGenerInvoiceWritingImpl 
                                               implements GnucashWritableCustomerInvoice 
{

	/**
	 * Create an editable invoice facading an existing JWSDP-peer.
	 *
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @param file      the file to register under
	 * @see GnucashGenerInvoiceImpl#GnucashInvoiceImpl(GncV2.GncBook.GncGncInvoice, GnucashFile)
	 */
	public GnucashCustomerInvoiceWritingImpl(final GncV2.GncBook.GncGncInvoice jwsdpPeer, final GnucashFile file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param file the file we are associated with.
	 */
	protected GnucashCustomerInvoiceWritingImpl(
			final GnucashFileWritingImpl file,
			final String internalID,
			final String invoiceNumber,
			final GnucashGenerJob job,
			final GnucashAccountImpl accountToTransferMoneyTo,
			final Date dueDate) {
		super(createInvoice(file, internalID, invoiceNumber, job, accountToTransferMoneyTo, dueDate), file);
	}
	
	/**
	 * @param file the file we are associated with.
	 * @throws WrongInvoiceTypeException 
	 */
	protected GnucashCustomerInvoiceWritingImpl(final GnucashGenerInvoiceWritingImpl invc) throws WrongInvoiceTypeException {
	    super(invc.getJwsdpPeer(), invc.getFile());

	    // No, we cannot check that first, because the super() method
	    // always has to be called first.
	    if ( ! invc.getOwnerType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) )
	      throw new WrongInvoiceTypeException();
	    
	    for ( GnucashGenerInvoiceEntry entry : invc.getGenerInvcEntries() )
	    {
	      addEntry(new GnucashCustomerInvoiceEntryWritingImpl(entry));
	    }

	    for ( GnucashTransaction trx : invc.getPayingTransactions() )
	    {
	      for ( GnucashTransactionSplit splt : trx.getSplits() ) 
	      {
	        String lot = splt.getLotID();
	        if ( lot != null ) {
	            for ( GnucashGenerInvoice invc1 : splt.getTransaction().getGnucashFile().getInvoices() ) {
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
	 */
	public GnucashWritableCustomerInvoiceEntry createEntry(final FixedPointNumber singleUnitPrice, final FixedPointNumber quantity) throws WrongInvoiceTypeException {
		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(singleUnitPrice, quantity);
		return (GnucashWritableCustomerInvoiceEntry) new GnucashCustomerInvoiceEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 *
	 * @return an entry using the given Tax-Table
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableCustomerInvoiceEntry createEntry(
			final FixedPointNumber singleUnitPrice,
			final FixedPointNumber quantity,
			final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(singleUnitPrice, quantity, tax);
		return new GnucashCustomerInvoiceEntryImpl(entryGener);
	}

	/**
	 * create and add a new entry.<br/>
	 * The entry will use the accounts of the
	 * SKR03.
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableCustomerInvoiceEntry createEntry(
			final FixedPointNumber singleUnitPrice, 
			final FixedPointNumber quantity,
			final FixedPointNumber tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		GnucashWritableGenerInvoiceEntry entryGener = createGenerEntry(singleUnitPrice, quantity, tax);
		return (GnucashWritableCustomerInvoiceEntry) new GnucashCustomerInvoiceEntryImpl(entryGener);
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see #addInvcEntry(GnucashGenerInvoiceEntryImpl)
	 */
	protected void removeEntry(final GnucashCustomerInvoiceEntryWritingImpl impl) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		removeInvcEntry(impl);
	}

	/**
	 * Called by
	 * ${@link GnucashCustomerInvoiceEntryWritingImpl#createInvoiceEntry(GnucashGenerInvoiceWritingImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}.
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
	 * @return the AccountID of the Account to transfer the money from
	 */
	private String getAccountIDToTransferMoneyFrom(final GnucashCustomerInvoiceEntryImpl entry) {
		return getInvcAccountIDToTransferMoneyFrom(entry);
	}

	/**
	 * Throw an IllegalStateException if we are not modifiable.
	 *
	 * @see #isModifiable()
	 */
	protected void atemptChange() {
		if (!isModifiable()) {
			throw new IllegalStateException("this customer invoice is NOT changable because there are already payment for it made!");
		}
	}

	/**
	 * @see GnucashWritableGenerInvoice#setGenerJob(GnucashGenerJob)
	 */
	public void setJob(final GnucashCustomerJob job) {
		setGenerJob(job);
	}

	/**
	 * @see GnucashWritableGenerInvoice#getWritableEntryById(java.lang.String)
	 */
	public GnucashWritableCustomerInvoiceEntry getWritableEntryById(final String id) {
		return (GnucashWritableCustomerInvoiceEntry) super.getGenerInvcEntryById(id);
	}

}

