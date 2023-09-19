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
import org.gnucash.read.spec.GnucashCustomerInvoiceEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceEntryImpl;
import org.gnucash.write.spec.GnucashWritableCustomerInvoiceEntry;

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
public class GnucashCustomerInvoiceEntryWritingImpl extends GnucashWritableGenerInvoiceEntryImpl 
                                                    implements GnucashWritableCustomerInvoiceEntry
{

	/**
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GncV2.GncBook.GncGncEntry, GnucashFileImpl)
	 */
	public GnucashCustomerInvoiceEntryWritingImpl(
		final GncV2.GncBook.GncGncEntry jwsdpPeer, 
		final GnucashWritableFileImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param invoice   tne invoice this entry shall belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GnucashGenerInvoice, GncV2.GncBook.GncGncEntry)
	 */
	public GnucashCustomerInvoiceEntryWritingImpl(
		final GnucashCustomerInvoiceWritingImpl invoice,
		final GncV2.GncBook.GncGncEntry jwsdpPeer) {
		super(invoice, jwsdpPeer);
		
		this.myInvoice = invoice;
	}

	/**
	 * @param invoice
	 * @param quantity
	 * @param price
	 * @return
	 */
	private static GncV2.GncBook.GncGncEntry createSKR03_16PercentInvoiceEntry(
			final GnucashCustomerInvoiceWritingImpl invoice,
			final FixedPointNumber quantity,
			final FixedPointNumber price) {
		return GnucashWritableGenerInvoiceEntryImpl.createSKR03_16PercentInvoiceEntry(invoice, quantity, price);
	}

	public GnucashCustomerInvoiceEntryWritingImpl(
		final GnucashCustomerInvoiceWritingImpl invoice,
		final FixedPointNumber quantity,
		final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		super(invoice, createSKR03_16PercentInvoiceEntry(invoice, quantity, price));
		
		invoice.addInvcEntry(this);
		this.myInvoice = invoice;
	}

	/**
	 * Create a taxable invoiceEntry.
	 * (It has the taxtable of the customer with a fallback
	 * to the first taxtable found assigned)
	 *
	 * @param invoice  the invoice to add this split to
	 * @param account  the income-account the money comes from
	 * @param quantity see ${@link GnucashGenerInvoiceEntry#getQuantity()}
	 * @param price    see ${@link GnucashGenerInvoiceEntry#getInvcPrice()}}
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashCustomerInvoiceEntryWritingImpl(
		final GnucashCustomerInvoiceWritingImpl invoice,
		final GnucashAccount account,
		final FixedPointNumber quantity,
		final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		super(invoice, createInvoiceEntry(invoice, account, quantity, price));
		
		invoice.addInvcEntry(this);
		this.myInvoice = invoice;
	}

	public GnucashCustomerInvoiceEntryWritingImpl(final GnucashCustomerInvoiceEntry entry) {
	    super(entry.getJwsdpPeer(), entry.getGnucashFile());
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
	    setInvcTaxable(val);
	}

	@Override
	public void setTaxTable(GCshTaxTable taxTab)
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    setInvcTaxTable(taxTab);
	}

	@Override
	public void setPrice(String price)
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    setInvcPrice(price);
	}

	@Override
	public void setPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	    setInvcPrice(price);
	}

}
