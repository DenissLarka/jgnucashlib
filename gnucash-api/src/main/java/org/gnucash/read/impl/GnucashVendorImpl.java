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
import org.gnucash.read.aux.GCshBillTerms;
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.aux.GCshTaxTable;
import org.gnucash.read.impl.aux.GCshAddressImpl;
import org.gnucash.read.impl.spec.GnucashVendorJobImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.SpecInvoiceCommon;
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
     * The currencyFormat to use for default-formating.<br/>
     * Please access only using {@link #getCurrencyFormat()}.
     *
     * @see #getCurrencyFormat()
     */
    private NumberFormat currencyFormat = null;

    // ---------------------------------------------------------------

    /**
     * @param peer    the JWSDP-object we are facading.
     * @param gncFile the file to register under
     */
    protected GnucashVendorImpl(final GncV2.GncBook.GncGncVendor peer, final GnucashFile gncFile) {
	super(new ObjectFactory().createSlotsType(), gncFile);

        // ::TODO: Slots
	jwsdpPeer = peer;
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GncV2.GncBook.GncGncVendor getJwsdpPeer() {
	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getId() {
	return jwsdpPeer.getVendorGuid().getValue();
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
    public String getName() {
	return jwsdpPeer.getVendorName();
    }

    /**
     * {@inheritDoc}
     */
    public GCshAddress getAddress() {
	return new GCshAddressImpl(jwsdpPeer.getVendorAddr());
    }

    // ---------------------------------------------------------------

    /**
     * @return the currency-format to use if no locale is given.
     */
    protected NumberFormat getCurrencyFormat() {
	if (currencyFormat == null) {
	    currencyFormat = NumberFormat.getCurrencyInstance();
	}

	return currencyFormat;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getTaxTableID() {
	GncV2.GncBook.GncGncVendor.VendorTaxtable vendTaxtable = jwsdpPeer.getVendorTaxtable();
	if (vendTaxtable == null) {
	    return null;
	}

	return vendTaxtable.getValue();
    }

    /**
     * {@inheritDoc}
     */
    public GCshTaxTable getTaxTable() {
	String id = getTaxTableID();
	if (id == null) {
	    return null;
	}
	return getGnucashFile().getTaxTableByID(id);
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getTermsID() {
	GncV2.GncBook.GncGncVendor.VendorTerms vendTerms = jwsdpPeer.getVendorTerms();
	if (vendTerms == null) {
	    return null;
	}

	return vendTerms.getValue();
    }

    /**
     * {@inheritDoc}
     */
    public GCshBillTerms getTerms() {
	String id = getTermsID();
	if (id == null) {
	    return null;
	}
	return getGnucashFile().getBillTermsByID(id);
    }

    // ---------------------------------------------------------------

    /**
     * date is not checked so invoiced that have entered payments in the future are
     * considered Paid.
     *
     * @return the current number of Unpaid invoices
     * @throws WrongInvoiceTypeException
     */
    public int getNofOpenBills() throws WrongInvoiceTypeException {
	return getGnucashFile().getUnpaidBillsForVendor_direct(this).size();
    }

    // -------------------------------------

    /**
     * @return the net sum of payments for invoices to this client
     */
    public FixedPointNumber getExpensesGenerated(GnucashGenerInvoice.ReadVariant readVar) {
	if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
	    return getExpensesGenerated_direct();
	else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB )
	    return getExpensesGenerated_viaAllJobs();
	
	return null; // Compiler happy
    }

    /**
     * @return the net sum of payments for invoices to this client
     */
    public FixedPointNumber getExpensesGenerated_direct() {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashVendorBill bllSpec : getPaidBills_direct()) {
//		    if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//		      GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getId().equals(this.getId())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getExpensesGenerated_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return the net sum of payments for invoices to this client
     */
    public FixedPointNumber getExpensesGenerated_viaAllJobs() {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashJobInvoice bllSpec : getPaidBills_viaAllJobs()) {
//		    if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//		      GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getId().equals(this.getId())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountWithoutTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getExpensesGenerated_viaAllJobs: Serious error");
	}

	return retval;
    }

    /**
     * @return formatted acording to the current locale's currency-format
     * @see #getExpensesGenerated()
     */
    public String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar) {
	return getCurrencyFormat().format(getExpensesGenerated(readVar));

    }

    /**
     * @param l the locale to format for
     * @return formatted acording to the given locale's currency-format
     * @see #getExpensesGenerated()
     */
    public String getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant readVar, final Locale l) {
	return NumberFormat.getCurrencyInstance(l).format(getExpensesGenerated(readVar));
    }

    // -------------------------------------

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     */
    public FixedPointNumber getOutstandingValue(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException {
	if ( readVar == GnucashGenerInvoice.ReadVariant.DIRECT )
	    return getOutstandingValue_direct();
	else if ( readVar == GnucashGenerInvoice.ReadVariant.VIA_JOB )
	    return getOutstandingValue_viaAllJobs();
	
	return null; // Compiler happy
    }

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     */
    public FixedPointNumber getOutstandingValue_direct() throws WrongInvoiceTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashVendorBill bllSpec : getUnpaidBills_direct()) {
//            if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//              GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getId().equals(this.getId())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountUnpaidWithTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getOutstandingValue_direct: Serious error");
	}

	return retval;
    }

    /**
     * @return the sum of left to pay Unpaid invoiced
     * @throws WrongInvoiceTypeException
     */
    public FixedPointNumber getOutstandingValue_viaAllJobs() throws WrongInvoiceTypeException {
	FixedPointNumber retval = new FixedPointNumber();

	try {
	    for (GnucashJobInvoice bllSpec : getUnpaidBills_viaAllJobs()) {
//            if ( invcGen.getType().equals(GnucashGenerInvoice.TYPE_VENDOR) ) {
//              GnucashVendorBill bllSpec = new GnucashVendorBillImpl(invcGen); 
		GnucashVendor vend = bllSpec.getVendor();
		if (vend.getId().equals(this.getId())) {
		    retval.add(((SpecInvoiceCommon) bllSpec).getAmountUnpaidWithTaxes());
		}
//            } // if invc type
	    } // for
	} catch (WrongInvoiceTypeException e) {
	    LOGGER.error("getOutstandingValue_viaAllJobs: Serious error");
	}

	return retval;
    }

    /**
     * @return Formatted acording to the current locale's currency-format
     * @see #getOutstandingValue()
     */
    public String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar) throws WrongInvoiceTypeException {
	return getCurrencyFormat().format(getOutstandingValue(readVar));
    }

    /**
     * @throws WrongInvoiceTypeException
     * @see #getOutstandingValue() Formatted acording to the given locale's
     *      currency-format
     */
    public String getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant readVar, final Locale l) throws WrongInvoiceTypeException {
	return NumberFormat.getCurrencyInstance(l).format(getOutstandingValue(readVar));
    }

    // -----------------------------------------------------------------

    /**
     * @return the jobs that have this vendor associated with them.
     * @throws WrongInvoiceTypeException 
     * @see GnucashVendor#getGenerJobs()
     */
    public java.util.Collection<GnucashVendorJob> getJobs() throws WrongInvoiceTypeException {

	List<GnucashVendorJob> retval = new LinkedList<GnucashVendorJob>();

	for ( GnucashGenerJob jobGener : getGnucashFile().getGenerJobs() ) {
	    if ( jobGener.getOwnerType().equals(GCshOwner.TYPE_VENDOR) ) {
		GnucashVendorJob jobSpec = new GnucashVendorJobImpl(jobGener);
		if ( jobSpec.getVendorId().equals(getId()) ) {
		    retval.add(jobSpec);
		}
	    }
	}

	return retval;
    }

    // -----------------------------------------------------------------

    @Override
    public Collection<GnucashGenerInvoice> getBills() throws WrongInvoiceTypeException {
	Collection<GnucashGenerInvoice> retval = new LinkedList<GnucashGenerInvoice>();

	for ( GnucashVendorBill invc : getGnucashFile().getBillsForVendor_direct(this) ) {
	    retval.add(invc);
	}
	
	for ( GnucashJobInvoice invc : getGnucashFile().getBillsForVendor_viaAllJobs(this) ) {
	    retval.add(invc);
	}
	
	return retval;
    }

    @Override
    public Collection<GnucashVendorBill> getPaidBills_direct() throws WrongInvoiceTypeException {
	return getGnucashFile().getPaidBillsForVendor_direct(this);
    }

    @Override
    public Collection<GnucashJobInvoice> getPaidBills_viaAllJobs() throws WrongInvoiceTypeException {
	return getGnucashFile().getPaidBillsForVendor_viaAllJobs(this);
    }

    @Override
    public Collection<GnucashVendorBill> getUnpaidBills_direct() throws WrongInvoiceTypeException {
	return getGnucashFile().getUnpaidBillsForVendor_direct(this);
    }

    @Override
    public Collection<GnucashJobInvoice> getUnpaidBills_viaAllJobs() throws WrongInvoiceTypeException {
	return getGnucashFile().getUnpaidBillsForVendor_viaAllJobs(this);
    }

    // -----------------------------------------------------------------

    public static int getHighestNumber(GnucashVendor vend) {
	return vend.getGnucashFile().getHighestVendorNumber();
    }

    // -----------------------------------------------------------------

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[GnucashVendorImpl:");
	buffer.append(" id: ");
	buffer.append(getId());
	buffer.append(" number: '");
	buffer.append(getNumber() + "'");
	buffer.append(" name: '");
	buffer.append(getName() + "'");
	buffer.append("]");
	return buffer.toString();
    }
}
