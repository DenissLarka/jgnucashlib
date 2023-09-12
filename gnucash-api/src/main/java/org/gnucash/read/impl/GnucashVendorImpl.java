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
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public class GnucashVendorImpl extends GnucashObjectImpl implements GnucashVendor {

	/**
	 * the JWSDP-object we are facading.
	 */
	private final GncV2.GncBook.GncGncVendor jwsdpPeer;

    /**
     * The file we belong to.
     */
    private final GnucashFile file;

	/**
	 * @param peer    the JWSDP-object we are facading.
	 * @param gncFile the file to register under
	 */
	protected GnucashVendorImpl(final GncV2.GncBook.GncGncVendor peer, final GnucashFile gncFile) {
		super(new ObjectFactory().createSlotsType(), gncFile);
		jwsdpPeer = peer;
		file = gncFile;
	}

	/**
	 * @return the JWSDP-object we are wrapping.
	 */
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

		for (GnucashJob job : getGnucashFile().getJobs()) {
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
	 * @throws WrongInvoiceTypeException 
	 */
	public int getNofOpenInvoices() throws WrongInvoiceTypeException {
		int count = 0;
		for (GnucashCustVendInvoice invoice : getGnucashFile().getInvoices()) {
		  if ( invoice instanceof GnucashVendorBill ) {
            if ( ((GnucashVendorBill) invoice).getVendor() != this ) {
              continue;
            }

            if (invoice.isNotFullyPaid()) {
              count++;
            }
		  }
		}
		return count;
	}

	/**
	 * @return the sum of payments for invoices to this client
	 * @throws WrongInvoiceTypeException 
	 */
	public FixedPointNumber getIncomeGenerated() throws WrongInvoiceTypeException {
		FixedPointNumber retval = new FixedPointNumber();

		for (GnucashCustVendInvoice invoice : getGnucashFile().getInvoices()) {
		  if ( invoice instanceof GnucashVendorBill ) {
            if ( ((GnucashVendorBill) invoice).getVendor() != this ) {
              continue;
            }
            retval.add(invoice.getInvcAmountWithoutTaxes());
		  }
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
	 * @return formated acording to the current locale's currency-format
	 * @throws WrongInvoiceTypeException 
	 * @see #getIncomeGenerated()
	 */
	public String getIncomeGeneratedFormatted() throws WrongInvoiceTypeException {
		return getCurrencyFormat().format(getIncomeGenerated());

	}

	/**
	 * @param l the locale to format for
	 * @return formated acording to the given locale's currency-format
	 * @throws WrongInvoiceTypeException 
	 * @see #getIncomeGenerated()
	 */
	public String getIncomeGeneratedFormatted(final Locale l) throws WrongInvoiceTypeException {
		return NumberFormat.getCurrencyInstance(l).format(getIncomeGenerated());
	}

	/**
	 * @return the sum of left to pay Unpaid invoiced
	 * @throws WrongInvoiceTypeException 
	 */
	public FixedPointNumber getOutstandingValue() throws WrongInvoiceTypeException {
		FixedPointNumber retval = new FixedPointNumber();

		for (GnucashCustVendInvoice invoice : getGnucashFile().getInvoices()) {
		  if ( invoice instanceof GnucashVendorBill ) {
            if ( ((GnucashVendorBill) invoice).getVendor() != this ) {
              continue;
            }
            retval.add(invoice.getInvcAmountUnpaidWithTaxes());
		  }
		}

		return retval;
	}

	/**
	 * @return Formatted acording to the current locale's currency-format
	 * @throws WrongInvoiceTypeException 
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
	public GnucashVendor.Address getAddress() {
		return new AddressImpl(jwsdpPeer.getVendorAddr());
	}

	public static class AddressImpl implements Address {

		/**
		 * The JWSDP-object we are wrapping.
		 */
		private final org.gnucash.generated.Address jwsdpPeer;

		/**
		 * @param newPeer the JWSDP-object we are wrapping.
		 */
		public AddressImpl(final org.gnucash.generated.Address newPeer) {
			super();
			jwsdpPeer = newPeer;
		}

		/**
		 * @return The JWSDP-object we are wrapping.
		 */
		public org.gnucash.generated.Address getJwsdpPeer() {
			return jwsdpPeer;
		}

		/**
		 * @see GnucashVendor.Address#getAddressName()
		 */
		public String getAddressName() {
			if (jwsdpPeer.getAddrName() == null) {
				return "";
			}
			return jwsdpPeer.getAddrName();
		}

		/**
		 * @see GnucashVendor.Address#getAddressLine1()
		 */
		public String getAddressLine1() {
			if (jwsdpPeer.getAddrAddr1() == null) {
				return "";
			}
			return jwsdpPeer.getAddrAddr1();
		}

		/**
		 * @see GnucashVendor.Address#getAddressLine2()
		 */
		public String getAddressLine2() {
			if (jwsdpPeer.getAddrAddr2() == null) {
				return "";
			}
			return jwsdpPeer.getAddrAddr2();
		}

		/**
		 * @return third and last line below the name
		 */
		public String getAddressLine3() {
			if (jwsdpPeer.getAddrAddr3() == null) {
				return "";
			}
			return jwsdpPeer.getAddrAddr3();
		}

		/**
		 * @return fourth and last line below the name
		 */
		public String getAddressLine4() {
			if (jwsdpPeer.getAddrAddr4() == null) {
				return "";
			}
			return jwsdpPeer.getAddrAddr4();
		}

		/**
		 * @return telephone
		 */
		public String getTel() {
			if (jwsdpPeer.getAddrPhone() == null) {
				return "";
			}
			return jwsdpPeer.getAddrPhone();
		}

		/**
		 * @return Fax
		 */
		public String getFax() {
			if (jwsdpPeer.getAddrFax() == null) {
				return "";
			}
			return jwsdpPeer.getAddrFax();
		}

		/**
		 * @return Email
		 */
		public String getEmail() {
			if (jwsdpPeer.getAddrEmail() == null) {
				return "";
			}
			return jwsdpPeer.getAddrEmail();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getAddressName() + "\n"
					+ getAddressLine1() + "\n"
					+ getAddressLine2();
		}
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
	
  // ------------------------------

  @Override
  public Collection<GnucashVendorBill> getUnpaidInvoices(GnucashCustVendInvoice.ReadVariant readVar) throws WrongInvoiceTypeException
  {
    if ( readVar == GnucashCustVendInvoice.ReadVariant.DIRECT )
      return file.getUnpaidInvoicesForVendor_direct(this);
    else if ( readVar == GnucashCustVendInvoice.ReadVariant.VIA_JOB )
      return file.getUnpaidInvoicesForVendor_viaJob(this);
    
    return null; // Compiler happy
  }

}
