package org.gnucash.read;

//other imports

import java.util.Collection;

//automatically created logger for debug and error -output
//automatically created propertyChangeListener-Support


/**
 * Interface all gnucash-entities implement.
 */
public interface GnucashObject {

    /**
     * @param name the name of the user-defined attribute
     * @return the value or null if not set
     */
    String getUserDefinedAttribute(String name);
    
    /**
     * 
     * @return all keys that can be used with ${@link #getUserDefinedAttribute(String)}}.
     */
    Collection<String> getUserDefinedAttributeKeys();

    /**
     * @return the File we belong to.
     */
    GnucashFile getGnucashFile();

}
