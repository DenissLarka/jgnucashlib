package org.example.gnucash.read;

import java.io.File;

import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public class GetJobInfo {
    // BEGIN Example data -- adapt to your needs
    private static String gcshFileName = "example_in.gnucash";
    private static String jobID        = "xyz";
    private static String jobName      = "abc";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GetJobInfo tool = new GetJobInfo();
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
	// Var. 1)
	GnucashGenerJob job = gcshFile.getGenerJobByID(jobID);
	// Var. 2)
	// Collection<GnucashGenerJob> jobList = gcshFile.getGenerJobsByName(jobName);
	// GnucashGenerJob job = jobList.iterator().next(); // first element

	try {
	    System.out.println("ID:              " + job.getId());
	} catch (Exception exc) {
	    System.out.println("ID:              " + "ERROR");
	}

	try {
	    System.out.println("Number:          " + job.getNumber());
	} catch (Exception exc) {
	    System.out.println("Number:          " + "ERROR");
	}

	try {
	    System.out.println("Name:            " + job.getName());
	} catch (Exception exc) {
	    System.out.println("Name:            " + "ERROR");
	}

	try {
	    System.out.println("Owner type:      " + job.getOwnerType());
	} catch (Exception exc) {
	    System.out.println("Owner type:      " + "ERROR");
	}

	try {
	    System.out.println("Owner ID:        " + job.getOwnerId());
	} catch (Exception exc) {
	    System.out.println("Owner ID:        " + "ERROR");
	}

	System.out.println("");
	try {
	    System.out.println("Income generated:  " + job.getIncomeGeneratedFormatted());
	} catch (Exception exc) {
	    System.out.println("Income generated:  " + "ERROR");
	}

	try {
	    System.out.println("Outstanding value: " + job.getOutstandingValueFormatted());
	} catch (Exception exc) {
	    System.out.println("Outstanding value: " + "ERROR");
	}

	// ---

	showInvoices(job);
    }

    // -----------------------------------------------------------------

    private void showInvoices(GnucashGenerJob job) throws WrongInvoiceTypeException {
	System.out.println("");
	System.out.println("Invoices:");

	System.out.println("");
	System.out.println("Paid invoices:");
	for (GnucashJobInvoice invc : job.getPaidInvoices()) {
	    System.out.println(" - " + invc.toString());
	}

	System.out.println("");
	System.out.println("Unpaid invoices:");
	for (GnucashJobInvoice invc : job.getUnpaidInvoices()) {
	    System.out.println(" - " + invc.toString());
	}
    }
}
