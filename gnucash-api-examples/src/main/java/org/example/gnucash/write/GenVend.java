package org.example.gnucash.write;

import java.io.File;

import org.gnucash.read.GnucashVendor;
import org.gnucash.write.GnucashWritableVendor;
import org.gnucash.write.impl.GnucashWritableFileImpl;

public class GenVend {
	// BEGIN Example data -- adapt to your needs
	private static String gcshInFileName = "example_in.gnucash";
	private static String gcshOutFileName = "example_out.gnucash";
	private static String name = "Vendorix the Great";
	// END Example data

	// -----------------------------------------------------------------

	public static void main(String[] args) {
		try {
			GenVend tool = new GenVend();
			tool.kernel();
		}
		catch (Exception exc) {
			System.err.println("Execution exception. Aborting.");
			exc.printStackTrace();
			System.exit(1);
		}
	}

	protected void kernel() throws Exception {
		GnucashWritableFileImpl gcshFile = new GnucashWritableFileImpl(new File(gcshInFileName));

		GnucashWritableVendor vend = gcshFile.createWritableVendor();
		vend.setNumber(GnucashVendor.getNewNumber(vend));
		vend.setName(name);
		System.err.println("Vendor: " + vend.getNumber() + " (" + vend.getName() + ")");

		gcshFile.writeFile(new File(gcshOutFileName));

		System.out.println(vend.getId());
	}
}
