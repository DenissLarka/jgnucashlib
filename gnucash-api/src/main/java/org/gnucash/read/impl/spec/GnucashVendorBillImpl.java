package org.gnucash.read.impl.spec;

import java.util.Collection;
import java.util.HashSet;

import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
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
  public GnucashVendorBillImpl(GncGncInvoice peer, GnucashFile gncFile)
  {
    super(peer, gncFile);
  }

  public GnucashVendorBillImpl(GnucashCustVendInvoice invc) throws WrongInvoiceTypeException
  {
    super(invc.getPeer(), invc.getFile());

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if (! invc.getOwnerType().equals(GnucashCustVendInvoice.TYPE_VENDOR) )
      throw new WrongInvoiceTypeException();
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
  public GnucashVendorBillEntry getEntryById(String id)
  {
    return new GnucashVendorBillEntryImpl(getCustVendInvcEntryById(id));
  }

  @Override
  public Collection<GnucashVendorBillEntry> getEntries()
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
      buffer.append(entries.size());
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
