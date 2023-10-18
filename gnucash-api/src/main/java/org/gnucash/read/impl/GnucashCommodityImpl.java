package org.gnucash.read.impl;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashCommodity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashCommodityImpl implements GnucashCommodity 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashCommodityImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    private final GncV2.GncBook.GncCommodity jwsdpPeer;

    // ---------------------------------------------------------------

    /**
     * @param peer    the JWSDP-object we are facading.
     * @param gncFile the file to register under
     */
    protected GnucashCommodityImpl(final GncV2.GncBook.GncCommodity peer) {
	jwsdpPeer = peer;
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GncV2.GncBook.GncCommodity getJwsdpPeer() {
	return jwsdpPeer;
    }

    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNameSpace() {
	if ( jwsdpPeer.getCmdtySpace() == null )
	    return null;
	
	return jwsdpPeer.getCmdtySpace();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
	if ( jwsdpPeer.getCmdtyId() == null )
	    return null;
	
	return jwsdpPeer.getCmdtyId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifId() {
	if ( getNameSpace() == null ||
	     getId() == null )
	    return null;
	
	return getNameSpace() + ":" + getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
	if ( jwsdpPeer.getCmdtyName() == null )
	    return null;
	
	return jwsdpPeer.getCmdtyName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXCode() {
	if ( jwsdpPeer.getCmdtyXcode() == null )
	    return null;
	
	return jwsdpPeer.getCmdtyXcode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getFraction() {
	if ( jwsdpPeer.getCmdtyFraction() == null )
	    return null;
	
	return jwsdpPeer.getCmdtyFraction();
    }

    // -----------------------------------------------------------------

    @Override
    public String toString() {
	return "NOT IMPLEMENTED YET";
    }

}
