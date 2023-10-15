package org.gnucash.read.aux;

import java.util.List;

public interface GCshBillTerms {

    public enum Type {
	DAYS,
	PROXIMO
    }

    // -----------------------------------------------------------

    public String getId();

    public int getRefcount();

    public String getName();

    public String getDescription();

    public boolean isInvisible();
    
    // ----------------------------
    
    public Type getType() throws BillTermsTypeException;

    public GCshBillTermsDays getDays();

    public GCshBillTermsProximo getProximo();

    // ----------------------------
    
    public String getParentId();

    public List<String> getChildren();

}
