package org.gnucash.read.auxiliary;

import org.gnucash.numbers.FixedPointNumber;

public interface GCshBillTermsDays {

	Integer getDueDays();

	Integer getDiscountDays();

	FixedPointNumber getDiscount();

}
