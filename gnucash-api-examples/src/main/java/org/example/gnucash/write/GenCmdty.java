package org.example.gnucash.write;

import java.io.File;

import org.gnucash.basetypes.GCshCmdtyCurrNameSpace;
import org.gnucash.basetypes.GCshCmdtyID_Exchange;
import org.gnucash.write.GnucashWritableCommodity;
import org.gnucash.write.impl.GnucashWritableFileImpl;

public class GenCmdty {
    // BEGIN Example data -- adapt to your needs
    private static String gcshInFileName                = "example_in.gnucash";
    private static String gcshOutFileName               = "example_out.gnucash";
    private static String name                          = "The Walt Disney Co.";
    private static GCshCmdtyCurrNameSpace.Exchange exchange = GCshCmdtyCurrNameSpace.Exchange.NYSE;
    private static String ticker                        = "DIS";
    private static String isin                          = "US2546871060";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GenCmdty tool = new GenCmdty();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	GnucashWritableFileImpl gcshFile = new GnucashWritableFileImpl(new File(gcshInFileName));

	GnucashWritableCommodity cmdty = gcshFile.createWritableCommodity();
	cmdty.setQualifId(new GCshCmdtyID_Exchange(exchange, ticker));
	cmdty.setXCode(isin);
	cmdty.setName(name);

	System.out.println("Commodity to write: " + cmdty.toString());
	gcshFile.writeFile(new File(gcshOutFileName));
	System.out.println("OK");
    }
}
