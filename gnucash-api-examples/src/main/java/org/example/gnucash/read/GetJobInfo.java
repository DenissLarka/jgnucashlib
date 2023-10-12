package org.example.gnucash.read;

import java.io.File;

import org.example.CommandLineTool;
import org.example.CouldNotExecuteException;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.spec.GnucashJobInvoice;
import org.gnucash.read.spec.WrongInvoiceTypeException;

public class GetJobInfo extends CommandLineTool
{
  // BEGIN Example data
  private static String  gcshFileName = "example_in.gnucash";
  private static String  jobID        = "xyz";
  // END Example data
  
  // -----------------------------------------------------------------
  
  public static void main( String[] args )
  {
    try
    {
      GetJobInfo tool = new GetJobInfo ();
      tool.execute(args);
    }
    catch (CouldNotExecuteException exc) 
    {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void kernel() throws Exception
  {
    GnucashFileImpl gcshFile = new GnucashFileImpl(new File(gcshFileName));
    
    GnucashGenerJob job = gcshFile.getGenerJobByID(jobID);
    
    try
    {
      System.out.println("ID:              " + job.getId());
    }
    catch ( Exception exc )
    {
      System.out.println("ID:              " + "ERROR");
    }
    
    try
    {
      System.out.println("Number:          " + job.getNumber());
    }
    catch ( Exception exc )
    {
      System.out.println("Number:          " + "ERROR");
    }
        
    try
    {
      System.out.println("Name:            " + job.getName());
    }
    catch ( Exception exc )
    {
      System.out.println("Name:            " + "ERROR");
    }
    
    try
    {
      System.out.println("Owner type:      " + job.getOwnerType());
    }
    catch ( Exception exc )
    {
      System.out.println("Owner type:      " + "ERROR");
    }

    try
    {
      System.out.println("Owner ID:        " + job.getOwnerId());
    }
    catch ( Exception exc )
    {
      System.out.println("Owner ID:        " + "ERROR");
    }

    System.out.println("");
    try
    {
      System.out.println("Income generated:  " + job.getIncomeGeneratedFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Income generated:  " + "ERROR");
    }

    try
    {
      System.out.println("Outstanding value: " + job.getOutstandingValueFormatted());
    }
    catch ( Exception exc )
    {
      System.out.println("Outstanding value: " + "ERROR");
    }
    
    // ---

    showInvoices(job);
  }

  // -----------------------------------------------------------------

  private void showInvoices(GnucashGenerJob job) throws WrongInvoiceTypeException
  {
    System.out.println("");
    System.out.println("Invoices:");

    System.out.println("");
    System.out.println("Paid invoices:");
    for ( GnucashJobInvoice invc : job.getPaidInvoices() )
    {
      System.out.println(" - " + invc.toString());
    }
    
    System.out.println("");
    System.out.println("Unpaid invoices:");
    for ( GnucashJobInvoice invc : job.getUnpaidInvoices() )
    {
      System.out.println(" - " + invc.toString());
    }
  }
}
