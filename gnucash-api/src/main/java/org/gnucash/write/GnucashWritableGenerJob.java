package org.gnucash.write;

import org.gnucash.read.GnucashGenerJob;

public interface GnucashWritableGenerJob extends GnucashGenerJob 
{
    
    /**
     * Set the description-text.
     *
     * @param desc the new description
     */
    void setName(String desc);

    /**
     * @param jobActive true is the job is to be (re)activated, false to deactivate
     */
    public void setActive(final boolean jobActive);

}
