package org.gnucash.read.auxiliary;

import org.gnucash.numbers.FixedPointNumber;

public interface GCshBillTermsProximo {

	Integer getDueDay();

	Integer getDiscountDay();

	FixedPointNumber getDiscount();

}
