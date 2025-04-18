package org.gnucash.currency;

import java.io.Serializable;
import java.util.Collection;
import java.util.Currency;
import java.util.Hashtable;
import java.util.Map;

import org.gnucash.numbers.FixedPointNumber;

/**
 * A CurrencyTable holds the translations from some currencies to one base-currency of one specfic point in time
 * (usually the current time).
 * </p>
 * <br/>
 * By default "EUR" is added with the value 1. (to be used as a base.currency)
 */
public class SimpleCurrencyTable implements Serializable {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 4464939590559924285L;

	// -------------------------------------------------------

	/**
	 * Just an overridden ToString to return this classe's name and hashCode.
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
		// setConversionFactor("GBP", new FixedPointNumber("769/523"));
	}

	/**
	 * maps a currency-name in capital letters(e.g. "GBP") to a factor {@link FixedPointNumber} that is to be multiplied
	 * with an amount of that currency to get the value in the base-currency.
	 *
	 * @see {@link #getConversionFactor(String)}
	 */
	private Map<String, FixedPointNumber> mIso4217CurrencyCodes2Factor = new Hashtable<String, FixedPointNumber>();

	/**
	 * @param iso4217CurrencyCode a currency-name in capital letters(e.g. "GBP")
	 * @return a factor {@link FixedPointNumber} that is to be multiplied with an amount of that currency to get the value
	 * in the base-currency.
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
	 * @param factor              a factor {@link FixedPointNumber} that is to be multiplied with an amount of that
	 *                            currency to get the value in the base-currency.
	 */
	public void setConversionFactor(final String iso4217CurrencyCode, final FixedPointNumber factor) {
		mIso4217CurrencyCodes2Factor.put(iso4217CurrencyCode, factor);
	}

	/**
	 * @param value               the value to convert
	 * @param iso4217CurrencyCode it's currency
	 * @return false if the conversion is not possible
	 */
	public boolean convertToBaseCurrency(final FixedPointNumber value, final String iso4217CurrencyCode) {
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
	public boolean convertFromBaseCurrency(final FixedPointNumber value, final String iso4217CurrencyCode) {
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
	public boolean convertToBaseCurrency(final FixedPointNumber value, final Currency pCurrency) {
		return convertFromBaseCurrency(value, pCurrency.getCurrencyCode());
	}

	/**
	 * @return all currency-names
	 */
	public Collection<String> getCurrencies() {
		return mIso4217CurrencyCodes2Factor.keySet();
	}

}
