/**
 * GnucashFileWritingImpl.java
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
package org.gnucash.write.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;


import org.gnucash.generated.GncAccount;
import org.gnucash.generated.GncBudget;
import org.gnucash.generated.GncCountData;
import org.gnucash.generated.GncTransaction;
import org.gnucash.generated.GncV2;
import org.gnucash.generated.Slot;
import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashAccount;
import org.gnucash.read.GnucashCustomer;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashInvoice;
import org.gnucash.read.GnucashInvoiceEntry;
import org.gnucash.read.GnucashJob;
import org.gnucash.read.GnucashTaxTable;
import org.gnucash.read.GnucashTransaction;
import org.gnucash.read.impl.GnucashAccountImpl;
import org.gnucash.read.impl.GnucashCustomerImpl;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.GnucashTaxTableImpl;
import org.gnucash.read.impl.GnucashTransactionImpl;
import org.gnucash.read.impl.spec.GnucashCustomerJobImpl;
import org.gnucash.write.GnucashWritableAccount;
import org.gnucash.write.GnucashWritableCustomer;
import org.gnucash.write.GnucashWritableFile;
import org.gnucash.write.GnucashWritableInvoice;
import org.gnucash.write.GnucashWritableCustomerJob;
import org.gnucash.write.GnucashWritableTransaction;
import org.gnucash.write.GnucashWritableTransactionSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

/**
 * @author Marcus@Wolschon.biz
 * created: 16.05.2005
 * <p>
 * Implementation of GnucashWritableFile based on GnucashFileImpl.
 * @see GnucashFileImpl
 */
public class GnucashFileWritingImpl extends GnucashFileImpl implements GnucashWritableFile {

	/**
	 *
	 */
	private static final int HEX = 16;

	/**
	 * true if this file has been modified.
	 */
	private boolean modified = false;

	/**
	 * @see {@link #getLastWriteTime()}
	 */
	private long lastWriteTime = 0;

	/**
	 * @return true if this file has been modified
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * @return the time in ms (compatible with File.lastModified) of the last write-operation
	 */
	public long getLastWriteTime() {
		return lastWriteTime;
	}

	/**
	 * @param pModified true if this file has been modified false after save, load or undo of changes
	 */
	public void setModified(final boolean pModified) {
		//boolean old = this.modified;
		modified = pModified;
		//      if (propertyChange != null)
		//         propertyChange.firePropertyChange("modified", old, pModified);
	}

	/**
	 * Keep the count-data up to date.
	 * The count-data is re-calculated on the fly before
	 * writing but we like to keep our internal model up-to-date
	 * just to be defensive.
	 * <gnc:count-data cd:type="commodity">2</gnc:count-data>
	 * <gnc:count-data cd:type="account">394</gnc:count-data>
	 * <gnc:count-data cd:type="transaction">1576</gnc:count-data>
	 * <gnc:count-data cd:type="schedxaction">4</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncCustomer">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncJob">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncTaxTable">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncInvoice">5</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncEntry">18</gnc:count-data>
	 *
	 * @param type the type to set it for
	 */
	protected void incrementCountDataFor(final String type) {

		if (type == null) {
			throw new IllegalArgumentException("null type given");
		}

		List l = getRootElement().getGncBook().getGncCountData();
		for (Iterator iter = l.iterator(); iter.hasNext(); ) {
			GncCountData gncCountData = (GncCountData) iter.next();

			if (type.equals(gncCountData.getCdType())) {
				gncCountData.setValue(gncCountData.getValue() + 1);
				setModified(true);
			}
		}
	}

	/**
	 * Keep the count-data up to date.
	 * The count-data is re-calculated on the fly before
	 * writing but we like to keep our internal model up-to-date
	 * just to be defensive.
	 * <gnc:count-data cd:type="commodity">2</gnc:count-data>
	 * <gnc:count-data cd:type="account">394</gnc:count-data>
	 * <gnc:count-data cd:type="transaction">1576</gnc:count-data>
	 * <gnc:count-data cd:type="schedxaction">4</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncCustomer">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncJob">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncTaxTable">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncInvoice">5</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncEntry">18</gnc:count-data>
	 *
	 * @param type the type to set it for
	 */
	protected void decrementCountDataFor(final String type) {

		if (type == null) {
			throw new IllegalArgumentException("null type given");
		}

		List l = getRootElement().getGncBook().getGncCountData();
		for (Iterator iter = l.iterator(); iter.hasNext(); ) {
			GncCountData gncCountData = (GncCountData) iter.next();

			if (type.equals(gncCountData.getCdType())) {
				gncCountData.setValue(gncCountData.getValue() - 1);
				setModified(true);
			}
		}
	}

	/**
	 * keep the count-data up to date.
	 * <gnc:count-data cd:type="commodity">2</gnc:count-data>
	 * <gnc:count-data cd:type="account">394</gnc:count-data>
	 * <gnc:count-data cd:type="transaction">1576</gnc:count-data>
	 * <gnc:count-data cd:type="schedxaction">4</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncCustomer">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncJob">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncTaxTable">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncInvoice">5</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncEntry">18</gnc:count-data>
	 *
	 * @param type  the type to set it for
	 * @param count the value
	 */
	@SuppressWarnings("unchecked")
	protected void setCountDataFor(final String type, final int count) {

		if (type == null) {
			throw new IllegalArgumentException("null type given");
		}

		List<GncCountData> l = getRootElement().getGncBook().getGncCountData();
		for (GncCountData gncCountData : l) {
			if (type.equals(gncCountData.getCdType())) {
				gncCountData.setValue(count);
				setModified(true);
			}
		}
	}

	/**
	 * @param file the file to load
	 * @throws IOException on bsic io-problems such as a FileNotFoundException
	 */
	public GnucashFileWritingImpl(final File file) throws IOException {
		super(file);
		setModified(false);
	}

