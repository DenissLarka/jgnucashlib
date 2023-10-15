package org.gnucash.numbers;

//other imports

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

//automatically created propertyChangeListener-Support

/**
 * This is a helper super-class for classes that can be converted to a BigDecimal
 * and shall extend that class but not use it's state.<br/>
 */
public abstract class BigDecimalWrapper extends BigDecimal {



	/**
	 * @return the value as a BigDecimal.
	 */
	public abstract BigDecimal getBigDecimal();

	//------------------------ support for propertyChangeListeners ------------------

	/**
	 * support for firing PropertyChangeEvents.
	 * (gets initialized only if we really have listeners)
	 */
	private volatile PropertyChangeSupport myPropertyChange = null;

	/**
	 * Returned value may be null if we never had listeners.
	 *
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return myPropertyChange;
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property.  The listener
	 * will be invoked only when a call on firePropertyChange names that
	 * specific property.
	 *
	 * @param propertyName The name of the property to listen on.
	 * @param listener     The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		if (myPropertyChange == null) {
			myPropertyChange = new PropertyChangeSupport(this);
		}
		myPropertyChange.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName The name of the property that was listened on.
	 * @param listener     The PropertyChangeListener to be removed
	 */
	public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(propertyName, listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
		if (myPropertyChange != null) {
			myPropertyChange.removePropertyChangeListener(listener);
		}
	}

	//-------------------------------------------------------

	/**
	 * Just an overridden ToString to return this classe's name
	 * and hashCode.
	 *
	 * @return className and hashCode
	 */
	@Override
	public String toString() {
		return getBigDecimal().toString();
	}

	/**
	 *
	 */
	protected BigDecimalWrapper() {
		super("0");
	}

	/**
	 * @see java.math.BigDecimal#abs()
	 */
	@Override
	public BigDecimal abs() {
		return getBigDecimal().abs();
	}

	/**
	 * @see java.math.BigDecimal#abs(java.math.MathContext)
	 */
	@Override
	public BigDecimal abs(MathContext mc) {
		return getBigDecimal().abs(mc);
	}

	/**
	 * @see java.math.BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
	 */
	@Override
	public BigDecimal add(BigDecimal augend, MathContext mc) {
		return getBigDecimal().add(augend, mc);
	}

	/**
	 * @see java.math.BigDecimal#add(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal add(BigDecimal augend) {
		return getBigDecimal().add(augend);
	}

	/**
	 * @see java.math.BigDecimal#byteValueExact()
	 */
	@Override
	public byte byteValueExact() {
		return getBigDecimal().byteValueExact();
	}

	/**
	 * @see java.math.BigDecimal#compareTo(java.math.BigDecimal)
	 */
	@Override
	public int compareTo(BigDecimal val) {
		return getBigDecimal().compareTo(val);
	}

