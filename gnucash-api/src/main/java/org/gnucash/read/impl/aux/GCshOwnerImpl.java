package org.gnucash.read.impl.aux;

import org.gnucash.generated.GncV2;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerInvoice;
import org.gnucash.read.aux.GCshOwner;
import org.gnucash.read.aux.OwnerJITypeUnsetException;
import org.gnucash.read.aux.WrongOwnerJITypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GCshOwnerImpl implements GCshOwner {

  private static final Logger LOGGER = LoggerFactory.getLogger(GCshOwnerImpl.class);
  
  // -----------------------------------------------------------------
  
  /**
   * The file we belong to.
   */
  private final GnucashFile file;
  
  // -----------------------------------------------------------------
	
  protected JIType                                   jiType;
  protected String                                   invcType;
  protected GncV2.GncBook.GncGncInvoice.InvoiceOwner invcOwner; // peer 1
  protected GncV2.GncBook.GncGncJob.JobOwner         jobOwner;  // peer 2
  
  private org.gnucash.generated.OwnerId generOwner;  // NOT peer

  // -----------------------------------------------------------------
  
  @SuppressWarnings("exports")
  public GCshOwnerImpl(
		final GncV2.GncBook.GncGncInvoice.InvoiceOwner peer,
		final GnucashFile gncFile) {
      this.jiType = JIType.INVOICE;
      this.invcOwner = peer;
      this.jobOwner = null;
      this.file = gncFile;
  }

  @SuppressWarnings("exports")
  public GCshOwnerImpl(
		final GncV2.GncBook.GncGncJob.JobOwner peer,
		final GnucashFile gncFile) {
      this.jiType = JIType.JOB;
      this.invcOwner = null;
      this.jobOwner = peer;
      this.file = gncFile;
  }
  
  // ------------------------------

  public GCshOwnerImpl(
	  JIType jiType,
	  String id,
	  final GnucashFile gncFile) throws WrongOwnerJITypeException {
      this.jiType = jiType;
      this.file = gncFile;
      
      if ( jiType == JIType.INVOICE )
      {
	  GnucashGenerInvoice invc = file.getGenerInvoiceByID(id); 
	  this.invcOwner = invc.getOwnerPeerObj();
	  this.invcType = invc.getType();
      }
      else if ( jiType == JIType.JOB )
      {
	  this.jobOwner = file.getGenerJobByID(id).getOwnerPeerObj();
      }
      else if ( jiType == JIType.UNSET )
      {
	  throw new WrongOwnerJITypeException();
      }
  }

//  public GCshOwnerImpl() {
//      this.jiType    = JIType.UNSET;
//      this.invcType  = TYPE_UNSET;
//      this.invcOwner = null;
//      this.jobOwner  = null;
//  }
//  
//  public GCshOwnerImpl(JIType jiType) throws WrongOwnerJITypeException {
//      if ( jiType == JIType.UNSET )
//	  throw new WrongOwnerJITypeException();
//      
//      this.jiType    = jiType;
//      this.invcType  = TYPE_UNSET;
//      this.invcOwner = null;
//      this.jobOwner  = null;
//  }
  
  // ::TODO : Will not work!
//  public GCshOwnerImpl(JIType jiType, String type, String id) throws WrongOwnerJITypeException, WrongInvoiceTypeException {
//      if ( jiType == JIType.UNSET )
//	  throw new WrongOwnerJITypeException();
//      
//      if ( jiType == JIType.INVOICE &&
//	   type.equals(TYPE_UNSET) )
//	  throw new WrongInvoiceTypeException();
//      
//      this.jiType    = jiType;
//      this.invcType  = type;
//      this.id = id;
//      
//      if ( jiType == JIType.INVOICE ) {
//	  org.gnucash.generated.OwnerId jwsdpPeer = new org.gnucash.generated.OwnerId();
//	  jwsdpPeer.setType(type);
//	  jwsdpPeer.setValue(id);
//	  this.invcOwner = GncV2.GncBook.GncGncInvoice.InvoiceOwner;
//      } else if ( jiType == JIType.JOB ) {
//	  org.gnucash.generated.OwnerId jwsdpPeer = new org.gnucash.generated.OwnerId();
//	  jwsdpPeer.setType(type);
//	  jwsdpPeer.setValue(id);
//	  this.jobOwner  = new GncV2.GncBook.GncGncJob.JobOwner;
//      }
//  }
  
  // ::TODO : Will not work!
//  @SuppressWarnings("exports")
//  public GCshOwnerImpl(JIType jiType, org.gnucash.generated.OwnerId jwsdpPeer) throws WrongOwnerJITypeException {
//      if ( jiType == JIType.UNSET )
//	  throw new WrongOwnerJITypeException();
//      
//      this.jiType    = jiType;
//      
//      if ( jwsdpPeer.getType().equals(TYPE_CUSTOMER) ) {
//	this.invcType  = TYPE_CUSTOMER;
//	// TODO
//	// this.invcOwner = getFromFile;
//      }
//      else if ( jwsdpPeer.getType().equals(TYPE_VENDOR) ) {
//	this.invcType  = TYPE_CUSTOMER;
//	// TODO
//	// this.invcOwner = getFromFile;
//      }
//      else if ( jwsdpPeer.getType().equals(TYPE_EMPLOYEE) ) {
//	this.invcType  = TYPE_CUSTOMER;
//	// TODO
//	// this.invcOwner = getFromFile;
//      }
//
//      // else -- what
//      this.jobOwner  = null;
//  }

//  @SuppressWarnings("exports")
//  public GCshOwnerImpl(GncV2.GncBook.GncGncInvoice.InvoiceOwner invcOwner, String invcType) throws WrongInvoiceTypeException {
//      this.jiType = JIType.INVOICE;
//      
//      if ( invcType == TYPE_UNSET )
//	throw new WrongInvoiceTypeException(); 
//      
//      this.invcType  = invcType;
//      this.invcOwner = invcOwner;
//      this.jobOwner  = null;
//  }
  
//  @SuppressWarnings("exports")
//  public GCshOwnerImpl(GncV2.GncBook.GncGncJob.JobOwner jobOwner, String jobType) throws WrongInvoiceTypeException {
//      this.jiType = JIType.JOB;
//
//      if ( jobType == TYPE_UNSET )
//	throw new WrongInvoiceTypeException();
//      
//      this.invcType  = jobType;
//      this.invcOwner = null;
//      this.jobOwner  = jobOwner;
//  }
  
  // -----------------------------------------------------------------
  
  @Override
  public JIType getJIType() {
	return jiType;
  }
  
  @Override
  public String getInvcType() throws WrongOwnerJITypeException {
      if ( jiType != JIType.INVOICE )
	  throw new WrongOwnerJITypeException();
      
      return invcType;
  }
  
  // -----------------------------------------------------------------
  
  @SuppressWarnings("exports")
  public GncV2.GncBook.GncGncInvoice.InvoiceOwner getInvcOwner() throws WrongOwnerJITypeException {
      if ( jiType != JIType.INVOICE )
	  throw new WrongOwnerJITypeException();
      
      return invcOwner;
  }

  @SuppressWarnings("exports")
  public GncV2.GncBook.GncGncJob.JobOwner getJobOwner() throws WrongOwnerJITypeException {
      if ( jiType != JIType.JOB )
	  throw new WrongOwnerJITypeException();
      
      return jobOwner;
  }

  // -----------------------------------------------------------------

  @Override
  public String getId() throws OwnerJITypeUnsetException {
	if ( jiType == JIType.INVOICE ) {
	    return invcOwner.getOwnerId().getValue();
	}
	else if ( jiType == JIType.JOB ) {
	    return jobOwner.getOwnerId().getValue();
	}
	else if ( jiType == JIType.UNSET ) {
	    throw new OwnerJITypeUnsetException();
	}
	
	return "ERROR"; // compiler happy 
  }
  
}
