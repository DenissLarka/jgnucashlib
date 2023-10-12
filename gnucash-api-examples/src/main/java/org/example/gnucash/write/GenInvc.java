package org.example.gnucash.write;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.GnucashGenerInvoiceEntry;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.GnucashVendor;
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.impl.NoTaxTableFoundException;
import org.gnucash.read.impl.aux.WrongOwnerTypeException;
import org.gnucash.read.spec.WrongInvoiceTypeException;
import org.gnucash.write.impl.GnucashWritableFileImpl;
import org.gnucash.write.impl.UnknownInvoiceTypeException;
import org.gnucash.write.spec.GnucashWritableCustomerInvoice;
import org.gnucash.write.spec.GnucashWritableCustomerInvoiceEntry;
import org.gnucash.write.spec.GnucashWritableJobInvoice;
import org.gnucash.write.spec.GnucashWritableJobInvoiceEntry;
import org.gnucash.write.spec.GnucashWritableVendorBill;
import org.gnucash.write.spec.GnucashWritableVendorBillEntry;

import org.example.CommandLineTool;
import org.example.CouldNotExecuteException;

public class GenInvc extends CommandLineTool
{
  enum InvoiceType
  {
    CUSTOMER,
    VENDOR,
    JOB
  }
  
  // BEGIN Example data
  private static String           gcshInFileName    = "example_in.gnucash";
  private static String           gcshOutFileName   = "example_out.gnucash";
  private static InvoiceType      type              = InvoiceType.CUSTOMER; // e.g.
  private static String           ownerID           = "xyz";
  private static String           incExpAcctID      = "xyz";
  private static String           recvblPayblAcctID = "xyz";
  private static String           number            = "1234";
  private static LocalDate        dateOpen          = LocalDate.now();
  private static LocalDate        datePost          = LocalDate.now();
  private static LocalDate        dateDue           = LocalDate.now();
  private static FixedPointNumber amount            = new FixedPointNumber("1250/100");
  // END Example data
  
  // -----------------------------------------------------------------

  private static GnucashAccount   incExpAcct      = null;
  private static GnucashAccount   recvblPayblAcct = null;

  // -----------------------------------------------------------------
  
