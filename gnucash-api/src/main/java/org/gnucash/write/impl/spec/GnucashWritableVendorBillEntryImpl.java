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
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.GnucashWritableGenerInvoiceEntryImpl;
import org.gnucash.write.spec.GnucashWritableVendorBillEntry;

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
public class GnucashWritableVendorBillEntryImpl extends GnucashWritableGenerInvoiceEntryImpl 
                                                implements GnucashWritableVendorBillEntry
{

	/**
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GncV2.GncBook.GncGncEntry, GnucashFileImpl)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableVendorBillEntryImpl(
		final GncV2.GncBook.GncGncEntry jwsdpPeer, 
		final GnucashWritableFileImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param invc   tne invoice this entry shall belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GnucashGenerInvoice, GncV2.GncBook.GncGncEntry)
	 */
	@SuppressWarnings("exports")
	public GnucashWritableVendorBillEntryImpl(
		final GnucashWritableCustomerInvoiceImpl invc,
		final GncV2.GncBook.GncGncEntry jwsdpPeer) {
		super(invc, jwsdpPeer);
		
		this.myInvoice = invc;
	}

	/**
	 * Create a taxable invoiceEntry.
	 * (It has the taxtable of the customer with a fallback
	 * to the first taxtable found assigned)
	 *
	 * @param bll  the invoice to add this split to
	 * @param account  the income-account the money comes from
	 * @param quantity see ${@link GnucashGenerInvoiceEntry#getQuantity()}
	 * @param price    see ${@link GnucashGenerInvoiceEntry#getInvcPrice()}}
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashWritableVendorBillEntryImpl(
		final GnucashWritableVendorBillImpl bll,
		final GnucashAccount account,
		final FixedPointNumber quantity,
		final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		super(bll, 
		      createVendBillEntry_int(bll, account, quantity, price));
		
		// Caution: Call addBillEntry one level above now
		// (GnucashWritableVendorBillImpl.createVendBillEntry)
		// invc.addBillEntry(this);
		this.myInvoice = bll;
	}

	public GnucashWritableVendorBillEntryImpl(final GnucashGenerInvoiceEntry entry) {
	    super(entry.getJwsdpPeer(), (GnucashWritableFileImpl) entry.getGenerInvoice().getFile());
	}

	public GnucashWritableVendorBillEntryImpl(final GnucashVendorBillEntry entry) {
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
	    setBillTaxable(val);
	}

	@Override
	public void setTaxTable(GCshTaxTable taxTab)
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    setBillTaxTable(taxTab);
	}

	@Override
	public void setPrice(String price)
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    setBillPrice(price);
	}

	@Override
	public void setPrice(FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	    setBillPrice(price);
	}

}
