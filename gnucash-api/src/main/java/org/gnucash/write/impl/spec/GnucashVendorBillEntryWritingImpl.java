package org.gnucash.write.impl.spec;

import java.beans.PropertyChangeSupport;
import java.util.Date;

import org.gnucash.Const;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.IllegalGenerInvoiceEntryActionException;
import org.gnucash.read.aux.TaxTable;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
import org.gnucash.write.impl.GnucashFileWritingImpl;
import org.gnucash.write.GnucashWritableFile;

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
public class GnucashVendorBillEntryWritingImpl extends GnucashGenerInvoiceEntryImpl 
                                                    implements GnucashWritableCustomerInvoiceEntry 
{

	/**
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GncV2.GncBook.GncGncEntry, GnucashFileImpl)
	 */
	public GnucashVendorBillEntryWritingImpl(
		final GncV2.GncBook.GncGncEntry jwsdpPeer, 
		final GnucashFileWritingImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param invoice   tne invoice this entry shall belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GnucashGenerInvoice, GncV2.GncBook.GncGncEntry)
	 */
	public GnucashVendorBillEntryWritingImpl(
		final GnucashCustomerInvoiceWritingImpl invoice,
		final GncV2.GncBook.GncGncEntry jwsdpPeer) {
		super(invoice, jwsdpPeer);
		
		this.invoice = invoice;
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
		GnucashAccount account = invoice.getFile().getAccountByName("Umsatzerl�se 16% USt");
		if (account == null) {
			account = invoice.getFile().getAccountByName("Umsatzerl�se 19% USt"); // national tax-rate has changed
		}
		if (account == null) {
			throw new IllegalStateException("Cannot file account 'Umsatzerl�se 16% USt' from SKR04!");
		}

		return createInvoiceEntry(invoice, account, quantity, price);
	}

	public GnucashVendorBillEntryWritingImpl(
		final GnucashCustomerInvoiceWritingImpl invoice,
		final FixedPointNumber quantity,
		final FixedPointNumber price) throws WrongInvoiceTypeException {
		super(invoice, createSKR03_16PercentInvoiceEntry(invoice, quantity, price));
		
		invoice.addInvcEntry(this);
		this.invoice = invoice;
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
	 */
	public GnucashVendorBillEntryWritingImpl(final GnucashCustomerInvoiceWritingImpl invoice,
                                                  final GnucashAccount account,
                                                  final FixedPointNumber quantity,
                                                  final FixedPointNumber price) throws WrongInvoiceTypeException {
		super(invoice, createInvoiceEntry(invoice, account, quantity, price));
		
		invoice.addInvcEntry(this);
		this.invoice = invoice;
	}

	/**
	 * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashCustomerInvoiceWritingImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
	 */
	protected static GncV2.GncBook.GncGncEntry createInvoiceEntry(
			final GnucashCustomerInvoiceWritingImpl invoice,
			final GnucashAccount account,
			final FixedPointNumber quantity,
			final FixedPointNumber price) {

		//      TODO: keep count-data in file intact <gnc:count-data cd:type="gnc:GncEntry">18</gnc:count-data>

		if (!invoice.isModifiable()) {
			throw new IllegalArgumentException("The given invoice has payments and is"
					+ " thus not modifiable");
		}

		GnucashFileWritingImpl gnucashFileWritingImpl = (GnucashFileWritingImpl) invoice.getFile();
		ObjectFactory factory = gnucashFileWritingImpl.getObjectFactory();

		GncV2.GncBook.GncGncEntry entry = gnucashFileWritingImpl.createGncGncEntryType();

		{
			GncV2.GncBook.GncGncEntry.EntryGuid guid = factory.createGncV2GncBookGncGncEntryEntryGuid();
			guid.setType("guid");
			guid.setValue((gnucashFileWritingImpl).createGUID());
			entry.setEntryGuid(guid);
		}

		entry.setEntryAction(ACTION_HOURS);
		{
			GncV2.GncBook.GncGncEntry.EntryDate entryDate = factory.createGncV2GncBookGncGncEntryEntryDate();
			entryDate.setTsDate(ENTRY_DATE_FORMAT.format(new Date()));
			entry.setEntryDate(entryDate);
		}
		entry.setEntryDescription("no description");
		{
			GncV2.GncBook.GncGncEntry.EntryEntered entered = factory.createGncV2GncBookGncGncEntryEntryEntered();
			entered.setTsDate(ENTRY_DATE_FORMAT.format(new Date()));
			entry.setEntryEntered(entered);
		}
		{
			GncV2.GncBook.GncGncEntry.EntryIAcct iacct = factory.createGncV2GncBookGncGncEntryEntryIAcct();
			iacct.setType("guid");
			iacct.setValue(account.getId());
			entry.setEntryIAcct(iacct);
		}
		entry.setEntryIDiscHow("PRETAX");
		entry.setEntryIDiscType("PERCENT");
		{

			GncV2.GncBook.GncGncEntry.EntryInvoice inv = factory.createGncV2GncBookGncGncEntryEntryInvoice();
			inv.setType("guid");
			inv.setValue(invoice.getId());
			entry.setEntryInvoice(inv);
		}
		entry.setEntryIPrice(price.toGnucashString());
		entry.setEntryITaxable(1);
		entry.setEntryITaxincluded(0);
		{
			//TODO: use not the first but the default taxtable
			GncV2.GncBook.GncGncEntry.EntryITaxtable taxtableref = factory.createGncV2GncBookGncGncEntryEntryITaxtable();
			taxtableref.setType("guid");

			TaxTable taxTable = null;
			// ::TODO
			// GnucashCustomer customer = invoice.getCustomer();
			// if (customer != null) {
			// 	taxTable = customer.getCustomerTaxTable();
			// }

			// use first tax-table found
			if (taxTable == null) {
				taxTable = invoice.getFile().getTaxTables().iterator().next();
			}

            /*GncV2Type.GncBookType.GncGncTaxTableType taxtable = (GncV2Type.GncBookType.GncGncTaxTableType)
				((GnucashFileImpl) invoice.getFile()).getRootElement().getGncBook().getGncGncTaxTable().get(0);

            taxtableref.setValue(taxtable.getTaxtableGuid().getValue());*/
			taxtableref.setValue(taxTable.getId());
			entry.setEntryITaxtable(taxtableref);
		}

		entry.setEntryQty(quantity.toGnucashString());
		entry.setVersion(Const.XML_FORMAT_VERSION);

		invoice.getFile().getRootElement().getGncBook().getBookElements().add(entry);
		invoice.getFile().setModified(true);

		return entry;
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
	public void setTaxTable(TaxTable taxTab)
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
