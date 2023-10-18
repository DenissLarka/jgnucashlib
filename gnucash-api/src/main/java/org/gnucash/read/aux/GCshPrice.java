package org.gnucash.read.aux;

import java.time.LocalDate;

import org.gnucash.numbers.FixedPointNumber;
import org.gnucash.read.GnucashCommodity;

public interface GCshPrice {

    String getId();

    String getCommodityQualifId();

    GnucashCommodity getCommodity();

    String getCurrencyQualifId();

    String getCurrencyCode();

    GnucashCommodity getCurrency();

    LocalDate getDate();

    String getSource();

    String getType();

    FixedPointNumber getValue();
    
}
