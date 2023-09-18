package org.gnucash.write.impl.aux;

import org.gnucash.generated.GncV2;
import org.gnucash.read.aux.WrongOwnerJITypeException;
import org.gnucash.read.impl.aux.GCshAddressImpl;
import org.gnucash.read.impl.aux.GCshOwnerImpl;
import org.gnucash.write.GnucashWritableCustomer;
import org.gnucash.write.aux.GCshWritableAddress;
import org.gnucash.write.aux.GCshWritableOwner;

/**
 * Writable implementation in {@link GCshAddressImpl}
 */
public class GCshWritableOwnerImpl extends GCshOwnerImpl 
                                   implements GCshWritableOwner 
{

	/**
	 * @param jwsdpPeer
	 * @throws WrongOwnerJITypeException 
	 */
	public GCshWritableOwnerImpl(JIType jiType, final org.gnucash.generated.OwnerId jwsdpPeer) throws WrongOwnerJITypeException {
		super(jiType, jwsdpPeer);
	}
	
	// -----------------------------------------------------------

	public void setJIType(JIType jiType) {
	    this.jiType = jiType;
	}

	public void setInvcType(String invcType) {
	    this.invcType = invcType;
	}

	public void setInvoiceOwner(GncV2.GncBook.GncGncInvoice.InvoiceOwner invcOwner) {
	    this.invcOwner = invcOwner;
	}

	public void setJobOwner(GncV2.GncBook.GncGncJob.JobOwner jobOwner) {
	    this.jobOwner = jobOwner;
	}

}
