package org.gnucash.read.impl.aux;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.aux.GCshBillTermsProximo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshBillTermsProximoImpl implements GCshBillTermsProximo {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCshBillTermsProximoImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    private final GncV2.GncBook.GncGncBillTerm.BilltermProximo jwsdpPeer;

    // ---------------------------------------------------------------

    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    @SuppressWarnings("exports")
    public GCshBillTermsProximoImpl(final GncV2.GncBook.GncGncBillTerm.BilltermProximo peer) {
	super();

	jwsdpPeer = peer;
    }

    // ---------------------------------------------------------------

    @Override
    public Integer getDueDay() {
	return jwsdpPeer.getBtProxDueDay();
    }

    @Override
    public Integer getDiscountDay() {
	return jwsdpPeer.getBtProxDiscDay();
    }

    @Override
    public FixedPointNumber getDiscount() {
	if ( jwsdpPeer.getBtProxDiscount() == null )
	    return null;
	
	return new FixedPointNumber(jwsdpPeer.getBtProxDiscount());
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[GCshBillTermsProximoImpl:");

	buffer.append(" due-day: ");
	buffer.append(getDueDay());

	buffer.append(" discount-day: ");
	buffer.append(getDiscountDay());

	buffer.append(" discount: ");
	buffer.append(getDiscount());

	buffer.append("]");

	return buffer.toString();
    }
    
}
