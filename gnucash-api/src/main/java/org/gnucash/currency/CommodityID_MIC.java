package org.gnucash.currency;

import org.gnucash.currency.CmdtyCurrID.Type;
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
 * to the enum CmdtyCurrNameSpace.MIC or to open another enum
 * in this class, if you absolutely need it. After all, this is FOSS...)
 *  
 * @param mic
 * @param secCode
 */
public class CommodityID_MIC extends CommodityID {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CommodityID_MIC.class);

    // ---------------------------------------------------------------

    private CmdtyCurrNameSpace.MIC mic;

    // ---------------------------------------------------------------
    
    public CommodityID_MIC() {
	super();
	type = Type.SECURITY_MIC;
    }

    public CommodityID_MIC(CmdtyCurrNameSpace.MIC mic, String secCode) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {
	
	super(mic.toString(), secCode);
	
	setType(Type.SECURITY_MIC);
	setMIC(mic);
    }

    public CommodityID_MIC(String nameSpace, String code) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException {

	super(nameSpace, code);

	setType(Type.SECURITY_MIC);
	setMIC(nameSpace);
    }

    // ---------------------------------------------------------------

    @Override
    public void setType(Type type) throws InvalidCmdtyCurrIDException {
//        if ( type != Type.SECURITY_MIC )
//            throw new InvalidCmdtyCurrIDException();

        super.setType(type);
    }
    
    // ----------------------------
    
    public CmdtyCurrNameSpace.MIC getMIC() throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_MIC )
	    throw new InvalidCmdtyCurrTypeException();
	
        return mic;
    }
    
    public void setMIC(CmdtyCurrNameSpace.MIC mic) throws InvalidCmdtyCurrTypeException {
	if ( type != Type.SECURITY_MIC )
	    throw new InvalidCmdtyCurrTypeException();
	
        this.mic = mic;
    }
    
    public void setMIC(String micStr) throws InvalidCmdtyCurrTypeException {
	if ( micStr == null )
	    throw new IllegalArgumentException("MIC string is null");

	if ( micStr.trim().equals("") )
	    throw new IllegalArgumentException("MIC string is empty");

        setMIC(CmdtyCurrNameSpace.MIC.valueOf(micStr.trim()));
    }
    
    // ---------------------------------------------------------------
    
    public static CommodityID_MIC parse(String str) throws InvalidCmdtyCurrIDException, InvalidCmdtyCurrTypeException {
	if ( str == null )
	    throw new IllegalArgumentException("Argument string is null");

	if ( str.equals("") )
	    throw new IllegalArgumentException("Argument string is empty");

	CommodityID_MIC result = new CommodityID_MIC();
	
	int posSep = str.indexOf(SEPARATOR);
	// Plausi ::MAGIC
	if ( posSep <= 3 ||
	     posSep >= str.length() - 2 )
	    throw new InvalidCmdtyCurrIDException();
	
	String nameSpaceLoc = str.substring(0, posSep).trim();
	String currSecCodeLoc = str.substring(posSep + 1, str.length()).trim();
	
	if ( nameSpaceLoc.equals(CmdtyCurrNameSpace.CURRENCY) )
	{
	    throw new InvalidCmdtyCurrTypeException();
	}	
	else 
	{
	    result.setType(Type.SECURITY_MIC);
	    result.setNameSpace(nameSpaceLoc);
	    result.setMIC(nameSpaceLoc);
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
	result = prime * result + ((mic == null) ? 0 : mic.hashCode());
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
	CommodityID_MIC other = (CommodityID_MIC) obj;
	if (type != other.type)
	    return false;
	if (mic != other.mic)
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
	if ( type != Type.SECURITY_MIC )
	    return "ERROR";

	String result = mic.toString() + SEPARATOR + code;

	return result;
    }

    @Override
    public String toStringLong() {
	if ( type != Type.SECURITY_MIC )
	    return "ERROR";

	String result = "CommodityID_MIC [";
	
	result += "namespace='" + getNameSpace() + "'";
	
	try {
	    result += ", mic='" + getMIC() + "'";
	} catch (InvalidCmdtyCurrTypeException e) {
	    result += ", mic=" + "ERROR";
	}
	
	result += ", secCode='" + getCode() + "'";
	
	result += "]";
	
	return result;
    }

}
