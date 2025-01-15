package org.example.gnucash.read;

import java.io.File;

import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.impl.GnucashFileImpl;

public class GetTrxSpltInfo {
  // BEGIN Example data -- adapt to your needs
  private static String gcshFileName = "example_in.gnucash";
  private static String spltID = "2d662bf8877a41f69c69276f2770b140";
  // END Example data

  // -----------------------------------------------------------------

  public static void main(String[] args) {
    try {
      GetTrxSpltInfo tool = new GetTrxSpltInfo();
      tool.kernel();
    } catch (Exception exc) {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  protected void kernel() throws Exception {
    GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));

    GnucashTransactionSplit splt = gcshFile.getTransactionSplitByID(spltID);

    try {
      System.out.println("ID:          " + splt.getId());
    } catch (Exception exc) {
      System.out.println("ID:          " + "ERROR");
    }

    try {
      System.out.println("Account ID:  " + splt.getAccountID());
    } catch (Exception exc) {
      System.out.println("Account ID:  " + "ERROR");
    }

    try {
      System.out.println("Lot:         " + splt.getLotID());
    } catch (Exception exc) {
      System.out.println("Lot:         " + "ERROR");
    }

    try {
      System.out.println("Action:      " + splt.getAction());
    } catch (Exception exc) {
      System.out.println("Action:      " + "ERROR");
    }

    try {
      System.out.println("Value:       " + splt.getValueFormatted());
    } catch (Exception exc) {
      System.out.println("Value:       " + "ERROR");
    }

    try {
      System.out.println("Quantity:    " + splt.getQuantityFormatted());
    } catch (Exception exc) {
      System.out.println("Quantity:    " + "ERROR");
    }

    try {
      System.out.println("Description: '" + splt.getDescription() + "'");
    } catch (Exception exc) {
      System.out.println("Description: " + "ERROR");
    }
  }
}
