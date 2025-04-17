package org.gnucash.write.auxiliary;

import org.gnucash.read.auxiliary.GCshOwner;

public interface GCshWritableOwner extends GCshOwner {

	void setJIType(JIType jiType);

	void setInvcType(String invcType);

}
