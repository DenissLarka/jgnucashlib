package org.example.gnucash.write;

import java.io.File;

import org.gnucash.basetypes.GCshCmdtyCurrNameSpace;
import org.gnucash.basetypes.GCshCmdtyID_Exchange;
import org.gnucash.basetypes.GCshCmdtyID_SecIdType;
import org.gnucash.write.GnucashWritableCommodity;
import org.gnucash.write.impl.GnucashWritableFileImpl;

public class GenCmdty {
    // BEGIN Example data -- adapt to your needs
    private static String gcshInFileName  = "example_in.gnucash";
    private static String gcshOutFileName = "example_out.gnucash";
    
    private static String cmdty1_name     = "The Walt Disney Co.";
    private static GCshCmdtyCurrNameSpace.Exchange cmdty1_exchange = GCshCmdtyCurrNameSpace.Exchange.NYSE;
    private static String cmdty1_ticker   = "DIS";
    private static String cmdty1_isin     = "US2546871060";
    
    private static String cmdty2_name     = "Unilever Plc";
    private static GCshCmdtyCurrNameSpace.SecIdType cmdty2_secIdType = GCshCmdtyCurrNameSpace.SecIdType.ISIN;
    private static String cmdty2_isin     = "GB00B10RZP78";
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

	GnucashWritableCommodity cmdty1 = gcshFile.createWritableCommodity();
	cmdty1.setQualifId(new GCshCmdtyID_Exchange(cmdty1_exchange, cmdty1_ticker));
	cmdty1.setXCode(cmdty1_isin);
	cmdty1.setName(cmdty1_name);
	System.out.println("Commodity no. 1 to write: " + cmdty1.toString());

	GnucashWritableCommodity cmdty2 = gcshFile.createWritableCommodity();
	cmdty2.setQualifId(new GCshCmdtyID_SecIdType(cmdty2_secIdType, cmdty2_isin));
	cmdty2.setXCode(cmdty2_isin);
	cmdty2.setName(cmdty2_name);
	System.out.println("Commodity no. 2 to write: " + cmdty2.toString());

	gcshFile.writeFile(new File(gcshOutFileName));
	System.out.println("OK");
    }
}
