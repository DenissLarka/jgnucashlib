package org.gnucash.read.impl.auxiliary;

import java.util.ArrayList;
import java.util.List;

import org.gnucash.generated.GncV2;
import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermChild;
import org.gnucash.read.auxiliary.BillTermsTypeException;
import org.gnucash.read.auxiliary.GCshBillTerms;
import org.gnucash.read.auxiliary.GCshBillTermsDays;
import org.gnucash.read.auxiliary.GCshBillTermsProximo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshBillTermsImpl implements GCshBillTerms {

    private static final Logger LOGGER = LoggerFactory.getLogger(GCshBillTermsImpl.class);

    /**
     * the JWSDP-object we are facading.
     */
    private final GncV2.GncBook.GncGncBillTerm jwsdpPeer;

    // ---------------------------------------------------------------

    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    @SuppressWarnings("exports")
    public GCshBillTermsImpl(final GncV2.GncBook.GncGncBillTerm peer) {
	super();

	jwsdpPeer = peer;
    }

    // ---------------------------------------------------------------

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

    public GCshBillTermsDays getDays() {
	if ( jwsdpPeer.getBilltermDays() == null )
	    return null;
	
	GCshBillTermsDays days = new GCshBillTermsDaysImpl(jwsdpPeer.getBilltermDays());
	return days;
    }

    public GCshBillTermsProximo getProximo() {
	if ( jwsdpPeer.getBilltermProximo() == null )
	    return null;
	
	GCshBillTermsProximo prox = new GCshBillTermsProximoImpl(jwsdpPeer.getBilltermProximo());
	return prox;
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

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[GCshBillTermsImpl:");

	buffer.append(" id: ");
	buffer.append(getId());

	buffer.append(" type: ");
	try {
	    buffer.append(getType());
	} catch (BillTermsTypeException e) {
	    buffer.append("ERROR");
	}

	buffer.append(" name: '");
	buffer.append(getName() + "'");

	buffer.append(" description: '");
	buffer.append(getDescription() + "'");

	try {
	    if ( getType() == Type.DAYS ) {
		buffer.append(" " + getDays());
	    } else if ( getType() == Type.PROXIMO ) {
		buffer.append(" " + getProximo());
	    }
	} catch ( Exception exc ) {
	    buffer.append("ERROR");
	}

	buffer.append("]");

	return buffer.toString();
    }
    
}
