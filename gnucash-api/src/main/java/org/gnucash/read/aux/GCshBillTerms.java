package org.gnucash.read.aux;

import java.util.List;

import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermDays;
import org.gnucash.generated.GncV2.GncBook.GncGncBillTerm.BilltermProximo;
import org.gnucash.read.GnucashFile;

public interface GCshBillTerms {

    public enum Type {
	DAYS,
	PROXIMO
    }

    // -----------------------------------------------------------

    GnucashFile getFile();

    // -----------------------------------------------------------

    public String getId();

    public int getRefcount();

    public String getName();

    public String getDescription();

    public boolean isInvisible();
    
    // ----------------------------
    
    public Type getType() throws BillTermsTypeException;

    @SuppressWarnings("exports")
    public BilltermDays getDays();

    @SuppressWarnings("exports")
    public BilltermProximo getProximo();

    // ----------------------------
    
    public String getParentId();

    public List<String> getChildren();

}
