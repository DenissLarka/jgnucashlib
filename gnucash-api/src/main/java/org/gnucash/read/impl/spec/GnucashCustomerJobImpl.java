package org.gnucash.read.impl.spec;

import java.util.Collection;
import java.util.HashSet;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.WrongInvoiceTypeException;
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
    @SuppressWarnings("exports")
    public GnucashCustomerJobImpl(
            final GncV2.GncBook.GncGncJob peer,
            final GnucashFile gncFile) {
        super(peer, gncFile);
    }

    public GnucashCustomerJobImpl(final GnucashGenerJob job) throws WrongInvoiceTypeException {
	super(job.getJwsdpPeer(), job.getFile());

	// No, we cannot check that first, because the super() method
	// always has to be called first.
	if ( ! job.getOwnerType().equals(GnucashGenerInvoice.TYPE_CUSTOMER) )
	    throw new WrongInvoiceTypeException();
	    
	for ( GnucashGenerInvoice invc : job.getGenerInvoices() )
	{
	    addInvoice(new GnucashJobInvoiceImpl(invc));
	}
    }

    // ---------------------------------------------------------------
    
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
    
    // ---------------------------------------------------------------
    
    @Override
    public Collection<GnucashJobInvoice> getInvoices() throws WrongInvoiceTypeException
    {
      Collection<GnucashJobInvoice> castInvcs = new HashSet<GnucashJobInvoice>();
      
      for ( GnucashGenerInvoice invc : getGenerInvoices() )
      {
        if ( invc.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
        {
          castInvcs.add(new GnucashJobInvoiceImpl(invc));
        }
      }
      
      return castInvcs;
    }

    public void addInvoice(final GnucashJobInvoice invc) {
        addGenerInvoice(invc);
    }

}
