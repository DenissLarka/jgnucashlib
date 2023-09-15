package org.gnucash.read.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustVendInvoiceEntry;
import org.gnucash.read.impl.GnucashCustVendInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorBillEntryImpl extends GnucashCustVendInvoiceEntryImpl
                                        implements GnucashVendorBillEntry 
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorBillEntryImpl.class);

  public GnucashVendorBillEntryImpl(
          final GnucashVendorBill invoice,
          final GncV2.GncBook.GncGncEntry peer) 
  {
    super(invoice, peer, true);
  }

  public GnucashVendorBillEntryImpl(
          final GnucashCustVendInvoice invoice,
          final GncV2.GncBook.GncGncEntry peer) throws WrongInvoiceTypeException 
  {
    super(invoice, peer, true);

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if ( ! invoice.getType().equals(GnucashCustVendInvoice.TYPE_VENDOR) )
      throw new WrongInvoiceTypeException();
  }

  public GnucashVendorBillEntryImpl(final GncV2.GncBook.GncGncEntry peer, final GnucashFileImpl gncFile) 
  {
    super(peer, gncFile);
  }

  public GnucashVendorBillEntryImpl(final GnucashCustVendInvoiceEntry entry) throws WrongInvoiceTypeException
  {
    super(entry.getCustVendInvoice(), entry.getJwsdpPeer(), false);

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if ( ! entry.getType().equals(GnucashCustVendInvoice.TYPE_VENDOR) )
      throw new WrongInvoiceTypeException();
  }

  public GnucashVendorBillEntryImpl(final GnucashVendorBillEntry entry)
  {
    super(entry.getCustVendInvoice(), entry.getJwsdpPeer(), false);
  }

  // ---------------------------------------------------------------

  public String getBillID()
  {
    return getCustVendInvoiceID();
  }
  
  public GnucashVendorBill getBill() throws WrongInvoiceTypeException
  {
    if ( myInvoice == null )
    {
      myInvoice = getCustVendInvoice();
      if ( ! myInvoice.getType().equals(GnucashCustVendInvoice.TYPE_VENDOR) )
        throw new WrongInvoiceTypeException();
        
      if ( myInvoice == null )
      {
        throw new IllegalStateException(
            "No vendor bill with id '" + getBillID()
            + "' for bill entry with id '" + getId() + "'");
      }
    }
    
    return new GnucashVendorBillImpl(myInvoice);
  }

  // ---------------------------------------------------------------

  @Override
  public FixedPointNumber getInvcPrice() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcPriceFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  // ---------------------------------------------------------------

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[GnucashVendorBillEntryImpl:");
    buffer.append(" id: ");
    buffer.append(getId());
    buffer.append(" bill-id: ");
    buffer.append(getBillID());
    //      buffer.append(" bill: ");
    //      GnucashVendorBill bill = getBill();
    //      buffer.append(invoice==null?"null":bill.getName());
    buffer.append(" description: '");
    buffer.append(getDescription() + "'");
    buffer.append(" action: '");
    buffer.append(getAction() + "'");
    buffer.append(" price: ");
    try
    {
      buffer.append(getBillPrice());
    }
    catch (WrongInvoiceTypeException e)
    {
      buffer.append("ERROR");
    }
    buffer.append(" quantity: ");
    buffer.append(getQuantity());
    buffer.append("]");
    return buffer.toString();
  }
}
