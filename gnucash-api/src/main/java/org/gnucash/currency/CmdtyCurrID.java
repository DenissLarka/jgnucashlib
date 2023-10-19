package org.gnucash.currency;

import java.util.Currency;

import javax.xml.stream.events.Namespace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In theory, the name space string, if type is set to "SECURITY", can 
 * be freely set in GnuCash. However, in practice, it usual to select one 
 * of the world's major exchanges' official abbreviation. We therefore 
 * limit the valid values to these abbreviations.
 * 
 * Yes, there are exceptions to this rule, but we currently do not 
 * support these but rather profit from enhanced type safety instead.
 * (Apart from that, nothing stops you from adding new exchange codes
 * to the enum CmdtyCurrNameSpace.Exchange or to open another enum
 * in this class, if you absolutely need it. After all, this is FOSS...)
 *  
 * @param exchange
 * @param secCode
 */
public class CmdtyCurrID {
    
    // https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gnc-commodity.h#L108
    // We do not use the GnuCash-internally used "NONCURRENCY"
    public enum Type {
	CURRENCY,
	SECURITY_EXCHANGE, // name space is (informal) abbrev. of major world exchange
	SECURITY_GENERAL,  // name space can be freely chosen
	UNSET
    }
    
    // ---------------------------------------------------------------
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdtyCurrID.class);

    public static final char SEPARATOR = ':';

    // ---------------------------------------------------------------

    private Type                        type;
    private Currency                    currency;
    private CmdtyCurrNameSpace.Exchange exchange;
    private String                      secCode;
    private String                      nameSpaceFree;

    // ---------------------------------------------------------------
    
    public CmdtyCurrID() {
	this.type = Type.UNSET;
    }

    public CmdtyCurrID(Currency curr) throws InvalidCmdtyCurrTypeException {
	this.type = Type.CURRENCY;
	setCurrency(curr);
	this.exchange      = CmdtyCurrNameSpace.Exchange.UNSET;
	this.secCode       = null;
	this.nameSpaceFree = null;
    }

    public CmdtyCurrID(CmdtyCurrNameSpace.Exchange exchange, String secCode) throws InvalidCmdtyCurrTypeException {
	this.type = Type.SECURITY_EXCHANGE;
	setExchange(exchange);
	setSecCode(secCode);
	this.currency      = null;
	this.nameSpaceFree = null;
    }

    public CmdtyCurrID(String nameSpaceFree, String code, boolean tryMap) throws InvalidCmdtyCurrTypeException {
	
	if ( nameSpaceFree == null )
	    throw new IllegalArgumentException("Name space is null");

	if ( nameSpaceFree.trim().equals("") )
	    throw new IllegalArgumentException("Name space is empty");

	if ( code == null )
	    throw new IllegalArgumentException("Security code is null");

	if ( code.trim().equals("") )
	    throw new IllegalArgumentException("Security code is empty");

	if ( nameSpaceFree.trim().equals(CmdtyCurrNameSpace.CURRENCY) )
	{
	    this.type = Type.CURRENCY;
	    setCurrency(code);
	    this.exchange      = CmdtyCurrNameSpace.Exchange.UNSET;
	    this.secCode       = null;
	    this.nameSpaceFree = null;
	}
	else
	{
	    if ( tryMap ) {
		try {
		    this.type = Type.SECURITY_EXCHANGE;
		    setExchange(nameSpaceFree);
		    this.nameSpaceFree = null;
		} catch ( Exception exc ) {
		    this.type = Type.SECURITY_GENERAL;
		    setNameSpaceFree(nameSpaceFree);
		    this.exchange = CmdtyCurrNameSpace.Exchange.UNSET;
		}
	    }
	    else
	    {
		this.type = Type.SECURITY_GENERAL;
		setNameSpaceFree(nameSpaceFree);
		this.exchange = CmdtyCurrNameSpace.Exchange.UNSET;
	    }
	    
	    setSecCode(code);
	    this.currency = null;	
	}
    }

