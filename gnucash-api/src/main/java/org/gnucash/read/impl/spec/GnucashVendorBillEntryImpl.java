package org.gnucash.read.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorBillEntryImpl extends GnucashGenerInvoiceEntryImpl
                                        implements GnucashVendorBillEntry 
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorBillEntryImpl.class);

  @SuppressWarnings("exports")
  public GnucashVendorBillEntryImpl(
          final GnucashVendorBill invoice,
          final GncV2.GncBook.GncGncEntry peer) 
  {
    super(invoice, peer, true);
  }

  @SuppressWarnings("exports")
  public GnucashVendorBillEntryImpl(
          final GnucashGenerInvoice invoice,
          final GncV2.GncBook.GncGncEntry peer) throws WrongInvoiceTypeException 
  {
    super(invoice, peer, true);

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if ( ! invoice.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) )
      throw new WrongInvoiceTypeException();
  }

  @SuppressWarnings("exports")
  public GnucashVendorBillEntryImpl(final GncV2.GncBook.GncGncEntry peer, final GnucashFileImpl gncFile) 
  {
    super(peer, gncFile);
  }

  public GnucashVendorBillEntryImpl(final GnucashGenerInvoiceEntry entry) throws WrongInvoiceTypeException
  {
    super(entry.getGenerInvoice(), entry.getJwsdpPeer(), false);

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if ( ! entry.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) )
      throw new WrongInvoiceTypeException();
  }

  public GnucashVendorBillEntryImpl(final GnucashVendorBillEntry entry)
  {
    super(entry.getGenerInvoice(), entry.getJwsdpPeer(), false);
  }

  // ---------------------------------------------------------------

  public String getBillID()
  {
    return getGenerInvoiceID();
  }
  
  public GnucashVendorBill getBill() throws WrongInvoiceTypeException
  {
    if ( myInvoice == null )
    {
      myInvoice = getGenerInvoice();
      if ( ! myInvoice.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) )
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
  public FixedPointNumber getPrice() throws WrongInvoiceTypeException {
    return getBillPrice();
  }

  @Override
  public String getPriceFormatted() throws WrongInvoiceTypeException {
      return getBillPriceFormatted();
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

  // ------------------------------

  @Override
  public FixedPointNumber getJobPrice() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getJobPriceFormatted() throws WrongInvoiceTypeException {
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
    buffer.append(" date: ");
    try {
	buffer.append(getDate().toLocalDate().format(DATE_FORMAT_PRINT));
    }
    catch (Exception e) {
	buffer.append(getDate().toLocalDate().toString());
    }
    buffer.append(" action: '");
    buffer.append(getAction() + "'");
    buffer.append(" price: ");
    try
    {
      buffer.append(getPrice());
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
