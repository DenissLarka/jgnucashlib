package org.gnucash.read.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import org.gnucash.basetypes.GCshCmdtyCurrID;
import org.gnucash.basetypes.InvalidCmdtyCurrTypeException;
import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashCommodityImpl implements GnucashCommodity 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashCommodityImpl.class);

    // ---------------------------------------------------------------

    /**
     * the JWSDP-object we are facading.
     */
    private final GncV2.GncBook.GncCommodity jwsdpPeer;

    /**
     * The file we belong to.
     */
    protected final GnucashFile file;
    
    // ---------------------------------------------------------------

    /**
     * @param peer    the JWSDP-object we are facading.
     * @param gncFile the file to register under
     */
    protected GnucashCommodityImpl(final GncV2.GncBook.GncCommodity peer, final GnucashFile gncFile) {
	this.jwsdpPeer = peer;
	this.file = gncFile;
    }

    // ---------------------------------------------------------------

    /**
     * @return the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GncV2.GncBook.GncCommodity getJwsdpPeer() {
	return jwsdpPeer;
    }

    public GnucashFile getGnucashFile() {
	return file;
    }

    // ---------------------------------------------------------------

    private String getNameSpace() {
	if ( jwsdpPeer.getCmdtySpace() == null )
	    return null;
	
	return jwsdpPeer.getCmdtySpace();
    }

    private String getId() {
	if ( jwsdpPeer.getCmdtyId() == null )
	    return null;
	
	return jwsdpPeer.getCmdtyId();
    }

    /**
     * {@inheritDoc}
     * @throws InvalidCmdtyCurrTypeException 
     */
    @Override
    public GCshCmdtyCurrID getQualifId() throws InvalidCmdtyCurrTypeException {
	if ( getNameSpace() == null ||
	     getId() == null )
	    return null;
	
	GCshCmdtyCurrID result = new GCshCmdtyCurrID(getNameSpace(), getId());
	
	return result;
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

    // ::TODO sort the entries by date
    @Override
    public Collection<GCshPrice> getQuotes() throws InvalidCmdtyCurrTypeException {
	Collection<GCshPrice> result = new ArrayList<GCshPrice>();
	
	Collection<GCshPrice> prices = getGnucashFile().getPrices();
	for ( GCshPrice price : prices ) {
	    if ( price.getFromCmdtyCurrQualifId().toString().equals(getQualifId().toString()) ) {
		result.add(price);
	    }
	}
	
	return result;
    }

    @Override
    public GCshPrice getYoungestQuote() throws InvalidCmdtyCurrTypeException {
	
	GCshPrice result = null;

	LocalDate youngestDate = LocalDate.of(1970, 1, 1); // ::MAGIC
	for ( GCshPrice price : getQuotes() ) {
	    if ( price.getDate().isAfter(youngestDate) ) {
		result = price;
		youngestDate = price.getDate();
	    }
	}

	return result;
    }

    // -----------------------------------------------------------------

    @Override
    public String toString() {
	
	String result = "[GnucashCommodityImpl:";
	try {
	    result += " qualif-id='" + getQualifId().toString() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += " qualif-id=" + "ERROR";
	} 
	result += ", name='" + getName() + "'"; 
	result += ", x-code='" + getXCode() + "'"; 
	result += ", fraction=" + getFraction() + "]";
	
	return result;
    }

}
