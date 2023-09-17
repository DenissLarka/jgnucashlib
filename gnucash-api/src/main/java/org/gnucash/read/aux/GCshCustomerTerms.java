package org.gnucash.read.aux;

import org.gnucash.read.GnucashFile;

public interface GCshCustomerTerms {

  GnucashFile getFile();

  String getType();

  String getValue();
}
