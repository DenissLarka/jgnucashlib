package org.gnucash.read.auxiliary;

import java.util.List;

public interface GCshBillTerms {

	enum Type {
		DAYS,
		PROXIMO
	}

	// -----------------------------------------------------------

	String getId();

	int getRefcount();

	String getName();

	String getDescription();

	boolean isInvisible();

	// ----------------------------

	Type getType() throws BillTermsTypeException;

	GCshBillTermsDays getDays();

	GCshBillTermsProximo getProximo();

	// ----------------------------

	String getParentId();

	List<String> getChildren();

}
