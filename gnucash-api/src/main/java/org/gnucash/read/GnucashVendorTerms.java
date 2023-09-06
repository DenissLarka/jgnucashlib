package org.gnucash.read;

/**
 * Contains vendor terms
 * @see GnucashVendor
 */

public interface GnucashVendorTerms {

  GnucashFile getFile();

  String getType();

  String getValue();
}