	/**
	 * @see java.math.BigDecimal#divide(java.math.BigDecimal, int, java.math.RoundingMode)
	 */
	@Override
	public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
		return getBigDecimal().divide(divisor, scale, roundingMode);
	}

	/**
	 * @see java.math.BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
	 */
	@Override
	public BigDecimal divide(BigDecimal divisor, MathContext mc) {
		return getBigDecimal().divide(divisor, mc);
	}

	/**
	 * @see java.math.BigDecimal#divide(java.math.BigDecimal, java.math.RoundingMode)
	 */
	@Override
	public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
		return getBigDecimal().divide(divisor, roundingMode);
	}

	/**
	 * @see java.math.BigDecimal#divide(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal divide(BigDecimal divisor) {
		return getBigDecimal().divide(divisor);
	}

	/**
	 * @see java.math.BigDecimal#divideAndRemainder(java.math.BigDecimal, java.math.MathContext)
	 */
	@Override
	public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
		return getBigDecimal().divideAndRemainder(divisor, mc);
	}

	/**
	 * @see java.math.BigDecimal#divideAndRemainder(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
		return getBigDecimal().divideAndRemainder(divisor);
	}

	/**
	 * @see java.math.BigDecimal#divideToIntegralValue(java.math.BigDecimal, java.math.MathContext)
	 */
	@Override
	public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
		return getBigDecimal().divideToIntegralValue(divisor, mc);
	}

	/**
	 * @see java.math.BigDecimal#divideToIntegralValue(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal divideToIntegralValue(BigDecimal divisor) {
		return getBigDecimal().divideToIntegralValue(divisor);
	}

	/**
	 * @see java.math.BigDecimal#doubleValue()
	 */
	@Override
	public double doubleValue() {
		return getBigDecimal().doubleValue();
	}

	/**
	 * @see java.math.BigDecimal#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object x) {
		return getBigDecimal().equals(x);
	}

	/**
	 * @see java.math.BigDecimal#floatValue()
	 */
	@Override
	public float floatValue() {
		return getBigDecimal().floatValue();
	}

	/**
	 * @see java.math.BigDecimal#hashCode()
	 */
	@Override
	public int hashCode() {
		return getBigDecimal().hashCode();
	}

	/**
	 * @see java.math.BigDecimal#intValue()
	 */
	@Override
	public int intValue() {
		return getBigDecimal().intValue();
	}

	/**
	 * @see java.math.BigDecimal#intValueExact()
	 */
	@Override
	public int intValueExact() {
		return getBigDecimal().intValueExact();
	}

	/**
	 * @see java.math.BigDecimal#longValue()
	 */
	@Override
	public long longValue() {
		return getBigDecimal().longValue();
	}

	/**
	 * @see java.math.BigDecimal#longValueExact()
	 */
	@Override
	public long longValueExact() {
		return getBigDecimal().longValueExact();
	}

	/**
	 * @see java.math.BigDecimal#max(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal max(BigDecimal val) {
		return getBigDecimal().max(val);
	}

	/**
	 * @see java.math.BigDecimal#min(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal min(BigDecimal val) {
		return getBigDecimal().min(val);
	}

	/**
	 * @see java.math.BigDecimal#movePointLeft(int)
	 */
	@Override
	public BigDecimal movePointLeft(int n) {
		return getBigDecimal().movePointLeft(n);
	}

	/**
	 * @see java.math.BigDecimal#movePointRight(int)
	 */
	@Override
	public BigDecimal movePointRight(int n) {
		return getBigDecimal().movePointRight(n);
	}

	/**
	 * @see java.math.BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
	 */
	@Override
	public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
		return getBigDecimal().multiply(multiplicand, mc);
	}

	/**
	 * @see java.math.BigDecimal#multiply(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal multiply(BigDecimal multiplicand) {
		return getBigDecimal().multiply(multiplicand);
	}

	/**
	 * @see java.math.BigDecimal#negate()
	 */
	@Override
	public BigDecimal negate() {
		return getBigDecimal().negate();
	}

	/**
	 * @see java.math.BigDecimal#negate(java.math.MathContext)
	 */
	@Override
	public BigDecimal negate(MathContext mc) {
		return getBigDecimal().negate(mc);
	}

	/**
	 * @see java.math.BigDecimal#plus()
	 */
	@Override
	public BigDecimal plus() {
		return getBigDecimal().plus();
	}

	/**
	 * @see java.math.BigDecimal#plus(java.math.MathContext)
	 */
	@Override
	public BigDecimal plus(MathContext mc) {
		return getBigDecimal().plus(mc);
	}

	/**
	 * @see java.math.BigDecimal#pow(int, java.math.MathContext)
	 */
	@Override
	public BigDecimal pow(int n, MathContext mc) {
		return getBigDecimal().pow(n, mc);
	}

	/**
	 * @see java.math.BigDecimal#pow(int)
	 */
	@Override
	public BigDecimal pow(int n) {
		return getBigDecimal().pow(n);
	}

	/**
	 * @see java.math.BigDecimal#precision()
	 */
	@Override
	public int precision() {
		return getBigDecimal().precision();
	}

	/**
	 * @see java.math.BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
	 */
	@Override
	public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
		return getBigDecimal().remainder(divisor, mc);
	}

	/**
	 * @see java.math.BigDecimal#remainder(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal remainder(BigDecimal divisor) {
		return getBigDecimal().remainder(divisor);
	}

	/**
	 * @see java.math.BigDecimal#round(java.math.MathContext)
	 */
	@Override
	public BigDecimal round(MathContext mc) {
		return getBigDecimal().round(mc);
	}

	/**
	 * @see java.math.BigDecimal#scale()
	 */
	@Override
	public int scale() {
		return getBigDecimal().scale();
	}

	/**
	 * @see java.math.BigDecimal#scaleByPowerOfTen(int)
	 */
	@Override
	public BigDecimal scaleByPowerOfTen(int n) {
		return getBigDecimal().scaleByPowerOfTen(n);
	}

	/**
	 * @see java.math.BigDecimal#setScale(int, java.math.RoundingMode)
	 */
	@Override
	public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
		return getBigDecimal().setScale(newScale, roundingMode);
	}

	/**
	 * @see java.math.BigDecimal#setScale(int)
	 */
	@Override
	public BigDecimal setScale(int newScale) {
		return getBigDecimal().setScale(newScale);
	}

	/**
	 * @see java.math.BigDecimal#shortValueExact()
	 */
	@Override
	public short shortValueExact() {
		return getBigDecimal().shortValueExact();
	}

	/**
	 * @see java.math.BigDecimal#signum()
	 */
	@Override
	public int signum() {
		return getBigDecimal().signum();
	}

	/**
	 * @see java.math.BigDecimal#stripTrailingZeros()
	 */
	@Override
	public BigDecimal stripTrailingZeros() {
		return getBigDecimal().stripTrailingZeros();
	}

	/**
	 * @see java.math.BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
	 */
	@Override
	public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
		return getBigDecimal().subtract(subtrahend, mc);
	}

	/**
	 * @see java.math.BigDecimal#subtract(java.math.BigDecimal)
	 */
	@Override
	public BigDecimal subtract(BigDecimal subtrahend) {
		return getBigDecimal().subtract(subtrahend);
	}

	/**
	 * @see java.math.BigDecimal#toBigInteger()
	 */
	@Override
	public BigInteger toBigInteger() {
		return getBigDecimal().toBigInteger();
	}

	/**
	 * @see java.math.BigDecimal#toBigIntegerExact()
	 */
	@Override
	public BigInteger toBigIntegerExact() {
		return getBigDecimal().toBigIntegerExact();
	}

	/**
	 * @see java.math.BigDecimal#toEngineeringString()
	 */
	@Override
	public String toEngineeringString() {
		return getBigDecimal().toEngineeringString();
	}

	/**
	 * @see java.math.BigDecimal#toPlainString()
	 */
	@Override
	public String toPlainString() {
		return getBigDecimal().toPlainString();
	}

	/**
	 * @see java.math.BigDecimal#ulp()
	 */
	@Override
	public BigDecimal ulp() {
		return getBigDecimal().ulp();
	}

	/**
	 * @see java.math.BigDecimal#unscaledValue()
	 */
	@Override
	public BigInteger unscaledValue() {
		return getBigDecimal().unscaledValue();
	}

	/**
	 * @see java.lang.Number#byteValue()
	 */
	@Override
	public byte byteValue() {
		return getBigDecimal().byteValue();
	}

	/**
	 * @see java.lang.Number#shortValue()
	 */
	@Override
	public short shortValue() {
		return getBigDecimal().shortValue();
	}

	//------------------------------------------------------- overloaded methods

}