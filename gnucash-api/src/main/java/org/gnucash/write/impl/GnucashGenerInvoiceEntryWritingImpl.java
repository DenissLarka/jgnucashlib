package org.gnucash.write.impl;

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
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.GnucashWritableGenerInvoice;
import org.gnucash.write.GnucashWritableGenerInvoiceEntry;
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
public class GnucashGenerInvoiceEntryWritingImpl extends GnucashGenerInvoiceEntryImpl 
                                                 implements GnucashWritableGenerInvoiceEntry 
{

	/**
	 * Our helper to implement the GnucashWritableObject-interface.
	 */
	private final GnucashWritableObjectHelper helper = new GnucashWritableObjectHelper(this);

	/**
	 * {@inheritDoc}
	 */
	public void setUserDefinedAttribute(final String name, final String value) {
		helper.setUserDefinedAttribute(name, value);
	}

	/**
	 * @param file      the file we belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GncV2.GncBook.GncGncEntry, GnucashFileImpl)
	 */
	public GnucashGenerInvoiceEntryWritingImpl(final GncV2.GncBook.GncGncEntry jwsdpPeer, final GnucashFileWritingImpl file) {
		super(jwsdpPeer, file);
	}

	/**
	 * @param invoice   tne invoice this entry shall belong to
	 * @param jwsdpPeer the JWSDP-object we are facading.
	 * @see GnucashGenerInvoiceEntryImpl#GnucashInvoiceEntryImpl(GnucashGenerInvoice, GncV2.GncBook.GncGncEntry)
	 */
	public GnucashGenerInvoiceEntryWritingImpl(final GnucashGenerInvoiceWritingImpl invoice,
			final GncV2.GncBook.GncGncEntry jwsdpPeer) {
		super(invoice, jwsdpPeer, true);
		
		this.invoice = invoice;
	}

	/**
	 * @param invoice
	 * @param quantity
	 * @param price
	 * @return
	 */
	protected static GncV2.GncBook.GncGncEntry createSKR03_16PercentInvoiceEntry(
			final GnucashGenerInvoiceWritingImpl invoice,
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

	public GnucashGenerInvoiceEntryWritingImpl(final GnucashGenerInvoiceWritingImpl invoice,
			final FixedPointNumber quantity,
			final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		super(invoice, createSKR03_16PercentInvoiceEntry(invoice, quantity, price), true);
		
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
	 * @throws NoTaxTableFoundException 
	 */
	public GnucashGenerInvoiceEntryWritingImpl(final GnucashGenerInvoiceWritingImpl invoice,
                                                   final GnucashAccount account,
                                                   final FixedPointNumber quantity,
                                                   final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		super(invoice, createInvoiceEntry(invoice, account, quantity, price), true);
		
		invoice.addInvcEntry(this);
		this.invoice = invoice;
	}

	/**
	 * @see {@link #GnucashInvoiceEntryWritingImpl(GnucashGenerInvoiceWritingImpl, GnucashAccount, FixedPointNumber, FixedPointNumber)}
	 */
	protected static GncV2.GncBook.GncGncEntry createInvoiceEntry(
			final GnucashGenerInvoiceWritingImpl invoice,
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

			GCshTaxTable taxTable = null;
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

    /*public GncV2Type.GncBookType.GncGncEntryType getJwsdpPeer() {
        return super.getJwsdpPeer();
    }*/

	/**
	 * @see {@link #getGenerInvoice()}
	 */
	private GnucashWritableGenerInvoice invoice;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GnucashWritableGenerInvoice getGenerInvoice() {
		if (invoice == null) {
			invoice = (GnucashWritableGenerInvoice) super.getGenerInvoice();
		}
		return invoice;

	}

	/**
	 * Set the description-text.
	 *
	 * @param desc the new description
	 */
	public void setDescription(final String desc) {
		if (desc == null) {
			throw new IllegalArgumentException("null description given! Please use the empty string instead of null for an empty description");
		}
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}
		Object old = getDescription();
		getJwsdpPeer().setEntryDescription(desc);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("description", old, desc);
		}
	}

	// -----------------------------------------------------------

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashGenerInvoiceEntry#isInvcTaxable()
	 */
	@Override
	public void setInvcTaxable(final boolean val) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).subtractInvcEntry(this);
		if (val) {
			getJwsdpPeer().setEntryITaxable(1);
		} else {
			getJwsdpPeer().setEntryITaxable(0);
		}
		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).addInvcEntry(this);

	}


	/**
	 * {@inheritDoc}
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public void setInvcTaxTable(final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	    	if ( ! getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) )
	    	    throw new WrongInvoiceTypeException();
	    
		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).subtractInvcEntry(this);

		super.setInvcTaxtable(tax);
		if (tax == null) {
			getJwsdpPeer().setEntryITaxable(0);
		} else {
			getJwsdpPeer().setEntryITaxable(1);
			if (getJwsdpPeer().getEntryITaxtable() == null) {
				getJwsdpPeer().setEntryITaxtable(
						((GnucashFileWritingImpl) getGenerInvoice().getFile())
								.getObjectFactory()
								.createGncV2GncBookGncGncEntryEntryITaxtable());
				getJwsdpPeer().getEntryITaxtable().setValue("guid");
			}
			getJwsdpPeer().getEntryITaxtable().setValue(tax.getId());
		}

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).addInvcEntry(this);

	}
	
	// ------------------------

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashGenerInvoiceEntry#isInvcTaxable()
	 */
	public void setBillTaxable(final boolean val) throws WrongInvoiceTypeException, NoTaxTableFoundException {

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).subtractBillEntry(this);
		if (val) {
			getJwsdpPeer().setEntryBTaxable(1);
		} else {
			getJwsdpPeer().setEntryBTaxable(0);
		}
		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).addBillEntry(this);

	}

	/**
	 * {@inheritDoc}
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 */
	public void setBillTaxTable(final GCshTaxTable tax) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	    	if ( ! getType().equals(GnucashGenerInvoice.TYPE_VENDOR) )
	    	    throw new WrongInvoiceTypeException();

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).subtractInvcEntry(this);

		super.setBillTaxtable(tax);
		if (tax == null) {
			getJwsdpPeer().setEntryBTaxable(0);
		} else {
			getJwsdpPeer().setEntryBTaxable(1);
			if (getJwsdpPeer().getEntryBTaxtable() == null) {
				getJwsdpPeer().setEntryBTaxtable(
						((GnucashFileWritingImpl) getGenerInvoice().getFile())
								.getObjectFactory()
								.createGncV2GncBookGncGncEntryEntryBTaxtable());
				getJwsdpPeer().getEntryBTaxtable().setValue("guid");
			}
			getJwsdpPeer().getEntryBTaxtable().setValue(tax.getId());
		}

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).addBillEntry(this);

	}
	
	// -----------------------------------------------------------

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NumberFormatException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	public void setInvcPrice(final String n) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
		this.setInvcPrice(new FixedPointNumber(n));
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	public void setInvcPrice(final FixedPointNumber price) throws WrongInvoiceTypeException, NoTaxTableFoundException {
	    	if ( ! getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) )
	    	    throw new WrongInvoiceTypeException();
	    
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This customer invoice has payments and is not modifiable!");
		}

		Object old = getInvcPrice();

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).subtractInvcEntry(this);
		getJwsdpPeer().setEntryIPrice(price.toGnucashString());
		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).addInvcEntry(this);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("price", old, price);
		}

	}

	public void setInvcPriceFormatted(final String n) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
		this.setInvcPrice(new FixedPointNumber(n));
	}

	// ------------------------

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NumberFormatException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	@Override
	public void setBillPrice(final String n) 
		throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
		this.setBillPrice(new FixedPointNumber(n));
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#setInvcPrice(FixedPointNumber)
	 */
	@Override
	public void setBillPrice(final FixedPointNumber price) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
	    	if ( ! getType().equals(GnucashGenerInvoice.TYPE_VENDOR) )
	    	    throw new WrongInvoiceTypeException();
	    
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This vendor bill has payments and is not modifiable!");
		}

		Object old = getBillPrice();

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).subtractBillEntry(this);
		getJwsdpPeer().setEntryBPrice(price.toGnucashString());
		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).addBillEntry(this);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("price", old, price);
		}

	}

	public void setBillPriceFormatted(final String n) throws NumberFormatException, WrongInvoiceTypeException, NoTaxTableFoundException {
		this.setBillPrice(new FixedPointNumber(n));
	}
	
	// -----------------------------------------------------------

	/**
	 * @see GnucashWritableGenerInvoiceEntry#setAction(java.lang.String)
	 */
	public void setAction(final String action) {
	  if ( action != null &&
           ! action.equals(ACTION_JOB) &&
           ! action.equals(ACTION_MATERIAL) &&
           ! action.equals(ACTION_HOURS) ) {
	    throw new IllegalGenerInvoiceEntryActionException();
	  }
	  
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}

		Object old = getAction();
		getJwsdpPeer().setEntryAction(action);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("action", old, action);
		}

	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#setQuantity(FixedPointNumber)
	 */
	public void setQuantity(final String n) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		FixedPointNumber fp = new FixedPointNumber(n);
		System.out.println("DEBUG: GnucashInvoiceEntryWritingImpl.setQuantity('" + n + "') - setting to " + fp.toGnucashString());
		this.setQuantity(fp);
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#setQuantityFormatted(String)
	 */
	public void setQuantityFormatted(final String n) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		FixedPointNumber fp = new FixedPointNumber(n);
		System.out.println("DEBUG: GnucashInvoiceEntryWritingImpl.setQuantityFormatted('" + n + "') - setting to " + fp.toGnucashString());
		this.setQuantity(fp);
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#setQuantity(FixedPointNumber)
	 */
	public void setQuantity(final FixedPointNumber quantity) throws WrongInvoiceTypeException, NoTaxTableFoundException {
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}

		Object old = getQuantity();

		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).subtractInvcEntry(this);
		getJwsdpPeer().setEntryQty(quantity.toGnucashString());
		((GnucashGenerInvoiceWritingImpl) getGenerInvoice()).addInvcEntry(this);

		PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
		if (propertyChangeSupport != null) {
			propertyChangeSupport.firePropertyChange("quantity", old, quantity);
		}

	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @throws NoTaxTableFoundException 
	 * @see GnucashWritableGenerInvoiceEntry#remove()
	 */
	public void remove() throws WrongInvoiceTypeException, NoTaxTableFoundException {
		if (!this.getGenerInvoice().isModifiable()) {
			throw new IllegalStateException("This Invoice has payments and is not modifiable!");
		}
		GnucashGenerInvoiceWritingImpl gnucashInvoiceWritingImpl = ((GnucashGenerInvoiceWritingImpl) getGenerInvoice());
		gnucashInvoiceWritingImpl.removeInvcEntry(this);
		gnucashInvoiceWritingImpl.getFile().getRootElement().getGncBook().getBookElements().remove(this.getJwsdpPeer());
		((GnucashFileWritingImpl) gnucashInvoiceWritingImpl.getFile()).decrementCountDataFor("gnc:GncEntry");
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashWritableFile getWritableGnucashFile() {
		return (GnucashWritableFile) super.getGnucashFile();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GnucashWritableFile getGnucashFile() {
		return (GnucashWritableFile) super.getGnucashFile();
	}

}
