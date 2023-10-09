package org.gnucash.read.aux;

import org.gnucash.read.GnucashFile;

public interface GCshCustomerTerms {

    GnucashFile getFile();

    // -----------------------------------------------------------

    // public String getId();

    public String getType();

    public String getValue();
}
