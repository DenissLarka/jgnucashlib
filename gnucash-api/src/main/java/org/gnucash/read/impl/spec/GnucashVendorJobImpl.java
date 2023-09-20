package org.gnucash.read.impl.spec;

import java.util.Collection;
import java.util.HashSet;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.impl.GnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashCustomerInvoice;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorJobImpl extends GnucashGenerJobImpl
                                  implements GnucashVendorJob
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorJobImpl.class);

    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    @SuppressWarnings("exports")
    public GnucashVendorJobImpl(
            final GncV2.GncBook.GncGncJob peer,
            final GnucashFile gncFile) {
        super(peer, gncFile);
    }

    public GnucashVendorJobImpl(final GnucashGenerJob job) throws WrongInvoiceTypeException {
	super(job.getJwsdpPeer(), job.getFile());

	// No, we cannot check that first, because the super() method
	// always has to be called first.
	if ( ! job.getOwnerType().equals(GnucashGenerInvoice.TYPE_VENDOR) )
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
    public String getVendorId() {
        return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    public GnucashVendor getVendor() {
        return file.getVendorByID(getVendorId());
    }

    // ---------------------------------------------------------------
    
    @Override
    public Collection<GnucashJobInvoice> getInvoices() throws WrongInvoiceTypeException
    {
      Collection<GnucashJobInvoice> castBills = new HashSet<GnucashJobInvoice>();
      
      for ( GnucashGenerInvoice invc : getGenerInvoices() )
      {
        if ( invc.getType().equals(GnucashGenerInvoice.TYPE_JOB) )
        {
          castBills.add(new GnucashJobInvoiceImpl(invc));
        }
      }
      
      return castBills;
    }

    @Override
    public void addInvoice(GnucashJobInvoice invc) {
        addGenerInvoice(invc);
    }

}
