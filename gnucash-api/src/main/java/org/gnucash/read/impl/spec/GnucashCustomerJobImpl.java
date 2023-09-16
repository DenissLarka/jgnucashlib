package org.gnucash.read.impl.spec;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.impl.GnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GnucashCustomerJobImpl extends GnucashGenerJobImpl
                                    implements GnucashCustomerJob
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashCustomerJobImpl.class);

    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    public GnucashCustomerJobImpl(
            final GncV2.GncBook.GncGncJob peer,
            final GnucashFile gncFile) {
        super(peer, gncFile);
    }

    // ----------------------------
    
    /**
     * {@inheritDoc}
     */
    public String getCustomerType() {
        return getOwnerType();
    }

    /**
     * {@inheritDoc}
     */
    public String getCustomerId() {
        return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    public GnucashCustomer getCustomer() {
        return file.getCustomerByID(getCustomerId());
    }
}