  public static void main( String[] args )
  {
    try
    {
      GenInvc tool = new GenInvc ();
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
    GnucashWritableFileImpl gcshFile = new GnucashWritableFileImpl(new File(gcshInFileName));

    try 
    {
      incExpAcct = gcshFile.getAccountByID(incExpAcctID);
      System.err.println("Income/expense account:     " + 
                         "Code: " + incExpAcct.getCode() + ", " +
                         "Type: " + incExpAcct.getType() + ", " + 
                         "Name: '" + incExpAcct.getQualifiedName() + "'");
    
      if ( ! incExpAcct.getType().equals(GnucashAccount.TYPE_INCOME) && 
           ! incExpAcct.getType().equals(GnucashAccount.TYPE_EXPENSE) )
      {
        System.err.println("Error: Account is neither an income nor an expenses account");
        throw new WrongAccountTypeException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not instantiate account with ID '" + incExpAcctID + "'");
      throw new NoAccountFoundException();
    }
    
    try 
    {
      recvblPayblAcct = gcshFile.getAccountByID(recvblPayblAcctID);
      System.err.println("Receivable/payable account: " + 
                         "Code: " + recvblPayblAcct.getCode() + ", " +
                         "Type: " + recvblPayblAcct.getType() + ", " + 
                         "Name: '" + recvblPayblAcct.getQualifiedName() + "'");

      if ( ! recvblPayblAcct.getType().equals(GnucashAccount.TYPE_RECEIVABLE) && 
           ! recvblPayblAcct.getType().equals(GnucashAccount.TYPE_PAYABLE) )
      {
        System.err.println("Error: Account is neither a receivable nor a payable account");
        throw new WrongAccountTypeException();
      }
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not instantiate account with ID '" + recvblPayblAcctID + "'");
      throw new NoAccountFoundException();
    }
    
    GnucashGenerInvoice invc = null;
    if ( type == InvoiceType.CUSTOMER )
      invc = doCustomer(gcshFile);
    else if ( type == InvoiceType.VENDOR )
      invc = doVendor(gcshFile);
    else if ( type == InvoiceType.JOB )
      invc = doJob(gcshFile);

    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("New Invoice ID: " + invc.getId());
  }

  // -----------------------------------------------------------------

  private GnucashWritableCustomerInvoice doCustomer(GnucashWritableFileImpl gcshFile)
      throws NoOwnerFoundException, WrongInvoiceTypeException, NoTaxTableFoundException, WrongOwnerTypeException, WrongAccountTypeException
  {
    if ( ! incExpAcct.getType().equals(GnucashAccount.TYPE_INCOME) )
    {
      System.err.println("Error: You selected a customer invoice, but account " + incExpAcctID.toString() + " is not an income account");
      throw new WrongAccountTypeException();
    }
    
    if ( ! recvblPayblAcct.getType().equals(GnucashAccount.TYPE_RECEIVABLE) )
    {
      System.err.println("Error: You selected a customer invoice, but account " + recvblPayblAcct.toString() + " is not an income account");
      throw new WrongAccountTypeException();
    }
    
    GnucashCustomer cust = null;
    try
    {
      cust = gcshFile.getCustomerByID(ownerID);
      System.err.println("Customer: " + cust.getNumber() + " (" + cust.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No customer with ID '" + ownerID + "' found");
      throw new NoOwnerFoundException();
    }
    
    GnucashWritableCustomerInvoice invc = gcshFile.createWritableCustomerInvoice(
                                                        number, 
                                                        cust, 
                                                        incExpAcct, recvblPayblAcct, 
                                                        dateOpen, datePost, dateDue);
    invc.setDescription("Generated by GenInv " + LocalDateTime.now().toString());
    
    GnucashWritableCustomerInvoiceEntry entry1 = invc.createEntry(incExpAcct, 
                                                                  new FixedPointNumber(amount), 
                                                                  new FixedPointNumber(1));
    entry1.setAction(GnucashGenerInvoiceEntry.ACTION_JOB);
    entry1.setDescription("Entry no. 1");
    entry1.setDate(dateOpen.minus(1, ChronoUnit.DAYS));

    GnucashWritableCustomerInvoiceEntry entry2 = invc.createEntry(incExpAcct, 
                                                                  new FixedPointNumber(amount), new FixedPointNumber(1),
                                                                  "DE_USt_Std");
    entry2.setAction(GnucashGenerInvoiceEntry.ACTION_HOURS);
    entry2.setDescription("Entry no. 2");
    entry2.setDate(dateOpen);

    GnucashWritableCustomerInvoiceEntry entry3 = invc.createEntry(incExpAcct, new FixedPointNumber(amount), 
                                                                  new FixedPointNumber(1),
                                                                  gcshFile.getTaxTableByName("FR_TVA_Std"));
    entry3.setAction(GnucashGenerInvoiceEntry.ACTION_MATERIAL);
    entry3.setDescription("Entry no. 3");
    entry3.setDate(dateOpen.plus(1, ChronoUnit.DAYS));

    invc.post(incExpAcct, recvblPayblAcct, 
              datePost, dateDue);
    
    return invc;
  }

  private GnucashWritableVendorBill doVendor(GnucashWritableFileImpl gcshFile)
      throws NoOwnerFoundException, WrongInvoiceTypeException, NoTaxTableFoundException, WrongOwnerTypeException, WrongAccountTypeException
  {
    if ( ! incExpAcct.getType().equals(GnucashAccount.TYPE_EXPENSE) )
    {
      System.err.println("Error: You selected a vendor bill, but account " + incExpAcctID.toString() + " is not an expenses account");
      throw new WrongAccountTypeException();
    }
    
    if ( ! recvblPayblAcct.getType().equals(GnucashAccount.TYPE_PAYABLE) )
    {
      System.err.println("Error: You selected a vendor bill, but account " + recvblPayblAcct.toString() + " is not a payable account");
      throw new WrongAccountTypeException();
    }
    
    GnucashVendor vend = null;
    try
    {
      vend = gcshFile.getVendorByID(ownerID);
      System.err.println("Vendor: " + vend.getNumber() + " (" + vend.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No vendor with ID '" + ownerID + "' found");
      throw new NoOwnerFoundException();
    }
    
    GnucashWritableVendorBill bll = gcshFile.createWritableVendorBill(
                                                    number, 
                                                    vend, 
                                                    incExpAcct, recvblPayblAcct, 
                                                    dateOpen, datePost, dateDue);
    bll.setDescription("Generated by GenInv " + LocalDateTime.now().toString());
    
    GnucashWritableVendorBillEntry entry1 = bll.createEntry(incExpAcct, 
                                                            new FixedPointNumber(amount), 
                                                            new FixedPointNumber(1));
    entry1.setAction(GnucashGenerInvoiceEntry.ACTION_JOB);
    entry1.setDescription("Entry no. 1");
    entry1.setDate(dateOpen.minus(1, ChronoUnit.DAYS));
    
    GnucashWritableVendorBillEntry entry2 = bll.createEntry(incExpAcct, 
                                                            new FixedPointNumber(amount), 
                                                            new FixedPointNumber(1),
                                                            "DE_USt_Std");
    entry2.setAction(GnucashGenerInvoiceEntry.ACTION_HOURS);
    entry2.setDescription("Entry no. 2");
    entry2.setDate(dateOpen);

    GnucashWritableVendorBillEntry entry3 = bll.createEntry(incExpAcct, 
                                                            new FixedPointNumber(amount), 
                                                            new FixedPointNumber(1),
                                                            gcshFile.getTaxTableByName("FR_TVA_Std"));
    entry3.setAction(GnucashGenerInvoiceEntry.ACTION_MATERIAL);
    entry3.setDescription("Entry no. 3");
    entry3.setDate(dateOpen.plus(1, ChronoUnit.DAYS));

    bll.post(incExpAcct, recvblPayblAcct, 
             datePost, dateDue);

    return bll;
  }
  
  private GnucashWritableJobInvoice doJob(GnucashWritableFileImpl gcshFile)
      throws NoOwnerFoundException, WrongInvoiceTypeException, NoTaxTableFoundException, WrongOwnerTypeException, WrongAccountTypeException, UnknownInvoiceTypeException
  {
    GnucashGenerJob job = null;
    try
    {
      job = gcshFile.getGenerJobByID(ownerID);
      System.err.println("(Gener.) job: " + job.getNumber() + " (" + job.getName() + ")");
    }
    catch ( Exception exc )
    {
      System.err.println("Error: No (gener.) job with ID '" + ownerID + "' found");
      throw new NoOwnerFoundException();
    }
    
    if ( job.getOwnerType().equals(GCshOwner.TYPE_CUSTOMER) &&
         ! incExpAcct.getType().equals(GnucashAccount.TYPE_INCOME) )
    {
      System.err.println("Error: You selected a customer job invoice, but account " + incExpAcctID.toString() + " is not an income account");
      throw new WrongAccountTypeException();
    }
    else if ( job.getOwnerType().equals(GCshOwner.TYPE_VENDOR) &&
              ! incExpAcct.getType().equals(GnucashAccount.TYPE_EXPENSE) )
    {
      System.err.println("Error: You selected a vendor job invoice, but account " + incExpAcctID.toString() + " is not an expenses account");
      throw new WrongAccountTypeException();
    }
    
    if ( job.getOwnerType().equals(GCshOwner.TYPE_CUSTOMER) &&
         ! recvblPayblAcct.getType().equals(GnucashAccount.TYPE_RECEIVABLE) )
    {
      System.err.println("Error: You selected a customer job invoice, but account " + recvblPayblAcct.toString() + " is not a receivable account");
      throw new WrongAccountTypeException();
    }
    else if ( job.getOwnerType().equals(GCshOwner.TYPE_VENDOR) &&
              ! recvblPayblAcct.getType().equals(GnucashAccount.TYPE_PAYABLE) )
    {
      System.err.println("Error: You selected a vendor job invoice, but account " + recvblPayblAcct.toString() + " is not a payable account");
      throw new WrongAccountTypeException();
    }
    
    GnucashWritableJobInvoice invc = gcshFile.createWritableJobInvoice(
                                                    number, 
                                                    job, 
                                                    incExpAcct, recvblPayblAcct, 
                                                    dateOpen, datePost, dateDue);
    invc.setDescription("Generated by GenInv " + LocalDateTime.now().toString());
    
    GnucashWritableJobInvoiceEntry entry1 = invc.createEntry(incExpAcct, 
                                                             new FixedPointNumber(amount), 
                                                             new FixedPointNumber(1));
    entry1.setAction(GnucashGenerInvoiceEntry.ACTION_JOB);
    entry1.setDescription("Entry no. 1");
    entry1.setDate(dateOpen.minus(1, ChronoUnit.DAYS));

    GnucashWritableJobInvoiceEntry entry2 = invc.createEntry(incExpAcct, 
                                                             new FixedPointNumber(amount), 
                                                             new FixedPointNumber(1),
                                                             "DE_USt_Std");
    entry2.setAction(GnucashGenerInvoiceEntry.ACTION_HOURS);
    entry2.setDescription("Entry no. 2");
    entry2.setDate(dateOpen);

    GnucashWritableJobInvoiceEntry entry3 = invc.createEntry(incExpAcct, 
                                                             new FixedPointNumber(amount), 
                                                             new FixedPointNumber(1),
                                                             gcshFile.getTaxTableByName("FR_TVA_Std"));
    entry3.setAction(GnucashGenerInvoiceEntry.ACTION_MATERIAL);
    entry3.setDescription("Entry no. 3");
    entry3.setDate(dateOpen.plus(1, ChronoUnit.DAYS));

    invc.post(incExpAcct, recvblPayblAcct, 
              datePost, dateDue);

    return invc;
  }
}
