package org.example.gnucash.write;

import java.io.File;

import org.gnucash.currency.CmdtyCurrID;
import org.gnucash.currency.CmdtyCurrNameSpace;
import org.gnucash.write.GnucashWritableCommodity;
import org.gnucash.write.impl.GnucashWritableFileImpl;

public class GenCmdty {
    // BEGIN Example data -- adapt to your needs
    private static String gcshInFileName                = "example_in.gnucash";
    private static String gcshOutFileName               = "example_out.gnucash";
    private static String name                          = "The Walt Disney Co.";
    private static CmdtyCurrNameSpace.Exchange exchange = CmdtyCurrNameSpace.Exchange.NYSE;
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
	cmdty.setName(name);
	cmdty.setQualifId(new CmdtyCurrID("NASDAQ", "MSFT", true));
	cmdty.setXCode(isin);

	System.out.println("Commodity to write: " + cmdty.toString());
	gcshFile.writeFile(new File(gcshOutFileName));
	System.out.println("OK");
    }
}
