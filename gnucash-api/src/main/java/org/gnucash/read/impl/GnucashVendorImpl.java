package org.gnucash.read.impl;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.gnucash.generated.GncV2;
import org.gnucash.generated.GncV2.GncBook.GncGncVendor.VendorTerms;
import org.gnucash.generated.ObjectFactory;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.aux.GCshAddress;
import org.gnucash.read.impl.aux.GCshAddressImpl;
import org.gnucash.read.impl.spec.GnucashVendorBillImpl;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorImpl extends GnucashObjectImpl 
                               implements GnucashVendor 
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorImpl.class);

	/**
	 * the JWSDP-object we are facading.
	 */
	private final GncV2.GncBook.GncGncVendor jwsdpPeer;

    /**
     * The file we belong to.
     */
    private final GnucashFile file;

    // ---------------------------------------------------------------

	/**
	 * @param peer    the JWSDP-object we are facading.
	 * @param gncFile the file to register under
	 */
	protected GnucashVendorImpl(final GncV2.GncBook.GncGncVendor peer, final GnucashFile gncFile) {
		super(new ObjectFactory().createSlotsType(), gncFile);
		
		jwsdpPeer = peer;
		file = gncFile;
	}

    // ---------------------------------------------------------------

	/**
	 * @return the JWSDP-object we are wrapping.
	 */
	@SuppressWarnings("exports")
	public GncV2.GncBook.GncGncVendor getJwsdpPeer() {
		return jwsdpPeer;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return jwsdpPeer.getVendorGuid().getValue();
	}

	/**
	 * @return the jobs that have this vendor associated with them.
	 * @see GnucashVendor#getJobs()
	 */
	public java.util.Collection<GnucashVendorJob> getJobs() {

		List<GnucashVendorJob> retval = new LinkedList<GnucashVendorJob>();

		for (GnucashGenerJob job : getGnucashFile().getJobs()) {
		  if ( job instanceof GnucashVendorJob ) {
            if ( ((GnucashVendorJob) job).getVendorId().equals(getId())) {
              retval.add((GnucashVendorJob) job);
            }
		  }
		}

		return retval;
	}

	/**
	 * date is not checked so invoiced that have entered payments in
	 * the future are considered Paid.
	 *
	 * @return the current number of Unpaid invoices
	 */
	public int getNofOpenBills() {
		return getGnucashFile().getUnpaidBillsForVendor_direct(this).size();
	}

	/**
	 * @return the net sum of payments for invoices to this client
	 */
	public FixedPointNumber getExpensesGenerated() {
		FixedPointNumber retval = new FixedPointNumber();

		try
		{
		  for ( GnucashGenerInvoice invcGen : getGnucashFile().getInvoices() ) {
		    if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
		      GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		      GnucashVendor vend = bllSpec.getVendor(); 
              if ( vend.getId().equals(this.getId()) ) {
                retval.add(bllSpec.getAmountWithoutTaxes());
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
	 * @see #getExpensesGenerated()
	 */
	public String getExpensesGeneratedFormatted() {
		return getCurrencyFormat().format(getExpensesGenerated());

	}

	/**
	 * @param l the locale to format for
	 * @return formatted acording to the given locale's currency-format
	 * @see #getExpensesGenerated()
	 */
	public String getExpensesGeneratedFormatted(final Locale l) {
		return NumberFormat.getCurrencyInstance(l).format(getExpensesGenerated());
	}

	/**
	 * @return the sum of left to pay Unpaid invoiced
	 * @throws WrongInvoiceTypeException 
	 */
	public FixedPointNumber getOutstandingValue() throws WrongInvoiceTypeException {
		FixedPointNumber retval = new FixedPointNumber();

        try
        {
          for ( GnucashGenerInvoice invcGen : getGnucashFile().getInvoices() ) {
            if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
              GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
              GnucashVendor vend = bllSpec.getVendor(); 
              if ( vend.getId().equals(this.getId()) ) {
                retval.add(bllSpec.getAmountUnpaidWithTaxes());
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
	public String getOutstandingValueFormatted() throws WrongInvoiceTypeException {
		return getCurrencyFormat().format(getOutstandingValue());
	}

	/**
	 * @throws WrongInvoiceTypeException 
	 * @see #getOutstandingValue()
	 * Formatted acording to the given locale's currency-format
	 */
	public String getOutstandingValueFormatted(final Locale l) throws WrongInvoiceTypeException {
		return NumberFormat.getCurrencyInstance(l).format(getOutstandingValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNumber() {
		return jwsdpPeer.getVendorId();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("exports")
	public VendorTerms getVendorTerms() {
		return jwsdpPeer.getVendorTerms();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return jwsdpPeer.getVendorName();
	}

	/**
	 * {@inheritDoc}
	 */
	public GCshAddress getAddress() {
		return new GCshAddressImpl(jwsdpPeer.getVendorAddr());
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
  public Collection<GnucashVendorBill> getPaidBills(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException
  {
    if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
      return file.getPaidBillsForVendor_direct(this);
    else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB ) 
      return file.getPaidBillsForVendor_viaJob(this);

    return null; // Compiler happy
  }

  @Override
  public Collection<GnucashVendorBill> getUnpaidBills(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException
  {
    if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
      return file.getUnpaidBillsForVendor_direct(this);
    else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB )
      return file.getUnpaidBillsForVendor_viaJob(this);
    
    return null; // Compiler happy
  }

  // -----------------------------------------------------------------

  public static int getHighestNumber(GnucashVendor vend)
  {
    return vend.getGnucashFile().getHighestVendorNumber();
  }

}
