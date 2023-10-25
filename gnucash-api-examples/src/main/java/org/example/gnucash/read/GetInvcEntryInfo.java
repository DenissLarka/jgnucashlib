package org.example.gnucash.read;

import java.io.File;

import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.spec.GnucashCustomerInvoiceEntryImpl;
import org.gnucash.read.impl.spec.GnucashJobInvoiceEntryImpl;
import org.gnucash.read.impl.spec.GnucashVendorBillEntryImpl;

public class GetInvcEntryInfo {
    // BEGIN Example data -- adapt to your needs
    private static String gcshFileName = "example_in.gnucash";
    private static String invcEntrID   = "xyz";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GetInvcEntryInfo tool = new GetInvcEntryInfo();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));

	// You normally would get the invoice-entry-ID by first choosing
	// a specific invoice (cf. GetInvcInfo), getting its list of entries
	// and then choosing from them.
	GnucashGenerInvoiceEntry entr = gcshFile.getGenerInvoiceEntryByID(invcEntrID);

	try {
	    System.out.println("ID:                " + entr.getId());
	} catch (Exception exc) {
	    System.out.println("ID:                " + "ERROR");
	}

	try {
	    System.out.println("toString (gener.): " + entr.toString());
	} catch (Exception exc) {
	    System.out.println("toString (gener.): " + "ERROR");
	}

	try {
	    if (entr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER)) {
		GnucashCustomerInvoiceEntryImpl spec = new GnucashCustomerInvoiceEntryImpl(entr);
		System.out.println("toString (spec):   " + spec.toString());
	    } else if (entr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR)) {
		GnucashVendorBillEntryImpl spec = new GnucashVendorBillEntryImpl(entr);
		System.out.println("toString (spec):   " + spec.toString());
	    } else if (entr.getType().equals(GnucashGenerInvoice.TYPE_JOB)) {
		GnucashJobInvoiceEntryImpl spec = new GnucashJobInvoiceEntryImpl(entr);
		System.out.println("toString (spec):   " + spec.toString());
	    }
	} catch (Exception exc) {
	    System.out.println("toString (spec):   " + "ERROR");
	}

	System.out.println("");
	try {
	    System.out.println("Type:              " + entr.getType());
	} catch (Exception exc) {
	    System.out.println("Type:              " + "ERROR");
	}

	try {
	    System.out.println("Gener. Invoice ID: " + entr.getGenerInvoiceID());
	} catch (Exception exc) {
	    System.out.println("Gener. Invoice ID: " + "ERROR");
	}

	try {
	    System.out.println("Action:            " + entr.getAction());
	} catch (Exception exc) {
	    System.out.println("Action:            " + "ERROR");
	}

	try {
	    System.out.println("Description:       '" + entr.getDescription() + "'");
	} catch (Exception exc) {
	    System.out.println("Description:       " + "ERROR");
	}

	System.err.println("");
	System.err.println("Taxes:");
	try {
	    if (entr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
		System.out.println("Taxable:           " + entr.isInvcTaxable());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
		System.out.println("Taxable:           " + entr.isBillTaxable());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_JOB))
		System.out.println("Taxable:           " + entr.isJobTaxable());
	} catch (Exception exc) {
	    System.out.println("Taxable:           " + "ERROR");
	}

	try {
	    if (entr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
		System.out.println("Tax perc.:         " + entr.getInvcApplicableTaxPercentFormatted());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
		System.out.println("Tax perc.:         " + entr.getBillApplicableTaxPercentFormatted());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_JOB))
		System.out.println("Tax perc.:         " + entr.getJobApplicableTaxPercentFormatted());
	} catch (Exception exc) {
	    System.out.println("Tax perc.:         " + "ERROR");
	}

	try {
	    System.out.println("Tax-table:");
	    if (entr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
		System.out.println(entr.getInvcTaxTable().toString());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
		System.out.println(entr.getBillTaxTable().toString());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_JOB))
		System.out.println(entr.getJobTaxTable().toString());
	} catch (Exception exc) {
	    System.out.println("ERROR");
	}

	System.out.println("");
	try {
	    if (entr.getType().equals(GnucashGenerInvoice.TYPE_CUSTOMER))
		System.out.println("Price:             " + entr.getInvcPriceFormatted());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_VENDOR))
		System.out.println("Price:             " + entr.getBillPriceFormatted());
	    else if (entr.getType().equals(GnucashGenerInvoice.TYPE_JOB))
		System.out.println("Price:             " + entr.getJobPriceFormatted());
	} catch (Exception exc) {
	    System.out.println("Price:             " + "ERROR");
	}

	try {
	    System.out.println("Quantity:          " + entr.getQuantityFormatted());
	} catch (Exception exc) {
	    System.out.println("Quantity:          " + "ERROR");
	}
    }
}
