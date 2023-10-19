package org.example.gnucash.read;

import java.io.File;

import org.gnucash.currency.CmdtyCurrNameSpace;
import org.gnucash.read.GnucashCommodity;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public class GetCmdtyInfo {
    // BEGIN Example data -- adapt to your needs
    private static String gcshFileName                  = "example_in.gnucash";
    private static CmdtyCurrNameSpace.Exchange exchange = CmdtyCurrNameSpace.Exchange.EURONEXT;
    private static String ticker                        = "MBG";
    private static String isin                          = "DE0007100000";
    private static String searchName                    = "merced";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GetCmdtyInfo tool = new GetCmdtyInfo();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));

	// Choose one of the following variants:
	// Var 1)
	GnucashCommodity cmdty = gcshFile.getCommodityByQualifID(exchange.toString(), ticker);
	// Var 2)
	// GnucashCommodity cmdty = gcshFile.getCommodityByQualifID(exchange + ":" + ticker);
	// Var 3)
	// GnucashCommodity cmdty = gcshFile.getCommodityByXCode(isin);
	// Var 4)
	// Collection<GnucashCommodity> cmdtyList = gcshFile.getCommoditiesByName(searchName);
	// GnucashCommodity cmdty = (GnucashCommodity) cmdtyList.iterator().next(); // first element

	try {
	    System.out.println("Full ID:           " + cmdty.getQualifId());
	} catch (Exception exc) {
	    System.out.println("Full ID:           " + "ERROR");
	}

	try {
	    System.out.println("X-Code:            " + cmdty.getXCode());
	} catch (Exception exc) {
	    System.out.println("X-Code :           " + "ERROR");
	}

	try {
	    System.out.println("Name:              '" + cmdty.getName() + "'");
	} catch (Exception exc) {
	    System.out.println("Name:              " + "ERROR");
	}

	try {
	    System.out.println("Fraction:          " + cmdty.getFraction());
	} catch (Exception exc) {
	    System.out.println("Fraction:          " + "ERROR");
	}
	
	// ---

	showQuotes(cmdty);
    }

    // -----------------------------------------------------------------

    private void showQuotes(GnucashCommodity cust) throws WrongInvoiceTypeException {
	System.out.println("");
	System.out.println("Quotes:");
	System.out.println("NOT IMPLEMENTED YET");
    }
}
