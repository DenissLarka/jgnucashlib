package org.gnucash.basetypes;

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
 * to the enum CmdtyCurrNameSpace.SecIdType or to open another enum
 * in this class, if you absolutely need it. After all, this is FOSS...)
 *  
 * @param exchange
 * @param secCode
 */
public class GCshCmdtyID_SecIdType extends GCshCmdtyID {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GCshCmdtyID_SecIdType.class);

    // ---------------------------------------------------------------

    private GCshCmdtyCurrNameSpace.SecIdType secIdType;

    // ---------------------------------------------------------------
    
    public GCshCmdtyID_SecIdType() {
	super();
	type = Type.SECURITY_SECIDTYPE;
    }

    public GCshCmdtyID_SecIdType(GCshCmdtyCurrNameSpace.SecIdType secIdType, String secCode) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	
	super(secIdType.toString(), secCode);
	
	setType(Type.SECURITY_SECIDTYPE);
	setSecIdType(secIdType);
    }

    public GCshCmdtyID_SecIdType(String nameSpace, String code) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {

	super(nameSpace, code);

	setType(Type.SECURITY_SECIDTYPE);
	setSecIdType(nameSpace);
    }

    // ---------------------------------------------------------------

    @Override
    public void setType(Type type) throws InvalidCmdtyCurrIDException {
//        if ( type != Type.SECURITY_SECIDTYPE )
//            throw new InvalidCmdtyCurrIDException();

        super.setType(type);
    }
    
    // ----------------------------
    
    public GCshCmdtyCurrNameSpace.SecIdType getSecIdType() throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_SECIDTYPE )
	    throw new InvalidCmdtyCurrTypeException();
	
        return secIdType;
    }
    
    public void setSecIdType(GCshCmdtyCurrNameSpace.SecIdType secIdType) throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_SECIDTYPE )
	    throw new InvalidCmdtyCurrTypeException();
	
        this.secIdType = secIdType;
    }
    
    public void setSecIdType(String secIdTypeStr) throws InvalidCmdtyCurrTypeException {
	if ( secIdTypeStr == null )
	    throw new IllegalArgumentException("Security ID type string is null");

	if ( secIdTypeStr.trim().equals("") )
	    throw new IllegalArgumentException("Security ID type string is empty");

        setSecIdType(GCshCmdtyCurrNameSpace.SecIdType.valueOf(secIdTypeStr.trim()));
    }
    
    // ---------------------------------------------------------------
    
    public static GCshCmdtyID_SecIdType parse(String str) throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( str == null )
	    throw new IllegalArgumentException("Argument string is null");

	if ( str.equals("") )
	    throw new IllegalArgumentException("Argument string is empty");

	GCshCmdtyID_SecIdType result = new GCshCmdtyID_SecIdType();
	
	int posSep = str.indexOf(SEPARATOR);
	// Plausi ::MAGIC
	if ( posSep <= 3 ||
	     posSep >= str.length() - 2 )
	    throw new InvalidCmdtyCurrIDException();
	
	String nameSpaceLoc = str.substring(0, posSep).trim();
	String currSecCodeLoc = str.substring(posSep + 1, str.length()).trim();
	
	if ( nameSpaceLoc.equals(GCshCmdtyCurrNameSpace.CURRENCY) )
	{
	    throw new InvalidCmdtyCurrTypeException();
	}	
	else 
	{
	    result.setType(Type.SECURITY_SECIDTYPE);
	    result.setNameSpace(nameSpaceLoc);
	    result.setSecIdType(nameSpaceLoc);
	    result.setCode(currSecCodeLoc);
	}
	
	return result;
    }
    
    // ---------------------------------------------------------------

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((type == null) ? 0 : type.hashCode());
	result = prime * result + ((nameSpace == null) ? 0 : nameSpace.hashCode());
	result = prime * result + ((code == null) ? 0 : code.hashCode());
	result = prime * result + ((secIdType == null) ? 0 : secIdType.hashCode());
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
	GCshCmdtyID_SecIdType other = (GCshCmdtyID_SecIdType) obj;
	if (type != other.type)
	    return false;
	if (secIdType != other.secIdType)
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

    @Override
    public String toStringShort() {
	if ( type != Type.SECURITY_SECIDTYPE )
	    return "ERROR";

	String result = secIdType.toString() + SEPARATOR + code;

	return result;
    }

    @Override
    public String toStringLong() {
	if ( type != Type.SECURITY_SECIDTYPE )
	    return "ERROR";

	String result = "CommodityID_SecIdType [";
	
	result += "namespace='" + getNameSpace() + "'";
	
	try {
	    result += ", secidtype='" + getSecIdType() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += ", secidtype=" + "ERROR";
	}
	
	result += ", secCode='" + getCode() + "'";
	
	result += "]";
	
	return result;
    }

}