	/**
	 * Used by GnucashTransactionImpl.createTransaction to add a new Transaction
	 * to this file.
	 *
	 * @see GnucashTransactionImpl#createSplit(GncTransaction.TrnSplits.TrnSplit)
	 */
	@SuppressWarnings("unchecked")
	protected void addTransaction(final GnucashTransactionImpl impl) {
		incrementCountDataFor("transaction");

		getRootElement().getGncBook().getBookElements().add(impl.getJwsdpPeer());
		setModified(true);
		transactionid2transaction.put(impl.getId(), impl);

	}

	/**
	 * @return all TaxTables defined in the book
	 * @see {@link GnucashTaxTable}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<GnucashTaxTable> getTaxTables() {
		if (taxTablesById == null) {

			taxTablesById = new HashMap<String, GnucashTaxTable>();
			List bookElements = this.getRootElement().getGncBook().getBookElements();
			for (Object bookElement : bookElements) {
				if (bookElement instanceof GncV2.GncBook.GncGncTaxTable) {
					GncV2.GncBook.GncGncTaxTable jwsdpPeer = (GncV2.GncBook.GncGncTaxTable) bookElement;
					GnucashTaxTableImpl gnucashTaxTable = new GnucashTaxTableImpl(jwsdpPeer, this);
					taxTablesById.put(gnucashTaxTable.getId(), gnucashTaxTable);
				}
			}
		}

		return taxTablesById.values();
	}

	/**
	 * @see {@link GnucashFileImpl#loadFile(java.io.File)}
	 */
	@Override
	protected void loadFile(final File pFile) throws IOException {
		super.loadFile(pFile);
		lastWriteTime = Math.max(pFile.lastModified(), System.currentTimeMillis());
	}

	/**
	 * @see GnucashFileImpl#setRootElement(GncV2)
	 */
	@Override
	public void setRootElement(final GncV2 rootElement) {
		super.setRootElement(rootElement);
	}

	/**
	 * @see GnucashWritableFile#writeFile(java.io.File)
	 */
	public void writeFile(final File file) throws IOException {

		if (file == null) {
			throw new IllegalArgumentException(
					"null not allowed for field this.file");
		}

		if (file.exists()) {
			throw new IllegalArgumentException(
					"Given file '" + file.getAbsolutePath() + "' does exist!");
		}

		checkAllCountData();

		setFile(file);

		OutputStream out = new FileOutputStream(file);
		out = new BufferedOutputStream(out);
		if (file.getName().endsWith(".gz")) {
			out = new GZIPOutputStream(out);
		}

		//Writer writer = new NamespaceAdderWriter(new OutputStreamWriter(out, "ISO8859-15"));
		Writer writer = new NamespaceAdderWriter(new OutputStreamWriter(out, "UTF-8"));
		try {

			JAXBContext context = getJAXBContext();
			Marshaller marshaller = context.createMarshaller();

			//marshaller.marshal(getRootElement(), writer);
			marshaller.marshal(getRootElement(), new WritingContentHandler(writer));

			setModified(false);
		}
		catch (JAXBException e) {
			LOGGER.error(e.getMessage(), e);
		}

		finally {
			writer.close();
		}
		lastWriteTime = Math.max(file.lastModified(), System.currentTimeMillis());
	}

	/**
	 * Calculate and set the correct valued for all the following count-data.<br/>
	 * Also check the that only valid elements are in the book-element
	 * and that they have the correct order.
	 * <p>
	 * <gnc:count-data cd:type="commodity">2</gnc:count-data>
	 * <gnc:count-data cd:type="account">394</gnc:count-data>
	 * <gnc:count-data cd:type="transaction">1576</gnc:count-data>
	 * <gnc:count-data cd:type="schedxaction">4</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncCustomer">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncJob">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncTaxTable">2</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncInvoice">5</gnc:count-data>
	 * <gnc:count-data cd:type="gnc:GncEntry">18</gnc:count-data>
	 */
	@SuppressWarnings("unchecked")
	private void checkAllCountData() {

		int commodity = 0;
		int account = 0;
		int transaction = 0;
		int cntCustomer = 0;
		int GncJob = 0;
		int GncTaxTable = 0;
		int GncInvoice = 0;
		int GncEntry = 0;
		/**
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link GncTemplateTransactions}
		 * {@link GncGncInvoice}
		 * {@link GncGncEntry}
		 * {@link GncGncJob}
		 * {@link GncGncTaxTable}
		 * {@link GncCommodity}
		 * {@link GncGncCustomer}
		 * {@link GncSchedxaction}
		 * {@link GncBudget}
		 * {@link GncAccount}
		 * {@link GncPricedb}
		 * {@link GncTransaction}
		 */
		List<Object> bookElements = getRootElement().getGncBook().getBookElements();
		for (Object element : bookElements) {
			if (element instanceof GncV2.GncBook.GncCommodity) {
				commodity++;
			} else if (element instanceof GncAccount) {
				account++;
			} else if (element instanceof GncTransaction) {
				transaction++;
			} else if (element instanceof GncV2.GncBook.GncGncCustomer) {
				cntCustomer++;
			} else if (element instanceof GncV2.GncBook.GncGncJob) {
				GncJob++;
			} else if (element instanceof GncV2.GncBook.GncGncTaxTable) {
				GncTaxTable++;
			} else if (element instanceof GncV2.GncBook.GncGncInvoice) {
				GncInvoice++;
			} else if (element instanceof GncV2.GncBook.GncGncEntry) {
				GncEntry++;
			} else if (element instanceof GncV2.GncBook.GncTemplateTransactions) {
			} else if (element instanceof GncV2.GncBook.GncSchedxaction) {
			} else if (element instanceof GncBudget) {
			} else if (element instanceof GncV2.GncBook.GncPricedb) {
			} else if (element instanceof GncV2.GncBook.GncGncEmployee) {
			} else if (element instanceof GncV2.GncBook.GncGncBillTerm) {
			} else if (element instanceof GncV2.GncBook.GncGncVendor) {
			} else {
				throw new IllegalStateException("Unecpected element in GNC:Book found! <" + element.toString() + ">");
			}
		}

		setCountDataFor("commodity", commodity);
		setCountDataFor("account", account);
		setCountDataFor("transaction", transaction);
		setCountDataFor("gnc:GncCustomer", cntCustomer);
		setCountDataFor("gnc:GncJob", GncJob);
		setCountDataFor("gnc:GncTaxTable", GncTaxTable);
		setCountDataFor("gnc:GncInvoice", GncInvoice);
		setCountDataFor("gnc:GncEntry", GncEntry);
		// make sure the correct sort-order of the entity-types is obeyed in writing.
		// (we do not enforce this in the xml-schema to allow for reading out of order files)
		java.util.Collections.sort(bookElements, new BookElementsSorter());
	}

