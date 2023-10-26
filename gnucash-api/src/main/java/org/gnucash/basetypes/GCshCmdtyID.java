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
 * to the enum CmdtyCurrNameSpace.Exchange or to open another enum
 * in this class, if you absolutely need it. After all, this is FOSS...)
 *  
 * @param exchange
 * @param secCode
 */
public class GCshCmdtyID extends GCshCmdtyCurrID {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GCshCmdtyID.class);

    // ---------------------------------------------------------------

    // ::EMPTY

    // ---------------------------------------------------------------
    
    public GCshCmdtyID() {
	super();
	type = Type.SECURITY_GENERAL;
    }

    public GCshCmdtyID(String nameSpaceFree, String code) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	
	super(nameSpaceFree, code);
	
//	if ( getType() != Type.SECURITY_GENERAL )
//	    throw new InvalidCmdtyCurrTypeException();

	setType(Type.SECURITY_GENERAL);
    }

    public GCshCmdtyID(GCshCmdtyCurrID cmdtyCurrID) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	
	super(cmdtyCurrID.getNameSpace(), cmdtyCurrID.getCode());

	if ( getType() == Type.CURRENCY )
	    throw new InvalidCmdtyCurrTypeException();

	setType(Type.SECURITY_GENERAL);
    }

    // ---------------------------------------------------------------

    @Override
    public void setType(Type type) throws InvalidCmdtyCurrIDException {
//        if ( type != Type.SECURITY_GENERAL )
//            throw new InvalidCmdtyCurrIDException();

        super.setType(type);
    }
    
    // ---------------------------------------------------------------
    
    public static GCshCmdtyID parse(String str) throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( str == null )
	    throw new IllegalArgumentException("Argument string is null");

	if ( str.equals("") )
	    throw new IllegalArgumentException("Argument string is empty");

	GCshCmdtyID result = new GCshCmdtyID();
	
	int posSep = str.indexOf(SEPARATOR);
	// Plausi ::MAGIC
	if ( posSep <= 3 ||
	     posSep >= str.length() - 2 )
	    throw new InvalidCmdtyCurrIDException();
	
	String nameSpaceLoc = str.substring(0, posSep).trim();
	String currSecCodeLoc = str.substring(posSep + 1, str.length()).trim();
	
	if ( nameSpaceLoc.equals(GCshCmdtyCurrNameSpace.CURRENCY) )
	{
	    throw new InvalidCmdtyCurrIDException();
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

    // ::EMPTY

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	return toStringShort();
    }

    @Override
    public String toStringShort() {
	if (type != Type.SECURITY_GENERAL)
	    return "ERROR";

	String result = super.toStringShort();

	return result;
    }

    @Override
    public String toStringLong() {
	if (type != Type.SECURITY_GENERAL)
	    return "ERROR";

	String result = "CommodityID [";
	
	result += "nameSpace='" + getNameSpace() + "'";
	result += ", secCode='" + getCode() + "'";
	
	result += "]";
	
	return result;
    }

}
