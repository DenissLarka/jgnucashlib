/**
 * CurrencyTable.java
 * created: 28.08.2005 14:20:13
 * (c) 2005 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
 */
package org.gnucash.currency;

//other imports

import java.io.Serializable;
import java.util.Collection;
import java.util.Currency;
import java.util.Hashtable;
import java.util.Map;

import org.gnucash.numbers.FixedPointNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//automatically created logger for debug and error -output
//automatically created propertyChangeListener-Support

/**
 * (c) 2005 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
 * Project: gnucashReader<br/>
 * CurrencyTable.java<br/>
 * created: 28.08.2005 14:20:13 <br/>
 * <br/>
 * <p>
 * A CurrencyTable holds the translations from some currencies to one base-currency
 * of one specfic point in time (usually the current time).</p><br/>
 * By default "EUR" is added with the value 1. (to be used as a base.currency)
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class SimpleCurrencyTable implements Serializable {

	/**
	 * Automatically created logger for debug and error-output.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCurrencyTable.class);

	//------------------------ support for propertyChangeListeners ------------------
	//
	///**
	// * support for firing PropertyChangeEvents.
	// * (gets initialized only if we really have listeners)
	// */
	//protected volatile PropertyChangeSupport myPropertyChangeSupport = null;
	//
	///**
	// * Add a PropertyChangeListener to the listener list.
	// * The listener is registered for all properties.
	// *
	// * @param listener  The PropertyChangeListener to be added
	// */
	//public final void addPropertyChangeListener(
	//                                            final PropertyChangeListener listener) {
	//    if (myPropertyChangeSupport == null) {
	//        myPropertyChangeSupport = new PropertyChangeSupport(this);
	//    }
	//    myPropertyChangeSupport.addPropertyChangeListener(listener);
	//}
	//
	///**
	// * Add a PropertyChangeListener for a specific property.  The listener
	// * will be invoked only when a call on firePropertyChange names that
	// * specific property.
	// *
	// * @param propertyName  The name of the property to listen on.
	// * @param listener  The PropertyChangeListener to be added
	// */
	//public final void addPropertyChangeListener(
	//                                            final String propertyName,
	//                                            final PropertyChangeListener listener) {
	//    if (myPropertyChangeSupport == null) {
	//        myPropertyChangeSupport = new PropertyChangeSupport(this);
	//    }
	//    myPropertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	//}
	//
	///**
	// * Remove a PropertyChangeListener for a specific property.
	// *
	// * @param propertyName  The name of the property that was listened on.
	// * @param listener  The PropertyChangeListener to be removed
	// */
	//public final void removePropertyChangeListener(
	//                                               final String propertyName,
	//                                               final PropertyChangeListener listener) {
	//    if (myPropertyChangeSupport != null) {
	//        myPropertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	//    }
	//}
	//
	///**
	// * Remove a PropertyChangeListener from the listener list.
	// * This removes a PropertyChangeListener that was registered
	// * for all properties.
	// *
	// * @param listener  The PropertyChangeListener to be removed
	// */
	//public synchronized void removePropertyChangeListener(
	//                                                      final PropertyChangeListener listener) {
	//    if (myPropertyChangeSupport != null) {
	//        myPropertyChangeSupport.removePropertyChangeListener(listener);
	//    }
	//}

	//-------------------------------------------------------

	/**
	 * Just an overridden ToString to return this classe's name
	 * and hashCode.
	 *
	 * @return className and hashCode
	 */
	public String toString() {
		return "CurrencyTable@" + hashCode();
	}

	/**
	 *
	 */
	public SimpleCurrencyTable() {
		super();
		setConversionFactor("EUR", new FixedPointNumber(1));
		//setConversionFactor("GBP", new FixedPointNumber("769/523"));
	}

	/**
	 * maps a currency-name in capital letters(e.g. "GBP")
	 * to a factor {@link FixedPointNumber}
	 * that is to be multiplied with an ammount of that currency
	 * to get the value in the base-currency.
	 *
	 * @see {@link #getConversionFactor(String)}
	 */
	private Map<String, FixedPointNumber> mIso4217CurrencyCodes2Factor = new Hashtable<String, FixedPointNumber>();

	/**
	 * @param iso4217CurrencyCode a currency-name in capital letters(e.g. "GBP")
	 * @return a factor {@link FixedPointNumber}
	 * that is to be multiplied with an ammount of that currency
	 * to get the value in the base-currency.
	 */
	public FixedPointNumber getConversionFactor(final String iso4217CurrencyCode) {
		return mIso4217CurrencyCodes2Factor.get(iso4217CurrencyCode);
	}

	/**
	 * forget all conversion-factors.
	 */
	public void clear() {
		mIso4217CurrencyCodes2Factor.clear();
	}

	/**
	 * @param iso4217CurrencyCode a currency-name in capital letters(e.g. "GBP")
	 * @param factor              a factor {@link FixedPointNumber}
	 *                            that is to be multiplied with an ammount of that currency
	 *                            to get the value in the base-currency.
	 */
	public void setConversionFactor(final String iso4217CurrencyCode,
			final FixedPointNumber factor) {
		mIso4217CurrencyCodes2Factor.put(iso4217CurrencyCode, factor);
	}

	/**
	 * @param value               the value to convert
	 * @param iso4217CurrencyCode it's currency
	 * @return false if the conversion is not possible
	 */
	public boolean convertToBaseCurrency(final FixedPointNumber value,
			final String iso4217CurrencyCode) {
		FixedPointNumber factor = getConversionFactor(iso4217CurrencyCode);
		if (factor == null) {
			return false;
		}
		value.multiply(factor);
		return true;
	}

	/**
	 * @param value               the value to convert
	 * @param iso4217CurrencyCode the currency to convert to
	 * @return false if the conversion is not possible
	 */
	public boolean convertFromBaseCurrency(final FixedPointNumber value,
			final String iso4217CurrencyCode) {
		FixedPointNumber factor = getConversionFactor(iso4217CurrencyCode);
		if (factor == null) {
			return false;
		}
		value.divideBy(factor);
		return true;
	}

	/**
	 * @param value     the value to convert
	 * @param pCurrency the currency to convert to
	 * @return false if the conversion is not possible
	 */
	public boolean convertToBaseCurrency(final FixedPointNumber value,
			final Currency pCurrency) {
		return convertFromBaseCurrency(value, pCurrency.getCurrencyCode());
	}

	/**
	 * @return all currency-names
	 */
	public Collection<String> getCurrencies() {
		return mIso4217CurrencyCodes2Factor.keySet();
	}

}