package org.example.gnucash.read;

import java.io.File;

import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.GnucashTransactionSplit;
import org.gnucash.read.impl.GnucashFileImpl;

public class GetTrxInfo {
    // BEGIN Example data -- adapt to your needs
    private static String gcshFileName = "example_in.gnucash";
    private static String trxID        = "xyz";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GetTrxInfo tool = new GetTrxInfo();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));

	GnucashTransaction trx = gcshFile.getTransactionByID(trxID);

	try {
	    System.out.println("ID:              " + trx.getId());
	} catch (Exception exc) {
	    System.out.println("ID:              " + "ERROR");
	}

	try {
	    System.out.println("Balance:         " + trx.getBalanceFormatted());
	} catch (Exception exc) {
	    System.out.println("Balance:         " + "ERROR");
	}

	try {
	    System.out.println("Cmdty/Curr:      '" + trx.getCmdtyCurrID() + "'");
	} catch (Exception exc) {
	    System.out.println("Cmdty/Curr:      " + "ERROR");
	}

	try {
	    System.out.println("Description:     '" + trx.getDescription() + "'");
	} catch (Exception exc) {
	    System.out.println("Description:     " + "ERROR");
	}

	// ---

	showSplits(trx);
    }

    // -----------------------------------------------------------------

    private void showSplits(GnucashTransaction trx) {
	System.out.println("");
	System.out.println("Splits:");

	for (GnucashTransactionSplit splt : trx.getSplits()) {
	    System.out.println(" - " + splt.toString());
	}
    }
}
