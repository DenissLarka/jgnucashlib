package org.gnucash.read.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashGenerInvoiceEntryImpl;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashJobInvoiceEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashJobInvoiceEntryImpl extends GnucashGenerInvoiceEntryImpl
                                        implements GnucashJobInvoiceEntry 
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashJobInvoiceEntryImpl.class);

  @SuppressWarnings("exports")
  public GnucashJobInvoiceEntryImpl(
          final GnucashJobInvoice invoice,
          final GncV2.GncBook.GncGncEntry peer) 
  {
    super(invoice, peer, true);
  }

  @SuppressWarnings("exports")
  public GnucashJobInvoiceEntryImpl(
          final GnucashGenerInvoice invoice,
          final GncV2.GncBook.GncGncEntry peer) throws WrongInvoiceTypeException 
  {
    super(invoice, peer, true);

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if ( ! invoice.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
      throw new WrongInvoiceTypeException();
  }

  @SuppressWarnings("exports")
  public GnucashJobInvoiceEntryImpl(final GncV2.GncBook.GncGncEntry peer, final GnucashFileImpl gncFile) 
  {
    super(peer, gncFile);
  }

  public GnucashJobInvoiceEntryImpl(final GnucashGenerInvoiceEntry entry) throws WrongInvoiceTypeException
  {
    super(entry.getGenerInvoice(), entry.getJwsdpPeer(), false);

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if ( ! entry.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
      throw new WrongInvoiceTypeException();
  }

  public GnucashJobInvoiceEntryImpl(final GnucashJobInvoiceEntry entry)
  {
    super(entry.getGenerInvoice(), entry.getJwsdpPeer(), false);
  }

  // ---------------------------------------------------------------

  public String getInvoiceID()
  {
    return getGenerInvoiceID();
  }
  
  public GnucashJobInvoice getInvoice() throws WrongInvoiceTypeException
  {
    if ( myInvoice == null )
    {
      myInvoice = getGenerInvoice();
      if ( ! myInvoice.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
        throw new WrongInvoiceTypeException();
        
      if ( myInvoice == null )
      {
        throw new IllegalStateException(
            "No job invoice with id '" + getInvoiceID()
            + "' for invoice entry with id '" + getId() + "'");
      }
    }
    
    return new GnucashJobInvoiceImpl(myInvoice);
  }

  // ---------------------------------------------------------------

  @Override
  public FixedPointNumber getPrice() throws WrongInvoiceTypeException {
    return getJobPrice();
  }

  @Override
  public String getPriceFormatted() throws WrongInvoiceTypeException {
      return getJobPriceFormatted();
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
  public FixedPointNumber getBillPrice() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getBillPriceFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  // ---------------------------------------------------------------

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[GnucashJobInvoiceEntryImpl:");
    buffer.append(" id: ");
    buffer.append(getId());
    buffer.append(" invoice-id: ");
    buffer.append(getInvoiceID());
    //      buffer.append(" invoice: ");
    //      GnucashJobInvoice invc = getInvoice();
    //      buffer.append(invoice==null?"null":invoice.getName());
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
