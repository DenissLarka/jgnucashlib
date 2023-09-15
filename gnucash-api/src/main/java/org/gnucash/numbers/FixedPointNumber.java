/**
 * FixedPointNumber.java Created on 14.05.2005 (c) 2005 by
 * "Wolschon Softwaredesign und Beratung".
 * ----------------------------------------------------------- major Changes:
 * 14.05.2005 - initial version ...
 */
package org.gnucash.numbers;

import java.math.BigDecimal;

/**
 * created: 14.05.2005 <br/>
 * Implementation of Fixed-point numbers that knows the String-format gnucash
 * uses and returns true if 2 numbers are compared that are mathematically equal
 * even if they have a different representation (unlike BigInteger). internal
 * format: "2/100" means "0.02"
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class FixedPointNumber extends BigDecimalWrapper implements Cloneable {

	/**
	 * Our FixedPointNumber.java.
	 * @see {@link BigDecimal}
	 */
	// private static final BigDecimal MINUSZERO = new BigDecimal("-0.0");

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		FixedPointNumber fp2 = new FixedPointNumber(getBigDecimal());
		return fp2;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public FixedPointNumber copy() {
		FixedPointNumber fp2 = new FixedPointNumber(getBigDecimal());
		return fp2;
	}

	/**
	 * @return a new FixedPointNumber that has the value of this one times -1.
	 */
	@Override
	public FixedPointNumber negate() {
		return new FixedPointNumber(value.negate());
	}

    /*    public double doubleValue() {
			return value.doubleValue();
        }
        public float floatValue() {
            return value.floatValue();
        }
        public int intValue() {
            return value.intValue();
        }
        public long longValue() {
            return value.longValue();
        }
    */

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		if (o instanceof FixedPointNumber) {
			FixedPointNumber n = (FixedPointNumber) o;
			return equals(n.getBigDecimal());
		}

		if (o instanceof BigDecimal) {
			/*//vvvvv fix for an issue with BigDecimal.compareTo
			//   "-0.0" compared to "0.0" is NOT 0
            if (this.value.abs().compareTo(MINUSZERO) == 0)
                this.value = MINUSZERO;

            */
			BigDecimal otherBigDecimal = (BigDecimal) o;/*
                                                         if (otherBigDecimal.abs().compareTo(MINUSZERO) == 0)
                                                             otherBigDecimal = MINUSZERO;
                                                        //^^^^^^^^*/
			return (otherBigDecimal).compareTo(value) == 0;
		}

		if (o instanceof Number) {
			return ((Number) o).doubleValue() == doubleValue();
		}

		return false;
	}

	/**
	 * our internal value.
	 */
	private BigDecimal value;

	/**
	 * @return true if we are >=0
	 */
	public boolean isPositive() {
		return value.signum() != -1;
	}

	public static FixedPointNumber max(final FixedPointNumber a,
			final FixedPointNumber b) {
		if (a.getBigDecimal().compareTo(b.getBigDecimal()) == -1) {
			return b;
		}
		return a;
	}

	public static FixedPointNumber min(final FixedPointNumber a,
			final FixedPointNumber b) {
		if (a.getBigDecimal().compareTo(b.getBigDecimal()) == -1) {
			return a;
		}
		return b;
	}

	/**
	 * @param n the value to subtract from this value
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber subtract(final int n) {
		value = value.subtract(new BigDecimal(n));
		return this;
	}

	/**
	 * @param n the value to subtract from this value
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber subtract(final FixedPointNumber n) {
		return subtract(n.getBigDecimal());
	}

	/**
	 * @param n the value to subtract from this value
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber subtract(final String n) {
		return subtract(new FixedPointNumber(n));
	}

	/**
	 * @param n the value to subtract from this value
	 * @return this (we are mutable) for easy operation-chaining
	 */
	@Override
	public FixedPointNumber subtract(final BigDecimal n) {
		value = value.subtract(n);
		return this;
	}

	/**
	 * @param n the value to add
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber add(final int n) {
		value = value.add(new BigDecimal(n));
		return this;
	}

	/**
	 * @param n the value to add
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber add(final FixedPointNumber n) {
		return add(n.getBigDecimal());
	}

	/**
	 * @param n the value to add
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber add(final String n) {
		return add(new FixedPointNumber(n));
	}

	/**
	 * @param n the value to add
	 * @return this (we are mutable) for easy operation-chaining
	 */
	@Override
	public FixedPointNumber add(final BigDecimal n) {
		value = value.add(n);
		return this;
	}

	/**
	 * @return the value as a BigDecimal.
	 */
	@Override
	public BigDecimal getBigDecimal() {
		return value;
	}

	/**
	 * @param n the value to multiply this value with (this object will
	 *          contain the new value)
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber multiply(final FixedPointNumber n) {
		return multiply(n.getBigDecimal());
	}

	/**
	 * @param n the value to multiply this value with (this object will
	 *          contain the new value)
	 * @return this (we are mutable) for easy operation-chaining
	 */
	@Override
	public FixedPointNumber multiply(final BigDecimal n) {
		value = value.multiply(n);
		return this;
	}

	/**
	 * @param n the value to multiply this value with (this object will
	 *          contain the new value)
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber multiply(final int n) {
		value = value.multiply(new BigDecimal(n));
		return this;
	}

	/**
	 * @param n the value to divide by
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber divideBy(final FixedPointNumber n) {
		return divideBy(n.getBigDecimal());
	}

	/**
	 * @param n the value to divide by
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber divideBy(final BigDecimal n) {
		BigDecimal n2 = n;

		value = value.setScale(value.scale() + n.precision()); // make sure we
		// have enough
		// digits after
		// the comma

		// workaround for a bug in BigDecimal
		if (n.scale() < value.scale()) {
			n2 = n.setScale(value.scale());
		}
		value = value.divide(n2, BigDecimal.ROUND_HALF_UP);
		return this;
	}

	/**
	 * @param n the value to divide by
	 * @return this (we are mutable) for easy operation-chaining
	 */
	public FixedPointNumber divideBy(final int n) {
		value = value.divide(new BigDecimal(n), BigDecimal.ROUND_HALF_UP);
		return this;
	}

	/**
	 * same as new FixedPointNumber(0).
	 */
	public FixedPointNumber() {
		value = new BigDecimal("0");
	}

	/**
	 * @param i the new value
	 */
	public FixedPointNumber(final int i) {
		value = new BigDecimal("" + i);

	}

	/**
	 * @param i the new value
	 */
	public FixedPointNumber(final long i) {
		value = new BigDecimal("" + i);

	}

	/**
	 * internally converts the double to a String.
	 *
	 * @deprecated Try not to use floating-point numbers. This class is for
	 * EXACT computation!
	 */
	@Deprecated
	public FixedPointNumber(final double d) throws NumberFormatException {
		value = new BigDecimal(d);
	}

	/**
	 * @param bd the value to initialize to
	 */
	public FixedPointNumber(final BigDecimal bd) {
		if (bd == null) {
			throw new IllegalArgumentException(
					"null BigDecimal given to create BigDecimal");
		}
		value = bd;
	}

	/**
	 * Accepts String in gnucash-format "5/100" = 0.5 or in the formats "0,5"
	 * and "0.5" and "123". Also ignores currency-symbols like or &euro; .
	 *
	 * @param gnucashString the String to parse
	 * @throws NumberFormatException if it cannot be parsed at all
	 */
	public FixedPointNumber(String gnucashString) throws NumberFormatException {

		int dividerIndex = gnucashString.indexOf('/');
		if (dividerIndex == -1) {

			int commaIndex = gnucashString.indexOf(',');
			if (commaIndex != -1) {
				gnucashString = gnucashString.replaceAll("\\.", "").replaceAll(
						"'", "");
				commaIndex = gnucashString.indexOf(',');
			}
			if (commaIndex == -1) {
				commaIndex = gnucashString.indexOf('.');
			}

			int divider = 1;

			if (commaIndex == -1) {
				// assume it's an integer

				String rightOfComma = removeCurrency(gnucashString);

				try {
					value = new BigDecimal(rightOfComma);
				}
				catch (NumberFormatException e) {
					throw new NumberFormatException("'" + rightOfComma
							+ "' cannot be parsed by Biginteger! input was \""
							+ gnucashString + "\"");
				}

			} else {
				String leftOfComma = gnucashString.substring(0, commaIndex)
						.trim();
				String rightOfComma = gnucashString.substring(commaIndex + 1)
						.trim();

				rightOfComma = removeCurrency(rightOfComma);

				try {
					value = new BigDecimal(leftOfComma + '.' + rightOfComma);
				}
				catch (NumberFormatException e) {
					throw new NumberFormatException("'" + leftOfComma + '.'
							+ rightOfComma
							+ "' cannot be parsed by Biginteger! input was \""
							+ gnucashString + "\"");
				}
			}

		} else {

			String beforeComma = gnucashString.substring(0, dividerIndex)
					.trim();

			int addIndex = beforeComma.indexOf('+');
			BigDecimal addMe = null;
			if (addIndex > 1) {
				addMe = new BigDecimal(beforeComma.substring(0, addIndex)
						.trim());
				beforeComma = beforeComma.substring(addIndex + 1).trim();
			}

			String divider = gnucashString.substring(dividerIndex + 1).trim();

			// special handling if the divider ist 100000...
			boolean simpleDivider = divider.charAt(0) == '1';
			if (simpleDivider) {
				for (int i = 1; i < divider.length(); i++) {
					if (divider.charAt(i) != '0') {
						simpleDivider = false;
						break;
					}
				}
			}

			if (simpleDivider) {
				int scale = divider.length() - 1;
				value = new BigDecimal(beforeComma).movePointLeft(scale);
			} else {
				value = new BigDecimal(beforeComma);
                /*if (value.scale()<4)
                    value.setScale(5);*/
				BigDecimal d = new BigDecimal(divider);
                /*if (d.scale()<4)
                    d.setScale(5);*/
				int scale = Math.max(Math.max(5, value.scale()), d.scale());
				if (d.compareTo(new BigDecimal(0)) != 0) {
					value = value.divide(d, scale, BigDecimal.ROUND_HALF_UP);
				}
			}

			if (addMe != null) {
				add(addMe);
			}

		}

		if (value == null) {
			throw new IllegalArgumentException("value is null!!! give string='"
					+ gnucashString + "'");
		}

	}

	/**
	 * @param input the string to remove the curency-symbol from (if it has one)
	 * @return the String without the currency
	 */
	private String removeCurrency(final String input) {
		String rightOfComma = input.replace('€', ' ').trim();
		rightOfComma = rightOfComma.replace('$', ' ').trim();
		rightOfComma = rightOfComma.replaceAll("&euro;", "").trim();
		rightOfComma = rightOfComma.replaceAll("&pound;", "").trim();
		while (rightOfComma.length() > 0) {
			if (Character.isDigit(rightOfComma
					.charAt(rightOfComma.length() - 1))) {
				break;
			}
			rightOfComma = rightOfComma.substring(0, rightOfComma.length() - 1);
		}

		return rightOfComma;
	}

	public String toGnucashString() {
		StringBuffer sb = new StringBuffer();

		if (value.scale() > 5) {
			value = value.setScale(5, java.math.RoundingMode.HALF_UP);
		}
		// try to have a divider of "100"
		int scaleAdjust = 2 - value.scale();

		sb.append(value.unscaledValue().toString());
		for (int i = 0; i < scaleAdjust; i++) {
			sb.append('0');
		}
		sb.append("/1");
		for (int i = 0; i < value.scale(); i++) {
			sb.append('0');
		}
		for (int i = 0; i < scaleAdjust; i++) {
			sb.append('0');
		}

		return sb.toString();
	}

	/**
	 * Format using the default NumberFormat.
	 * @see java.lang.Object#toString()
	 */
    /*public String toString() {
     return NumberFormat.getNumberInstance().format(super.doubleValue());
    }*/

	/**
	 * @param other the value to compare to
	 * @return true if and only if this>other
	 */
	public boolean isGreaterThan(final FixedPointNumber other) {
		return isGreaterThan(other.getBigDecimal());
	}

    /**
     * @param other the value to compare to
     * @return as ifGreaterThan, but with given tolerance allowed
     */
    public boolean isGreaterThan(final FixedPointNumber other, double tolerance) {
      return isGreaterThan(other.getBigDecimal(), tolerance);
    }

	/**
	 * @param other the value to compare to
	 * @return true if and only if this>other
	 */
	public boolean isGreaterThan(final BigDecimal other) {
		return value.compareTo(other) > 0.0;
	}

    /**
     * @param other the value to compare to
     * @return as ifGreaterThan, but with given tolerance allowed
     */
    public boolean isGreaterThan(final BigDecimal other, double tolerance) {
      if ( tolerance <= 0.0 )
        throw new IllegalArgumentException("Tolerance must be > 0.0");

      BigDecimal diff = value.subtract(other);
      
      if ( diff.doubleValue() > tolerance )
      {
        return true;
      }
      else
      {
        if ( Math.abs(diff.doubleValue()) <= tolerance )
        {
          return false;
        }
        else
        {
          return true;
        }
      }
    }

	/**
	 * @param other the value to compare to
	 * @return true if and only if this&lt;other
	 */
	public boolean isLessThan(final FixedPointNumber other) {
		return isLessThan(other.getBigDecimal());
	}

    public boolean isLessThan(final FixedPointNumber other, double tolerance) {
      return isLessThan(other.getBigDecimal(), tolerance);
    }

	/**
	 * @param other the value to compare to
	 * @return true if and only if this&lt;other
	 */
	public boolean isLessThan(final BigDecimal other) {
		return value.compareTo(other) < 0.0;
	}

    public boolean isLessThan(final BigDecimal other, double tolerance) {
      if ( tolerance <= 0.0 )
        throw new IllegalArgumentException("Tolerance must be > 0.0");
      
      BigDecimal diff = value.subtract(other);
      
      if ( diff.doubleValue() < - tolerance )
      {
        return true;
      }
      else
      {
        if ( Math.abs(diff.doubleValue()) <= tolerance )
        {
          return false;
        }
        else
        {
          return true;
        }
      }
    }

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
