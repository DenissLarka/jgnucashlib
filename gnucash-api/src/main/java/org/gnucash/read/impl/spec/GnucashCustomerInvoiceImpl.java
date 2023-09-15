package org.gnucash.read.impl.spec;

import java.util.Collection;
import java.util.HashSet;

import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustVendInvoiceEntry;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.impl.GnucashCustVendInvoiceImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerInvoiceEntry;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashCustomerInvoiceImpl extends GnucashCustVendInvoiceImpl
                                        implements GnucashCustomerInvoice
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashCustomerInvoiceImpl.class);

  public GnucashCustomerInvoiceImpl(final GncGncInvoice peer, final GnucashFile gncFile)
  {
    super(peer, gncFile);
  }

  public GnucashCustomerInvoiceImpl(final GnucashCustVendInvoice invc) throws WrongInvoiceTypeException
  {
    super(invc.getJwsdpPeer(), invc.getFile());

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if ( ! invc.getOwnerType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) )
      throw new WrongInvoiceTypeException();
    
    for ( GnucashCustVendInvoiceEntry entry : invc.getCustVendInvcEntries() )
    {
      addEntry(new GnucashCustomerInvoiceEntryImpl(entry));
    }

    for ( GnucashTransaction trx : invc.getPayingTransactions() )
    {
      for ( GnucashTransactionSplit splt : trx.getSplits() ) 
      {
        String lot = splt.getLotID();
        if ( lot != null ) {
            for ( GnucashCustVendInvoice invc1 : splt.getTransaction().getGnucashFile().getInvoices() ) {
                String lotID = invc1.getLotID();
                if ( lotID != null &&
                     lotID.equals(lot) ) {
                    // Check if it's a payment transaction. 
                    // If so, add it to the invoice's list of payment transactions.
                    if ( splt.getSplitAction().equals(GnucashTransactionSplit.ACTION_PAYMENT) ) {
                        addPayingTransaction(splt);
                    }
                } // if lotID
            } // for invc
        } // if lot
      } // for splt
    } // for trx
  }
  
  // -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public String getCustomerId(GnucashCustVendInvoice.ReadVariant readVar) {
    return getOwnerId(readVar);
  }

  @Override
  public GnucashCustomer getCustomer() throws WrongInvoiceTypeException
  {
    return getCustomer_direct();
  }

  public GnucashCustomer getCustomer_direct() throws WrongInvoiceTypeException {
    if ( ! getJwsdpPeer().getInvoiceOwner().getOwnerType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) )
      throw new WrongInvoiceTypeException();
    
    return file.getCustomerByID(getJwsdpPeer().getInvoiceOwner().getOwnerId().getValue());
  }

  public GnucashCustomer getCustomer_viaJob() throws WrongInvoiceTypeException {
    if ( ! getJob().getOwnerType().equals(GnucashJob.TYPE_CUSTOMER) )
      throw new WrongInvoiceTypeException();
    
    return ((GnucashCustomerJobImpl) getJob()).getCustomer();
  }

  // ---------------------------------------------------------------

  @Override
  public GnucashCustomerInvoiceEntry getEntryById(String id) throws WrongInvoiceTypeException
  {
    return new GnucashCustomerInvoiceEntryImpl(getCustVendInvcEntryById(id));
  }

  @Override
  public Collection<GnucashCustomerInvoiceEntry> getEntries() throws WrongInvoiceTypeException
  {
    Collection<GnucashCustomerInvoiceEntry> castEntries = new HashSet<GnucashCustomerInvoiceEntry>();
    
    for ( GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries() )
    {
      if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) )
      {
        castEntries.add(new GnucashCustomerInvoiceEntryImpl(entry));
      }
    }
    
    return castEntries;
  }

  @Override
  public void addEntry(final GnucashCustomerInvoiceEntry entry)
  {
    addCustVendInvcEntry(entry);
  }

  // -----------------------------------------------------------------

  @Override
  public FixedPointNumber getAmountUnpaidWithTaxes() throws WrongInvoiceTypeException
  {
    return getInvcAmountUnpaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithTaxes() throws WrongInvoiceTypeException
  {
    return getInvcAmountPaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithoutTaxes() throws WrongInvoiceTypeException
  {
    return getInvcAmountPaidWithoutTaxes();
  }

  @Override
  public FixedPointNumber getAmountWithTaxes() throws WrongInvoiceTypeException
  {
    return getInvcAmountWithTaxes();
  }
  
  @Override
  public FixedPointNumber getAmountWithoutTaxes() throws WrongInvoiceTypeException
  {
    return getInvcAmountWithoutTaxes();
  }

  @Override
  public String getAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getInvcAmountUnpaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getInvcAmountPaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getInvcAmountPaidWithoutTaxesFormatted();
  }

  @Override
  public String getAmountWithTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getInvcAmountWithTaxesFormatted();
  }

  @Override
  public String getAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException
  {
    return getInvcAmountWithoutTaxesFormatted();
  }
  
  // ------------------------------
  
  @Override
  public boolean isFullyPaid() throws WrongInvoiceTypeException
  {
    return isInvcFullyPaid();
  }
  
  @Override
  public boolean isNotFullyPaid() throws WrongInvoiceTypeException
  {
    return isNotInvcFullyPaid();
  }
  
  // ------------------------------

  @Override
  public FixedPointNumber getBillAmountUnpaidWithTaxes() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getBillAmountPaidWithTaxes() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getBillAmountPaidWithoutTaxes() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getBillAmountWithTaxes() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }
  
  @Override
  public FixedPointNumber getBillAmountWithoutTaxes() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getBillAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getBillAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getBillAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getBillAmountWithTaxesFormatted() throws WrongInvoiceTypeException 
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getBillAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }
  
  // ------------------------------

  @Override
  public boolean isBillFullyPaid() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public boolean isNotBillFullyPaid() throws WrongInvoiceTypeException
  {
    throw new WrongInvoiceTypeException();
  }
  
  // -----------------------------------------------------------------

  @Override
  public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[GnucashCustomerInvoiceImpl:");
      buffer.append(" id: ");
      buffer.append(getId());
      buffer.append(" customer-id (dir.): ");
      buffer.append(getCustomerId(GnucashCustVendInvoice.ReadVariant.DIRECT));
      buffer.append(" invoice-number: '");
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
