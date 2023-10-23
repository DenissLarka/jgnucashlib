package org.gnucash.currency;

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
 * @param code
 */
public class CmdtyCurrID {
    
    // https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/gnc-commodity.h#L108
    // We do not use the GnuCash-internally used "NONCURRENCY"
    public enum Type {
	CURRENCY,
	SECURITY_EXCHANGE, // name space is semi-formal abbrev. of major world exchange
	SECURITY_MIC,      // name space is formal abbrev. of major world exchange
	SECURITY_GENERAL,  // name space can be freely chosen
	UNSET
    }
    
    // ---------------------------------------------------------------
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdtyCurrID.class);

    public static final char SEPARATOR = ':';

    // ---------------------------------------------------------------

    protected Type    type;
    protected String  nameSpace;
    protected String  code;

    // ---------------------------------------------------------------
    
    public CmdtyCurrID() {
	this.type = Type.UNSET;
    }

    public CmdtyCurrID(String nameSpaceFree, String code) throws InvalidCmdtyCurrTypeException {
	
	if ( nameSpaceFree == null )
	    throw new IllegalArgumentException("Name space is null");

	if ( nameSpaceFree.trim().equals("") )
	    throw new IllegalArgumentException("Name space is empty");

	if ( code == null )
	    throw new IllegalArgumentException("Security code is null");

	if ( code.trim().equals("") )
	    throw new IllegalArgumentException("Security code is empty");

	if ( nameSpaceFree.trim().equals(CmdtyCurrNameSpace.CURRENCY) ) {
	    this.type = Type.CURRENCY;
	} else {
	    this.type = Type.SECURITY_GENERAL;
	}

	setNameSpace(nameSpaceFree.trim());
	setCode(code.trim());
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
    
    public void setType(Type type) throws InvalidCmdtyCurrIDException {
        this.type = type;
    }
    
    public String getNameSpace() {
        return nameSpace;
    }
    
    public void setNameSpace(String nameSpace) throws InvalidCmdtyCurrTypeException {
	if ( nameSpace == null )
	    throw new IllegalArgumentException("Name space is null");

	if ( nameSpace.trim().equals("") )
	    throw new IllegalArgumentException("Name space is empty");

        this.nameSpace = nameSpace.trim();
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String secCode) throws InvalidCmdtyCurrTypeException {
	if ( secCode == null )
	    throw new IllegalArgumentException("Security code is null");

	if ( secCode.trim().equals("") )
	    throw new IllegalArgumentException("Security code is empty");

        this.code = secCode.trim();
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
	    result.setNameSpace(CmdtyCurrNameSpace.CURRENCY);
	    result.setCode(currSecCodeLoc);
	}	
	else 
	{
	    result.setType(Type.SECURITY_GENERAL);
	    result.setNameSpace(nameSpaceLoc);
	    result.setCode(currSecCodeLoc);
	}
	
	return result;
    }
    
    // ---------------------------------------------------------------

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((nameSpace == null) ? 0 : nameSpace.hashCode());
	result = prime * result + ((code == null) ? 0 : code.hashCode());
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
	if (nameSpace == null) {
	    if (other.nameSpace != null)
		return false;
	} else if (!nameSpace.equals(other.nameSpace))
	    return false;
	if (code == null) {
	    if (other.code != null)
		return false;
	} else if (!code.equals(other.code))
	    return false;
	return true;
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	return toStringShort();
    }

    public String toStringShort() {
	
	String result = nameSpace + SEPARATOR + code;

	return result;
    }

    public String toStringLong() {
	String result = "CmdtyCurrID [type=" + getType();
	
	result += ", nameSpace='" + getNameSpace() + "'";
	result += ", code='" + getCode() + "'";
	
	result += "]";
	
	return result;
    }

}
