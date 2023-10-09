package org.gnucash.read.impl.aux;

import java.util.ArrayList;
import java.util.List;

import org.gnucash.generated.GncV2;
import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermChild;
import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermDays;
import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermProximo;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.aux.BillTermsTypeException;
import org.gnucash.read.aux.GCshBillTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshBillTermsImpl implements GCshBillTerms {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCshBillTermsImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    private final GncV2.GncBook.GncGncBillTerm jwsdpPeer;

    /**
     * The file we belong to.
     */
    private final GnucashFile file;

    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    @SuppressWarnings("exports")
    public GCshBillTermsImpl(final GncV2.GncBook.GncGncBillTerm peer, final GnucashFile gncFile) {
	super();

	jwsdpPeer = peer;
	file = gncFile;
    }

    /**
     * The gnucash-file is the top-level class to contain everything.
     * 
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
    public GncV2.GncBook.GncGncBillTerm getJwsdpPeer() {
	return jwsdpPeer;
    }

    // -----------------------------------------------------------

    public String getId() {
	return jwsdpPeer.getBilltermGuid().getValue();
    }

    public int getRefcount() {
	return jwsdpPeer.getBilltermRefcount();
    }

    public String getName() {
	return jwsdpPeer.getBilltermName();
    }

    public String getDescription() {
	return jwsdpPeer.getBilltermDesc();
    }

    public boolean isInvisible() {
	if (jwsdpPeer.getBilltermInvisible() == 1)
	    return true;
	else
	    return false;
    }
    
    // ------------------------

    public Type getType() throws BillTermsTypeException {
	if ( getDays() != null )
	    return Type.DAYS;
	else if ( getProximo() != null )
	    return Type.PROXIMO;
	else
	    throw new BillTermsTypeException();
    }

    @SuppressWarnings("exports")
    public BilltermDays getDays() {
	return jwsdpPeer.getBilltermDays();
    }

    @SuppressWarnings("exports")
    public BilltermProximo getProximo() {
	return jwsdpPeer.getBilltermProximo();
    }

    // ------------------------

    public String getParentId() {
	if ( jwsdpPeer.getBilltermParent() == null )
	    return null;

	return jwsdpPeer.getBilltermParent().getValue();
    }

    public List<String> getChildren() {

	if ( jwsdpPeer.getBilltermChild() == null )
	    return null;
	
	List<String> result = new ArrayList<String>();

	for (BilltermChild child : jwsdpPeer.getBilltermChild()) {
	    result.add(new String(child.getValue()));
	}

	return result;
    }

}
