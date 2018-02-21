/**
 * GnucashWritableFile.java
 * Created on 16.05.2005
 * (c) 2005 by "Wolschon Softwaredesign und Beratung".
 * <p>
 * Permission is granted to use, modify, publish and sub-license this code
 * as specified in the contract. If nothing else is specified these rights
 * are given non-exclusively with no restrictions solely to the contractor(s).
 * If no specified otherwise I reserve the right to use, modify, publish and
 * sub-license this code to other parties myself.
 * <p>
 * Otherwise, this code is made available under GPLv3 or later.
 * <p>
 * -----------------------------------------------------------
 * major Changes:
 * 16.05.2005 - initial version
 * ...
 */
package biz.wolschon.fileformats.gnucash;

import org.gnucash.generated.GncV2;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.xml.GnucashAccount;
import org.gnucash.xml.GnucashCustomer;
import org.gnucash.xml.GnucashFile;
import org.gnucash.xml.GnucashJob;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * created: 16.05.2005 <br/>
 *
 * Extension of GnucashFile that allows writing. <br/>
 * All the instances for accounts,... it returns can be assumed
 * to implement the respetive *Writable-interfaces.
 *
 * @see GnucashFile
 * @see biz.wolschon.fileformats.gnucash.jwsdpimpl.GnucashFileWritingImpl
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 *
 */
public interface GnucashWritableFile extends GnucashFile, GnucashWritableObject {

	/**
	 * @return true if this file has been modified.
	 */
	boolean isModified();

	/**
	 * The value is guaranteed not to be bigger then the maximum of
	 * the current system-time and the modification-time in the file
	 * at the time of the last (full) read or sucessfull write.<br/
	 * It is thus suitable to detect if the file has been modified outside of
	 * this library
	 * @return the time in ms (compatible with File.lastModified) of the last write-operation
	 */
	long getLastWriteTime();

	/**
	 * @param pB true if this file has been modified.
	 * @see {@link #isModified()}
	 */
	void setModified(boolean pB);


	/**
	 * Write the data to the given file.
	 * That file becomes the new file returned by
	 * {@link GnucashFile#getGnucashFile()}
	 * @param file the file to write to
	 * @throws IOException kn io-poblems
	 * @throws JAXBException on xml-problems or content by the xml-schema
	 */
	void writeFile(File file) throws IOException, JAXBException;

	/**
	 * @return the underlying JAXB-element
	 */
	GncV2 getRootElement();


	/**
	 * @param id the unique id of the customer to look for
	 * @return the customer or null if it's not found
	 */
	GnucashWritableCustomer getCustomerByID(String id);


	/**
	 *
	 * @return a read-only collection of all accounts that have no parent
	 */
	Collection<? extends GnucashWritableAccount> getWritableRootAccounts();

	/**
	 *
	 * @return a read-only collection of all accounts
	 */
	Collection<? extends GnucashWritableAccount> getWritableAccounts();


	/**
	 * @see GnucashFile#getTransactionByID(String)
	 * @return A changable version of the transaction.
	 */
	GnucashWritableTransaction getTransactionByID(String id);

	/**
	 * @see GnucashFile#getInvoiceByID(String)
	 * @param id the id to look for
	 * @return A changable version of the invoice.
	 */
	GnucashWritableInvoice getInvoiceByID(String id);

	/**
	 * @see GnucashFile#getAccountByName(String)
	 * @param name the name to look for
	 * @return A changable version of the account.
	 */
	GnucashWritableAccount getAccountByName(String name);

	/**
	 * @param type the type to look for
	 * @return A changable version of all accounts of that type.
	 */
	Collection<GnucashWritableAccount> getAccountsByType(String type);

	/**
	 * @see GnucashFile#getAccountByID(String)
	 * @param id the id of the account to fetch
	 * @return A changable version of the account or null of not found.
	 */
	GnucashWritableAccount getAccountByID(String id);

	/**
	 * @see GnucashFile#getJobByID(String)
	 * @param jobID the id of the job to fetch
	 * @return A changable version of the job or null of not found.
	 */
	GnucashWritableJob getJobByID(String jobID);

	/**
	 * @param jnr the job-number to look for.
	 * @return the (first) jobs that have this number or null if not found
	 */
	GnucashWritableJob getJobByNumber(final String jnr);

	/**
	 * @return all jobs as writable versions.
	 */
	Collection<GnucashWritableJob> getWritableJobs();

	/**
	 * Add a new currency.<br/>
	 * If the currency already exists, add a new price-quote for it.
	 * @param pCmdtySpace the namespace (e.g. "GOODS" or "ISO4217")
	 * @param pCmdtyId the currency-name
	 * @param conversionFactor the conversion-factor from the base-currency (EUR).
	 * @param pCmdtyNameFraction number of decimal-places after the comma
	 * @param pCmdtyName common name of the new currency
	 * @throws JAXBException if we cannot create the underlying XML-entities
	 */
	public void addCurrency(final String pCmdtySpace, final String pCmdtyId, final FixedPointNumber conversionFactor, final int pCmdtyNameFraction, final String pCmdtyName) throws JAXBException;

	/**
	 * @see GnucashFile#getTransactions()
	 * @return writable versions of all transactions in the book.
	 */
	Collection getWritableTransactions();

	/**
	 * @return a new transaction with no splits that is already added to this file
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableTransaction createWritableTransaction() throws JAXBException;

	/**
	 * @return a new transaction with no splits that is already added to this file
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableTransaction createWritableTransaction(final String id) throws JAXBException;


	/**
	 *
	 * @param impl the transaction to remove.
	 * @throws JAXBException if we have issues with the XML-backend
	 */
	void removeTransaction(GnucashWritableTransaction impl) throws JAXBException;


	/**
	 * @return a new customer with no values that is already added to this file
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableCustomer createWritableCustomer() throws JAXBException;

	/**
	 * @return a new job with no values that is already added to this file
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableJob createWritableJob(final GnucashCustomer customer) throws JAXBException;

	/**
	 * @return a new job with no values that is already added to this file
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableJob createWritableJob(final String id, final GnucashCustomer customer) throws JAXBException;


	/**
	 * @return a new account that is already added to this file as a top-level account
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableAccount createWritableAccount() throws JAXBException;

	/**
	 * @return a new account that is already added to this file as a top-level account
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableAccount createWritableAccount(final String id) throws JAXBException;

	/**
	 * @return a new invoice with no entries that is already added to this file
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableInvoice createWritableInvoice(final String invoiceNumber,
												 final GnucashJob job,
												 final GnucashAccount accountToTransferMoneyTo,
												 final java.util.Date dueDate) throws JAXBException;

	/**
	 * FOR USE BY EXTENSIONS ONLY
	 * @return a new invoice with no entries that is already added to this file
	 * @throws JAXBException if we have problems with the xml-backend
	 */
	GnucashWritableInvoice createWritableInvoice(final String internalID,
												 final String invoiceNumber,
												 final GnucashJob job,
												 final GnucashAccount accountToTransferMoneyTo,
												 final java.util.Date dueDate) throws JAXBException;

	/**
	 * @param impl the account to remove
	 */
	void removeAccount(GnucashWritableAccount impl);

	/**
	 * THIS METHOD IS ONLY TO BE USED BY EXTENSIONS TO THIS LIBRARY!<br/>
	 * @throws JAXBException on XML-backend-errors
	 * @return a new customer
	 * @param id the id the customer shall have
	 */
	GnucashWritableCustomer createWritableCustomer(String id) throws JAXBException;


}
