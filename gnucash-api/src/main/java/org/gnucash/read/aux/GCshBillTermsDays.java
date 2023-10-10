package org.gnucash.read.aux;

import org.gnucash.numbers.FixedPointNumber;

public interface GCshBillTermsDays {

    public Integer getDueDays();

    public Integer getDiscountDays();

    public FixedPointNumber getDiscount();

}
