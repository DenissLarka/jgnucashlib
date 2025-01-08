package org.gnucash.read.auxiliary;

import org.gnucash.numbers.FixedPointNumber;

public interface GCshBillTermsProximo {

    public Integer getDueDay();

    public Integer getDiscountDay();

    public FixedPointNumber getDiscount();

}