	/**
	 * @return the underlying JAXB-element
	 * @see GnucashWritableFile#getRootElement()
	 */
	@Override
	public GncV2 getRootElement() {
		return super.getRootElement();
	}

	/**
	 * create a GUID for a new element.
	 * (guids are globally unique and not tied
	 * to a specific kind of entity)
	 *
	 * @return the new gnucash-guid
	 */
	protected String createGUID() {

		int len = "74e492edf60d6a28b6c1d01cc410c058".length();

		StringBuffer sb = new StringBuffer(Long.toHexString(System.currentTimeMillis()));

		while (sb.length() < len) {
			sb.append(Integer.toHexString((int) (Math.random() * HEX)).charAt(0));
		}

		return sb.toString();
	}

	/**
	 */
	protected GncTransaction createGncTransaction() {
		GncTransaction retval = getObjectFactory().createGncTransaction();
		incrementCountDataFor("transaction");
		return retval;
	}

	/**
	 */
	protected GncTransaction.TrnSplits.TrnSplit createGncTransactionTypeTrnSplitsTypeTrnSplitType() {
		GncTransaction.TrnSplits.TrnSplit retval = getObjectFactory().createGncTransactionTrnSplitsTrnSplit();
		//incrementCountDataFor();
		return retval;
	}

	/**
	 */
	protected GncV2.GncBook.GncGncInvoice createGncGncInvoiceType() {
		GncV2.GncBook.GncGncInvoice retval = getObjectFactory().createGncV2GncBookGncGncInvoice();
		incrementCountDataFor("gnc:GncInvoice");
		return retval;
	}

	/**
	 */
	protected GncV2.GncBook.GncGncEntry createGncGncEntryType() {
		GncV2.GncBook.GncGncEntry retval = getObjectFactory().createGncV2GncBookGncGncEntry();
		incrementCountDataFor("gnc:GncEntry");
		return retval;
	}

	/**
	 */
	protected GncV2.GncBook.GncGncCustomer createGncGncCustomerType() {
		GncV2.GncBook.GncGncCustomer retval = getObjectFactory().createGncV2GncBookGncGncCustomer();
		incrementCountDataFor("gnc:GncCustomer");
		return retval;
	}

	/**
	 * @return the jaxb-job
	 */
	public GncV2.GncBook.GncGncJob createGncGncJobType() {
		GncV2.GncBook.GncGncJob retval = getObjectFactory().createGncV2GncBookGncGncJob();
		incrementCountDataFor("gnc:GncJob");
		return retval;
	}

