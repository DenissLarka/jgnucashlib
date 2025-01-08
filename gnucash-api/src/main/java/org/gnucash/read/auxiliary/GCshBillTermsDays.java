package org.gnucash.read.auxiliary;

import org.gnucash.numbers.FixedPointNumber;

public interface GCshBillTermsDays {

    public Integer getDueDays();

    public Integer getDiscountDays();

    public FixedPointNumber getDiscount();

}
