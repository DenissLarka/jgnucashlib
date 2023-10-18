package org.gnucash.read;

public interface GnucashCommodity {

    /**
     * @return the name space of the commodity (e.g., "CURRENCY" 
     *         in case of a currency, or one of the major stock
     *         exchanges' name, such as "AMEX", "NASDAQ" or "EUREX". 
     */
    String getNameSpace();

    /**
     * @return the currency/security/commodity ID.
     *         This is *not* a technical ID (i.e. not a UUID, as in the
     *         other GnuCash entities), but a) an ISO4217 currency code,
     *         in case of a currency (name space = "CURRENCY"), or 
     *         b) a security/commodity code unique within the given name space
     *         (typically, this is the security ticker, which is unique
     *         in the realm of a specific stock exchange, such as "AMEX",
     *         "NASDAQ" or "EUREX").
     */
    String getId();

    /**
     * @return the combination of getNameSpace() and getId(), 
     *         separated by a colon. This is used to make the so-called ID
     *         a real ID (i.e., unique).
     */
    String getNameSpaceId();

    /**
     * @return the "extended" code of a commodity
     *         (typically, this is the ISIN in case you have 
     *         a global portfolio; if you have a local portfolio,
     *         this could also be the corresponding regional security/commodity
     *         ID, such as "CUSIP" (USA, Canada), "SEDOL" (UK), or
     *         "WKN" (Germany, Austria, Switzerland)). 
     */
    String getXCode();

    /**
     * @return the name of the currency/security/commodity 
     */
    String getName();

    int getFraction();

    // ------------------------------------------------------------

    /*
    Collection<GnucashPrice> getQuotes();
    
    String getQuoteSource();
    
    String getQuoteTz();
    */

}