	/**
	 * @see GnucashFile#getCustomerByID(java.lang.String)
	 */
	@Override
	public GnucashWritableCustomer getCustomerByID(final String arg0) {
		return (GnucashWritableCustomer) super.getCustomerByID(arg0);
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @see GnucashFileImpl#createAccount(GncAccount)
	 */
	@Override
	protected GnucashAccount createAccount(final GncAccount jwsdpAccount) {
		GnucashAccount account = new GnucashAccountWritingImpl(jwsdpAccount, this);
		return account;
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @see GnucashFileImpl#createInvoice(GncV2.GncBook.GncGncInvoice)
	 */
	@Override
	protected GnucashInvoice createInvoice(final GncV2.GncBook.GncGncInvoice jwsdpInvoice) {
		GnucashInvoice invoice = new GnucashInvoiceWritingImpl(jwsdpInvoice, this);
		return invoice;
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @param jwsdpInvoiceEntry the xml-object to represent in the entry.
	 * @return a new invoice-entry, already registred with this file.
	 * @see GnucashFileImpl#createInvoiceEntry(GncV2.GncBook.GncGncEntry)
	 */
	@Override
	protected GnucashInvoiceEntry createInvoiceEntry(final GncV2.GncBook.GncGncEntry jwsdpInvoiceEntry) {
		GnucashInvoiceEntry entry = new GnucashInvoiceEntryWritingImpl(jwsdpInvoiceEntry, this);
		return entry;
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @see GnucashFileImpl#createJob(GncV2.GncBook.GncGncJob)
	 */
	@Override
	protected GnucashCustomerJobImpl createJob(final GncV2.GncBook.GncGncJob jwsdpjob) {
		GnucashCustomerJobImpl job = new GnucashCustomerJobWritingImpl(jwsdpjob, this);
		return job;
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @param jwsdpCustomer the jwsdp-object the customer shall wrap
	 * @return the new customer
	 * @see GnucashFileImpl#createCustomer(GncV2.GncBook.GncGncCustomer)
	 */
	@Override
	protected GnucashCustomerImpl createCustomer(final GncV2.GncBook.GncGncCustomer jwsdpCustomer) {
		GnucashCustomerImpl customer = new GnucashCustomerWritingImpl(jwsdpCustomer, this);
		return customer;
	}

	/**
	 * This overridden method creates the writable version of the returned object.
	 *
	 * @see GnucashFileImpl#createTransaction(GncTransaction)
	 */
	@Override
	protected GnucashTransactionImpl createTransaction(final GncTransaction jwsdpTransaction) {
		GnucashTransactionImpl account = new GnucashTransactionWritingImpl(jwsdpTransaction, this);
		return account;
	}

	/**
	 * (c) 2005 by Wolschon Softwaredesign und Beratung - All rights reserved<br>
	 * Project: gnucashReader <br>
	 * GnucashFileWritingImpl.java <br>
	 * <p>
	 * Helper-Class needed for writing Gnucash-Files that are binary-identical
	 * to what gnucash itself writes.
	 *
	 * @author <a href="mailto:marcus@wolschon.biz">Marcus Wolschon</a>
	 */
	private static class WritingContentHandler implements ContentHandler {

		/**
		 * where to write it to.
		 */
		private final Writer writer;

		/**
		 * Our logger for debug- and error-ourput.
		 */
		private static final Logger LOGGER = LoggerFactory.getLogger(WritingContentHandler.class);

		/**
		 * @param pwriter where to write it to
		 */
		public WritingContentHandler(final Writer pwriter) {
			writer = pwriter;
		}

		/**
		 * @see org.xml.sax.ContentHandler#endDocument()
		 */
		public void endDocument() throws SAXException {

			try {
				writer.write("\n\n"
						+ "<!-- Local variables: -->\n"
						+ "<!-- mode: xml        -->\n"
						+ "<!-- End:             -->\n");
			}
			catch (IOException e) {
				LOGGER.error("Problem in WritingContentHandler", e);
			}

		}

		/**
		 * @see org.xml.sax.ContentHandler#startDocument()
		 */
		public void startDocument() throws SAXException {

			try {
				//old gnucash-version writer.write("<?xml version=\"1.0\"?>\n");
				writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
			}
			catch (IOException e) {
				LOGGER.error("Problem in WritingContentHandler", e);
			}
		}

		private final String encodeme[] = new String[] {"&", ">", "<"};
		private final String encoded[] = new String[] {"&amp;", "&gt;", "&lt;"};

		/**
		 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			try {
				if (last_was == LAST_WAS_OPENELEMENT) {
					writer.write(">");
				}

				if (last_was == LAST_WAS_CLOSEELEMENT) {
					return;
				}

				// make shure GUIDs are written with non-capital letters
				if (isGUID) {
					String s = new String(ch, start, length);
					writer.write(s.toLowerCase());
				} else {

					StringBuffer sb = new StringBuffer();
					sb.append(ch, start, length);

					for (int j = 0; j < encodeme.length; j++) {
						int index = 0;
						while ((index = sb.indexOf(encodeme[j], index)) != -1) {
							sb.replace(index,
									index + encodeme[j].length(),
									encoded[j]);
							index += encoded[j].length() - encodeme[j].length() + 1;
						}

					}

					//                    String s = sb.toString();
					//                    if(s.indexOf("bis 410") != -1) {
					//                     System.err.println(s+"---"+Integer.toHexString(s.charAt(s.length()-1)));
					//                    }

					writer.write(sb.toString());
				}

				last_was = LAST_WAS_CHARACTERDATA;
			}
			catch (IOException e) {
				LOGGER.error("Problem in WritingContentHandler", e);
			}

		}

		public void ignorableWhitespace(final char[] ch, final int start, final int length) {
			/*try {
				writer.write(ch, start, length);
                last_was = LAST_WAS_CHARACTERDATA;
            } catch (IOException e) {
                LOGGER.error("Problem in WritingContentHandler", e);
            }*/

		}

		/**
		 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
		 */
		public void endPrefixMapping(final String prefix) throws SAXException {
			LOGGER.debug("WritingContentHandler.endPrefixMapping(prefix='" + prefix + "')");

		}

		/**
		 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
		 */
		public void skippedEntity(final String name) throws SAXException {
			LOGGER.debug("WritingContentHandler.skippedEntity(name='" + name + "')");

		}

		/**
		 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
		 */
		public void setDocumentLocator(final Locator locator) {

		}

		/**
		 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
		 */
		public void processingInstruction(final String target, final String data)
				throws SAXException {
			try {
				writer.write("<?" + target);
				if (data != null) {
					writer.write(data);
				}

				writer.write("?>\n");
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void startPrefixMapping(final String prefix, final String uri)
				throws SAXException {
			LOGGER.debug("WritingContentHandler.startPrefixMapping(prefix='"
					+ prefix + "')");

		}

		public void endElement(
				final String namespaceURI,
				final String localName,
				final String qName) throws SAXException {
			try {

				// create <slot:value type="string"></slot:value> instead of <slot:value type="string"/>
				if ((isTrnDescription || isSlotvalueTypeString)
						&& last_was != LAST_WAS_CHARACTERDATA) {
					characters(new char[0], 0, 0);
				}

				if (qName.equals("gnc_template-transactions")) {
					insideGncTemplateTransactions = false;
				}

				depth -= 2;

				if (last_was == LAST_WAS_CLOSEELEMENT) {
					writer.write("\n");
					writeSpaces();
					writer.write("</" + qName + ">");
				}

				if (last_was == LAST_WAS_OPENELEMENT) {
					writer.write("/>");
				}

				if (last_was == LAST_WAS_CHARACTERDATA) {
					writer.write("</" + qName + ">");
				}

				last_was = LAST_WAS_CLOSEELEMENT;
			}
			catch (IOException e) {
				LOGGER.error("Problem in WritingContentHandler", e);
			}

		}

		boolean isGUID = false;
		boolean isSlotvalueTypeString = false;
		boolean isTrnDescription = false;
		boolean insideGncTemplateTransactions = false;

		/**
		 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(
				final String namespaceURI,
				final String localName,
				final String qName,
				final Attributes atts) throws SAXException {
			try {
				if (last_was == LAST_WAS_OPENELEMENT) {
					writer.write(">\n");
					writeSpaces();

				}

				if (last_was == LAST_WAS_CLOSEELEMENT) {
					writer.write("\n");
					writeSpaces();
				}

				writer.write("<" + qName);

				if (qName.equals("gnc_template-transactions")) {
					insideGncTemplateTransactions = true;
				}

				isTrnDescription = qName.equals("trn_description");
				isGUID = false;
				isSlotvalueTypeString = false;
				for (int i = 0; i < atts.getLength(); i++) {
					writer.write(" " + atts.getQName(i)
							+ "=\"" + atts.getValue(i) + "\"");

					if (atts.getQName(i).equals("type")
							&&
							atts.getValue(i).equals("guid")) {
						isGUID = true;
					}

					if (qName.equals("slot_value")
							&&
							atts.getQName(i).equals("type")
							&&
							atts.getValue(i).equals("string")) {
						isSlotvalueTypeString = true;
					}

				}
				depth += 2;

				last_was = LAST_WAS_OPENELEMENT;
			}
			catch (IOException e) {
				LOGGER.error("Problem in WritingContentHandler", e);
			}

		}

		/**
		 * @throws IOException
		 */
		private void writeSpaces() throws IOException {

			if (insideGncTemplateTransactions) {
				if (depth < 6) {
					return;
				}

				writer.write(getSpaces(), 0, depth - 6);
				return;
			}

			if (depth < 4) {
				return;
			}

			writer.write(getSpaces(), 0, depth - 4);
		}

		int depth = 0;

		int last_was = 0;
		private static final int LAST_WAS_OPENELEMENT = 1;
		private static final int LAST_WAS_CLOSEELEMENT = 2;
		private static final int LAST_WAS_CHARACTERDATA = 3;

		private char[] spaces;

		protected char[] getSpaces() {
			if (spaces == null || spaces.length < depth) {
				spaces = new char[depth];
				Arrays.fill(spaces, ' ');
			}

			return spaces;
		}
	}

	/**
	 * @author Marcus@Wolschon.biz
	 * created: 13.05.2005
	 * <p>
	 * <p>
	 * replaces ':' in tag-names and attribute-names by '_'
	 */
	public static class NamespaceAdderWriter extends Writer {

		/**
		 * @param input where to write to
		 */
		public NamespaceAdderWriter(final Writer input) {
			super();
			output = input;
		}

		/**
		 * @return where to write to
		 */
		public Writer getWriter() {
			return output;
		}

		/**
		 * where to write to.
		 */
		private final Writer output;

		/**
		 *
		 */
		private boolean isInQuotation = false;

		/**
		 *
		 */
		private boolean isInTag = false;

		/**
		 * @see java.io.Writer#flush()
		 */
		@Override
		public void flush() throws IOException {
			output.flush();
		}

		/**
		 * @see java.io.Writer#write(char[], int, int)
		 */
		@Override
		public void write(final char[] cbuf, final int off, final int len) throws IOException {

			for (int i = off; i < off + len; i++) {
				if (isInTag && (cbuf[i] == '"' || cbuf[i] == '\'')) {
					toggleIsInQuotation();
				} else if (cbuf[i] == '<' && !isInQuotation) {
					isInTag = true;
				} else if (cbuf[i] == '>' && !isInQuotation) {
					isInTag = false;
				} else if (cbuf[i] == '_' && isInTag && !isInQuotation) {

					// do NOT replace the second "_" in but everywhere else inside tag-names
					// cmdty:quote_source
					// cmdty:get_quotes
					// fs:ui_type
					// invoice:billing_id
					// recurrence:period_type

					if (i <= "fs:ui".length() || !(new String(cbuf, i - "fs:ui".length(), "fs:ui".length()).equals("fs:ui"))) {
						if (i <= "cmdty:get".length() || !(new String(cbuf, i - "cmdty:get".length(), "cmdty:get".length())
								.equals("cmdty:get"))) {
							if (i <= "cmdty:quote".length() || !(new String(cbuf, i - "cmdty:quote".length(), "cmdty:quote".length())
									.equals("cmdty:quote"))) {
								if (i <= "invoice:billing".length() || !(new String(cbuf, i - "invoice:billing".length(),
										"invoice:billing".length()).equals("invoice:billing"))) {
									if (i <= "recurrence:period".length() || !(new String(cbuf, i - "recurrence:period".length(),
											"recurrence:period".length()).equals("recurrence:period"))) {
										cbuf[i] = ':';
									}
								}
							}
						}
					}
				}

			}

			output.write(cbuf, off, len);

			// this is a quick hack to add the missing xmlns-declarations
			if (len == 7 && new String(cbuf, off, len).equals("<gnc-v2")) {
				output.write("\n" +
						"     xmlns:gnc=\"http://www.gnucash.org/XML/gnc\"\n" +
						"     xmlns:act=\"http://www.gnucash.org/XML/act\"\n" +
						"     xmlns:book=\"http://www.gnucash.org/XML/book\"\n" +
						"     xmlns:cd=\"http://www.gnucash.org/XML/cd\"\n" +
						"     xmlns:cmdty=\"http://www.gnucash.org/XML/cmdty\"\n" +
						"     xmlns:price=\"http://www.gnucash.org/XML/price\"\n" +
						"     xmlns:slot=\"http://www.gnucash.org/XML/slot\"\n" +
						"     xmlns:split=\"http://www.gnucash.org/XML/split\"\n" +
						"     xmlns:sx=\"http://www.gnucash.org/XML/sx\"\n" +
						"     xmlns:trn=\"http://www.gnucash.org/XML/trn\"\n" +
						"     xmlns:ts=\"http://www.gnucash.org/XML/ts\"\n" +
						"     xmlns:fs=\"http://www.gnucash.org/XML/fs\"\n" +
						"     xmlns:bgt=\"http://www.gnucash.org/XML/bgt\"\n" +
						"     xmlns:recurrence=\"http://www.gnucash.org/XML/recurrence\"\n" +
						"     xmlns:lot=\"http://www.gnucash.org/XML/lot\"\n" +
						"     xmlns:cust=\"http://www.gnucash.org/XML/cust\"\n" +
						"     xmlns:job=\"http://www.gnucash.org/XML/job\"\n" +
						"     xmlns:addr=\"http://www.gnucash.org/XML/addr\"\n" +
						"     xmlns:owner=\"http://www.gnucash.org/XML/owner\"\n" +
						"     xmlns:taxtable=\"http://www.gnucash.org/XML/taxtable\"\n" +
						"     xmlns:tte=\"http://www.gnucash.org/XML/tte\"\n" +
						"     xmlns:employee=\"http://www.gnucash.org/XML/employee\"\n" +
						"     xmlns:order=\"http://www.gnucash.org/XML/order\"\n" +
						"     xmlns:billterm=\"http://www.gnucash.org/XML/billterm\"\n" +
						"     xmlns:bt-days=\"http://www.gnucash.org/XML/bt-days\"\n" +
						"     xmlns:bt-prox=\"http://www.gnucash.org/XML/bt-prox\"\n" +
						"     xmlns:invoice=\"http://www.gnucash.org/XML/invoice\"\n" +
						"     xmlns:entry=\"http://www.gnucash.org/XML/entry\"\n" +
						"     xmlns:vendor=\"http://www.gnucash.org/XML/vendor\"");
			}

		}

		/**
		 * @see java.io.Writer#close()
		 */
		@Override
		public void close() throws IOException {
			output.close();
		}

		/**
		 *
		 */
		private void toggleIsInQuotation() {
			if (isInQuotation) {
				isInQuotation = false;
			} else {
				isInQuotation = true;
			}
		}
	}

	/**
	 * @see GnucashWritableFile#getTransactionByID(java.lang.String)
	 */
	@Override
	public GnucashWritableTransaction getTransactionByID(final String id) {
		return (GnucashWritableTransaction) super.getTransactionByID(id);
	}

	/**
	 * @param jobID the id of the job to fetch
	 * @return A changable version of the job or null of not found.
	 * @see GnucashFile#getJobByID(String)
	 * @see GnucashWritableFile#getJobByID(String)
	 */
	@Override
	public GnucashWritableCustomerJob getJobByID(final String jobID) {
		return (GnucashWritableCustomerJob) super.getJobByID(jobID);
	}

	/**
	 * @see GnucashWritableFile#getWritableJobs()
	 */
	public Collection<GnucashWritableCustomerJob> getWritableJobs() {

		Collection<GnucashJob> jobs = getJobs();
		if (jobs == null) {
			throw new IllegalStateException("getJobs() returned null");
		}
		Collection<GnucashWritableCustomerJob> retval = new ArrayList<GnucashWritableCustomerJob>(jobs.size());
		for (GnucashJob job : jobs) {
			retval.add((GnucashWritableCustomerJob) job);
		}
		return retval;
	}

	/**
	 * @param id the unique invoice-id
	 * @return A changable version of the Invoice or null if not found.
	 * @see GnucashFile#getInvoiceByID(String)
	 */
	@Override
	public GnucashWritableInvoice getInvoiceByID(final String id) {
		return (GnucashWritableInvoice) super.getInvoiceByID(id);
	}

	/**
	 * @param type the type to look for
	 * @return A changable version of all accounts of that type.
	 * @see {@link GnucashWritableFile#getAccountsByType(String)}
	 */
	public Collection getAccountsByType(final String type) {
		Collection retval = new LinkedList();
		for (Object element : getWritableAccounts()) {
			GnucashWritableAccount account = (GnucashWritableAccount) element;

			if (account.getType() == null) {
				if (type == null) {
					retval.add(account);
				}
			} else if (account.getType().equals(type)) {
				retval.add(account);
			}

		}
		return retval;
	}

	/**
	 * @param name the name of the account
	 * @return A changable version of the first account with that name.
	 * @see GnucashFile#getAccountByName(String)
	 */
	@Override
	public GnucashWritableAccount getAccountByName(final String name) {
		return (GnucashWritableAccount) super.getAccountByName(name);
	}

	/**
	 * @param id the unique account-id
	 * @return A changable version of the account or null if not found.
	 * @see GnucashFile#getAccountByID(String)
	 */
	@Override
	public GnucashWritableAccount getAccountByID(final String id) {
		return (GnucashWritableAccount) super.getAccountByID(id);
	}

	/**
	 * @see GnucashWritableFile#getWritableTransactions()
	 */
	@SuppressWarnings("unchecked")
	public Collection<? extends GnucashWritableTransaction> getWritableTransactions() {
		return (Collection<? extends GnucashWritableTransaction>) getTransactions();
	}

	/**
	 * @param impl what to remove
	 */
	public void removeTransaction(final GnucashWritableTransaction impl) {

		Collection<GnucashWritableTransactionSplit> c = new LinkedList<GnucashWritableTransactionSplit>();
		c.addAll(impl.getWritingSplits());
		for (GnucashWritableTransactionSplit element : c) {
			element.remove();
		}

		getRootElement().getGncBook().getBookElements().remove(((GnucashTransactionWritingImpl) impl).getJwsdpPeer());
		setModified(true);
		transactionid2transaction.remove(impl.getId());

	}

	/**
	 * Add a new currency.<br/>
	 * If the currency already exists, add a new price-quote for it.
	 *
	 * @param pCmdtySpace        the namespace (e.g. "GOODS" or "ISO4217")
	 * @param pCmdtyId           the currency-name
	 * @param conversionFactor   the conversion-factor from the base-currency (EUR).
	 * @param pCmdtyNameFraction number of decimal-places after the comma
	 * @param pCmdtyName         common name of the new currency
	 */
	@SuppressWarnings("unchecked")
	public void addCurrency(final String pCmdtySpace, final String pCmdtyId, final FixedPointNumber conversionFactor,
			final int pCmdtyNameFraction, final String pCmdtyName) {

		if (conversionFactor == null) {
			throw new IllegalArgumentException("null conversionFactor given");
		}
		if (pCmdtySpace == null) {
			throw new IllegalArgumentException("null comodity-space given");
		}
		if (pCmdtyId == null) {
			throw new IllegalArgumentException("null comodity-id given");
		}
		if (pCmdtyName == null) {
			throw new IllegalArgumentException("null comodity-name given");
		}
		if (getCurrencyTable().getConversionFactor(pCmdtySpace, pCmdtyId) == null) {

			GncV2.GncBook.GncCommodity newCurrency = getObjectFactory().createGncV2GncBookGncCommodity();
			newCurrency.setCmdtyFraction(pCmdtyNameFraction);
			newCurrency.setCmdtySpace(pCmdtySpace);
			newCurrency.setCmdtyId(pCmdtyId);
			newCurrency.setCmdtyName(pCmdtyName);
			newCurrency.setVersion("2.0.0");
			getRootElement().getGncBook().getBookElements().add(newCurrency);
			incrementCountDataFor("commodity");
		}
		// add price-quote
		GncV2.GncBook.GncPricedb.Price.PriceCommodity currency = new GncV2.GncBook.GncPricedb.Price.PriceCommodity();
		currency.setCmdtySpace(pCmdtySpace);
		currency.setCmdtyId(pCmdtyId);

		GncV2.GncBook.GncPricedb.Price.PriceCurrency baseCurrency = getObjectFactory().createGncV2GncBookGncPricedbPricePriceCurrency();
		baseCurrency.setCmdtySpace("ISO4217");
		baseCurrency.setCmdtyId(getDefaultCurrencyID());

		GncV2.GncBook.GncPricedb.Price newQuote = getObjectFactory().createGncV2GncBookGncPricedbPrice();
		newQuote.setPriceSource("JGnucashLib");
		newQuote.setPriceId(getObjectFactory().createGncV2GncBookGncPricedbPricePriceId());
		newQuote.getPriceId().setType("guid");
		newQuote.getPriceId().setValue(createGUID());
		newQuote.setPriceCommodity(currency);
		newQuote.setPriceCurrency(baseCurrency);
		newQuote.setPriceTime(getObjectFactory().createGncV2GncBookGncPricedbPricePriceTime());
		newQuote.getPriceTime().setTsDate(PRICEQUOTEDATEFORMAT.format(new Date()));
		newQuote.setPriceType("last");
		newQuote.setPriceValue(conversionFactor.toGnucashString());

		List bookElements = getRootElement().getGncBook().getBookElements();
		for (Object element : bookElements) {
			if (element instanceof GncV2.GncBook.GncPricedb) {
				GncV2.GncBook.GncPricedb prices = (GncV2.GncBook.GncPricedb) element;
				prices.getPrice().add(newQuote);
				getCurrencyTable().setConversionFactor(pCmdtySpace, pCmdtyId, conversionFactor);
				return;
			}
		}
		throw new IllegalStateException("No priceDB in Book in Gnucash-file");
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashWritableTransaction createWritableTransaction() {
		return new GnucashTransactionWritingImpl(this, createGUID());
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashWritableTransaction createWritableTransaction(final String id) {
		return new GnucashTransactionWritingImpl(this, id);
	}

	/**
	 * @see GnucashWritableFile#createWritableTransaction()
	 */
	public GnucashWritableInvoice createWritableInvoice(final String invoiceNumber,
			final GnucashJob job,
			final GnucashAccount accountToTransferMoneyTo,
			final java.util.Date dueDate) {
		return createWritableInvoice(createGUID(),
				invoiceNumber,
				job,
				accountToTransferMoneyTo,
				dueDate);
	}

	/**
	 * FOR USE BY EXTENSIONS ONLY!
	 *
	 * @see GnucashWritableFile#createWritableTransaction()
	 */
	public GnucashWritableInvoice createWritableInvoice(final String internalID,
			final String invoiceNumber,
			final GnucashJob job,
			final GnucashAccount accountToTransferMoneyTo,
			final java.util.Date dueDate) {
		GnucashInvoiceWritingImpl retval = new GnucashInvoiceWritingImpl(this,
				internalID,
				invoiceNumber,
				job,
				(GnucashAccountImpl) accountToTransferMoneyTo,
				dueDate);

		invoiceid2invoice.put(retval.getId(), retval);
		return retval;
	}

	/**
	 * @see GnucashWritableFile#createWritableCustomer()
	 */
	public GnucashWritableCustomer createWritableCustomer() {
		return createWritableCustomer(createGUID());
	}

	/**
	 * THIS METHOD IS ONLY TO BE USED BY EXTENSIONS TO THIS LIBRARY!<br/>
	 *
	 * @param id the internal id the customer shall have
	 * @return the new customer. (already added to this file)
	 */
	public GnucashWritableCustomer createWritableCustomer(final String id) {
		if (id == null) {
			throw new IllegalArgumentException("null id given!");
		}
		GnucashCustomerWritingImpl w = new GnucashCustomerWritingImpl(this, id);
		super.customerid2customer.put(w.getId(), w);
		return w;
	}

	/**
	 * @param impl what to remove
	 */
	public void removeCustomer(final GnucashWritableCustomer impl) {
		customerid2customer.remove(impl.getId());
		getRootElement().getGncBook().getBookElements().remove(((GnucashCustomerWritingImpl) impl).getJwsdpPeer());
		setModified(true);
	}

	/**
	 * @see GnucashWritableFile#createWritableJob(GnucashCustomer)
	 */
	public GnucashWritableCustomerJob createWritableJob(final GnucashCustomer customer) {
		if (customer == null) {
			throw new IllegalArgumentException("null customer given");
		}
		return this.createWritableJob(this.createGUID(), customer);
	}

	/**
	 * @see GnucashWritableFile#createWritableJob(String, GnucashCustomer)
	 */
	public GnucashWritableCustomerJob createWritableJob(final String id, final GnucashCustomer customer) {
		if (customer == null) {
			throw new IllegalArgumentException("null customer given");
		}
		GnucashCustomerJobWritingImpl w = new GnucashCustomerJobWritingImpl(this, id, customer);
		super.jobid2job.put(w.getId(), w);
		return w;
	}

	/**
	 * @param impl what to remove
	 */
	public void removeJob(final GnucashWritableCustomerJob impl) {
		jobid2job.remove(impl.getId());
		getRootElement().getGncBook().getBookElements().remove(((GnucashCustomerJobWritingImpl) impl).getJwsdpPeer());
		setModified(true);
	}

	/**
	 * @see GnucashWritableFile#createWritableAccount()
	 */
	public GnucashWritableAccount createWritableAccount() {
		GnucashWritableAccount w = new GnucashAccountWritingImpl(this);
		super.accountid2account.put(w.getId(), w);
		return w;
	}

	/**
	 * @see GnucashWritableFile#createWritableAccount()
	 */
	public GnucashWritableAccount createWritableAccount(final String newID) {
		GnucashWritableAccount w = new GnucashAccountWritingImpl(this, newID);
		super.accountid2account.put(w.getId(), w);
		return w;
	}

	/**
	 * @param impl what to remove
	 */
	public void removeAccount(final GnucashWritableAccount impl) {
		if (impl.getTransactionSplits().size() > 0) {
			throw new IllegalStateException("cannot remove account while it contains transaction-splits!");
		}

		getRootElement().getGncBook().getBookElements().remove(((GnucashAccountWritingImpl) impl).getJwsdpPeer());
		setModified(true);
		super.accountid2account.remove(impl.getId());
	}

	/**
	 * @return a read-only collection of all accounts that have no parent
	 */
	@SuppressWarnings("unchecked")
	public Collection<? extends GnucashWritableAccount> getWritableRootAccounts() {
		return (Collection<? extends GnucashWritableAccount>) getRootAccounts();
	}

	/**
	 * @return a read-only collection of all accounts
	 */
	@SuppressWarnings("unchecked")
	public Collection<GnucashWritableAccount> getWritableAccounts() {
		TreeSet<GnucashWritableAccount> retval = new TreeSet();
		for (GnucashAccount account : getAccounts()) {
			retval.add((GnucashWritableAccount) account);
		}
		return retval;
	}

	/**
	 * @param jnr the job-number to look for.
	 * @return the (first) jobs that have this number or null if not found
	 */
	public GnucashWritableCustomerJob getJobByNumber(final String jnr) {
		if (jobid2job == null) {
			throw new IllegalStateException("no root-element loaded");
		}

		for (GnucashJob gnucashJob : jobid2job.values()) {
			GnucashWritableCustomerJob job = (GnucashWritableCustomerJob) gnucashJob;
			if (job.getJobNumber().equals(jnr)) {
				return job;
			}
		}
		return null;

	}

	/**
	 * @param impl an invoice to remove
	 */
	public void removeInvoice(final GnucashInvoiceWritingImpl impl) {

		if (impl.getPayingTransactions().size() > 0) {
			throw new IllegalArgumentException("cannot remove this invoice! It has payments!");
		}

		GnucashTransaction postTransaction = impl.getPostTransaction();
		if (postTransaction != null) {
			((GnucashWritableTransaction) postTransaction).remove();
		}

		invoiceid2invoice.remove(impl.getId());
		getRootElement().getGncBook().getBookElements().remove(impl.getJwsdpPeer());
		this.decrementCountDataFor("gnc:GncInvoice");
		setModified(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GnucashWritableFile getWritableGnucashFile() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setUserDefinedAttribute(final String aName, final String aValue) {
		List<Slot> slots = getRootElement().getGncBook().getBookSlots().getSlot();
		for (Slot slot : slots) {
			if (slot.getSlotKey().equals(aName)) {
				slot.getSlotValue().getContent().clear();
				slot.getSlotValue().getContent().add(aValue);
				return;
			}
		}
		// create new slot
		Slot newSlot = getObjectFactory().createSlot();
		newSlot.setSlotKey(aName);
		newSlot.setSlotValue(getObjectFactory().createSlotValue());
		newSlot.getSlotValue().getContent().add(aValue);
		newSlot.getSlotValue().setType("string");
		getRootElement().getGncBook().getBookSlots().getSlot().add(newSlot);
	}

	/* (non-Javadoc)
	 * @see org.gnucash.write.jwsdpimpl.GnucashFileImpl#getRootAccounts()
	 */
	@Override
	public Collection<? extends GnucashAccount> getRootAccounts() {
		// TODO Auto-generated method stub
		Collection<? extends GnucashAccount> rootAccounts = super.getRootAccounts();
		if (rootAccounts.size() > 1) {
			GnucashAccount root = null;
			StringBuilder roots = new StringBuilder();
			for (GnucashAccount gnucashAccount : rootAccounts) {
				if (gnucashAccount == null) {
					continue;
				}
				if (gnucashAccount.getType() != null && gnucashAccount.getType().equals("ROOT")) {
					root = gnucashAccount;
					continue;
				}
				roots.append(gnucashAccount.getId()).append("=\"").append(gnucashAccount.getName()).append("\" ");
			}
			LOGGER.warn("file has more then one root-account! Attaching excess accounts to root-account: "
					+ roots.toString());
			LinkedList<GnucashAccount> rootAccounts2 = new LinkedList<GnucashAccount>();
			rootAccounts2.add(root);
			for (GnucashAccount gnucashAccount : rootAccounts) {
				if (gnucashAccount == null) {
					continue;
				}
				if (gnucashAccount == root) {
					continue;
				}
				((GnucashWritableAccount) gnucashAccount).setParentAccount(root);

			}
			rootAccounts = rootAccounts2;
		}
		return rootAccounts;
	}
}
