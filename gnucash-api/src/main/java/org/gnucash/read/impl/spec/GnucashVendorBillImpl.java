package org.gnucash.read.impl.spec;

import java.util.Collection;
import java.util.HashSet;

import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.messages.ApplicationMessages;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.GnucashGenerInvoiceImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorBillEntry;
import org.gnucash.read.spec.SpecInvoiceCommon;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorBillImpl extends GnucashGenerInvoiceImpl implements GnucashVendorBill, SpecInvoiceCommon {
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorBillImpl.class);
  private static ApplicationMessages bundle = ApplicationMessages.getInstance();

  @SuppressWarnings("exports")
  public GnucashVendorBillImpl(final GncGncInvoice peer, final GnucashFile gncFile) {
    super(peer, gncFile);
  }

  public GnucashVendorBillImpl(final GnucashGenerInvoice invc) throws WrongInvoiceTypeException {
    super(invc.getJwsdpPeer(), invc.getFile());

    // No, we cannot check that first, because the super() method
    // always has to be called first.
    if (!invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT).equals(GnucashGenerInvoice.TYPE_VENDOR)
        && !invc.getOwnerType(GnucashGenerInvoice.ReadVariant.DIRECT).equals(GnucashGenerInvoice.TYPE_JOB))
      throw new WrongInvoiceTypeException();

    for (GnucashGenerInvoiceEntry entry : invc.getGenerEntries()) {
      addEntry(new GnucashVendorBillEntryImpl(entry));
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
              if (splt.getAction().equals(bundle.getMessage("ACTION_PAYMENT"))) {
                addPayingTransaction(splt);
              }
            } // if lotID
          } // for invc
        } // if lot
      } // for splt
    } // for trx
  }

  // -----------------------------------------------------------------

  @Override
  public String getVendorId() {
    return getOwnerId();
  }

  @Override
  public GnucashVendor getVendor() throws WrongInvoiceTypeException {
    return getVendor_direct();
  }

  public GnucashVendor getVendor_direct() throws WrongInvoiceTypeException {
    if (!getJwsdpPeer().getInvoiceOwner().getOwnerType().equals(GnucashGenerInvoice.TYPE_VENDOR))
      throw new WrongInvoiceTypeException();

    return file.getVendorByID(getJwsdpPeer().getInvoiceOwner().getOwnerId().getValue());
  }

  // ---------------------------------------------------------------

  @Override
  public GnucashVendorBillEntry getEntryById(String id) throws WrongInvoiceTypeException {
    return new GnucashVendorBillEntryImpl(getGenerEntryById(id));
  }

  @Override
  public Collection<GnucashVendorBillEntry> getEntries() throws WrongInvoiceTypeException {
    Collection<GnucashVendorBillEntry> castEntries = new HashSet<GnucashVendorBillEntry>();

    for (GnucashGenerInvoiceEntry entry : getGenerEntries()) {
      if (entry.getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
        castEntries.add(new GnucashVendorBillEntryImpl(entry));
      }
    }

    return castEntries;
  }

  @Override
  public void addEntry(final GnucashVendorBillEntry entry) {
    addGenerEntry(entry);
  }

  // -----------------------------------------------------------------

  @Override
  public FixedPointNumber getAmountUnpaidWithTaxes() throws WrongInvoiceTypeException {
    return getBillAmountUnpaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithTaxes() throws WrongInvoiceTypeException {
    return getBillAmountPaidWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountPaidWithoutTaxes() throws WrongInvoiceTypeException {
    return getBillAmountPaidWithoutTaxes();
  }

  @Override
  public FixedPointNumber getAmountWithTaxes() throws WrongInvoiceTypeException {
    return getBillAmountWithTaxes();
  }

  @Override
  public FixedPointNumber getAmountWithoutTaxes() throws WrongInvoiceTypeException {
    return getBillAmountWithoutTaxes();
  }

  @Override
  public String getAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    return getBillAmountUnpaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    return getBillAmountPaidWithTaxesFormatted();
  }

  @Override
  public String getAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    return getBillAmountPaidWithoutTaxesFormatted();
  }

  @Override
  public String getAmountWithTaxesFormatted() throws WrongInvoiceTypeException {
    return getBillAmountWithTaxesFormatted();
  }

  @Override
  public String getAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    return getBillAmountWithoutTaxesFormatted();
  }

  // ------------------------------

  @Override
  public boolean isFullyPaid() throws WrongInvoiceTypeException {
    return isBillFullyPaid();
  }

  @Override
  public boolean isNotFullyPaid() throws WrongInvoiceTypeException {
    return isNotBillFullyPaid();
  }

  // ------------------------------

  @Override
  public FixedPointNumber getInvcAmountUnpaidWithTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getInvcAmountPaidWithTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getInvcAmountPaidWithoutTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getInvcAmountWithTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getInvcAmountWithoutTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountWithTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getInvcAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  // ------------------------------

  @Override
  public FixedPointNumber getJobAmountUnpaidWithTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getJobAmountPaidWithTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getJobAmountPaidWithoutTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getJobAmountWithTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public FixedPointNumber getJobAmountWithoutTaxes() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getJobAmountUnpaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getJobAmountPaidWithTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getJobAmountPaidWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getJobAmountWithTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public String getJobAmountWithoutTaxesFormatted() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  // ------------------------------

  @Override
  public boolean isInvcFullyPaid() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public boolean isNotInvcFullyPaid() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  // ------------------------------

  @Override
  public boolean isJobFullyPaid() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  @Override
  public boolean isNotJobFullyPaid() throws WrongInvoiceTypeException {
    throw new WrongInvoiceTypeException();
  }

  // -----------------------------------------------------------------

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("[GnucashVendorBillImpl:");
    buffer.append(" id: ");
    buffer.append(getId());
    buffer.append(" vendor-id: ");
    buffer.append(getVendorId());
    buffer.append(" bill-number: '");
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
