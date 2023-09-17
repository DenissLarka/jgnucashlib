package org.gnucash.read.aux;

import org.gnucash.read.GnucashFile;

public interface GnucashCustomerTerms {

  GnucashFile getFile();

  String getType();

  String getValue();
}
