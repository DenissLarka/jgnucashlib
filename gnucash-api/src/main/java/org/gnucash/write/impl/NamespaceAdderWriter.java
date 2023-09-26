package org.gnucash.write.impl;

import java.io.IOException;
import java.io.Writer;

/**
 * replaces ':' in tag-names and attribute-names by '_'
 */
class NamespaceAdderWriter extends Writer {

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
    	if (isInTag && (cbuf[i] == '"' || 
                cbuf[i] == '\'')) {
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

    	    if (i <= "fs:ui".length() || 
    		!(new String(cbuf, i - "fs:ui".length(), "fs:ui".length()).equals("fs:ui"))) {
    		if (i <= "cmdty:get".length() || 
    		    !(new String(cbuf, i - "cmdty:get".length(), "cmdty:get".length()).equals("cmdty:get"))) {
    		    if (i <= "cmdty:quote".length() || 
    			!(new String(cbuf, i - "cmdty:quote".length(), "cmdty:quote".length()).equals("cmdty:quote"))) {
    			if (i <= "invoice:billing".length() || 
    			    !(new String(cbuf, i - "invoice:billing".length(),
    					"invoice:billing".length()).equals("invoice:billing"))) {
    			    if (i <= "recurrence:period".length() || 
    				!(new String(cbuf, i - "recurrence:period".length(),
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
    	output.write("\n" + "     xmlns:gnc=\"http://www.gnucash.org/XML/gnc\"\n"
    		+ "     xmlns:act=\"http://www.gnucash.org/XML/act\"\n"
    		+ "     xmlns:book=\"http://www.gnucash.org/XML/book\"\n"
    		+ "     xmlns:cd=\"http://www.gnucash.org/XML/cd\"\n"
    		+ "     xmlns:cmdty=\"http://www.gnucash.org/XML/cmdty\"\n"
    		+ "     xmlns:price=\"http://www.gnucash.org/XML/price\"\n"
    		+ "     xmlns:slot=\"http://www.gnucash.org/XML/slot\"\n"
    		+ "     xmlns:split=\"http://www.gnucash.org/XML/split\"\n"
    		+ "     xmlns:sx=\"http://www.gnucash.org/XML/sx\"\n"
    		+ "     xmlns:trn=\"http://www.gnucash.org/XML/trn\"\n"
    		+ "     xmlns:ts=\"http://www.gnucash.org/XML/ts\"\n"
    		+ "     xmlns:fs=\"http://www.gnucash.org/XML/fs\"\n"
    		+ "     xmlns:bgt=\"http://www.gnucash.org/XML/bgt\"\n"
    		+ "     xmlns:recurrence=\"http://www.gnucash.org/XML/recurrence\"\n"
    		+ "     xmlns:lot=\"http://www.gnucash.org/XML/lot\"\n"
    		+ "     xmlns:cust=\"http://www.gnucash.org/XML/cust\"\n"
    		+ "     xmlns:job=\"http://www.gnucash.org/XML/job\"\n"
    		+ "     xmlns:addr=\"http://www.gnucash.org/XML/addr\"\n"
    		+ "     xmlns:owner=\"http://www.gnucash.org/XML/owner\"\n"
    		+ "     xmlns:taxtable=\"http://www.gnucash.org/XML/taxtable\"\n"
    		+ "     xmlns:tte=\"http://www.gnucash.org/XML/tte\"\n"
    		+ "     xmlns:employee=\"http://www.gnucash.org/XML/employee\"\n"
    		+ "     xmlns:order=\"http://www.gnucash.org/XML/order\"\n"
    		+ "     xmlns:billterm=\"http://www.gnucash.org/XML/billterm\"\n"
    		+ "     xmlns:bt-days=\"http://www.gnucash.org/XML/bt-days\"\n"
    		+ "     xmlns:bt-prox=\"http://www.gnucash.org/XML/bt-prox\"\n"
    		+ "     xmlns:invoice=\"http://www.gnucash.org/XML/invoice\"\n"
    		+ "     xmlns:entry=\"http://www.gnucash.org/XML/entry\"\n"
    		+ "     xmlns:vendor=\"http://www.gnucash.org/XML/vendor\"");
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