//    public CmdtyCurrID(String nameSpaceFree, String code) throws InvalidCmdtyCurrTypeException {
//	
//	if ( nameSpaceFree == null )
//	    throw new IllegalArgumentException("Name space is null");
//
//	if ( nameSpaceFree.trim().equals("") )
//	    throw new IllegalArgumentException("Name space is empty");
//	
//	if ( nameSpaceFree.trim().equals(CmdtyCurrNameSpace.CURRENCY) )
//	{
//	    this.type = Type.CURRENCY;
//	    setCurrency(code);
//	    this.exchange      = CmdtyCurrNameSpace.Exchange.UNSET;
//	    this.secCode       = null;
//	    this.nameSpaceFree = null;
//	}
//	else
//	{
//	    this.type = Type.SECURITY_GENERAL;
//	    setNameSpaceFree(nameSpaceFree);
//	    setSecCode(code);
//	    this.currency = null;
//	    this.exchange = CmdtyCurrNameSpace.Exchange.UNSET;
//	}
//    }

    // ---------------------------------------------------------------

    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
        
        if ( type == Type.CURRENCY )
        {
            this.exchange      = CmdtyCurrNameSpace.Exchange.UNSET;
            this.secCode       = null;
            this.nameSpaceFree = null;
        }
        else if ( type == Type.SECURITY_EXCHANGE )
        {
            this.currency      = null;
            this.nameSpaceFree = null;
        }
        else if ( type == Type.SECURITY_GENERAL )
        {
            this.currency = null;
            this.exchange = CmdtyCurrNameSpace.Exchange.UNSET;
        }
    }
    
    // ----------------------------
    
    public Currency getCurrency() throws InvalidCmdtyCurrTypeException {
	if ( type != Type.CURRENCY )
	    throw new InvalidCmdtyCurrTypeException();
	
        return currency;
    }
    
    public void setCurrency(Currency currency) throws InvalidCmdtyCurrTypeException {
	if ( type != Type.CURRENCY )
	    throw new InvalidCmdtyCurrTypeException();
	
        this.currency = currency;
    }

    public void setCurrency(String iso4217CurrCode) throws InvalidCmdtyCurrTypeException {
	if ( iso4217CurrCode == null )
	    throw new IllegalArgumentException("Argument string is null");

	setCurrency(Currency.getInstance(iso4217CurrCode));
    }

    // ----------------------------
    
    public CmdtyCurrNameSpace.Exchange getExchange() throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_EXCHANGE )
	    throw new InvalidCmdtyCurrTypeException();
	
        return exchange;
    }
    
    public void setExchange(CmdtyCurrNameSpace.Exchange exchange) throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_EXCHANGE )
	    throw new InvalidCmdtyCurrTypeException();
	
        this.exchange = exchange;
    }
    
    public void setExchange(String exchangeStr) throws InvalidCmdtyCurrTypeException {
	if ( exchangeStr == null )
	    throw new IllegalArgumentException("Exchange string is null");

	if ( exchangeStr.trim().equals("") )
	    throw new IllegalArgumentException("Exchange string is empty");

        setExchange(CmdtyCurrNameSpace.Exchange.valueOf(exchangeStr.trim()));
    }
    
    public String getSecCode() throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_EXCHANGE && 
             type != Type.SECURITY_GENERAL )
	    throw new InvalidCmdtyCurrTypeException();
	
        return secCode;
    }
    
    public void setSecCode(String secCode) throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_EXCHANGE && 
	     type != Type.SECURITY_GENERAL )
	    throw new InvalidCmdtyCurrTypeException();
	
	if ( secCode == null )
	    throw new IllegalArgumentException("Security code is null");

	if ( secCode.trim().equals("") )
	    throw new IllegalArgumentException("Security code is empty");

        this.secCode = secCode.trim();
    }
    
    // ----------------------------
    
    public String getNameSpaceFree() throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_GENERAL )
	    throw new InvalidCmdtyCurrTypeException();
	
        return nameSpaceFree;
    }
    
    public void setNameSpaceFree(String nameSpace) throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_GENERAL )
	    throw new InvalidCmdtyCurrTypeException();
	
	if ( nameSpace == null )
	    throw new IllegalArgumentException("Name space is null");

	if ( nameSpace.trim().equals("") )
	    throw new IllegalArgumentException("Name space is empty");

        this.nameSpaceFree = nameSpace.trim();
    }
    
    // ---------------------------------------------------------------
    
    public static CmdtyCurrID parse(String str) throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( str == null )
	    throw new IllegalArgumentException("Argument string is null");

	if ( str.equals("") )
	    throw new IllegalArgumentException("Argument string is empty");

	CmdtyCurrID result = new CmdtyCurrID();
	
	int posSep = str.indexOf(SEPARATOR);
	// Plausi ::MAGIC
	if ( posSep <= 3 ||
	     posSep >= str.length() - 2 )
	    throw new InvalidCmdtyCurrIDException();
	
	String nameSpaceLoc = str.substring(0, posSep).trim();
	String currSecCodeLoc = str.substring(posSep + 1, str.length()).trim();
	
	if ( nameSpaceLoc.equals(CmdtyCurrNameSpace.CURRENCY) )
	{
	    result.setType(Type.CURRENCY);
	    result.setCurrency(Currency.getInstance(currSecCodeLoc));
	}	
	else 
	{
	    try {
		CmdtyCurrNameSpace.Exchange exchangeLoc = CmdtyCurrNameSpace.Exchange.valueOf(nameSpaceLoc);
		result.setType(Type.SECURITY_EXCHANGE);
		result.setExchange(exchangeLoc);
		result.setSecCode(currSecCodeLoc);
	    }
	    catch ( Exception exc )
	    {
		result.setType(Type.SECURITY_GENERAL);
		result.setNameSpaceFree(nameSpaceLoc);
		result.setSecCode(currSecCodeLoc);
	    }
	}
	
	return result;
    }
    
    // ---------------------------------------------------------------

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((currency == null) ? 0 : currency.hashCode());
	result = prime * result + ((exchange == null) ? 0 : exchange.hashCode());
	result = prime * result + ((nameSpaceFree == null) ? 0 : nameSpaceFree.hashCode());
	result = prime * result + ((secCode == null) ? 0 : secCode.hashCode());
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	CmdtyCurrID other = (CmdtyCurrID) obj;
	if (type != other.type)
	    return false;
	if (currency == null) {
	    if (other.currency != null)
		return false;
	} else if (!currency.equals(other.currency))
	    return false;
	if (exchange != other.exchange)
	    return false;
	if (nameSpaceFree == null) {
	    if (other.nameSpaceFree != null)
		return false;
	} else if (!nameSpaceFree.equals(other.nameSpaceFree))
	    return false;
	if (secCode == null) {
	    if (other.secCode != null)
		return false;
	} else if (!secCode.equals(other.secCode))
	    return false;
	return true;
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	return toStringShort();
    }

    public String toStringShort() {
	
	String result = "ERROR";

	if (type == Type.CURRENCY) {
	    result = CmdtyCurrNameSpace.CURRENCY.toString() + SEPARATOR + currency.getCurrencyCode();
	} else if (type == Type.SECURITY_EXCHANGE) {
	    result = exchange.toString() + SEPARATOR + secCode;
	} else if (type == Type.SECURITY_GENERAL) {
	    result = nameSpaceFree + SEPARATOR + secCode;
	} else if (type == Type.UNSET) {
	    result = "UNSET";
	}

	return result;
    }

    public String toStringLong() {
	String result = "CmdtyCurrID [type=" + getType();
	
	if ( type == Type.CURRENCY )
	{
	    try {
		result += ", currency='" + getCurrency().getCurrencyCode() + "'";
	    } catch (InvalidCmdtyCurrTypeException e) {
		result += ", currency=" + "ERROR";
	    }
	}
	else if ( type == Type.SECURITY_EXCHANGE )
	{
	    try {
		result += ", exchange='" + getExchange() + "'";
	    } catch (InvalidCmdtyCurrTypeException e) {
		result += ", exchange=" + "ERROR";
	    }
	    
	    try {
		result += ", secCode='" + getSecCode() + "'";
	    } catch (InvalidCmdtyCurrTypeException e) {
		result += ", secCode=" + "ERROR";
	    }
	}
	else if ( type == Type.SECURITY_GENERAL )
	{
	    try {
		result += ", nameSpaceFree='" + getNameSpaceFree() + "'";
	    } catch (InvalidCmdtyCurrTypeException e) {
		result += ", nameSpaceFree=" + "ERROR";
	    }

	    try {
		result += ", secCode='" + getSecCode() + "'";
	    } catch (InvalidCmdtyCurrTypeException e) {
		result += ", secCode=" + "ERROR";
	    }
	}
	
	result += "]";
	
	return result;
    }

}
