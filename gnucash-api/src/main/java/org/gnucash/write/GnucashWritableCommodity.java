package org.gnucash.write;

import org.gnucash.currency.InvalidCmdtyCurrIDException;
import org.gnucash.currency.InvalidCmdtyCurrTypeException;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.write.impl.ObjectCascadeException;

/**
 * Commodity that can be modified
 */
public interface GnucashWritableCommodity extends GnucashCommodity
{

    void remove() throws InvalidCmdtyCurrTypeException, ObjectCascadeException, InvalidCmdtyCurrIDException;
    
    // ---------------------------------------------------------------

    public void setQualifId(CmdtyCurrID qualifId) throws InvalidCmdtyCurrTypeException;

    public void setXCode(String xCode);

    public void setName(String name);

    public void setFraction(Integer fract);
}
