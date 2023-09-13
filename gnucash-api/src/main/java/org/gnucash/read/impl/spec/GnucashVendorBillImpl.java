package org.gnucash.read.impl.spec;

import java.util.Collection;
import java.util.HashSet;

import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustVendInvoiceEntry;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.GnucashCustVendInvoiceImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public class GnucashVendorBillImpl extends GnucashCustVendInvoiceImpl
                                      implements GnucashVendorBill
{
  public GnucashVendorBillImpl(final GncGncInvoice peer, final GnucashFile gncFile)
  {
    super(peer, gncFile);
  }

  public GnucashVendorBillImpl(final GnucashCustVendInvoice invc) throws WrongInvoiceTypeException
  {
    super(invc.getJwsdpPeer(), invc.getFile());

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if (! invc.getOwnerType().equals(GnucashCustVendInvoice.TYPE_VENDOR) )
      throw new WrongInvoiceTypeException();
    
    for ( GnucashCustVendInvoiceEntry entry : invc.getCustVendInvcEntries() )
    {
      addEntry(new GnucashVendorBillEntryImpl(entry));
    }
  }
  
  // -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public String getVendorId(GnucashCustVendInvoice.ReadVariant readVar) {
    return getOwnerId(readVar);
  }

  @Override
  public GnucashVendor getVendor() {
    return getVendorDirectly();
  }

  public GnucashVendor getVendorViaJob() {
    assert getJob().getOwnerType().equals(GnucashJob.TYPE_VENDOR);
    return ((GnucashVendorJobImpl) getJob()).getVendor();
  }

  public GnucashVendor getVendorDirectly() {
    assert getJwsdpPeer().getInvoiceOwner().getOwnerType().equals(GnucashCustVendInvoice.TYPE_VENDOR);
    return file.getVendorByID(getJwsdpPeer().getInvoiceOwner().getOwnerId().getValue());
  }

  // ---------------------------------------------------------------

  @Override
  public GnucashVendorBillEntry getEntryById(String id) throws WrongInvoiceTypeException
  {
    return new GnucashVendorBillEntryImpl(getCustVendInvcEntryById(id));
  }

  @Override
  public Collection<GnucashVendorBillEntry> getEntries() throws WrongInvoiceTypeException
  {
    Collection<GnucashVendorBillEntry> castEntries = new HashSet<GnucashVendorBillEntry>();
    
    for ( GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries() )
    {
      if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_VENDOR) )
      {
        castEntries.add(new GnucashVendorBillEntryImpl(entry));
      }
    }
    
    return castEntries;
  }

  @Override
  public void addEntry(final GnucashVendorBillEntry entry)
  {
    addCustVendInvcEntry(entry);
  }

  // -----------------------------------------------------------------

  @Override
  public FixedPointNumber getAmountUnpaidWithTaxes() throws WrongInvoiceTypeException
  {
    return getBillAmountUnpaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithTaxes() throws WrongInvoiceTypeException
  {
    return getBillAmountPaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithoutTaxes() throws WrongInvoiceTypeException
  {
    return getBillAmountPaidWithoutTaxes();
  }

  @Override
  public FixedPointNumber getAmountWithTaxes() throws WrongInvoiceTypeException
  {
    return getBillAmountWithTaxes();
  }
  
  @Override
  public FixedPointNumber getAmountWithoutTaxes() throws WrongInvoiceTypeException
  {
    return getBillAmountWithoutTaxes();
  }

  @Override
  public String getAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getBillAmountUnpaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getBillAmountPaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getBillAmountPaidWithoutTaxesFormatted();
  }

  @Override
  public String getAmountWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getBillAmountWithTaxesFormatted();
  }

  @Override
  public String getAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getBillAmountWithoutTaxesFormatted();
  }
  
  // ------------------------------
  
  @Override
  public boolean isFullyPaid() throws WrongInvoiceTypeException
  {
    return isBillFullyPaid();
  }
  
  @Override
  public boolean isNotFullyPaid() throws WrongInvoiceTypeException
  {
    return isNotBillFullyPaid();
  }
  
  // ------------------------------

  @Override
  public FixedPointNumber getInvcAmountUnpaidWithTaxes() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getInvcAmountPaidWithTaxes() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getInvcAmountPaidWithoutTaxes() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getInvcAmountWithTaxes() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }
  
  @Override
  public FixedPointNumber getInvcAmountWithoutTaxes() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }
  
  // ------------------------------

  @Override
  public boolean isInvcFullyPaid() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public boolean isNotInvcFullyPaid() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }
  
  // -----------------------------------------------------------------

  @Override
  public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[GnucashVendorBillImpl:");
      buffer.append(" id: ");
      buffer.append(getId());
      buffer.append(" vendor-id (dir.): ");
      buffer.append(getVendorId(GnucashCustVendInvoice.ReadVariant.DIRECT));
      buffer.append(" bill-number: '");
      buffer.append(getNumber() + "'");
      buffer.append(" description: '");
      buffer.append(getDescription() + "'");
      buffer.append(" #entries: ");
      try {
        buffer.append(getEntries().size());
      }
      catch (WrongInvoiceTypeException e) {
        buffer.append("ERROR");
      }
      buffer.append(" date-opened: ");
      try {
        buffer.append(getDateOpened().toLocalDate().format(DATE_OPENED_FORMAT_PRINT));
      }
      catch (Exception e) {
        buffer.append(getDateOpened().toLocalDate().toString());
      }
      buffer.append("]");
      return buffer.toString();
  }
  
}
