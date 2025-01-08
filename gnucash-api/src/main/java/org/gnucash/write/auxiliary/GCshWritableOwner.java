package org.gnucash.write.auxiliary;

import org.gnucash.read.auxiliary.GCshOwner;

public interface GCshWritableOwner extends GCshOwner {

	public void setJIType(JIType jiType);

	public void setInvcType(String invcType);

}
