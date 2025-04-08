package org.example.gnucash.read;

import java.io.File;

import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.auxiliary.GCshBillTerms;
import org.gnucash.read.auxiliary.GCshTaxTable;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.GnucashVendorBill;
import org.gnucash.read.spec.GnucashVendorJob;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public class GetVendInfo {
  // BEGIN Example data -- adapt to your needs
  private static String gcshFileName = "example_in.gnucash";
  private static String vendID = "087e1a3d43fa4ef9a9bdd4b4797c4231";
  // END Example data

  // -----------------------------------------------------------------

  public static void main(String[] args) {
    try {
      GetVendInfo tool = new GetVendInfo();
      tool.kernel();
    } catch (Exception exc) {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  protected void kernel() throws Exception {
    GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));

    GnucashVendor vend = gcshFile.getVendorByID(vendID);

    try {
      System.out.println("ID:                " + vend.getId());
    } catch (Exception exc) {
      System.out.println("ID:                " + "ERROR");
    }

    try {
      System.out.println("Number:            '" + vend.getNumber() + "'");
    } catch (Exception exc) {
      System.out.println("Number:            " + "ERROR");
    }

    try {
      System.out.println("Name:              '" + vend.getName() + "'");
    } catch (Exception exc) {
      System.out.println("Name:              " + "ERROR");
    }

    try {
      System.out.println("Address:           '" + vend.getAddress() + "'");
    } catch (Exception exc) {
      System.out.println("Address:           " + "ERROR");
    }

    System.out.println("");
    try {
      String taxTabID = vend.getTaxTableID();
      System.out.println("Tax table ID:      " + taxTabID);

      if (vend.getTaxTableID() != null) {
        try {
          GCshTaxTable taxTab = gcshFile.getTaxTableByID(taxTabID);
          System.out.println("Tax table:        " + taxTab.toString());
        } catch (Exception exc2) {
          System.out.println("Tax table:        " + "ERROR");
        }
      }
    } catch (Exception exc) {
      System.out.println("Tax table ID:      " + "ERROR");
    }

    System.out.println("");
    try {
      String bllTrmID = vend.getTermsID();
      System.out.println("Bill terms ID:     " + bllTrmID);

      if (vend.getTermsID() != null) {
        try {
          GCshBillTerms bllTrm = gcshFile.getBillTermsByID(bllTrmID);
          System.out.println("Bill Terms:        " + bllTrm.toString());
        } catch (Exception exc2) {
          System.out.println("Bill Terms:        " + "ERROR");
        }
      }
    } catch (Exception exc) {
      System.out.println("Bill terms ID:     " + "ERROR");
    }

    System.out.println("");
    System.out.println("Expenses generated:");
    try {
      System.out.println(" - direct: " + vend.getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant.DIRECT));
    } catch (Exception exc) {
      System.out.println(" - direct: " + "ERROR");
    }

    try {
      System.out
          .println(" - via all jobs: " + vend.getExpensesGeneratedFormatted(GnucashGenerInvoice.ReadVariant.VIA_JOB));
    } catch (Exception exc) {
      System.out.println(" - via all jobs: " + "ERROR");
    }

    System.out.println("Outstanding value:");
    try {
      System.out.println(" - direct: " + vend.getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant.DIRECT));
    } catch (Exception exc) {
      System.out.println(" - direct: " + "ERROR");
    }

    try {
      System.out
          .println(" - via all jobs: " + vend.getOutstandingValueFormatted(GnucashGenerInvoice.ReadVariant.VIA_JOB));
    } catch (Exception exc) {
      System.out.println(" - via all jobs: " + "ERROR");
    }

    // ---

    showJobs(vend);
    showBills(vend);
  }

  // -----------------------------------------------------------------

  private void showJobs(GnucashVendor vend) throws WrongInvoiceTypeException {
    System.out.println("");
    System.out.println("Jobs:");
    for (GnucashVendorJob job : vend.getJobs()) {
      System.out.println(" - " + job.toString());
    }
  }

  private void showBills(GnucashVendor vend) throws WrongInvoiceTypeException {
    System.out.println("");
    System.out.println("Bills:");

    System.out.println("Number of open bills: " + vend.getNofOpenBills());

    System.out.println("");
    System.out.println("Paid bills (direct):");
    for (GnucashVendorBill bll : vend.getPaidBills_direct()) {
      System.out.println(" - " + bll.toString());
    }

    System.out.println("");
    System.out.println("Paid bills (via all jobs):");
    for (GnucashJobInvoice bll : vend.getPaidBills_viaAllJobs()) {
      System.out.println(" - " + bll.toString());
    }

    System.out.println("");
    System.out.println("Unpaid bills (direct):");
    for (GnucashVendorBill bll : vend.getUnpaidBills_direct()) {
      System.out.println(" - " + bll.toString());
    }

    System.out.println("");
    System.out.println("Unpaid bills (via all jobs):");
    for (GnucashJobInvoice bll : vend.getUnpaidBills_viaAllJobs()) {
      System.out.println(" - " + bll.toString());
    }
  }
}
