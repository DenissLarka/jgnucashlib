package org.gnucash.write.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.gnucash.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Helper-Class needed for writing Gnucash-Files that are binary-identical to
 * what gnucash itself writes.
 */
class WritingContentHandler implements ContentHandler {

	/**
	 * Our logger for debug- and error-ourput.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WritingContentHandler.class);

	/**
	 * where to write it to.
	 */
	private final Writer writer;

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
			writer.write("\n\n" + "<!-- Local variables: -->\n" + "<!-- mode: xml        -->\n"
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
			// old gnucash-version writer.write("<?xml version=\"1.0\"?>\n");
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
	public void characters(char[] ch, int start, int length) throws SAXException {
		try {
			if (last_was == LAST_WAS_OPEN_ELEMENT) {
				writer.write(">");
			}

			if (last_was == LAST_WAS_CLOSE_ELEMENT) {
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
						sb.replace(index, index + encodeme[j].length(), encoded[j]);
						index += encoded[j].length() - encodeme[j].length() + 1;
					}

				}

				// String s = sb.toString();
				// if(s.indexOf("bis 410") != -1) {
				// System.err.println(s+"---"+Integer.toHexString(s.charAt(s.length()-1)));
				// }

				writer.write(sb.toString());
			}

			last_was = LAST_WAS_CHARACTER_DATA;
		}
		catch (IOException e) {
			LOGGER.error("Problem in WritingContentHandler", e);
		}

	}

	public void ignorableWhitespace(final char[] ch, final int start, final int length) {
		/*
		 * try { writer.write(ch, start, length); last_was = LAST_WAS_CHARACTERDATA; }
		 * catch (IOException e) { LOGGER.error("Problem in WritingContentHandler", e);
		 * }
		 */

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
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 * java.lang.String)
	 */
	public void processingInstruction(final String target, final String data) throws SAXException {
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

	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		LOGGER.debug("WritingContentHandler.startPrefixMapping(prefix='" + prefix + "')");

	}

	public void endElement(final String namespaceURI, final String localName, final String qName)
			throws SAXException {
		try {

			// create <slot:value type="string"></slot:value> instead of <slot:value
			// type="string"/>
			if ((isTrnDescription || isSlotvalueTypeString) &&
					last_was != LAST_WAS_CHARACTER_DATA) {
				characters(new char[0], 0, 0);
			}

			if (qName.equals("gnc_template-transactions")) {
				insideGncTemplateTransactions = false;
			}

			depth -= 2;

			if (last_was == LAST_WAS_CLOSE_ELEMENT) {
				writer.write("\n");
				writeSpaces();
				writer.write("</" + qName + ">");
			}

			if (last_was == LAST_WAS_OPEN_ELEMENT) {
				writer.write("/>");
			}

			if (last_was == LAST_WAS_CHARACTER_DATA) {
				writer.write("</" + qName + ">");
			}

			last_was = LAST_WAS_CLOSE_ELEMENT;
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
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(final String namespaceURI, final String localName, final String qName,
			final Attributes atts) throws SAXException {
		try {
			if (last_was == LAST_WAS_OPEN_ELEMENT) {
				writer.write(">\n");
				writeSpaces();

			}

			if (last_was == LAST_WAS_CLOSE_ELEMENT) {
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
				writer.write(" " + atts.getQName(i) + "=\"" + atts.getValue(i) + "\"");

				if (atts.getQName(i).equals("type") &&
						atts.getValue(i).equals(Const.XML_DATA_TYPE_GUID)) {
					isGUID = true;
				}

				if (qName.equals("slot_value") && atts.getQName(i).equals("type") &&
						atts.getValue(i).equals("string")) {
					isSlotvalueTypeString = true;
				}

			}
			depth += 2;

			last_was = LAST_WAS_OPEN_ELEMENT;
		}
		catch (IOException e) {
			LOGGER.error("Problem in WritingContentHandler", e);
		}

	}

	/**
	 *
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
	private static final int LAST_WAS_OPEN_ELEMENT = 1;
	private static final int LAST_WAS_CLOSE_ELEMENT = 2;
	private static final int LAST_WAS_CHARACTER_DATA = 3;

	private char[] spaces;

	protected char[] getSpaces() {
		if (spaces == null || spaces.length < depth) {
			spaces = new char[depth];
			Arrays.fill(spaces, ' ');
		}

		return spaces;
	}
}
