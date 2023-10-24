package org.gnucash.read.impl.aux;

import java.security.InvalidAlgorithmParameterException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

import org.gnucash.Const;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.CmdtyCurrNameSpace;
import org.gnucash.currency.CommodityID;
import org.gnucash.currency.CurrencyID;
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
     * The currency-format to use for formatting.<br/>
     */
    private NumberFormat currencyFormat = null;

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

    // ----------------------------
    
    @Override
    public CmdtyCurrID getFromCmdtyCurrQualifId() throws InvalidCmdtyCurrTypeException {
	if ( jwsdpPeer.getPriceCommodity() == null )
	    return null;
		
	PriceCommodity cmdty = jwsdpPeer.getPriceCommodity();
	if ( cmdty.getCmdtySpace() == null ||
	     cmdty.getCmdtyId() == null )
	    return null;
		    
	CmdtyCurrID result = new CmdtyCurrID(cmdty.getCmdtySpace(), cmdty.getCmdtyId());
	    
	return result;
    }

    @Override
    public CommodityID getFromCommodityQualifId() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	CmdtyCurrID cmdtyCurrID = getFromCmdtyCurrQualifId();
	return new CommodityID(cmdtyCurrID);
    }

    @Override
    public CurrencyID getFromCurrencyQualifId() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	CmdtyCurrID cmdtyCurrID = getFromCmdtyCurrQualifId();
	return new CurrencyID(cmdtyCurrID);
    }

    @Override
    public GnucashCommodity getFromCommodity() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	CommodityID cmdtyID = getFromCommodityQualifId();
	GnucashCommodity cmdty = file.getCommodityByQualifID(cmdtyID);
	return cmdty;
    }
    
    @Override
    public String getFromCurrencyCode() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getFromCurrencyQualifId().getCurrency().getCurrencyCode();
    }

    @Override
    public GnucashCommodity getFromCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	CurrencyID currID = getFromCurrencyQualifId(); 
	GnucashCommodity cmdty = file.getCommodityByQualifID(currID);
	return cmdty;
    }
    
    // ----------------------------
    
    @Override
    public CurrencyID getToCurrencyQualifId() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	if ( jwsdpPeer.getPriceCurrency() == null )
	    return null;
		
	PriceCurrency curr = jwsdpPeer.getPriceCurrency();
	if ( curr.getCmdtySpace() == null ||
	     curr.getCmdtyId() == null )
	    return null;
	
	CurrencyID result = new CurrencyID(curr.getCmdtySpace(), curr.getCmdtyId());
		    
	return result;
    }

    @Override
    public String getToCurrencyCode() throws InvalidCmdtyCurrTypeException {
	if ( jwsdpPeer.getPriceCurrency() == null )
	    return null;
		
	PriceCurrency curr = jwsdpPeer.getPriceCurrency();
	if ( curr.getCmdtySpace() == null ||
	     curr.getCmdtyId() == null )
	    return null;
	
	if ( ! curr.getCmdtySpace().equals(CmdtyCurrNameSpace.CURRENCY) )
	    throw new InvalidCmdtyCurrTypeException();
	
	return curr.getCmdtyId();
    }

    @Override
    public GnucashCommodity getToCurrency() throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( getToCurrencyQualifId() == null )
	    return null;
	
	GnucashCommodity cmdty = file.getCommodityByQualifID(getToCurrencyQualifId());
	
	return cmdty;
    }

    // ----------------------------
    
    /**
     * @return The currency-format to use for formating.
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    private NumberFormat getCurrencyFormat() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	if (currencyFormat == null) {
	    currencyFormat = NumberFormat.getCurrencyInstance();
	}

//	// the currency may have changed
//	if ( ! getCurrencyQualifId().getType().equals(CmdtyCurrID.Type.CURRENCY) )
//	    throw new InvalidCmdtyCurrTypeException();
	    
	Currency currency = Currency.getInstance(getToCurrencyCode());
	currencyFormat.setCurrency(currency);

	return currencyFormat;
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

    @Override
    public String getValueFormatted() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	return getCurrencyFormat().format(getValue());
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	String result = "GCshPriceImpl [id=" + getId();
	
	try {
	    result += ", cmdty-qualif-id='" + getFromCmdtyCurrQualifId() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += ", cmdty-qualif-id=" + "ERROR";
	}
	
	try {
	    result += ", curr-qualif-id='" + getToCurrencyQualifId() + "'";
	} catch (Exception e) {
	    result += ", curr-qualif-id=" + "ERROR";
	}
	
	result += ", date=" + getDate(); 
	result += ", source='" + getSource() + "'"; 
	result += ", type=" + getType();
	
	try {
	    result += ", value=" + getValueFormatted() + "]";
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    result += ", value=" + "ERROR" + "]";
	}
	
	return result;
    }
    
    
}
