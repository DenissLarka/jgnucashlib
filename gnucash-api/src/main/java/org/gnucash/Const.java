package org.gnucash;

public class Const {

  public static final String XML_FORMAT_VERSION = "2.0.0";

  public static final String XML_DATA_TYPE_GUID = "guid";
  public static final String XML_DATA_TYPE_STRING = "string";

  // -----------------------------------------------------------------

  public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
  public static final String STANDARD_DATE_FORMAT_BOOK = "yyyy-MM-dd HH:mm:ss";

  // -----------------------------------------------------------------

  public static final double DIFF_TOLERANCE = 0.005;

  // -----------------------------------------------------------------
  // ::TODO
  // The following constants are Locale-specific. This should be cleaned-up.
  // ::MAGIC
  public static final int HEX = 16;
  public static final String CODEPAGE = "UTF-8";

  /*
   * This is an ugly ad-hoc solution to be cleaned-up in a future release. In this particular case, it is the text added
   * to a specific slot in a specific type of transaction (as automatically generated by GnuCash). Obviously, this
   * should be mapped to locale-specific config file entries.
   */
  // public static final String INVC_READ_ONLY_SLOT_TEXT = "Aus einer Rechnung erzeugt. Für Änderungen müssen Sie die
  // Buchung der Rechnung löschen.";

  // public static final String ACTION_JOB = "Auftrag";
  // public static final String ACTION_MATERIAL = "Material";
  // public static final String ACTION_HOURS = "Stunden";

  // public static final String ACTION_INVOICE = "Rechnung";
  // public static final String ACTION_BILL = "Lieferantenrechnung";
  // public static final String ACTION_PAYMENT = "Zahlung";
  // public static final String ACTION_BUY = "Kauf";
  // public static final String ACTION_SELL = "Verkauf";
}
