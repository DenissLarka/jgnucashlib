package org.gnucash.write.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.GnucashJobInvoiceEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceEntryImpl;
import org.gnucash.write.spec.GnucashWritableJobInvoiceEntry;

/**
 * Additional supported properties for PropertyChangeListeners:
 * <ul>
 * <li>description</li>
 * <li>price</li>
 * <li>quantity</li>
 * <li>action</li>
 * </ul>
 * Entry-Line in an invoice that can be created or removed.
 */
public class GnucashWritableJobInvoiceEntryImpl extends GnucashWritableGenerInvoiceEntryImpl 
                                                implements GnucashWritableJobInvoiceEntry
{

	/**
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GncV2.GncBook.GncGncEntry, GnucashFileImpl)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableJobInvoiceEntryImpl(
		final GncV2.GncBook.GncGncEntry jwsdpPeer, 
		final GnucashWritableFileImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param invoice   tne invoice this entry shall belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GnucashGenerInvoice, GncV2.GncBook.GncGncEntry)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableJobInvoiceEntryImpl(
		final GnucashWritableJobInvoiceImpl invoice,
		final GncV2.GncBook.GncGncEntry jwsdpPeer) {
		super(invoice, jwsdpPeer);
		
		this.myInvoice = invoice;
	}

	/**
	 * Create a taxable invoiceEntry.
	 * (It has the taxtable of the job with a fallback
	 * to the first taxtable found assigned)
	 *
	 * @param invoice  the invoice to add this split to
	 * @param account  the income-account the money comes from
	 * @param quantity see ${@link GnucashGenerInvoiceEntry#getQuantity()}
	 * @param price    see ${@link GnucashGenerInvoiceEntry#getInvcPrice()}}
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableJobInvoiceEntryImpl(
		final GnucashWritableJobInvoiceImpl invoice,
		final GnucashAccount account,
		final FixedPointNumber quantity,
		final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		super(invoice, createJobInvoiceEntry(invoice, account, quantity, price));
		
		invoice.addJobEntry(this);
		this.myInvoice = invoice;
	}

	public GnucashWritableJobInvoiceEntryImpl(final GnucashGenerInvoiceEntry entry) {
	    super(entry.getJwsdpPeer(), (GnucashWritableFileImpl) entry.getGenerInvoice().getFile());
	}

	public GnucashWritableJobInvoiceEntryImpl(final GnucashJobInvoiceEntry entry) {
	    super(entry.getJwsdpPeer(), (GnucashWritableFileImpl) entry.getGenerInvoice().getFile());
	}

	// -----------------------------------------------------------

	@Override
	public GnucashWritableFile getWritableGnucashFile() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public void setUserDefinedAttribute(String name, String value) {
	    // TODO Auto-generated method stub
	    
	}

	// -----------------------------------------------------------

	@Override
	public void setTaxable(boolean val)
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    setJobTaxable(val);
	}

	@Override
	public void setTaxTable(GCshTaxTable taxTab)
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    setJobTaxTable(taxTab);
	}

	@Override
	public void setPrice(String price)
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    setJobPrice(price);
	}

	@Override
	public void setPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	    setJobPrice(price);
	}

}
