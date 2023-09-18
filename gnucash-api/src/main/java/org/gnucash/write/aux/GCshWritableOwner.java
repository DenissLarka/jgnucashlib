package org.gnucash.write.aux;

import org.gnucash.read.aux.GCshOwner;

public interface GCshWritableOwner extends GCshOwner {

	public void setJIType(JIType jiType);

	public void setInvcType(String invcType);

}
