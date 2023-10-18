package org.gnucash.currency;

import java.util.Currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CmdtyCurrID {
    
    public enum Type {
	CURRENCY,
	SECURITY,
	UNSET
    }
    
    // ---------------------------------------------------------------
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdtyCurrID.class);

    public static final char SEPARATOR = ':';

    // ---------------------------------------------------------------

    private Type      type;
    private String    nameSpace;
    private String    secCode;
    private Currency  currency;

    // ---------------------------------------------------------------
    
    public CmdtyCurrID() {
	this.type = Type.UNSET;
    }

    public CmdtyCurrID(Currency curr) {
	this.type      = Type.CURRENCY;
	this.nameSpace = CurrencyNameSpace.NAMESPACE_CURRENCY;
	this.currency  = curr;
	this.secCode   = null;
    }

    public CmdtyCurrID(String nameSpace, String secCode) {
	this.type      = Type.SECURITY;
	this.nameSpace = nameSpace;
	this.currency  = null;
	this.secCode   = secCode;
    }

    // ---------------------------------------------------------------

    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public String getNameSpace() {
        return nameSpace;
    }
    
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace.trim();
    }
    
    public String getSecCode() {
        return secCode;
    }
    
    public void setSecCode(String secCode) {
        this.secCode = secCode.trim();
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setCurrency(String iso4217CurrCode) {
        this.currency = Currency.getInstance(iso4217CurrCode);
    }

    // ---------------------------------------------------------------
    
    
    
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((currency == null) ? 0 : currency.hashCode());
	result = prime * result + ((nameSpace == null) ? 0 : nameSpace.hashCode());
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
	if (currency == null) {
	    if (other.currency != null)
		return false;
	} else if (!currency.equals(other.currency))
	    return false;
	if (nameSpace == null) {
	    if (other.nameSpace != null)
		return false;
	} else if (!nameSpace.equals(other.nameSpace))
	    return false;
	if (secCode == null) {
	    if (other.secCode != null)
		return false;
	} else if (!secCode.equals(other.secCode))
	    return false;
	if (type != other.type)
	    return false;
	return true;
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	return toStringShort();
    }

    public String toStringShort() {
	
	if ( type == Type.UNSET )
	    return "UNSET";
	
	String result = nameSpace + SEPARATOR;
	if ( type == Type.CURRENCY )
	    result += currency.getCurrencyCode();
	else if ( type == Type.SECURITY )
	    result += secCode;
	
	return result;
    }

    public String toStringLong() {
	return "CmdtyCurrID [type=" + type + 
		     ", nameSpace=" + nameSpace + 
		       ", secCode=" + secCode + 
		      ", currency=" + currency + "]";
    }

}
