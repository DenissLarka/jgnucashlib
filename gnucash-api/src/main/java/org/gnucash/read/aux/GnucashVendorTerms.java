package org.gnucash.read.aux;

import org.gnucash.read.GnucashFile;

public interface GnucashVendorTerms {

  GnucashFile getFile();

  String getType();

  String getValue();
}
