package org.gnucash.read.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashGenerJobImpl implements GnucashGenerJob {

  protected static final Logger LOGGER = LoggerFactory.getLogger(GnucashGenerJobImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    protected final GncV2.GncBook.GncGncJob jwsdpPeer;

    /**
     * The file we belong to.
     */
    protected final GnucashFile file;

    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    @SuppressWarnings("exports")
    public GnucashGenerJobImpl(
            final GncV2.GncBook.GncGncJob peer,
            final GnucashFile gncFile) {
        super();
        
        jwsdpPeer = peer;
        file = gncFile;
    }

    /**
     * The gnucash-file is the top-level class to contain everything.
     * @return the file we are associated with
     */
    public GnucashFile getFile() {
        return file;
    }

    /**
     *
     * @return The JWSDP-Object we are wrapping.
     */
    @SuppressWarnings("exports")
    public GncV2.GncBook.GncGncJob getJwsdpPeer() {
        return jwsdpPeer;
    }


    /**
     * @return the unique-id to identify this object with across name- and hirarchy-changes
     */
    public String getId() {
        assert  jwsdpPeer.getJobGuid().getType().equals("guid");

        String guid = jwsdpPeer.getJobGuid().getValue();
        if (guid == null) {
            throw new IllegalStateException(
                    "job has a null guid-value! guid-type="
                   + jwsdpPeer.getJobGuid().getType());
        }

         return guid;
    }

    /**
     * @return all invoices that refer to this job.
     * @see GnucashGenerJob#getInvoices()
     */
    public Collection<GnucashGenerInvoice> getInvoices() {
        List<GnucashGenerInvoice> retval = new LinkedList<GnucashGenerInvoice>();
        for (GnucashGenerInvoice invoice : getFile().getInvoices()) {
            if (invoice.getJobID().equals(getId())) {
                retval.add(invoice);
            }
        }

        return retval;
    }

    /**
     * @return true if the job is still active
     */
    public boolean isJobActive() {
        return getJwsdpPeer().getJobActive() == 1;
    }

    /**
     * {@inheritDoc}
     */
    public String getJobNumber() {
        return jwsdpPeer.getJobId();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return jwsdpPeer.getJobName();
    }

    // ----------------------------
    
    /**
     * {@inheritDoc}
     */
    public String getOwnerType() {
        return jwsdpPeer.getJobOwner().getOwnerType();
    }

    /**
     * {@inheritDoc}
     */
    public String getOwnerId() {
        assert jwsdpPeer.getJobOwner().getOwnerId().getType().equals("guid");
        return jwsdpPeer.getJobOwner().getOwnerId().getValue();
    }
}
