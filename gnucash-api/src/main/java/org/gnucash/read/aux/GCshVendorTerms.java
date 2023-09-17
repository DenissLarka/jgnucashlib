package org.gnucash.read.aux;

import org.gnucash.read.GnucashFile;

public interface GCshVendorTerms {

  GnucashFile getFile();

  String getType();

  String getValue();
}
