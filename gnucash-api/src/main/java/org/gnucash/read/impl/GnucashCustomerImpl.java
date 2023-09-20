package org.gnucash.read.impl;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.gnucash.generated.GncV2;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.aux.GCshAddress;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.aux.GCshAddressImpl;
import org.gnucash.read.impl.spec.GnucashCustomerInvoiceImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashCustomerImpl extends GnucashObjectImpl 
                                 implements GnucashCustomer 
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashCustomerImpl.class);

	/**
	 * the JWSDP-object we are facading.
	 */
	private final GncV2.GncBook.GncGncCustomer jwsdpPeer;

    /**
     * The file we belong to.
     */
    private final GnucashFile file;

    // ---------------------------------------------------------------

	/**
	 * @param peer    the JWSDP-object we are facading.
	 * @param gncFile the file to register under
	 */
	protected GnucashCustomerImpl(final GncV2.GncBook.GncGncCustomer peer, final GnucashFile gncFile) {
		super((peer.getCustSlots() == null) ? new ObjectFactory().createSlotsType() : peer.getCustSlots(), gncFile);
		
		if (peer.getCustSlots() == null) {
			peer.setCustSlots(getSlots());
		}
		
		jwsdpPeer = peer;
        file = gncFile;
	}

    // ---------------------------------------------------------------

	/**
	 * @return the JWSDP-object we are wrapping.
	 */
	@SuppressWarnings("exports")
	public GncV2.GncBook.GncGncCustomer getJwsdpPeer() {
		return jwsdpPeer;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return jwsdpPeer.getCustGuid().getValue();
	}

	/**
	 * @return the jobs that have this customer associated with them.
	 * @see GnucashCustomer#getJobs()
	 */
	public java.util.Collection<GnucashCustomerJob> getJobs() {

		List<GnucashCustomerJob> retval = new LinkedList<GnucashCustomerJob>();

		for (GnucashGenerJob job : getGnucashFile().getJobs()) {
		  if ( job instanceof GnucashCustomerJob ) {
            if ( ((GnucashCustomerJob) job).getCustomerId().equals(getId()) ) {
              retval.add((GnucashCustomerJob) job);
            }
		  }		    
		}

		return retval;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDiscount() {
		return jwsdpPeer.getCustDiscount();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNotes() {
		return jwsdpPeer.getCustNotes();
	}

	/**
	 * date is not checked so invoiced that have entered payments in
	 * the future are considered Paid.
	 *
	 * @return the current number of Unpaid invoices
	 */
	public int getNofOpenInvoices() {
		return getGnucashFile().getUnpaidInvoicesForCustomer_direct(this).size();
	}

	/**
	 * @return the net sum of payments for invoices to this client
	 */
	public FixedPointNumber getIncomeGenerated() {
		FixedPointNumber retval = new FixedPointNumber();

		try
		{
		  for ( GnucashGenerInvoice invcGen : getGnucashFile().getGenerInvoices() ) {
		    if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) ) {
		      GnucashCustomerInvoice invcSpec = new GnucashCustomerInvoiceImpl(invcGen); 
		      GnucashCustomer cust = invcSpec.getCustomer(); 
              if ( cust.getId().equals(this.getId()) ) {
                retval.add(invcSpec.getAmountWithoutTaxes());
              }
            } // if invc type
		  } // for
		}
        catch (WrongInvoiceTypeException e)
        {
          LOGGER.error("getIncomeGenerated: Serious error");
        }

		return retval;
	}

	/**
	 * The currencyFormat to use for default-formating.<br/>
	 * Please access only using {@link #getCurrencyFormat()}.
	 *
	 * @see #getCurrencyFormat()
	 */
	private NumberFormat currencyFormat = null;

	/**
	 * @return formatted acording to the current locale's currency-format
	 * @throws WrongInvoiceTypeException 
	 * @see #getIncomeGenerated()
	 */
	public String getIncomeGeneratedFormatted() {
		return getCurrencyFormat().format(getIncomeGenerated());

	}

	/**
	 * @param l the locale to format for
	 * @return formatted acording to the given locale's currency-format
	 * @see #getIncomeGenerated()
	 */
	public String getIncomeGeneratedFormatted(final Locale l) {
		return NumberFormat.getCurrencyInstance(l).format(getIncomeGenerated());
	}

	/**
	 * @return the sum of left to pay Unpaid invoiced
	 * @throws WrongInvoiceTypeException 
	 */
	public FixedPointNumber getOutstandingValue() {
		FixedPointNumber retval = new FixedPointNumber();

        try
        {
          for ( GnucashGenerInvoice invcGen : getGnucashFile().getGenerInvoices() ) {
            if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) ) {
              GnucashCustomerInvoice invcSpec = new GnucashCustomerInvoiceImpl(invcGen); 
              GnucashCustomer cust = invcSpec.getCustomer(); 
              if ( cust.getId().equals(this.getId()) ) {
                retval.add(invcSpec.getAmountUnpaidWithTaxes());
              }
            } // if invc type
          } // for
        }
        catch (WrongInvoiceTypeException e)
        {
          LOGGER.error("getOutstandingValue: Serious error");
        }

		return retval;
	}

	/**
	 * @return Formatted acording to the current locale's currency-format
	 * @see #getOutstandingValue()
	 */
	public String getOutstandingValueFormatted() {
		return getCurrencyFormat().format(getOutstandingValue());
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @see #getOutstandingValue()
	 * Formatted acording to the given locale's currency-format
	 */
	public String getOutstandingValueFormatted(final Locale l) {
		return NumberFormat.getCurrencyInstance(l).format(getOutstandingValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNumber() {
		return jwsdpPeer.getCustId();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCustomerTaxTableID() {
		GncV2.GncBook.GncGncCustomer.CustTaxtable custTaxtable = jwsdpPeer.getCustTaxtable();
		if (custTaxtable == null) {
			return null;
		}
		return custTaxtable.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshTaxTable getCustomerTaxTable() {
		String id = getCustomerTaxTableID();
		if (id == null) {
			return null;
		}
		return getGnucashFile().getTaxTableByID(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return jwsdpPeer.getCustName();
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshAddress getAddress() {
		return new GCshAddressImpl(jwsdpPeer.getCustAddr());
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshAddress getShippingAddress() {
		return new GCshAddressImpl(jwsdpPeer.getCustShipaddr());
	}

	/**
	 * @return the currency-format to use if no locale is given.
	 */
	protected NumberFormat getCurrencyFormat() {
		if (currencyFormat == null) {
			currencyFormat = NumberFormat.getCurrencyInstance();
		}

		return currencyFormat;
	}
	
  // -----------------------------------------------------------------

  @Override
  public Collection<GnucashCustomerInvoice> getPaidInvoices(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException
  {
    if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
      return file.getPaidInvoicesForCustomer_direct(this);
    else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB )
      return file.getPaidInvoicesForCustomer_viaJob(this);

    return null; // Compiler happy
  }

  @Override
  public Collection<GnucashCustomerInvoice> getUnpaidInvoices(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException
  {
    if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
      return file.getUnpaidInvoicesForCustomer_direct(this);
    else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB )
      return file.getUnpaidInvoicesForCustomer_viaJob(this);
    
    return null; // Compiler happy
  }

  // -----------------------------------------------------------------

  public static int getHighestNumber(GnucashCustomer cust)
  {
    return cust.getGnucashFile().getHighestCustomerNumber();
  }

}
