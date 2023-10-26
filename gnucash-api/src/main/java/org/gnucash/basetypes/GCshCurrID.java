package org.gnucash.basetypes;

import java.util.Currency;

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
public class GCshCurrID extends GCshCmdtyCurrID {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GCshCurrID.class);

    // ---------------------------------------------------------------

    private Currency currency;

    // ---------------------------------------------------------------
    
    public GCshCurrID() {
	super();
	type = Type.CURRENCY;
    }

    public GCshCurrID(Currency curr) throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {

	super(GCshCmdtyCurrNameSpace.CURRENCY, curr.getCurrencyCode());
	
	setType(Type.CURRENCY);
	setCurrency(curr);
    }

    public GCshCurrID(String currStr) throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {

	super(GCshCmdtyCurrNameSpace.CURRENCY, currStr);
	
	setType(Type.CURRENCY);
	setCurrency(currStr);
    }

    public GCshCurrID(String nameSpaceFree, String code) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	
	super(nameSpaceFree, code);

	if ( getType() != Type.CURRENCY )
	    throw new InvalidCmdtyCurrTypeException();

	setType(Type.CURRENCY);
	setCurrency(code);
    }

    public GCshCurrID(GCshCmdtyCurrID cmdtyCurrID) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	
	super(cmdtyCurrID.getNameSpace(), cmdtyCurrID.getCode());

	if ( getType() != Type.CURRENCY )
	    throw new InvalidCmdtyCurrTypeException();

	setType(Type.CURRENCY);
	setCurrency(code);
    }

    // ---------------------------------------------------------------

    @Override
    public void setType(Type type) throws InvalidCmdtyCurrIDException {
//        if ( type != Type.CURRENCY )
//            throw new InvalidCmdtyCurrIDException();

        super.setType(type);
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
	
	if ( currency == null )
	    throw new IllegalArgumentException("Argument currency is null");

	this.currency = currency;
    }

    public void setCurrency(String iso4217CurrCode) throws InvalidCmdtyCurrTypeException {
	if ( iso4217CurrCode == null )
	    throw new IllegalArgumentException("Argument string is null");

	setCurrency(Currency.getInstance(iso4217CurrCode));
    }

    // ---------------------------------------------------------------
    
    public static GCshCurrID parse(String str) throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( str == null )
	    throw new IllegalArgumentException("Argument string is null");

	if ( str.equals("") )
	    throw new IllegalArgumentException("Argument string is empty");

	GCshCurrID result = new GCshCurrID();
	
	int posSep = str.indexOf(SEPARATOR);
	// Plausi ::MAGIC
	if ( posSep <= 3 ||
	     posSep >= str.length() - 2 )
	    throw new InvalidCmdtyCurrIDException();
	
	String nameSpaceLoc = str.substring(0, posSep).trim();
	String currSecCodeLoc = str.substring(posSep + 1, str.length()).trim();
	
	if ( nameSpaceLoc.equals(GCshCmdtyCurrNameSpace.CURRENCY) )
	{
	    result.setType(Type.CURRENCY);
	    result.setNameSpace(nameSpaceLoc);
	    result.setCode(currSecCodeLoc);
	    result.setCurrency(Currency.getInstance(currSecCodeLoc));
	}	
	else 
	{
	    throw new InvalidCmdtyCurrIDException();
	}
	
	return result;
    }
    
    // ---------------------------------------------------------------

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((currency == null) ? 0 : currency.hashCode());
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
	GCshCurrID other = (GCshCurrID) obj;
	if (type != other.type)
	    return false;
	if (currency == null) {
	    if (other.currency != null)
		return false;
	} else if (!currency.equals(other.currency))
	    return false;
	return true;
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	return toStringShort();
    }

    @Override
    public String toStringShort() {
	if (type != Type.CURRENCY)
	    return "ERROR";

	String result = GCshCmdtyCurrNameSpace.CURRENCY.toString() + 
		        SEPARATOR + 
		        currency.getCurrencyCode();

	return result;
    }

    @Override
    public String toStringLong() {
	if (type != Type.CURRENCY)
	    return "ERROR";

	String result = "CurrencyID [";

	result += "nameSpace='" + getNameSpace() + "'";
	result += ", secCode='" + getCode() + "'";

	try {
	    result += ", currency='" + getCurrency().getCurrencyCode() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += ", currency=" + "ERROR";
	}

	result += "]";

	return result;
    }

}
