/**
 * GnucashInvoiceImpl.java
 * License: GPLv3 or later
 * Created on 13.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * -----------------------------------------------------------
 * major Changes:
 * 13.05.2005 - initial version
 * ...
 */
package org.gnucash.read.impl;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.gnucash.generated.GncV2;
import org.gnucash.generated.GncV2.GncBook.GncGncInvoice;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashCustVendInvoice;
import org.gnucash.read.GnucashCustVendInvoiceEntry;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.spec.WrongInvoiceTypeException;

/**
 *
 * created: 13.05.2005 <br/>
 * Implementation of GnucashInvoice that uses JWSDP.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class GnucashCustVendInvoiceImpl implements GnucashCustVendInvoice {

  protected static final DateTimeFormatter DATE_OPENED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
  protected static final DateFormat        DATE_OPENED_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");  
  protected static final DateTimeFormatter DATE_OPENED_FORMAT_PRINT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateFormat DATE_POSTED_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

  /**
   * @see GnucashCustVendInvoice#getDateOpened()
   */
  protected ZonedDateTime dateOpened;
  
  // -----------------------------------------------------------------

  /**
   * @param peer the JWSDP-object we are facading.
   * @see #jwsdpPeer
   * @param gncFile the file to register under
   */
  public GnucashCustVendInvoiceImpl(
          final GncV2.GncBook.GncGncInvoice peer,
          final GnucashFile gncFile) {
      super();
      
      jwsdpPeer = peer;
      file = gncFile;

  }

  // -----------------------------------------------------------------

	/**
	 * @return getAmmountWithoutTaxes().isMoreThen(getAmmountPayedWithoutTaxes())
	 * @throws WrongInvoiceTypeException 
	 * @see GnucashCustVendInvoice#isNotFullyPayed()
	 */
	public boolean isNotFullyPayed() throws WrongInvoiceTypeException {
		return getInvcAmmountWithTaxes().isMoreThen(getInvcAmmountPayedWithTaxes());
	}

	/**
	 * The transactions that are paying for this invoice.
	 */
	private final Collection<GnucashTransaction> payingTransactions = new LinkedList<GnucashTransaction>();

	/**
	 * {@inheritDoc}
	 */
	public void addPayingTransaction(final GnucashTransactionSplit trans) {
		payingTransactions.add(trans.getTransaction());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addTransaction(final GnucashTransaction trans) {
		//

	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<GnucashTransaction> getPayingTransactions() {
		return payingTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAccountIDToTransferMoneyTo() {
		return jwsdpPeer.getInvoicePostacc().getValue();
	}

	/**
	 * @return the transaction that transferes the money from the customer to
	 *         the account for money you are to get and the one you owe the
	 *         taxes.
	 */
	public GnucashTransaction getPostTransaction() {
		if (jwsdpPeer.getInvoicePosttxn() == null) {
			return null; //unposted invoices have no postlot
		}
		return file.getTransactionByID(jwsdpPeer.getInvoicePosttxn().getValue());
	}
	
  // -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public FixedPointNumber getInvcAmmountUnPayedWithTaxes() throws WrongInvoiceTypeException {
  
  	// System.err.println("debug: GnucashInvoiceImpl.getAmmountUnPayed(): "
  	// + "getAmmountWithoutTaxes()="+getAmmountWithoutTaxes()+" getAmmountPayedWithTaxes()="+getAmmountPayedWithTaxes() );
  
  	return ((FixedPointNumber) getInvcAmmountWithTaxes().clone()).subtract(getInvcAmmountPayedWithTaxes());
  }

  /**
	 * {@inheritDoc}
	 */
	public FixedPointNumber getInvcAmmountPayedWithTaxes() {

      FixedPointNumber takenFromReceivableAccount = new FixedPointNumber();
		for (GnucashTransaction trx : getPayingTransactions()) {

			for (GnucashTransactionSplit split : trx.getSplits()) {

				if ( split.getAccount().getType().equals(GnucashAccount.ACCOUNTTYPE_RECEIVABLE) &&
                     ! split.getValue().isPositive() ) {
				  takenFromReceivableAccount.subtract(split.getValue());
               }
			}

		}

		//        System.err.println("getInvcAmmountPayedWithTaxes="+takenFromReceivableAccount.doubleValue());

		return takenFromReceivableAccount;
	}

	@Override
	public FixedPointNumber getInvcAmmountPayedWithoutTaxes() throws WrongInvoiceTypeException
	{
	  FixedPointNumber retval = new FixedPointNumber();
	  
	  for (GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries()) {
        if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) ) {
          retval.add(entry.getInvcSumExclTaxes());
        }
	  }
	  
	  return retval;
	}

    /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public FixedPointNumber getInvcAmmountWithTaxes() throws WrongInvoiceTypeException {
  
  	FixedPointNumber retval = new FixedPointNumber();
  
  	//TODO: we should sum them without taxes grouped by tax% and
  	//      multiply the sums with the tax% to be calculatng
  	//      correctly
  
  	for (GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries()) {
      if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) ) {
  		retval.add(entry.getInvcSumInclTaxes());
      }
  	}
  	
  	return retval;
  }

    /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public FixedPointNumber getInvcAmmountWithoutTaxes() throws WrongInvoiceTypeException {
  
  	FixedPointNumber retval = new FixedPointNumber();
  
  	for (GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries()) {
      if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_CUSTOMER) ) {
  		retval.add(entry.getInvcSumExclTaxes());
      }
  	}
  
  	return retval;
  }
  
  // ------------------------------
  
  /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public String getInvcAmmountUnPayedWithTaxesFormatet() throws WrongInvoiceTypeException {
      return this.getCurrencyFormat().format(this.getInvcAmmountUnPayedWithTaxes());
  }

  /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public String getInvcAmmountPayedWithTaxesFormatet()
  {
    return this.getCurrencyFormat().format(this.getInvcAmmountPayedWithTaxes());
  }

  /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public String getInvcAmmountPayedWithoutTaxesFormatet() throws WrongInvoiceTypeException
  {
    return this.getCurrencyFormat().format(this.getInvcAmmountPayedWithoutTaxes());
  }

  /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public String getInvcAmmountWithTaxesFormatet() throws WrongInvoiceTypeException {
      return this.getCurrencyFormat().format(this.getInvcAmmountWithTaxes());
  }

  /**
   * {@inheritDoc}
   * @throws WrongInvoiceTypeException 
   */
  public String getInvcAmmountWithoutTaxesFormatet() throws WrongInvoiceTypeException {
      return this.getCurrencyFormat().format(this.getInvcAmmountWithoutTaxes());
  }

  // -----------------------------------------------------------------

    /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public FixedPointNumber getBillAmmountUnPayedWithTaxes() throws WrongInvoiceTypeException {
    
      // System.err.println("debug: GnucashInvoiceImpl.getAmmountUnPayed(): "
      // + "getBillAmmountUnPayed()="+getBillAmmountWithoutTaxes()+" getBillAmmountPayedWithTaxes()="+getAmmountPayedWithTaxes() );
    
      return ((FixedPointNumber) getBillAmmountWithTaxes().clone()).subtract(getBillAmmountPayedWithTaxes());
    }
    
    /**
     * {@inheritDoc}
     */
    public FixedPointNumber getBillAmmountPayedWithTaxes() {

      FixedPointNumber takenFromPayableAccount = new FixedPointNumber();
        for (GnucashTransaction trx : getPayingTransactions()) {

            for (GnucashTransactionSplit split : trx.getSplits()) {

              System.err.print("ee31: " + split.getAccountID());
              System.err.println(" (" + split.getAccount().getType() + ")");
                if ( split.getAccount().getType().equals(GnucashAccount.ACCOUNTTYPE_PAYABLE) &&
                     split.getValue().isPositive() ) {
                  System.err.println("ee32: " + split.getValue());
                  takenFromPayableAccount.add(split.getValue());
                }
            }

        }

        //        System.err.println("getBillAmmountPayedWithTaxes="+takenFromPayableAccount.doubleValue());

        return takenFromPayableAccount;
    }

    public FixedPointNumber getBillAmmountPayedWithoutTaxes() throws WrongInvoiceTypeException
    {
      FixedPointNumber retval = new FixedPointNumber();
      
      for (GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries()) {
        if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_VENDOR) ) {
          retval.add(entry.getInvcSumExclTaxes());
        }
      }
      
      return retval;
    }
    
    /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public FixedPointNumber getBillAmmountWithTaxes() throws WrongInvoiceTypeException {
    
      FixedPointNumber retval = new FixedPointNumber();
    
      //TODO: we should sum them without taxes grouped by tax% and
      //      multiply the sums with the tax% to be calculatng
      //      correctly
    
      for (GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries()) {
        if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_VENDOR) ) {
          retval.add(entry.getBillSumInclTaxes());
          System.err.println("ee1: " + entry.getBillSumInclTaxes());
        }
      }
      
      return retval;
    }

      /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public FixedPointNumber getBillAmmountWithoutTaxes() throws WrongInvoiceTypeException {
    
      FixedPointNumber retval = new FixedPointNumber();
    
      for (GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries()) {
        if ( entry.getType().equals(GnucashCustVendInvoice.TYPE_VENDOR) ) {
          retval.add(entry.getBillSumExclTaxes());
        }
      }
    
      return retval;
    }

    // ------------------------------
    
    /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public String getBillAmmountUnPayedWithTaxesFormatet() throws WrongInvoiceTypeException {
        return this.getCurrencyFormat().format(this.getBillAmmountUnPayedWithTaxes());
    }

    /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public String getBillAmmountPayedWithTaxesFormatet()
    {
      return this.getCurrencyFormat().format(this.getBillAmmountPayedWithTaxes());
    }

    /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public String getBillAmmountPayedWithoutTaxesFormatet() throws WrongInvoiceTypeException
    {
      return this.getCurrencyFormat().format(this.getBillAmmountPayedWithoutTaxes());
    }

    /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public String getBillAmmountWithTaxesFormatet() throws WrongInvoiceTypeException {
        return this.getCurrencyFormat().format(this.getBillAmmountWithTaxes());
    }

    /**
     * {@inheritDoc}
     * @throws WrongInvoiceTypeException 
     */
    public String getBillAmmountWithoutTaxesFormatet() throws WrongInvoiceTypeException {
        return this.getCurrencyFormat().format(this.getBillAmmountWithoutTaxes());
    }

  // -----------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * @throws WrongInvoiceTypeException 
	 */
	public TaxedSum[] getInvcTaxes() throws WrongInvoiceTypeException {

		List<TaxedSum> taxedSums = new LinkedList<TaxedSum>();

		invoiceentries:
		for (GnucashCustVendInvoiceEntry entry : getCustVendInvcEntries()) {
			FixedPointNumber taxpercent = entry.getApplicableTaxPercend();

			for (TaxedSum taxedSum2 : taxedSums) {
				TaxedSum taxedSum = taxedSum2;
				if (taxedSum.getTaxpercent().equals(taxpercent)) {
					taxedSum.setTaxsum(
							taxedSum.getTaxsum().add(
									entry.getInvcSumInclTaxes().subtract(entry.getInvcSumExclTaxes())
							)
					);
					continue invoiceentries;
				}
			}

			TaxedSum taxedSum = new TaxedSum(taxpercent, entry.getInvcSumInclTaxes().subtract(entry.getInvcSumExclTaxes()));
			taxedSums.add(taxedSum);

		}

		return taxedSums.toArray(new TaxedSum[taxedSums.size()]);

	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashJob getJob() {
		return file.getJobByID(getJobID());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJobID() {
		return getJwsdpPeer().getInvoiceOwner().getOwnerId().getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJobType() {
		return getJob().getOwnerType();
	}

	/**
	 * the JWSDP-object we are facading.
	 */
	protected GncV2.GncBook.GncGncInvoice jwsdpPeer;

	/**
	 * The file we belong to.
	 */
	protected final GnucashFile file;

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return getJwsdpPeer().getInvoiceGuid().getValue();
	}

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return getJwsdpPeer().getInvoiceOwner().getOwnerType();
    }

	/**
	 * {@inheritDoc}
	 */
	public String getLotID() {
		if (getJwsdpPeer().getInvoicePostlot() == null) {
			return null; //unposted invoices have no postlot
		}
		return getJwsdpPeer().getInvoicePostlot().getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return getJwsdpPeer().getInvoiceNotes();
	}
	
	// ----------------------------

    /**
     * {@inheritDoc}
     */
    public GncGncInvoice getPeer() {
        return jwsdpPeer;
    }

	/**
	 * {@inheritDoc}
	 */
	public GnucashFile getFile() {
		return file;
	}

    // ----------------------------

	/**
	 * The entries of this invoice.
	 */
	protected Collection<GnucashCustVendInvoiceEntry> entries = new HashSet<GnucashCustVendInvoiceEntry>();

    /**
     * {@inheritDoc}
     */
    public GnucashCustVendInvoiceEntry getCustVendInvcEntryById(final String id) {
        for (GnucashCustVendInvoiceEntry element : getCustVendInvcEntries()) {
            if (element.getId().equals(id)) {
                return element;
            }

        }
        return null;
    }

	/**
	 * {@inheritDoc}
	 */
	public Collection<GnucashCustVendInvoiceEntry> getCustVendInvcEntries() {
	    return entries;
	}

    /**
     * {@inheritDoc}
     */
    public void addCustVendInvcEntry(final GnucashCustVendInvoiceEntry entry) {
        if (!entries.contains(entry)) {
            entries.add(entry);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	public ZonedDateTime getDateOpened() {
		if (dateOpened == null) {
			String s = getJwsdpPeer().getInvoiceOpened().getTsDate();
			try {
				//"2001-09-18 00:00:00 +0200"
				dateOpened = ZonedDateTime.parse(s, DATE_OPENED_FORMAT);
			}
			catch (Exception e) {
				IllegalStateException ex = new IllegalStateException(
						"unparsable date '"
								+ s
								+ "' in invoice!");
				ex.initCause(e);
				throw ex;
			}

		}
		return dateOpened;
	}

	/**
	 * @see #getDateOpenedFormatet()
	 * @see #getDatePostedFormatet()
	 */
	private DateFormat dateFormat = null;

	/**
	 * @see #getDateOpenedFormatet()
	 * @see #getDatePostedFormatet()
	 * @return the Dateformat to use.
	 */
	protected DateFormat getDateFormat() {
		if (dateFormat == null) {
			dateFormat = DateFormat.getDateInstance();
		}

		return dateFormat;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDateOpenedFormatet() {
		return getDateFormat().format(getDateOpened());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDatePostedFormatet() {
		return getDateFormat().format(getDatePosted());
	}

	/**
	 * @see GnucashCustVendInvoice#getDatePosted()
	 */
	protected ZonedDateTime datePosted;

	/**
	 * {@inheritDoc}
	 */
	public ZonedDateTime getDatePosted() {
		if (datePosted == null) {
			String s = getJwsdpPeer().getInvoiceOpened().getTsDate();
			try {
				//"2001-09-18 00:00:00 +0200"
				datePosted = ZonedDateTime.parse(s, DATE_OPENED_FORMAT);
			}
			catch (Exception e) {
				IllegalStateException ex = new IllegalStateException(
						"unparsable date '"
								+ s
								+ "' in invoice!");
				ex.initCause(e);
				throw ex;
			}

		}
		return datePosted;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNumber() {
		return getJwsdpPeer().getInvoiceId();
	}

    public String getOwnerId(ReadVariant readVar) {
      if ( readVar == ReadVariant.DIRECT )
        return getOwnerId_direct();
      else if ( readVar == ReadVariant.VIA_JOB )
        return getOwnerId_viaJob();
      
      return null; // Compiler happy
    }

    protected String getOwnerId_direct() {
      assert getJwsdpPeer().getInvoiceOwner().getOwnerId().getType().equals("guid");
        return getJwsdpPeer().getInvoiceOwner().getOwnerId().getValue();
    }

    protected String getOwnerId_viaJob() {
        return getJob().getOwnerId();
    }

    public String getOwnerType() {
      return getJwsdpPeer().getInvoiceOwner().getOwnerType();
    }

//    public InvoiceOwner getOwner() {
//      return getJwsdpPeer().getInvoiceOwner();
//    }

	/**
	 * sorts primarily on the date the transaction happened
	 * and secondarily on the date it was entered.
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @param o invoice to compare with
	 * @return -1 0 or 1
	 */
	public int compareTo(final GnucashCustVendInvoice o) {

		GnucashCustVendInvoice other = o;

		try {
			int compare = other.getDatePosted().compareTo(getDatePosted());
			if (compare != 0) {
				return compare;
			}

			return other.getDateOpened().compareTo(getDateOpened());
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 *
	 * @return The JWSDP-Object we are wrapping
	 */
	public GncV2.GncBook.GncGncInvoice getJwsdpPeer() {
		return jwsdpPeer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[GnucashCustVendInvoiceImpl:");
        buffer.append(" id: ");
        buffer.append(getId());
        buffer.append(" owner-id (dir.): ");
        buffer.append(getOwnerId(ReadVariant.DIRECT));
        buffer.append(" owner-type: ");
        buffer.append(getOwnerType());
		buffer.append(" cust/vend-invoice-number: '");
		buffer.append(getNumber() + "'");
		buffer.append(" description: '");
		buffer.append(getDescription() + "'");
		buffer.append(" #entries: ");
		buffer.append(entries.size());
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
	
	// ---------------------------------------------------------------

	/**
	 * The currencyFormat to use for default-formating.<br/>
	 * Please access only using {@link #getCurrencyFormat()}.
	 * @see #getCurrencyFormat()
	 */
	private NumberFormat currencyFormat = null;

	/**
	 *
	 * @return the currency-format to use if no locale is given.
	 */
	protected NumberFormat getCurrencyFormat() {
		if (currencyFormat == null) {
			currencyFormat = NumberFormat.getCurrencyInstance();
		}

		return currencyFormat;
	}

}
