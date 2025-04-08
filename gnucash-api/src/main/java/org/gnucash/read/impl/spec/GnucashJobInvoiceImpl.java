package org.gnucash.read.impl.spec;

import java.util.Collection;
import java.util.HashSet;

import org.gnucash.Const;
import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashJobInvoiceEntry;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.SpecInvoiceCommon;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashJobInvoiceImpl extends GnucashGenerInvoiceImpl implements GnucashJobInvoice, SpecInvoiceCommon {
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashJobInvoiceImpl.class);

  @SuppressWarnings("exports")
  public GnucashJobInvoiceImpl(final GncGncInvoice peer, final GnucashFile gncFile) {
    super(peer, gncFile);
  }

  public GnucashJobInvoiceImpl(final GnucashGenerInvoice invc) throws WrongInvoiceTypeException {
    super(invc.getJwsdpPeer(), invc.getFile());

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if (!invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT).equals(GnucashGenerInvoice.TYPE_CUSTOMER)
        && !invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT).equals(GnucashGenerInvoice.TYPE_JOB))
      throw new WrongInvoiceTypeException();

    for (GnucashGenerInvoiceEntry entry : invc.getGenerEntries()) {
      addEntry(new GnucashJobInvoiceEntryImpl(entry));
    }

    for (GnucashTransaction trx : invc.getPayingTransactions()) {
      for (GnucashTransactionSplit splt : trx.getSplits()) {
        String lot = splt.getLotID();
        if (lot != null) {
          for (GnucashGenerInvoice invc1 : splt.getTransaction().getGnucashFile().getGenerInvoices()) {
            String lotID = invc1.getLotID();
            if (lotID != null && lotID.equals(lot)) {
              // Check if it's a payment transaction.
              // If so, add it to the invoice's list of payment transactions.
              if (splt.getAction().equals(Const.ACTION_PAYMENT)) {
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
  public String getJobId() {
    return getOwnerId_direct();
  }

  /**
   * {@inheritDoc}
   */
  public String getJobType() {
    return getGenerJob().getOwnerType();
  }

  // -----------------------------------------------------------------

  @Override
  public String getOwnerId(ReadVariant readVar) {
    if (readVar == ReadVariant.DIRECT)
      return getOwnerId_direct();
    else if (readVar == ReadVariant.VIA_JOB)
      return getOwnerId_viaJob();

    return null; // Compiler happy
  }

  protected String getOwnerId_viaJob() {
    return getGenerJob().getOwnerId();
  }

  /**
   * {@inheritDoc}
   */
  public String getCustomerId() throws WrongInvoiceTypeException {
    if (!getGenerJob().getOwnerType().equals(TYPE_CUSTOMER))
      throw new WrongInvoiceTypeException();

    return getOwnerId_viaJob();
  }

  /**
   * {@inheritDoc}
   */
  public String getVendorId() throws WrongInvoiceTypeException {
    if (!getGenerJob().getOwnerType().equals(TYPE_VENDOR))
      throw new WrongInvoiceTypeException();

    return getOwnerId_viaJob();
  }

  // ----------------------------

  public GnucashGenerJob getGenerJob() {
    return file.getGenerJobByID(getJobId());
  }

  public GnucashCustomerJob getCustJob() throws WrongInvoiceTypeException {
    if (!getGenerJob().getOwnerType().equals(TYPE_CUSTOMER))
      throw new WrongInvoiceTypeException();

    return new GnucashCustomerJobImpl(getGenerJob());
  }

  public GnucashVendorJob getVendJob() throws WrongInvoiceTypeException {
    if (!getGenerJob().getOwnerType().equals(TYPE_VENDOR))
      throw new WrongInvoiceTypeException();

    return new GnucashVendorJobImpl(getGenerJob());
  }

  // ------------------------------

  @Override
  public GnucashCustomer getCustomer() throws WrongInvoiceTypeException {
    if (!getGenerJob().getOwnerType().equals(TYPE_CUSTOMER))
      throw new WrongInvoiceTypeException();

    return getFile().getCustomerByID(getCustomerId());
  }

  @Override
  public GnucashVendor getVendor() throws WrongInvoiceTypeException {
    if (!getGenerJob().getOwnerType().equals(TYPE_VENDOR))
      throw new WrongInvoiceTypeException();

    return getFile().getVendorByID(getVendorId());
  }

  // ---------------------------------------------------------------

  @Override
  public GnucashJobInvoiceEntry getEntryById(String id) throws WrongInvoiceTypeException {
    return new GnucashJobInvoiceEntryImpl(getGenerEntryById(id));
  }

  @Override
  public Collection<GnucashJobInvoiceEntry> getEntries() throws WrongInvoiceTypeException {
    Collection<GnucashJobInvoiceEntry> castEntries = new HashSet<GnucashJobInvoiceEntry>();

    for (GnucashGenerInvoiceEntry entry : getGenerEntries()) {
      if (entry.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
        castEntries.add(new GnucashJobInvoiceEntryImpl(entry));
      }
    }

    return castEntries;
  }

  @Override
  public void addEntry(final GnucashJobInvoiceEntry entry) {
    addGenerEntry(entry);
  }

  // -----------------------------------------------------------------

  @Override
  public FixedPointNumber getAmountUnpaidWithTaxes() throws WrongInvoiceTypeException {
    return getJobAmountUnpaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithTaxes() throws WrongInvoiceTypeException {
    return getJobAmountPaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithoutTaxes() throws WrongInvoiceTypeException {
    return getJobAmountPaidWithoutTaxes();
  }

  @Override
  public FixedPointNumber getAmountWithTaxes() throws WrongInvoiceTypeException {
    return getJobAmountWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountWithoutTaxes() throws WrongInvoiceTypeException {
    return getJobAmountWithoutTaxes();
  }

  @Override
  public String getAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    return getJobAmountUnpaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    return getJobAmountPaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    // return getJobAmountPaidWithoutTaxesFormatted();
    return null;
  }

  @Override
  public String getAmountWithTaxesFormatted() throws WrongInvoiceTypeException {
    return getJobAmountWithTaxesFormatted();
  }

  @Override
  public String getAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    return getJobAmountWithoutTaxesFormatted();
  }

  // ------------------------------

  @Override
  public boolean isFullyPaid() throws WrongInvoiceTypeException {
    return isJobFullyPaid();
  }

  @Override
  public boolean isNotFullyPaid() throws WrongInvoiceTypeException {
    return isNotJobFullyPaid();
  }

  // ------------------------------

//  @Override
//  public FixedPointNumber getInvcAmountUnpaidWithTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public FixedPointNumber getInvcAmountPaidWithTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public FixedPointNumber getInvcAmountPaidWithoutTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public FixedPointNumber getInvcAmountWithTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//  
//  @Override
//  public FixedPointNumber getInvcAmountWithoutTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getInvcAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getInvcAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getInvcAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getInvcAmountWithTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getInvcAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException
//  {
//    throw new WrongInvoiceTypeException();
//  }
//  
//  // ------------------------------
//
//  @Override
//  public FixedPointNumber getBillAmountUnpaidWithTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public FixedPointNumber getBillAmountPaidWithTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public FixedPointNumber getBillAmountPaidWithoutTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public FixedPointNumber getBillAmountWithTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//  
//  @Override
//  public FixedPointNumber getBillAmountWithoutTaxes() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getBillAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getBillAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getBillAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getBillAmountWithTaxesFormatted() throws WrongInvoiceTypeException 
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public String getBillAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException
//  {
//    throw new WrongInvoiceTypeException();
//  }
//  
//  // ------------------------------
//
//  @Override
//  public boolean isInvcFullyPaid() throws WrongInvoiceTypeException
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public boolean isNotInvcFullyPaid() throws WrongInvoiceTypeException
//  {
//    throw new WrongInvoiceTypeException();
//  }
//  
//  // ------------------------------
//
//  @Override
//  public boolean isBillFullyPaid() throws WrongInvoiceTypeException
//  {
//    throw new WrongInvoiceTypeException();
//  }
//
//  @Override
//  public boolean isNotBillFullyPaid() throws WrongInvoiceTypeException
//  {
//    throw new WrongInvoiceTypeException();
//  }

  // -----------------------------------------------------------------

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[GnucashJobInvoiceImpl:");
    buffer.append(" id: ");
    buffer.append(getId());
    buffer.append(" job-id: ");
    buffer.append(getJobId());
    buffer.append(" invoice-number: '");
    buffer.append(getNumber() + "'");
    buffer.append(" description: '");
    buffer.append(getDescription() + "'");
    buffer.append(" #entries: ");
    try {
      buffer.append(getEntries().size());
    } catch (WrongInvoiceTypeException e) {
      buffer.append("ERROR");
    }
    buffer.append(" date-opened: ");
    try {
      buffer.append(getDateOpened().toLocalDate().format(DATE_OPENED_FORMAT_PRINT));
    } catch (Exception e) {
      buffer.append(getDateOpened().toLocalDate().toString());
    }
    buffer.append("]");
    return buffer.toString();
  }

}
