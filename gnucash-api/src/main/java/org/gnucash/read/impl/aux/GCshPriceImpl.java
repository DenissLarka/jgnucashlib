package org.gnucash.read.impl.aux;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.gnucash.Const;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.InvalidCmdtyCurrIDException;
import org.gnucash.currency.InvalidCmdtyCurrTypeException;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.GncV2.GncBook.GncPricedb.Price.PriceCommodity;
import org.gnucash.generated.GncV2.GncBook.GncPricedb.Price.PriceCurrency;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.GCshPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshPriceImpl implements GCshPrice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCshPriceImpl.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
    
    // -----------------------------------------------------------

    /**
     * The JWSDP-object we are wrapping.
     */
    private final GncV2.GncBook.GncPricedb.Price jwsdpPeer;

    private final GnucashFile file;

    // -----------------------------------------------------------

    /**
     * @param newPeer the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GCshPriceImpl(final GncV2.GncBook.GncPricedb.Price newPeer, final GnucashFile file) {
	super();
		
	this.jwsdpPeer = newPeer;
	this.file      = file;
    }

    // -----------------------------------------------------------
	
    @Override
    public String getId() {
	if ( jwsdpPeer.getPriceId() == null )
	    return null;
		    
	return jwsdpPeer.getPriceId().getValue();
    }

    @Override
    public CmdtyCurrID getCommodityQualifId() throws InvalidCmdtyCurrTypeException {
	if ( jwsdpPeer.getPriceCommodity() == null )
	    return null;
		
	PriceCommodity cmdty = jwsdpPeer.getPriceCommodity();
	if ( cmdty.getCmdtySpace() == null ||
	     cmdty.getCmdtyId() == null )
	    return null;
		    
	CmdtyCurrID result = new CmdtyCurrID(cmdty.getCmdtySpace(), cmdty.getCmdtyId(), true);
	    
	return result;
    }

    @Override
    public GnucashCommodity getCommodity() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( getCommodityQualifId() == null )
	    return null;
	
	GnucashCommodity cmdty = file.getCommodityByQualifID(getCommodityQualifId());
	
	return cmdty;
    }

    @Override
    public CmdtyCurrID getCurrencyQualifId() throws InvalidCmdtyCurrTypeException {
	if ( jwsdpPeer.getPriceCurrency() == null )
	    return null;
		
	PriceCurrency curr = jwsdpPeer.getPriceCurrency();
	if ( curr.getCmdtySpace() == null ||
	     curr.getCmdtyId() == null )
	    return null;
	
	CmdtyCurrID result = new CmdtyCurrID(curr.getCmdtySpace(), curr.getCmdtyId(), true);
		    
	return result;
    }

    @Override
    public String getCurrencyCode() {
	if ( jwsdpPeer.getPriceCurrency() == null )
	    return null;
		
	PriceCurrency curr = jwsdpPeer.getPriceCurrency();
	if ( curr.getCmdtySpace() == null ||
	     curr.getCmdtyId() == null )
	    return null;
		    
	return curr.getCmdtyId();
    }

    @Override
    public GnucashCommodity getCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( getCurrencyQualifId() == null )
	    return null;
	
	GnucashCommodity cmdty = file.getCommodityByQualifID(getCurrencyQualifId());
	
	return cmdty;
    }

    @Override
    public LocalDate getDate() {
	if ( jwsdpPeer.getPriceTime() == null )
	    return null;
	
	String dateStr = jwsdpPeer.getPriceTime().getTsDate();
	try {
	    return ZonedDateTime.parse(dateStr, DATE_FORMAT).toLocalDate();
	} catch (Exception e) {
	    IllegalStateException ex = new IllegalStateException("unparsable date '" + dateStr + "' in invoice!");
	    ex.initCause(e);
	    throw ex;
	}
    }

    @Override
    public String getSource() {
	if ( jwsdpPeer.getPriceSource() == null )
	    return null;
	
	return jwsdpPeer.getPriceSource();
    }

    @Override
    public String getType() {
	if ( jwsdpPeer.getPriceType() == null )
	    return null;
	
	return jwsdpPeer.getPriceType();
    }

    @Override
    public FixedPointNumber getValue() {
	if ( jwsdpPeer.getPriceValue() == null )
	    return null;
	
	return new FixedPointNumber(jwsdpPeer.getPriceValue());
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	String result = "GCshPriceImpl [id=" + getId();
	
	try {
	    result += ", cmdty-qualif-id='" + getCommodityQualifId() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += ", cmdty-qualif-id=" + "ERROR";
	}
	
	try {
	    result += ", curr-qualif-id='" + getCurrencyQualifId() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += ", curr-qualif-id=" + "ERROR";
	}
	
	result += ", date=" + getDate(); 
	result += ", source=" + getSource(); 
	result += ", type=" + getType(); 
	result += ", value=" + getValue() + "]";
	
	return result;
    }
    
    
}
