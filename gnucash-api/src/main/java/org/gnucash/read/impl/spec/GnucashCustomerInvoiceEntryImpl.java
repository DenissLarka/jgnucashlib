package org.gnucash.read.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustVendInvoiceEntry;
import org.gnucash.read.impl.GnucashCustVendInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashCustVendInvoiceImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerInvoiceEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public class GnucashCustomerInvoiceEntryImpl extends GnucashCustVendInvoiceEntryImpl
                                             implements GnucashCustomerInvoiceEntry 
{
  public GnucashCustomerInvoiceEntryImpl(
          final GnucashCustomerInvoice invoice,
          final GncV2.GncBook.GncGncEntry peer) {
      super(invoice, peer);
  }

  public GnucashCustomerInvoiceEntryImpl(
          final GnucashCustVendInvoice invoice,
          final GncV2.GncBook.GncGncEntry peer) throws WrongInvoiceTypeException {
      super(invoice, peer);

      if ( ! invoice.getType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) )
        throw new WrongInvoiceTypeException();
  }

  public GnucashCustomerInvoiceEntryImpl(final GncV2.GncBook.GncGncEntry peer, final GnucashFileImpl gncFile) {
      super(peer, gncFile);
  }

  public GnucashCustomerInvoiceEntryImpl(
      final GnucashCustVendInvoiceEntry entry)
  {
    super(entry.getCustVendInvoice(), entry.getJwsdpPeer());
  }

  // ---------------------------------------------------------------

  public String getInvoiceID()
  {
    return getCustVendInvoiceID();
  }
  
  public GnucashCustomerInvoice getInvoice() throws WrongInvoiceTypeException
  {
    if ( myInvoice == null )
    {
      myInvoice = getCustVendInvoice();
      if ( ! myInvoice.getType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) )
        throw new WrongInvoiceTypeException();
        
      if ( myInvoice == null )
      {
        throw new IllegalStateException(
            "No customer invoice with id '" + getInvoiceID()
            + "' for invoice entry with id '" + getId() + "'");
      }
    }
    
    return new GnucashCustomerInvoiceImpl(myInvoice);
  }

  // ---------------------------------------------------------------

  @Override
  public FixedPointNumber getBillPrice() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getBillPriceFormatet() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  // ---------------------------------------------------------------

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[GnucashCustomerInvoiceEntryImpl:");
    buffer.append(" id: ");
    buffer.append(getId());
    buffer.append(" invoice-id: ");
    buffer.append(getInvoiceID());
    //      buffer.append(" invoice: ");
    //      GnucashCustomerInvoice invc = getInvoice();
    //      buffer.append(invoice==null?"null":invoice.getName());
    buffer.append(" description: '");
    buffer.append(getDescription() + "'");
    buffer.append(" action: '");
    buffer.append(getAction() + "'");
    buffer.append(" price: ");
    try
    {
      buffer.append(getInvcPrice());
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
