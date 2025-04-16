package org.example.gnucash.messages;

import java.util.Locale;
import java.util.Set;

import org.gnucash.messages.ApplicationMessages;

public class Messages {
  public static final String INVC_READ_ONLY_SLOT_TEXT = "Aus einer Rechnung erzeugt. Für Änderungen müssen Sie die Buchung der Rechnung löschen.";

  public static final String ACTION_JOB = "Auftrag";
  public static final String ACTION_MATERIAL = "Material";
  public static final String ACTION_HOURS = "Stunden";

  public static final String ACTION_INVOICE = "Rechnung";
  public static final String ACTION_BILL = "Lieferantenrechnung";
  public static final String ACTION_PAYMENT = "Zahlung";
  public static final String ACTION_BUY = "Kauf";
  public static final String ACTION_SELL = "Verkauf";

  private static ApplicationMessages bundle;

  public static void main(String[] args) {
    ApplicationMessages.setup();

    bundle = ApplicationMessages.getInstance();

    Set<String> langs = bundle.getTranslations();
    System.out.println(langs);

    String str = bundle.getMessage("ACTION_HOURS");
    System.out.println("DE: " + str);

    str = bundle.getMessage("INVC_READ_ONLY_SLOT_TEXT");
    System.out.print("DE: " + str + " | equals: ");
    System.out.println(INVC_READ_ONLY_SLOT_TEXT.contentEquals(str));

    System.out.println();
    Locale localeNl = new Locale("nl", "NL");
    bundle.setLocale(localeNl);

    str = bundle.getMessage("ACTION_HOURS");
    System.out.println("NL: " + str);

    str = bundle.getMessage("INVC_READ_ONLY_SLOT_TEXT");
    System.out.print("NL: " + str + " | equals: ");
    System.out.println(INVC_READ_ONLY_SLOT_TEXT.contentEquals(str));

    System.out.println();
    Locale localeEn = new Locale("en", "UK");
    bundle.setLocale(localeEn);

    str = bundle.getMessage("ACTION_HOURS");
    System.out.println("EN: " + str);

    str = bundle.getMessage("INVC_READ_ONLY_SLOT_TEXT");
    System.out.print("EN: " + str + " | equals: ");
    System.out.println(INVC_READ_ONLY_SLOT_TEXT.contentEquals(str));

    str = bundle.getMessage("Err_NoVendorId");
    System.out.println("EN: " + str);

    str = bundle.getMessage("Err_NoInvoiceEntry", "Aap", 5);
    System.out.println("EN: " + str);

  }

}
