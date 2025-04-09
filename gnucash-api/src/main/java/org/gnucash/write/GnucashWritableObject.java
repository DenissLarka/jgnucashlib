package org.gnucash.write;

import org.gnucash.read.GnucashObject;

/**
 * Interface that all interfaces for writable gnucash-entities shall implement
 */
public interface GnucashWritableObject {

	/**
	 * @return the File we belong to.
	 */
	GnucashWritableFile getWritableGnucashFile();

	/**
	 * @param name  the name of the user-defined attribute
	 * @param value the value or null if not set
	 * @see {@link GnucashObject#getUserDefinedAttribute(String)}
	 */
	void setUserDefinedAttribute(String name, String value);
}
